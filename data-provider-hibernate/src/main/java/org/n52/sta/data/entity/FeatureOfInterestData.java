package org.n52.sta.data.entity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class FeatureOfInterestData extends StaData<StaFeatureEntity<?>> implements FeatureOfInterest {

    public FeatureOfInterestData(AbstractFeatureEntity<?> dataEntity) {
        this(tryToCast(dataEntity));
    }

    public FeatureOfInterestData(StaFeatureEntity<?> dataEntity) {
        super(dataEntity);
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
    public Geometry getFeature() {
        return data.getGeometry();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Observation<?>> getObservations() {
        return toSet(data.getObservations(), ObservationData::new);
    }

    private static StaFeatureEntity<?> tryToCast(AbstractFeatureEntity<?> feature) {
        Optional<StaFeatureEntity<?>> staFeature = feature instanceof StaFeatureEntity
                ? Optional.of((StaFeatureEntity<?>) feature)
                : Optional.empty();
        return staFeature.orElseThrow(() -> new IllegalStateException("Entity is not a StaFeature"));
    }

}
