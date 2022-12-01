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

package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;

abstract class DatabaseEntityAdapter<T extends DescribableEntity> {

    private final EntityServiceLookup serviceLookup;

    protected DatabaseEntityAdapter(EntityServiceLookup serviceLookup) {
        Objects.requireNonNull(serviceLookup, "serviceLookup must not be null");
        this.serviceLookup = serviceLookup;
    }

    protected ParameterEntity< ? > convertParameter(T entity,
            Map.Entry<String, Object> parameter) {

        String key = parameter.getKey();
        Object value = parameter.getValue();
        ParameterEntity parameterEntity;

        Class< ? > valueType = value.getClass();
        if (Number.class.isAssignableFrom(valueType)) {
            parameterEntity = ParameterFactory.from(entity, ParameterFactory.ValueType.QUANTITY);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (Boolean.class.isAssignableFrom(valueType)) {
            parameterEntity = ParameterFactory.from(entity, ParameterFactory.ValueType.BOOLEAN);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (String.class.isAssignableFrom(valueType)) {
            parameterEntity = ParameterFactory.from(entity, ParameterFactory.ValueType.TEXT);
            parameterEntity.setValue(value);
        } else {
            // TODO handle type 'JSON'
            throw new RuntimeException("can not handle parameter with unknown type: " + key);
        }
        parameterEntity.setName(key);

        return parameterEntity;
    }

    protected abstract Optional<T> getEntity(String id);

    protected String generateId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /*
     * protected <E extends Identifiable> E getOrSaveMandatory(E input, Class<E> type) throws EditorException
     * { if (input == null) { String typeName = type.getSimpleName(); throw new
     * EditorException("The input for '" + typeName + "' is null!"); } return getOrSave(input, type); }
     * protected <E extends Identifiable> Optional<E> getOrSaveOptional(E input, Class<E> type) throws
     * EditorException { return input == null ? Optional.empty() : Optional.of(getOrSave(input, type)); }
     * private <E extends Identifiable> E getOrSave(E input, Class<E> type) throws EditorException {
     * AbstractEntityService<E> service = getService(type); Optional<E> optionalEntity =
     * service.getEntity(input.getId()); return optionalEntity.isPresent() ? optionalEntity.get() :
     * service.save(input); }
     */

    protected <E extends Identifiable> EntityService<E> getService(Class<E> entityType) {
        return serviceLookup.getService(entityType)
                            .orElseThrow(() -> {
                                String msg = String.format("No registered service found for '%s'",
                                                           entityType.getSimpleName());
                                return new IllegalStateException(msg);
                            });
    }

    protected static <T> void setIfNotNull(Supplier<T> supplier, Consumer<T> consumer) {
        T value = supplier.get();
        if (value != null) {
            consumer.accept(value);
        }
    }

    protected static <T> void errorIfNotNull(Supplier<T> supplier, String property) throws EditorException {
        if (supplier.get() != null) {
            throw new EditorException("Patch not implemented on given property: " + property);
        }
    }

}
