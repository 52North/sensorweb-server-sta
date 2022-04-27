package org.n52.sta.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.n52.sta.api.entity.Identifiable;

public final class EntityServiceLookup {

    private Map<String, EntityProvider<?>> entityProvidersByLowerCaseName;

    public EntityServiceLookup() {
        this.entityProvidersByLowerCaseName = new HashMap<>();
    }

    public boolean contains(String name) {
        String lowerCasedName = name.toLowerCase();
        return entityProvidersByLowerCaseName.containsKey(lowerCasedName);
    }

    @SuppressWarnings("unchecked")
    public <T extends Identifiable> Optional<EntityProvider<T>> getEntityProvider(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String lowerCasedName = name.toLowerCase();
        EntityProvider<?> entityProvider = entityProvidersByLowerCaseName.get(lowerCasedName);
        return Optional.ofNullable((EntityProvider<T>) entityProvider);
    }

    public void addEntityService(StaEntityType entityType, EntityProvider<?> entityProvider) {
        Objects.requireNonNull(entityType, "entityType must not be null!");
        addEntityService(entityType.name(), entityProvider);
    }

    public void addEntityService(String name, EntityProvider<?> entityProvider) {
        Objects.requireNonNull(entityProvider, "entityProvider must not be null!");
        Objects.requireNonNull(name, "name must not be null!");
        entityProvidersByLowerCaseName.put(name.toLowerCase(), entityProvider);
    }

    @SuppressWarnings("all")
    public enum StaEntityType {
        Thing, Things, Location, Locations, HistoricalLocation, HistoricalLocations,
        Sensor, Sensors, Datastream, Datastreams, Observation, Observations,
        ObservedProperty, ObservedProperties, FeatureOfInterest, FeaturesOfInterest;
    }
}
