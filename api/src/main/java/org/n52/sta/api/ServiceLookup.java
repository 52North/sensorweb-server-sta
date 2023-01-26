package org.n52.sta.api;

import java.util.Optional;
import java.util.Set;

import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.service.EntityService;

public interface ServiceLookup {

    boolean contains(Class< ? extends Identifiable> type);

    Set<Class< ? >> getRegisteredEntityTypes();

    <T extends Identifiable> Optional<EntityService<T>> getService(Class<T> type);
}
