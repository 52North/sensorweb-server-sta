
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.platform.PlatformBooleanParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformQuantityParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformTextParameterEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.*;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.*;
import org.n52.sta.data.repositories.entity.PlatformRepository;
import org.n52.sta.data.support.ThingGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ThingEntityEditor extends DatabaseEntityAdapter<PlatformEntity>
        implements EntityEditorDelegate<Thing, ThingData> {

    @Autowired
    private PlatformRepository platformRepository;

    private EntityEditorDelegate<Location, LocationData> locationEditor;

    public ThingEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.locationEditor = (EntityEditorDelegate<Location, LocationData>)
                getService(Location.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ThingData getOrSave(Thing entity) throws EditorException {
        Optional<PlatformEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new ThingData(e, Optional.empty()))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public ThingData save(Thing entity) throws EditorException {
        String staIdentifier = entity.getId();
        EntityService<Thing> thingService = getService(Thing.class);
        if (thingService.exists(staIdentifier)) {
            throw new EditorException("Thing already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setIdentifier(id);
        platformEntity.setStaIdentifier(id);
        platformEntity.setName(entity.getName());
        platformEntity.setDescription(entity.getDescription());

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(platformEntity, entry))
               .forEach(platformEntity::addParameter);

        // save entity
        PlatformEntity saved = platformRepository.save(platformEntity);

        // save related entities
        platformEntity.setLocations(Streams.stream(entity.getLocations())
                .map(o -> locationEditor.getOrSave(o))
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        platformRepository.flush();

        return new ThingData(saved, Optional.empty());
    }

    @Override
    public ThingData update(Thing entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @SuppressWarnings("unchecked")
    private PlatformParameterEntity< ? > convertParameter(PlatformEntity platform,
            Map.Entry<String, Object> parameter) {
        String key = parameter.getKey();
        Object value = parameter.getValue();

        // TODO review ParameterFactory and DTOTransformerImpl#convertParameters
        PlatformParameterEntity parameterEntity;
        Class<?> valueType = value.getClass();
        if (Number.class.isAssignableFrom(valueType)) {
            parameterEntity = new PlatformQuantityParameterEntity();
            value = BigDecimal.valueOf((Double) value);
        } else if (Boolean.class.isAssignableFrom(valueType)) {
            parameterEntity = new PlatformBooleanParameterEntity();
        } else if (String.class.isAssignableFrom(valueType)) {
            parameterEntity = new PlatformTextParameterEntity();
        } else {
            // TODO handle other cases from DTOTransformerImpl#convertParameters
            throw new RuntimeException("can not handle parameter with unknown type: " + key);
        }

        parameterEntity.setName(key);
        parameterEntity.setValue(value);
        parameterEntity.setDescribeableEntity(platform);
        return parameterEntity;
    }

    @Override
    protected Optional<PlatformEntity> getEntity(String id) {
        ThingGraphBuilder graphBuilder = ThingGraphBuilder.createEmpty();
        return platformRepository.findByStaIdentifier(id, graphBuilder);
    }
}