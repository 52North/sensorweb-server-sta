/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sta.data.cloudnative.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.*;
import org.n52.sta.data.cloudnative.dao.StaEntityDao;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.service.CloudNativeAbstractSensorThingsEntityServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public abstract class AbstractStaEntityDao<T extends StaDTO> implements StaEntityDao<T> {
    protected final ObjectMapper mapper = new ObjectMapper();
    protected final DSLContext ctx;
    protected final StaFirehoseClient firehoseClient;

    @Autowired
    protected AbstractStaEntityDao(DSLContext ctx, StaFirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        configureJacksonMapper();
    }

    private void configureJacksonMapper() {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        mapper.registerModule(javaTimeModule);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getColumn(Condition condition,
                                      String columnName,
                                      Class<T> entityClass) {
        if(condition == null || columnName == null || entityClass == null) {
            return Optional.empty();
        }
        try {
            Table<?> table = getEntityTable();
            if (table == null) {
                throw new STAInvalidQueryException(String.format(
                        CloudNativeAbstractSensorThingsEntityServiceImpl.INVALID_ENTITY_TYPE,
                        entityClass.getSimpleName()
                ));
            }
            return Optional.ofNullable(ctx
                    .select(DSL.field(columnName))
                    .from(table)
                    .where(condition)
                    .fetchOne(DSL.field(columnName, String.class))
            );
        } catch (STAInvalidQueryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getColumnList(Condition condition,
                                      Pageable pageable,
                                      String columnName,
                                      Class<T> entityClass) throws STAInvalidQueryException {

        if(condition == null || columnName == null || entityClass == null) {
            return null;
        }

        Table<?> table = getEntityTable();
        if (table == null) {
            throw new STAInvalidQueryException(String.format(
                    CloudNativeAbstractSensorThingsEntityServiceImpl.INVALID_ENTITY_TYPE,
                    entityClass.getSimpleName()
            ));
        }

        SelectConditionStep<Record1<String>> selectQuery = ctx
                .select(DSL.field(columnName, String.class))
                .from(table)
                .where(condition);

        if (pageable.getSort().isSorted()) {
            List<SortField<?>> sortFields = pageable.getSort().stream()
                    .map(order -> {
                        Field<?> field = checkPropertyName(order.getProperty());
                        return order.isAscending() ? field.asc() : field.desc();
                    })
                    .collect(Collectors.toList());
            selectQuery = (SelectConditionStep<Record1<String>>) selectQuery.orderBy(sortFields);
        }

        if (pageable.isPaged()) {
            selectQuery = (SelectConditionStep<Record1<String>>) selectQuery.limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset());
        }

        Result<Record1<String>> result =  selectQuery.fetch();

        return result.getValues(DSL.field(columnName, String.class));
    }


    @Override
    public Optional<T> findById(Long id,
                                QueryOptions queryOptions,
                                Class<T> entityClass) throws STAInvalidQueryException {


        Condition predicate = getEntityId().eq(id);
        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, queryOptions).fetch();
        return Optional.ofNullable(mapResultToDTO(result)).map(dto -> dto.get(0));
    }

    @Override
    public Optional<T> findOne(Condition predicate,
                               QueryOptions queryOptions,
                               Class<T> entityClass) throws STAInvalidQueryException {


        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, queryOptions).fetch();
        return Optional.ofNullable(mapResultToDTO(result)).map(dto -> dto.get(0));
    }

    @Override
    public Optional<T> findByStaIdentifier(String identifier,
                                           QueryOptions queryOptions,
                                           Class<T> entityClass) throws STAInvalidQueryException {

        Condition predicate = getStaEntityId().eq(identifier);

        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, queryOptions).fetch();
        return Optional.ofNullable(mapResultToDTO(result)).map(dto -> dto.get(0));
    }

    @Override
    public List<T> findAll(Condition predicate,
                           QueryOptions queryOptions,
                           Class<T> entityClass) throws STAInvalidQueryException {

        Result<Record> result = selectQueryBuilder(predicate, entityClass, null, queryOptions).fetch();

        return mapResultToDTO(result);
    }

    @Override
    public List<T> findAll(Condition predicate,
                           Sort sort,
                           QueryOptions queryOptions,
                           Class<T> entityClass) throws STAInvalidQueryException {
        Result<Record> result = selectQueryBuilder(predicate, entityClass, sort, queryOptions).fetch();

        return mapResultToDTO(result);
    }

    protected abstract List<T> mapResultToDTO(Result<Record> result);

    @Override
    public Page<T> findAll(Condition predicate,
                           Pageable pageable,
                           QueryOptions queryOptions,
                           Class<T> entityClass)
            throws STAInvalidQueryException {
        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        SelectSeekStepN<Record> query = (SelectSeekStepN<Record>) selectQueryBuilder(
                predicate,
                entityClass,
                sort,
                queryOptions);
        if(pageable.isPaged()) {
            query = (SelectSeekStepN<Record>) query.limit(pageable.getPageSize()).offset((int) pageable.getOffset());
        }
        Result<Record> result = query.fetch();
        List<T> content = mapResultToDTO(result);

        return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public boolean existsByStaIdentifier(String identifier,
                                         Class<T> entityClass)
            throws STAInvalidQueryException {

        Condition predicate = getStaEntityId().eq(identifier);
        return count(predicate, entityClass) > 0;
    }

    @Override
    public long count(Condition spec, Class<T> entityClass) throws STAInvalidQueryException {
        Table<?> table = getEntityTable();
        if (table == null) {
            throw new STAInvalidQueryException(String.format(
                    CloudNativeAbstractSensorThingsEntityServiceImpl.INVALID_ENTITY_TYPE,
                    entityClass.getSimpleName()
            ));
        }
        Long count = ctx
                .selectCount()
                .from(table)
                .where(spec)
                .fetchOne(0, Long.class);

        return count == null ? 0 : count;
    }

    public Select<Record> selectQueryBuilder(@NotNull Condition where,
                                             @NotNull Class<T> entityClass,
                                             @Nullable Sort sort,
                                             @Nullable QueryOptions queryOptions)
            throws STAInvalidQueryException {


        Table<?> table = getEntityTable();
        if (table == null) {
            throw new STAInvalidQueryException(String.format(
                    CloudNativeAbstractSensorThingsEntityServiceImpl.INVALID_ENTITY_TYPE,
                    entityClass.getSimpleName()
            ));
        }
        List<Field<?>> select = new ArrayList<>(getSelect(queryOptions));
        Table<?> from = getJoin(select, table, queryOptions);
        List<SortField<?>> orderBy = getOrderBy(sort);

        if(orderBy != null) {
            return ctx.select(select).from(from).where(where).orderBy(orderBy);
        }
        return ctx.select(select).from(from).where(where);

    }

    private List<SortField<?>> getOrderBy(Sort sort) {
        return sort == null ? null : sort.stream()
                    .map(order -> {
                        Field<?> field = DSL.field(order.getProperty());
                        return order.isAscending() ? field.asc() : field.desc();
                    })
                    .collect(Collectors.toList());
    }

    private Table<?> getJoin(List<Field<?>> fromTables, Table<?> table, QueryOptions queryOptions)
            throws STAInvalidQueryException {

        Set<Table<?>> joins = createJoinList(queryOptions);

        for (Table<?> toJoin : joins) {
            table = table.leftJoin(toJoin).onKey();
            if (queryOptions == null || queryOptions.getSelectFilter() == null) {
                fromTables.addAll(Arrays.asList(toJoin.fields()));
            }
        }

        return table;
    }

    private List<Field<?>> getSelect(QueryOptions queryOptions) {

        List<Field<?>> fieldList = new ArrayList<>();

        // always try to minimize the columns to be fetched from a columnar data store
        if(queryOptions != null && queryOptions.getSelectFilter() != null) {
            fieldList.add(getEntityId());
            fieldList.addAll(queryOptions
                    .getSelectFilter()
                    .getItems()
                    .stream()
                    .map(this::checkPropertyName)
                    .collect(Collectors.toList()));

        }
        // gotta fetch all fields 'cause select clause is not specified
        else {
            fieldList = getEntityTableFields();
        }

        return fieldList;
    }
}
