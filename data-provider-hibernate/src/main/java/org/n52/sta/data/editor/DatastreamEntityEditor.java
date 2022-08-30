
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.aggregate.ThingAggregate;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.DatastreamRepository;
import org.n52.sta.data.repositories.value.OfferingRepository;
import org.n52.sta.data.support.DatastreamGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class DatastreamEntityEditor extends DatabaseEntityAdapter<AbstractDatasetEntity> implements
        EntityEditorDelegate<Datastream, DatastreamData> {

    @Autowired
    private DatastreamRepository datastreamRepository;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private ValueHelper valueHelper;

    @Autowired
    private EntityPropertyMapping propertyMapping;

    private EntityEditorDelegate<Thing, ThingData> thingEditor;
    private EntityEditorDelegate<Sensor, SensorData> sensorEditor;
    private EntityEditorDelegate<ObservedProperty, ObservedPropertyData> observedPropertyEditor;
    private EntityEditorDelegate<Observation, ObservationData> observationEditor;

    public DatastreamEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);

        // TODO feature toggles, e.g. mobile, FOI-update, ...
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        this.sensorEditor = (EntityEditorDelegate<Sensor, SensorData>)
                getService(Sensor.class).unwrapEditor();
        this.observedPropertyEditor = (EntityEditorDelegate<ObservedProperty, ObservedPropertyData>)
                getService(ObservedProperty.class).unwrapEditor();
        this.observationEditor = (EntityEditorDelegate<Observation, ObservationData>)
                getService(Observation.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public DatastreamData getOrSave(Datastream entity) throws EditorException {
        Optional<AbstractDatasetEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new DatastreamData(e, Optional.of(propertyMapping)))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public DatastreamData save(Datastream entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

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
        ThingData thing = thingEditor.getOrSave(entity.getThing());
        ThingAggregate thingAggregate = new ThingAggregate(thing);
        if (thingAggregate.isMobile()) {
            dataset.setDatasetType(DatasetType.trajectory);
            dataset.setMobile(true);
        } else {
            dataset.setDatasetType(DatasetType.timeseries);
            dataset.setMobile(false);
        }
        dataset.setPlatform(thing.getData());

        SensorData sensor = sensorEditor.getOrSave(entity.getSensor());
        ProcedureEntity sensorEntity = sensor.getData();
        dataset.setProcedure(sensorEntity);

        ObservedPropertyData observedProperty = observedPropertyEditor.getOrSave(entity.getObservedProperty());
        dataset.setObservableProperty(observedProperty.getData());

        Set<Observation> observations = entity.getObservations();
        Streams.stream(observations)
               .findFirst()
               .ifPresentOrElse(o -> dataset.setObservationType(getObservationType(o)),
                                () -> dataset.setObservationType(ObservationType.not_initialized));

        // TODO create aggregate on multiple FOIs <- observation

        // TODO update first/last observation
        // -> decoupling via updateFirstLastObservationHandler?
        // -> enable event handling

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(e -> convertParameter(dataset, e))
               .forEach(dataset::addParameter);

        DatasetEntity savedEntity = datastreamRepository.save(dataset);

        // Set Observations
        dataset.setObservations(Streams.stream(observations)
                                       .map(o -> observationEditor.getOrSave(o))
                                       .map(StaData::getData)
                                       .collect(Collectors.toSet()));
        // TODO explicitly save all references, too? if so, what about CASCADE.PERSIST?
        // sensorEntity.addDatastream(savedEntity);
        // sensorEditor.update(new SensorData(sensorEntity, propertyMapping));

        return new DatastreamData(savedEntity, Optional.ofNullable(propertyMapping));
    }

    private ObservationType getObservationType(Observation observation) {
        throw new EditorException();
    }

    @Override
    public Datastream update(Datastream entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
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

    private void assertNew(Datastream datastream) throws EditorException {
        String staIdentifier = datastream.getId();
        if (getEntity(staIdentifier).isPresent()) {
            throw new EditorException("Datastream already exists with ID '" + staIdentifier + "'");
        }
    }
}
