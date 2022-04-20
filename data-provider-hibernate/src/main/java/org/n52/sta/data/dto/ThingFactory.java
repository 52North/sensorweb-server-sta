package org.n52.sta.data.dto;

import java.util.Set;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.sta.api.dto.ThingDto;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class ThingFactory extends BaseDtoFactory<ThingDto, ThingFactory> {

    public static Thing create(PlatformEntity entity) {
        ThingFactory factory = create();

        factory.withMetadata(entity);
        factory.setDatastreams(entity);
        factory.setProperties(entity);
        

        return factory.get();
    }

    public static ThingFactory create() {
        return new ThingFactory(new ThingDto());
    }

    private ThingFactory(ThingDto dto) {
        super(dto);
    }

    private ThingFactory setDatastreams(PlatformEntity entity) {
        Set<AbstractDatasetEntity> datasets = entity.getDatasets();
        Streams.stream(datasets).forEach(this::addDatastream);
        return this;
    }

    private ThingFactory addDatastream(AbstractDatasetEntity entity) {
        get().addDatastream(DatastreamFactory.create(entity));
        return this;
    }

    public ThingFactory addDatastream(Datastream datastream) {
        get().addDatastream(datastream);
        return this;
    }

    public ThingFactory addLocation(Location location) {
        get().addLocation(location);
        return this;
    }

    public ThingFactory addLocation(HistoricalLocation location) {
        get().addHistoricalLocation(location);
        return this;
    }

}
