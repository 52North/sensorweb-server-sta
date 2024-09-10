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

package org.n52.sta.data.cloudnative.test.dao;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.SensorDaoImpl;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class SensorDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private SensorDaoImpl sensorDao;

    @BeforeEach
    public void setUp() {
        sensorDao = new SensorDaoImpl(ctx, null);
    }

    @Test
    public void testWithExistsByName() {
        String name = "DS18B20";
        Class<SensorDTO> entityClass = SensorDTO.class;
        boolean result = false;
        try {
            result = sensorDao.existsByName(name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result);
    }

    @Test
    public void testWithFindByName() {
        String name = "DS18B20";
        Class<SensorDTO> entityClass = SensorDTO.class;
        Optional<SensorDTO> result = Optional.empty();
        try {
            result = sensorDao.findByName(name, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getName(), "DS18B20");
    }

    @Test
    public void testWithGetColumn() {
        String name = "sta_identifier";
        String value = "42d1b9af-591c-46ec-bf86-8a9185bec736";
        Condition condition = DSL.field(name).eq(value);
        Class<SensorDTO> entityClass = SensorDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = sensorDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 1L;
        QueryOptions options = null;
        Class<SensorDTO> entityClass = SensorDTO.class;
        Optional<SensorDTO> result = Optional.empty();
        try {
            result = sensorDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "42d1b9af-591c-46ec-bf86-8a9185bec736");
    }

    @Test
    public void testWithFindOne() {
        String description = "DS18B20 is an air temperature sensor…";
        Condition predicate = StaEntity.SENSOR.DESCRIPTION.eq(description);
        QueryOptions options = null;
        Class<SensorDTO> entityClass = SensorDTO.class;
        Optional<SensorDTO> result = Optional.empty();
        try {
            result = sensorDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "42d1b9af-591c-46ec-bf86-8a9185bec736";
        QueryOptions options = null;
        Class<SensorDTO> entityClass = SensorDTO.class;
        Optional<SensorDTO> result = Optional.empty();
        try {
            result = sensorDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithFindAll() {
        Condition predicate = StaEntity.SENSOR_PROPERTIES.NAME.eq("pr_code");
        QueryOptions options = null;
        Class<SensorDTO> entityClass = SensorDTO.class;
        List<SensorDTO> result = List.of();
        try {
            result = sensorDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getProperties().findValue("pr_code").toString(), "\"pr.001.sample\"");
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "42d1b9af-591c-46ec-bf86-8a9185bec736";
        Class<SensorDTO> entityClass = SensorDTO.class;
        boolean result = false;
        try {
            result = sensorDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }

}
