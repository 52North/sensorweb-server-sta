package org.n52.sta.data.dto;

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.api.dto.ObservedPropertyDto;
import org.n52.sta.api.entity.ObservedProperty;

public class ObservedPropertyFactory extends BaseDtoFactory<ObservedPropertyDto, ObservedPropertyFactory> {

    public static ObservedProperty create(PhenomenonEntity entity) {
        ObservedPropertyFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setDefinition(entity.getIdentifier());

        return factory.get();
    }

    public static ObservedPropertyFactory create() {
        return new ObservedPropertyFactory(new ObservedPropertyDto());
    }

    public ObservedPropertyFactory(ObservedPropertyDto dto) {
        super(dto);
    }

    private ObservedPropertyFactory setDefinition(String definition) {
        get().setDefinition(definition);
        return this;
    }

}
