package org.n52.sta.data.dto;

import java.util.Set;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.api.dto.ObservedPropertyDto;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.ObservedProperty;

public class ObservedPropertyFactory extends BaseDtoFactory<ObservedPropertyDto, ObservedPropertyFactory> {

    public static ObservedProperty create(PhenomenonEntity entity) {
        ObservedPropertyFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setDatastreams(entity);
        factory.setDefinition(entity.getIdentifier());

        return factory.get();
    }

    public static ObservedPropertyFactory create() {
        return new ObservedPropertyFactory(new ObservedPropertyDto());
    }

    public ObservedPropertyFactory(ObservedPropertyDto dto) {
        super(dto);
    }

    public ObservedPropertyFactory setDefinition(String definition) {
        get().setDefinition(definition);
        return this;
    }

    private ObservedPropertyFactory setDatastreams(PhenomenonEntity entity) {
        Set<AbstractDatasetEntity> datasets = entity.getDatasets();
        Streams.stream(datasets).forEach(this::addDataset);
        return this;
    }

    private ObservedPropertyFactory addDataset(AbstractDatasetEntity entity) {
        return addDataset(DatastreamFactory.create(entity));
    }

    public ObservedPropertyFactory addDataset(Datastream datastream) {
        get().addDatastream(datastream);
        return this;
    }

}
