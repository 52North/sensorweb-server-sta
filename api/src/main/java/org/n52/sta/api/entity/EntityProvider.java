package org.n52.sta.api.entity;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.domain.AggregateFactory;

public interface EntityProvider<T> {
    
    boolean exists(String id) throws ProviderException;

    AggregateFactory<T> getEntity(String id, QueryOptions options) throws ProviderException;

    EntityCollection<AggregateFactory<T>> getEntities(QueryOptions options) throws ProviderException;

}
