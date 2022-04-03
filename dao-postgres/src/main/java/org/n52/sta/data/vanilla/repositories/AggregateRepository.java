package org.n52.sta.data.vanilla.repositories;

import java.util.Set;

public interface AggregateRepository<T> {

    Set<T> findAllByAggregationId(Long id);

}
