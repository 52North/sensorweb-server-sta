package org.n52.sta.data.entity;

import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.old.utils.TimeUtil;

public class HistoricalLocationData extends StaData<HistoricalLocationEntity> implements HistoricalLocation {

    public HistoricalLocationData(HistoricalLocationEntity data) {
        super(data);
    }

    @Override
    public Time getTime() {
        Date time = data.getTime();
        DateTime dateTime = TimeUtil.createDateTime(time);
        return TimeUtil.createTime(dateTime);
    }

    @Override
    public Set<Location> getLocations() {
        return toSet(data.getLocations(), LocationData::new);
    }

    @Override
    public Thing getThing() {
        return new ThingData(data.getPlatform());
    }
}
