package org.n52.sta.data.dto;

import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.api.dto.FeatureOfInterestDto;
import org.n52.sta.api.entity.FeatureOfInterest;

public class FeatureOfInterestFactory extends BaseDtoFactory<FeatureOfInterestDto, FeatureOfInterestFactory> {

    public static FeatureOfInterest create(FeatureEntity entity) {
        FeatureOfInterestFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setFeature(entity);
        return factory.get();
    }

    public static FeatureOfInterestFactory create() {
        return new FeatureOfInterestFactory(new FeatureOfInterestDto());
    }

    public FeatureOfInterestFactory(FeatureOfInterestDto dto) {
        super(dto);
    }

    private FeatureOfInterestFactory setFeature(FeatureEntity entity) {
        get().setFeature(entity.getGeometry());
        return this;
    }
}
