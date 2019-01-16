package org.n52.sta.data.repositories;

import java.util.Optional;

import org.n52.series.db.beans.DescribableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ParameterDataRepository<T extends DescribableEntity> extends JpaRepository<T, Long>, QuerydslPredicateExecutor<T> {

    boolean existsByIdentifier(String identifier);

    Optional<T> findByIdentifier(String identifier);

    T getOneByIdentifier(String identifier);

}