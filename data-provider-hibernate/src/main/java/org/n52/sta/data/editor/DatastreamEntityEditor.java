
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.dataset.DatasetBooleanParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetQuantityParameterEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetTextParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityEditorLookup;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.aggregate.DatastreamAggregate;
import org.n52.sta.api.domain.aggregate.SensorAggregate;
import org.n52.sta.api.domain.aggregate.ThingAggregate;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.repositories.entity.DatastreamRepository;
import org.n52.sta.data.repositories.parameter.DatastreamParameterRepository;
import org.n52.sta.data.repositories.value.FormatRepository;
import org.n52.sta.data.repositories.value.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DatastreamEntityEditor extends DatabaseEntityAdapter<AbstractDatasetEntity> implements
        EntityEditor<Datastream> {

    @Autowired
    private DatastreamRepository datastreamRepository;

    @Autowired
    private DatastreamParameterRepository parameterRepository;
    
    @Autowired
    private EntityPropertyMapping propertyMapping;

    @Autowired
    private ValueHelper valueHelper;

    public DatastreamEntityEditor(EntityServiceLookup serviceLookup, EntityEditorLookup editorLookup) {
        super(serviceLookup, editorLookup);

        // TODO feature toggles, e.g. mobile, FOI-update, ...
    }

    @Override
    public Datastream save(Datastream entity) throws EditorException {

        String staIdentifier = entity.getId();
        EntityService<Datastream> datastreamService = getService(Datastream.class);
        if (datastreamService.exists(staIdentifier)) {
            throw new EditorException("Datastream already exists with ID '" + staIdentifier + "'");
        }

        DatasetEntity dataset = new DatasetEntity();
        dataset.setIdentifier(entity.getId());
        dataset.setStaIdentifier(entity.getId());
        dataset.setName(entity.getName());
        dataset.setDescription(entity.getDescription());
        dataset.setObservationType(ObservationType.simple);

        Time resultTime = entity.getResultTime();
        valueHelper.setStartTime(dataset::setResultTimeStart, resultTime);
        valueHelper.setEndTime(dataset::setResultTimeEnd, resultTime);

        // TODO check handling race conditions and rollback handling

        FormatEntity formatEntity = valueHelper.getOrSaveFormat(entity.getObservationType());
        dataset.setOMObservationType(formatEntity);

        UnitEntity unitEntity = valueHelper.getOrSaveUnit(entity.getUnitOfMeasurement());
        dataset.setUnit(unitEntity);

        // parameters are save as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(this::convertParameter)
               .forEach(dataset::addParameter);

        Thing thing = getOrSaveMandatory(entity.getThing(), Thing.class);
        ThingAggregate thingAggregate = new ThingAggregate(thing, getEditor(Thing.class));

        // TODO isn't mobile an DatasetAggregationEntity?
        if (thingAggregate.isMobile()) {

            // TODO handle specific datastream ...

            dataset.setDatasetType(DatasetType.trajectory);
            dataset.setMobile(true);
        } else {
            dataset.setDatasetType(DatasetType.timeseries);
        }

        Sensor sensor = getOrSaveMandatory(entity.getSensor(), Sensor.class);
        SensorAggregate sensorAggregate = new SensorAggregate(sensor, getEditor(Sensor.class));

        // TODO DTOTransformerImpl#createDatasetEntity

        // TODO Option 1: go for completeness (hardcode c/p)
        // TODO Option 2: go for MVP -> resolve domain chain
        // TODO Option 3: ...

        
        
        // TODO return aggregate? pass to domain service?
        DatasetEntity savedEntity = datastreamRepository.save(dataset);
        return new DatastreamData(savedEntity, propertyMapping);
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
    
    private DatasetParameterEntity<?> convertParameter(Map.Entry<String, Object> parameter) {
        String key = parameter.getKey();
        Object value = parameter.getValue();
        Class< ? extends Object> valueType = value.getClass();
        if (Number.class.isAssignableFrom(valueType)) {
            DatasetQuantityParameterEntity parameterEntity = new DatasetQuantityParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (Boolean.class.isAssignableFrom(valueType)) {
            DatasetBooleanParameterEntity parameterEntity = new DatasetBooleanParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((Boolean) value);
        } else if (String.class.isAssignableFrom(valueType)) {
            DatasetTextParameterEntity parameterEntity = new DatasetTextParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((String) value);
        } else {
            // TODO handle other cases from DTOTransformerImpl#convertParameters
        }
        return null;
    }

}
