package org.n52.sta.api;

import org.n52.sta.api.entity.Identifiable;

/**
 * Interface for Entity Provider Implementations that can be delegated to. Used for assuring that autowired services
 * are actually an implementation that can be delegated to.
 * @param <T> Type of the Entity
 */
public interface EntityProviderDelegate<T extends Identifiable> extends EntityProvider<T> {
}
