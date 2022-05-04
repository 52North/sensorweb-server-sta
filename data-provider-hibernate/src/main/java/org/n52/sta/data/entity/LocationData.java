package org.n52.sta.data.entity;

import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class LocationData extends StaData<LocationEntity> implements Location {

    public LocationData(LocationEntity data) {
        super(data);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public String getEncodingType() {
        return null;
    }

    @Override
    public Geometry getGeometry() {
        return data.getGeometry();
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return toSet(data.getHistoricalLocations(), HistoricalLocationData::new);
    }

    @Override
    public Set<Thing> getThings() {
        return toSet(data.getPlatforms(), ThingData::new);
    }
}
