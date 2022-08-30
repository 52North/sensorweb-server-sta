package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
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
    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;
    private EntityEditorDelegate<HistoricalLocation, HistoricalLocationData> historicalLocationEditor;

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
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        this.historicalLocationEditor = (EntityEditorDelegate<HistoricalLocation, HistoricalLocationData>)
                getService(HistoricalLocation.class).unwrapEditor();
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
        Objects.requireNonNull(entity, "entity must not be null");

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
                .map(locationEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        platformEntity.setDatasets(Streams.stream(entity.getDatastreams())
                .map(datastreamEditor::getOrSave)
                .map(StaData::getData)
                .collect(Collectors.toSet()));

        platformEntity.setHistoricalLocations(Streams.stream(entity.getHistoricalLocations())
                .map(historicalLocationEditor::getOrSave)
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

    @Override
    protected Optional<PlatformEntity> getEntity(String id) {
        ThingGraphBuilder graphBuilder = ThingGraphBuilder.createEmpty();
        return platformRepository.findByStaIdentifier(id, graphBuilder);
    }
}
