package org.n52.sta.data.entity;

import java.util.Map;
import java.util.Set;

import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class ThingData extends StaData<PlatformEntity> implements Thing {
    
    public ThingData(PlatformEntity data) {
        super(data);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public Set<HistoricalLocation> getHistoricalLocations() {
        return toSet(data.getHistoricalLocations(), HistoricalLocationData::new);
    }

    @Override
    public Set<Location> getLocations() {
        return toSet(data.getLocations(), LocationData::new);
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(data.getDatasets(), DatastreamData::new);
    }
}
