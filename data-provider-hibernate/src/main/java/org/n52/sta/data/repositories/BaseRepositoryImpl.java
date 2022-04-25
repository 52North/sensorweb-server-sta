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
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.n52.sta.data.support.EntityGraphBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

/**
 * Extends {@link SimpleJpaRepository} to provide dynamic fetchment of entity
 * members via {@link EntityGraph}.
 */
public class BaseRepositoryImpl<T>
        extends SimpleJpaRepository<T, Long> implements BaseRepository<T>, RepositoryConstants {

    private final EntityManager em;

    private final CriteriaBuilder criteriaBuilder;

    BaseRepositoryImpl(JpaEntityInformation<T, Long> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
        this.criteriaBuilder = em.getCriteriaBuilder();
    }

    @Override
    public Optional<String> getColumn(String columnName, Specification<T> spec) {
        return getSingleResult(createColumnQuery(columnName, spec));
    }

    @Override
    public List<String> getColumnList(String columnName, Specification<T> spec, Pageable pageable) {
        TypedQuery<String> query = createColumnQuery(columnName, spec, pageable);
        if (pageable.isUnpaged()) {
            return query.getResultList();
        } else {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
            return query.getResultList();
        }
    }

    @Override
    public Optional<T> findById(Long id, EntityGraphBuilder<T> entityGraph) {
        Class<T> entityClass = getDomainClass();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.where(criteriaBuilder.equal(root.get("id"), id));

        TypedQuery<T> typedQuery = em.createQuery(query);
        addEntityGraph(typedQuery, entityGraph.buildGraph(em));
        return getSingleResult(typedQuery);
    }

    @Override
    public Optional<T> findOne(Specification<T> spec, EntityGraphBuilder<T> entityGraph) {
        TypedQuery<T> query = getQuery(spec, entityGraph);
        return getSingleResult(query);
    }

    @Override
    public List<T> findAll(Specification<T> spec, EntityGraphBuilder<T> entityGraph) {
        return findAll(spec, Sort.unsorted(), entityGraph);
    }

    @Override
    public List<T> findAll(Specification<T> spec, Sort sort, EntityGraphBuilder<T> entityGraph) {
        return getQuery(spec, entityGraph, sort).getResultList();
    }

    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable, EntityGraphBuilder<T> entityGraph) {
        TypedQuery<T> query = getQuery(spec, entityGraph, pageable.getSort());
        return readPage(query, getDomainClass(), pageable, spec);
    }

    private TypedQuery<T> getQuery(Specification<T> spec, EntityGraphBuilder<T> entityGraph) {
        return getQuery(spec, entityGraph, Sort.unsorted());
    }

    private TypedQuery<T> getQuery(Specification<T> spec, EntityGraphBuilder<T> entityGraphBuilder, Sort sort) {
        Class<T> entityClass = getDomainClass();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        asPredicate(spec, root, query).ifPresent(query::where);

        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, criteriaBuilder));
        }

        TypedQuery<T> typedQuery = em.createQuery(query);
        EntityGraph<T> entityGraph = entityGraphBuilder.buildGraph(em);
        addEntityGraph(typedQuery, entityGraph);
        return typedQuery;
    }

    private TypedQuery<String> createColumnQuery(String columnName, Specification<T> spec) {
        return createColumnQuery(columnName, spec, null);
    }

    private TypedQuery<String> createColumnQuery(String columnName, Specification<T> spec, Pageable pageable) {
        Objects.requireNonNull(columnName, "columnName must not be null");

        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
        Root<T> root = query.from(getDomainClass());
        query.select(root.get(columnName));
        asPredicate(spec, root, query).ifPresent(query::where);

        if (pageable != null && pageable.getSort().isSorted()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        return em.createQuery(query);
    }

    private <R> Optional<R> getSingleResult(TypedQuery<R> typedQuery) {
        try {
            return Optional.of(typedQuery.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private void addEntityGraph(TypedQuery<T> query, EntityGraph<T> entityGraph) {
        if (entityGraph != null) {
            query.setHint("javax.persistence.fetchgraph", entityGraph);
            //query.setHint("javax.persistence.loadgraph", entityGraph);
        }
    }

    private Optional<Predicate> asPredicate(Specification<T> spec, Root<T> root, CriteriaQuery<?> query) {
        return spec != null
                ? Optional.ofNullable(spec.toPredicate(root, query, criteriaBuilder))
                : Optional.empty();
    }

}
