
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.parameter.dataset.DatasetBooleanParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetQuantityParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetTextParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.aggregate.ThingAggregate;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.DatastreamRepository;
import org.n52.sta.data.repositories.value.OfferingRepository;
import org.n52.sta.data.support.DatastreamGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class DatastreamEntityEditor extends DatabaseEntityAdapter<AbstractDatasetEntity> implements
        EntityEditor<Datastream> {

    @Autowired
    private DatastreamRepository datastreamRepository;

    @Autowired
    private ThingEntityEditor thingEditor;

    @Autowired
    private SensorEntityEditor sensorEditor;

    @Autowired
    private ObservedPropertyEntityEditor observedPropertyEditor;

    @Autowired
    private ObservationEntityEditor observationEditor;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private ValueHelper valueHelper;

    @Autowired
    private EntityPropertyMapping propertyMapping;

    public DatastreamEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);

        // TODO feature toggles, e.g. mobile, FOI-update, ...
    }

    @Override
    public DatastreamData save(Datastream entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");
        assertNew(entity);
        
        // DTOTransformerImpl#createDatasetEntity
        // CommonDatastreamService
        // DatastreamService (old package)

        // TODO check handling race conditions and rollback handling

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        
        // metadata
        DatasetEntity dataset = new DatasetEntity();
        dataset.setIdentifier(id);
        dataset.setStaIdentifier(id);
        dataset.setName(entity.getName());
        dataset.setDescription(entity.getDescription());

        // values
        Time resultTime = entity.getResultTime();
        valueHelper.setStartTime(dataset::setResultTimeStart, resultTime);
        valueHelper.setEndTime(dataset::setResultTimeEnd, resultTime);
        valueHelper.setFormat(dataset::setOMObservationType, entity.getObservationType());
        valueHelper.setUnit(dataset::setUnit, entity.getUnitOfMeasurement());
        OfferingEntity offering = getOrSaveOfferingValue(entity.getSensor());
        dataset.setOffering(offering);

        // references
        ThingData thing = thingEditor.save(entity.getThing());
        ThingAggregate thingAggregate = new ThingAggregate(thing);
        if (thingAggregate.isMobile()) {
            dataset.setDatasetType(DatasetType.trajectory);
            dataset.setMobile(true);
        } else {
            dataset.setDatasetType(DatasetType.timeseries);
            dataset.setMobile(false);
        }
        dataset.setPlatform(thing.getData());

        SensorData sensor = sensorEditor.save(entity.getSensor());
        ProcedureEntity sensorEntity = sensor.getData();
        dataset.setProcedure(sensorEntity);

        ObservedPropertyData observedProperty = observedPropertyEditor.save(entity.getObservedProperty());
        dataset.setObservableProperty(observedProperty.getData());

        Set<Observation> observations = entity.getObservations();
        Streams.stream(observations)
               .findFirst()
               .ifPresentOrElse(o -> dataset.setObservationType(getObservationType(o)),
                                () -> dataset.setObservationType(ObservationType.not_initialized));
        dataset.setObservations(observationEditor.saveAll(observations, dataset));

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(this::convertParameter)
               .filter(p -> p != null)
               .forEach(dataset::addParameter);

        DatasetEntity savedEntity = datastreamRepository.save(dataset);

        // TODO explicitly save all references, too? if so, what about CASCADE.PERSIST?
        // sensorEntity.addDatastream(savedEntity);
        // sensorEditor.update(new SensorData(sensorEntity, propertyMapping));

        return new DatastreamData(savedEntity, propertyMapping);
    }

    private ObservationType getObservationType(Observation observation) {

        return null;
    }

    @Override
    public Datastream update(Datastream entity) throws EditorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String id) throws EditorException {
        // TODO Auto-generated method stub

    }

    @Override
    protected Optional<AbstractDatasetEntity> getEntity(String id) {
        return datastreamRepository.findByStaIdentifier(id, DatastreamGraphBuilder.createEmpty());
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    private OfferingEntity getOrSaveOfferingValue(Sensor sensor) {
        Optional<OfferingEntity> optionalOffering = offeringRepository.findByIdentifier(sensor.getId());
        if (optionalOffering.isPresent()) {
            return optionalOffering.get();
        }

        OfferingEntity offering = new OfferingEntity();
        offering.setIdentifier(sensor.getId());
        offering.setName(sensor.getName());
        offering.setDescription(sensor.getDescription());
        return offeringRepository.save(offering);
    }

    private DatasetParameterEntity< ? > convertParameter(Map.Entry<String, Object> parameter) {
        String key = parameter.getKey();
        Object value = parameter.getValue();

        // TODO review ParameterFactory and DTOTransformerImpl#convertParameters

        if (value instanceof Number) {
            DatasetQuantityParameterEntity parameterEntity = new DatasetQuantityParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (value instanceof Boolean) {
            DatasetBooleanParameterEntity parameterEntity = new DatasetBooleanParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((Boolean) value);
        } else if (value instanceof String) {
            DatasetTextParameterEntity parameterEntity = new DatasetTextParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((String) value);
        } else {
            // TODO handle other cases from DTOTransformerImpl#convertParameters
        }
        return null;
    }

    private void assertNew(Datastream datastream) throws EditorException {
        String staIdentifier = datastream.getId();
        if (getEntity(staIdentifier).isPresent()) {
            throw new EditorException("Datastream already exists with ID '" + staIdentifier + "'");
        }
    }
}
