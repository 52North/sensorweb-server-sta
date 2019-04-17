package org.n52.sta.data.repositories;

import java.util.Optional;

import org.n52.series.db.beans.sta.AbstractStaEntity;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NameRepository<T extends AbstractStaEntity> extends AbstractStaRepository<T>{

    boolean existsByName(String name);

    Optional<T> findByName(String name);
}
