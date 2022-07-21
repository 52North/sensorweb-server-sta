
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;
import org.n52.series.db.beans.parameter.observation.ObservationBooleanParameterEntity;
import org.n52.series.db.beans.parameter.observation.ObservationQuantityParameterEntity;
import org.n52.series.db.beans.parameter.observation.ObservationTextParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.repositories.entity.ObservationRepository;
import org.n52.sta.data.support.GraphBuilder;
import org.n52.sta.data.support.ObservationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class ObservationEntityEditor extends DatabaseEntityAdapter<DataEntity> implements EntityEditor<Observation> {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private DatastreamEntityEditor datastreamEditor;

    @Autowired
    private ValueHelper valueHelper;

    @Autowired
    private EntityPropertyMapping propertyMapping;

    protected ObservationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);

        // TODO Auto-generated constructor stub
    }

    @Override
    public ObservationData save(Observation entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");
        assertNew(entity);

        DatasetEntity datastream = (DatasetEntity) getDatastreamOf(entity);
        return Streams.stream(saveAll(Collections.singleton(entity), datastream))
                      .map(savedEntity -> new ObservationData(savedEntity, propertyMapping))
                      .findFirst()
                      .orElseThrow();
    }

    @Override
    public ObservationData update(Observation entity) throws EditorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String id) throws EditorException {
        // TODO Auto-generated method stub

    }

    @Override
    protected Optional<DataEntity> getEntity(String id) {
        GraphBuilder<DataEntity> graphBuilder = ObservationGraphBuilder.createEmpty();
        return observationRepository.findByStaIdentifier(id, graphBuilder);
    }

    Set<DataEntity< ? >> saveAll(Set<Observation> observations, DatasetEntity datasetEntity)
            throws EditorException {
        Objects.requireNonNull(observations, "observations must not be null");
        Objects.requireNonNull(datasetEntity, "datasetEntity must not be null");
        Set<DataEntity< ? >> entities = Streams.stream(observations)
                                               .map(o -> createEntity(o, datasetEntity))
                                               .collect(Collectors.toSet());
        return Streams.stream(observationRepository.saveAll(entities))
                      .collect(Collectors.toSet());
    }

    private AbstractDatasetEntity getDatastreamOf(Observation entity) throws EditorException {
        DatastreamData datastream = (DatastreamData) getOrSaveMandatory(entity.getDatastream(), Datastream.class);
        return datastream.getData();
//        return datastreamEditor.getEntity(datastream.getId())
//                               .orElseThrow(() -> new IllegalStateException("Datastream not found for Observation!"));
    }

    private DataEntity< ? > createEntity(Observation observation, DatasetEntity datasetEntity) throws EditorException {
        FormatEntity formatEntity = datasetEntity.getOmObservationType();
        Object value = observation.getResult();
        String format = formatEntity.getFormat();
        switch (format) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                QuantityDataEntity quantityObservationEntity = new QuantityDataEntity();
                if (value == null || value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityObservationEntity.setValue(null);
                } else {
                    double doubleValue = value instanceof String
                            ? Double.parseDouble((String) value)
                            : (double) value;
                    quantityObservationEntity.setValue(BigDecimal.valueOf(doubleValue));
                }
                return initDataEntity(quantityObservationEntity, observation, datasetEntity);

            // TODO add further observation types

            default:
                throw new EditorException("Unknown OMObservation type: " + format);
        }
    }

    private DataEntity< ? > initDataEntity(DataEntity< ? > data, Observation observation, DatasetEntity dataset) {

        // metadata
        String id = observation.getId() == null
                ? generateId()
                : observation.getId();
        data.setIdentifier(id);
        data.setStaIdentifier(id);

        // values
        Time phenomenonTime = observation.getPhenomenonTime();
        valueHelper.setStartTime(data::setSamplingTimeStart, phenomenonTime);
        valueHelper.setEndTime(data::setSamplingTimeEnd, phenomenonTime);

        Time validTime = observation.getValidTime();
        valueHelper.setStartTime(data::setValidTimeStart, validTime);
        valueHelper.setEndTime(data::setValidTimeEnd, validTime);

        Time resultTime = observation.getResultTime();
        valueHelper.setTime(data::setResultTime, resultTime);

        Map<String, Object> parameters = observation.getParameters();
        Streams.stream(parameters.entrySet())
               .map(this::convertParameter)
               .filter(p -> p != null)
               .forEach(data::addParameter);

        // following parameters have to be set explicitly, too
        if (parameters.containsKey(propertyMapping.getSamplingGeometry())) {
            GeometryEntity geometryEntity = valueToGeometry(propertyMapping.getSamplingGeometry(), parameters);
            data.setGeometryEntity(geometryEntity);
        }

        if (parameters.containsKey(propertyMapping.getVerticalFrom())) {
            BigDecimal verticalFrom = valueToDouble(propertyMapping.getVerticalFrom(), parameters);
            data.setVerticalFrom(verticalFrom);
        }

        if (parameters.containsKey(propertyMapping.getVerticalTo())) {
            BigDecimal verticalTo = valueToDouble(propertyMapping.getVerticalTo(), parameters);
            data.setVerticalFrom(verticalTo);
        }

        if (parameters.containsKey(propertyMapping.getVerticalFromTo())) {
            BigDecimal verticalFromTo = valueToDouble(propertyMapping.getVerticalFromTo(), parameters);
            data.setVerticalFrom(verticalFromTo);
            data.setVerticalTo(verticalFromTo);
        }

        // references
        data.setDataset(dataset);
        return data;
    }

    private BigDecimal valueToDouble(String parameter, Map<String, Object> parameters) {
        Object value = parameters.get(parameter);
        Double doubleValue = value instanceof String
                ? Double.parseDouble((String) value)
                : (Double) value;
        return BigDecimal.valueOf(doubleValue);
    }

    private GeometryEntity valueToGeometry(String parameter, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    private DatasetParameterEntity< ? > convertParameter(Map.Entry<String, Object> parameter) {
        String key = parameter.getKey();
        Object value = parameter.getValue();

        // TODO review ParameterFactory and DTOTransformerImpl#convertParameters

        if (value instanceof Number) {
            ObservationQuantityParameterEntity parameterEntity = new ObservationQuantityParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (value instanceof Boolean) {
            ObservationBooleanParameterEntity parameterEntity = new ObservationBooleanParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((Boolean) value);
        } else if (value instanceof String) {
            ObservationTextParameterEntity parameterEntity = new ObservationTextParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((String) value);
            // } else {
            // // TODO handle other cases from DTOTransformerImpl#convertParameters
        }
        return null;
    }

    private void assertNew(Observation observation) throws EditorException {
        String staIdentifier = observation.getId();
        if (getEntity(staIdentifier).isPresent()) {
            throw new EditorException("Observation already exists with ID '" + staIdentifier + "'");
        }
    }

}
