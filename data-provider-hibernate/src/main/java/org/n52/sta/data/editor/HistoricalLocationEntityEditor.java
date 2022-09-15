package org.n52.sta.data.editor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.HistoricalLocationData;
import org.n52.sta.data.entity.LocationData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.HistoricalLocationRepository;
import org.n52.sta.data.support.HistoricalLocationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class HistoricalLocationEntityEditor extends DatabaseEntityAdapter<HistoricalLocationEntity>
        implements
        EntityEditorDelegate<HistoricalLocation, HistoricalLocationData> {

    @Autowired
    private HistoricalLocationRepository historicalLocationRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Thing, ThingData> thingEditor;
    private EntityEditorDelegate<Location, LocationData> locationEditor;

    public HistoricalLocationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        this.locationEditor = (EntityEditorDelegate<Location, LocationData>)
                getService(Location.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public HistoricalLocationData getOrSave(HistoricalLocation entity) throws EditorException {
        Optional<HistoricalLocationEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new HistoricalLocationData(e, Optional.empty()))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public HistoricalLocationData save(HistoricalLocation entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String staIdentifier = entity.getId();
        EntityService<HistoricalLocation> historicalLocationService = getService(HistoricalLocation.class);
        if (historicalLocationService.exists(staIdentifier)) {
            throw new EditorException("HistoricalLocation already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();

        HistoricalLocationEntity historicalLocationEntity = new HistoricalLocationEntity();
        historicalLocationEntity.setIdentifier(id);
        historicalLocationEntity.setStaIdentifier(id);
        valueHelper.setStartTime(historicalLocationEntity::setTime, entity.getTime());

        PlatformEntity thing = thingEditor.getOrSave(entity.getThing()).getData();
        historicalLocationEntity.setThing(thing);

        historicalLocationEntity.setLocations(Streams.stream(entity.getLocations())
                                                     .map(locationEditor::getOrSave)
                                                     .map(StaData::getData)
                                                     .collect(Collectors.toSet()));

        historicalLocationRepository.save(historicalLocationEntity);
        historicalLocationRepository.flush();
        return new HistoricalLocationData(historicalLocationEntity, Optional.empty());
    }

    @Override
    public HistoricalLocationData update(HistoricalLocation oldEntity,
                                         HistoricalLocation updateEntity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        HistoricalLocationEntity historicalLocation = getEntity(id)
                .orElseThrow(() -> new EditorException("could not find entity with id: " + id));

        updateLocations(historicalLocation);
        updateThing(historicalLocation);

        historicalLocationRepository.deleteByStaIdentifier(id);
    }

    @Override
    protected Optional<HistoricalLocationEntity> getEntity(String id) {
        HistoricalLocationGraphBuilder graphBuilder = HistoricalLocationGraphBuilder.createEmpty();
        return historicalLocationRepository.findByStaIdentifier(id, graphBuilder);
    }

    private void updateLocations(HistoricalLocationEntity historicalLocation) {
        for (LocationEntity location : historicalLocation.getLocations()) {
            location.getHistoricalLocations()
                    .remove(historicalLocation);
        }
    }

    private void updateThing(HistoricalLocationEntity historicalLocation) {
        historicalLocation.getThing().setHistoricalLocations(null);
    }
}
