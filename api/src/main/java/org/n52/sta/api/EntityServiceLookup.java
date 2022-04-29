package org.n52.sta.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.service.EntityService;

public final class EntityServiceLookup {

    private final Map<Class<? extends Identifiable>, EntityService<? extends Identifiable>> entityServicesByType;

    public EntityServiceLookup() {
        this.entityServicesByType = new HashMap<>();
    }

    public boolean contains(Class<? extends Identifiable> type) {
        return entityServicesByType.containsKey(type);
    }

    public Set<Class<?>> getRegisteredEntityTypes() {
        return new HashSet<>(entityServicesByType.keySet());
    }

    @SuppressWarnings("unchecked")
    public <T extends Identifiable> Optional<EntityService<T>> getService(Class<T> type) {
        if (type == null) {
            return Optional.empty();
        }
        EntityProvider<?> entityProvider = entityServicesByType.get(type);
        return Optional.ofNullable((EntityService<T>) entityProvider);
    }

    public <T extends Identifiable> void addEntityService(Class<T> type, EntityService<T> service) {
        Objects.requireNonNull(type, "typemust not be null!");
        Objects.requireNonNull(service, "service must not be null!");
        entityServicesByType.put(type, service);
    }

}
