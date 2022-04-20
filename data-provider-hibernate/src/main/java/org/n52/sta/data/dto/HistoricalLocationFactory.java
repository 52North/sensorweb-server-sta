package org.n52.sta.data.dto;

import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.HistoricalLocationDto;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.utils.TimeUtil;

public class HistoricalLocationFactory {

    public static HistoricalLocation create(HistoricalLocationEntity entity) {
        HistoricalLocationFactory factory = create();
        factory.setIdentifier(entity.getStaIdentifier());
        factory.setThing(entity.getPlatform());
        factory.setTime(entity.getTime());
        factory.setLocations(entity);

        return factory.get();
    }
    

    public static HistoricalLocationFactory create() {
        return new HistoricalLocationFactory(new HistoricalLocationDto());
    }

    private final HistoricalLocationDto dto;

    public HistoricalLocationFactory(HistoricalLocationDto dto) {
        this.dto = dto;
    }

    public HistoricalLocationFactory setIdentifier(String identifer) {
        dto.setId(identifer);
        return this;
    }

    private HistoricalLocationFactory setTime(Date time) {
        DateTime dateTime = TimeUtil.createDateTime(time);
        setTime(TimeUtil.createTime(dateTime));
        return this;
    }

    public HistoricalLocationFactory setTime(Time time) {
        dto.setTime(time);
        return this;
    }

    private HistoricalLocationFactory setLocations(HistoricalLocationEntity entity) {
        Set<LocationEntity> locations = entity.getLocations();
        Streams.stream(locations).forEach(this::addLocation);
        return this;
    }

    private HistoricalLocationFactory addLocation(LocationEntity entity) {
        dto.addLocation(LocationFactory.create(entity));
        return this;
    }

    private HistoricalLocationFactory setThing(PlatformEntity entity) {
        dto.setThing(ThingFactory.create(entity));
        return this;
    }

    public HistoricalLocation get() {
        return dto;
    }

}
