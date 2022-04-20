package org.n52.sta.data.dto;

import java.util.Optional;
import java.util.Set;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.sta.api.dto.FeatureOfInterestDto;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class FeatureOfInterestFactory extends BaseDtoFactory<FeatureOfInterestDto, FeatureOfInterestFactory> {

    public static FeatureOfInterest create(AbstractFeatureEntity<?> entity) {
        Optional<StaFeatureEntity<?>> feature = tryToCast(entity);
        return feature.map(FeatureOfInterestFactory::create)
                .orElseThrow(() -> new IllegalStateException("No StaFeature"));
    }

    private static Optional<StaFeatureEntity<?>> tryToCast(AbstractFeatureEntity<?> entity) {
        return entity instanceof StaFeatureEntity
                ? Optional.of((StaFeatureEntity<?>) entity)
                : Optional.empty();
    }

    public static FeatureOfInterest create(StaFeatureEntity<?> entity) {
        FeatureOfInterestFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setObservations(entity);
        factory.setFeature(entity);
        return factory.get();
    }

    public static FeatureOfInterestFactory create() {
        return new FeatureOfInterestFactory(new FeatureOfInterestDto());
    }

    public FeatureOfInterestFactory(FeatureOfInterestDto dto) {
        super(dto);
    }

    private FeatureOfInterestFactory setObservations(StaFeatureEntity<?> entity) {
        Set<DataEntity<?>> observations = entity.getObservations();
        Streams.stream(observations).forEach(this::addObservation);
        return this;
    }

    private FeatureOfInterestFactory addObservation(DataEntity<?> entity) {
        return addObservation(ObservationFactory.create(entity));
    }

    public FeatureOfInterestFactory addObservation(Observation<?> observation) {
        get().addObservation(observation);
        return this;
    }

    private FeatureOfInterestFactory setFeature(StaFeatureEntity<?> entity) {
        get().setFeature(entity.getGeometry());
        return this;
    }
}
