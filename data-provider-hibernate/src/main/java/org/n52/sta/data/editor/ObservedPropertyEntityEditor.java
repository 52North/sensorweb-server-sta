
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.phenomenon.PhenomenonBooleanParameterEntity;
import org.n52.series.db.beans.parameter.phenomenon.PhenomenonParameterEntity;
import org.n52.series.db.beans.parameter.phenomenon.PhenomenonQuantityParameterEntity;
import org.n52.series.db.beans.parameter.phenomenon.PhenomenonTextParameterEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.repositories.entity.PhenomenonRepository;
import org.n52.sta.data.support.ObservedPropertyGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ObservedPropertyEntityEditor extends DatabaseEntityAdapter<PhenomenonEntity> implements
        EntityEditorDelegate<ObservedProperty, ObservedPropertyData> {

    @Autowired
    private PhenomenonRepository phenomenonRepository;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public ObservedPropertyEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ObservedPropertyData getOrSave(ObservedProperty entity) throws EditorException {
        Optional<PhenomenonEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new ObservedPropertyData(e, Optional.empty()))
                .orElseGet(() -> save(entity));
    }

    @Override
    public ObservedPropertyData save(ObservedProperty entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String staIdentifier = entity.getId();
        EntityService<ObservedProperty> service = getService(ObservedProperty.class);

        if (service.exists(staIdentifier)) {
            throw new EditorException("ObservedProperty already exists with Id '" + staIdentifier + "'");
        }

        // definition (mapped in the data model as identifier) must be unique
        if (phenomenonRepository.existsByIdentifier(entity.getDefinition())) {
            throw new EditorException("ObservedProperty with given definition already exists!");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        PhenomenonEntity phenomenonEntity = new PhenomenonEntity();
        phenomenonEntity.setIdentifier(entity.getDefinition());
        phenomenonEntity.setStaIdentifier(id);
        phenomenonEntity.setName(entity.getName());
        phenomenonEntity.setDescription(entity.getDescription());

        PhenomenonEntity saved = phenomenonRepository.save(phenomenonEntity);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
                .map(entry -> convertParameters(phenomenonEntity, entry))
                .forEach(phenomenonEntity::addParameter);


        // save related entities
        phenomenonEntity.setDatasets(Streams.stream(entity.getDatastreams())
                .map(o -> datastreamEditor.getOrSave(o))
                .map(StaData::getData).collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        phenomenonRepository.flush();

        return new ObservedPropertyData(saved, Optional.empty());
    }

    protected ParameterEntity<?> convertParameters(PhenomenonEntity entity,
                                                            Map.Entry<String, Object> parameter) {

        String key = parameter.getKey();
        Object value = parameter.getValue();
        ParameterEntity parameterEntity;

        Class<?> valueType = value.getClass();
        if (Number.class.isAssignableFrom(valueType)) {
            parameterEntity = ParameterFactory.from(entity, ParameterFactory.ValueType.QUANTITY);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (Boolean.class.isAssignableFrom(valueType)) {
            parameterEntity = ParameterFactory.from(entity, ParameterFactory.ValueType.BOOLEAN);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (String.class.isAssignableFrom(valueType)) {
            parameterEntity = ParameterFactory.from(entity, ParameterFactory.ValueType.TEXT);
            parameterEntity.setValue(value);
        } else {
            // TODO handle type 'JSON'
            throw new RuntimeException("can not handle parameter with unknown type: " + key);
        }
        parameterEntity.setName(key);

        return parameterEntity;
    }

    @Override
    public ObservedPropertyData update(ObservedProperty entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @Override
    protected Optional<PhenomenonEntity> getEntity(String id) {
        ObservedPropertyGraphBuilder graphBuilder = ObservedPropertyGraphBuilder.createEmpty();
        return phenomenonRepository.findByStaIdentifier(id, graphBuilder);
    }
}
