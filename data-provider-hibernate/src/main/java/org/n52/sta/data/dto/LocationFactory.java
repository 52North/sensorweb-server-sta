package org.n52.sta.data.dto;

import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.dto.LocationDto;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class LocationFactory extends BaseDtoFactory<LocationDto, LocationFactory> {

    public static Location create(LocationEntity entity) {
        LocationFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setGeometry(entity.getGeometry());
        factory.setLocations(entity.getHistoricalLocations());
        factory.setThings(entity.getPlatforms());

        return factory.get();
    }

    public static LocationFactory create() {
        return new LocationFactory(new LocationDto());
    }

    private LocationFactory(LocationDto dto) {
        super(dto);
    }
    
    private LocationFactory setLocations(Set<HistoricalLocationEntity> locations) {
        Streams.stream(locations).forEach(this::addLocation);
        return this;
    }

    private LocationFactory addLocation(HistoricalLocationEntity entity) {
        return addLocation(HistoricalLocationFactory.create(entity));
    }

    public LocationFactory addLocation(HistoricalLocation location) {
        get().addHistoricalLocation(location);
        return this;
    }

    private LocationFactory setThings(Set<PlatformEntity> platforms) {
        Streams.stream(platforms).forEach(this::addThing);
        return this;
    }

    private LocationFactory addThing(PlatformEntity entity) {
        return addThing(ThingFactory.create(entity));
    }

    private LocationFactory addThing(Thing thing) {
        get().addThing(thing);
        return this;
    }

    private void setGeometry(Geometry geometry) {
        get().setGeometry(geometry);
    }

}
