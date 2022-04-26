/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.data.repositories;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityGraph;

import org.n52.sta.data.support.EntityGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Custom repository interface for extending {@link SimpleJpaRepository}.
 *
 * All repositories extending from this interface will get injected the
 * custom implementation of all methods, in particular those providing
 * an {@link EntityGraph} parameter.
 *
 * @see BaseRepositoryImpl
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaSpecificationExecutor<T>, JpaRepository<T, Long> {

    /**
     * Gets the property value of a single entity matching the given
     * {@link Specification}.
     *
     * Fetches the value of a single entity stored in column {@code columnName}.
     *
     * @param columnName The column name of the property
     * @param spec       The query specification
     * @return The value stored in column with columnName, Optional.empty()
     *         otherwise
     */
    Optional<String> getColumn(String columnName, Specification<T> spec);

    /**
     * Gets the property values of all entities matching the given
     * {@link Specification}.
     *
     * Fetches the values of all entities stored in column {@code columnName}.
     *
     * @param columnName The column name of the property
     * @param spec       The query specification
     * @param pageable   The pagination information
     * @return The list of values stored in column with columnName, Optional.empty()
     *         otherwise
     */
    List<String> getColumnList(String columnName, Specification<T> spec, Pageable pageable);

    boolean existsByStaIdentifier(String staIdentifier);

    Optional<T> findByStaIdentifier(String identifier, EntityGraphBuilder<T> graphBuilder);

    void deleteByStaIdentifier(String identifier);

    Optional<T> findById(Long id, EntityGraphBuilder<T> entityGraphBuilder);

    Optional<T> findOne(Specification<T> spec, EntityGraphBuilder<T> entityGraphBuilder);

    List<T> findAll(Specification<T> spec, EntityGraphBuilder<T> entityGraphBuilder);

    List<T> findAll(Specification<T> spec, Sort sort, EntityGraphBuilder<T> entityGraphBuilder);

    Page<T> findAll(Specification<T> spec, Pageable pageable, EntityGraphBuilder<T> entityGraphBuilder);

}
