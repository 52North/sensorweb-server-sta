package org.n52.sta.data.editor;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.LocationData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.LocationRepository;
import org.n52.sta.data.support.LocationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;
import java.util.Optional;

public class LocationEntityEditor extends DatabaseEntityAdapter<LocationEntity>
        implements
        EntityEditorDelegate<Location, LocationData> {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Thing, ThingData> thingEditor;

    public LocationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public LocationData getOrSave(Location entity) throws EditorException {
        Optional<LocationEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new LocationData(e, Optional.empty()))
                .orElseGet(() -> save(entity));
    }

    @Override
    public LocationData save(Location entity) throws EditorException {

        String staIdentifier = entity.getId();
        EntityService<Location> locationService = getService(Location.class);
        if (locationService.exists(staIdentifier)) {
            throw new EditorException("Location already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();

        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setIdentifier(id);
        locationEntity.setStaIdentifier(id);
        locationEntity.setName(entity.getName());
        locationEntity.setDescription(entity.getDescription());
        valueHelper.setFormat(locationEntity::setLocationEncoding, entity.getEncodingType());

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
                .map(entry -> convertParameter(locationEntity, entry))
                .forEach(locationEntity::addParameter);

        LocationEntity saved = locationRepository.save(locationEntity);

        return new LocationData(saved, Optional.empty());
    }

    @Override
    public LocationData update(Location entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @Override
    protected Optional<LocationEntity> getEntity(String id) {
        LocationGraphBuilder graphBuilder = LocationGraphBuilder.createEmpty();
        return locationRepository.findByStaIdentifier(id, graphBuilder);
    }

    private ParameterEntity<?> convertParameter(LocationEntity locationEntity, Map.Entry<String, Object> entry) {
        throw new EditorException();
    }
}