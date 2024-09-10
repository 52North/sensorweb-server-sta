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
import org.jooq.impl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.ThingDaoImpl;
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
public class ThingDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private ThingDaoImpl thingDao;

    @BeforeEach
    public void setUp() {
        thingDao = new ThingDaoImpl(ctx, null);
    }

    @Test
    public void testWithExistsByName() {
        String name = "oven";
        Class<ThingDTO> entityClass = ThingDTO.class;
        boolean result = false;
        try {
            result = thingDao.existsByName(name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result);
    }

    @Test
    public void testWithFindByName() {
        String name = "oven";
        Class<ThingDTO> entityClass = ThingDTO.class;
        Optional<ThingDTO> result = Optional.empty();
        try {
            result = thingDao.findByName(name, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getName(), "oven");
    }

    @Test
    public void testWithGetColumn() {
        String name = "sta_identifier";
        String value = "33ed6521-b484-495e-8159-ab4906363235";
        Condition condition = DSL.field(name).eq(value);
        Class<ThingDTO> entityClass = ThingDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = thingDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 2L;
        QueryOptions options = null;
        Class<ThingDTO> entityClass = ThingDTO.class;
        Optional<ThingDTO> result = Optional.empty();
        try {
            result = thingDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "33ed6521-b484-495e-8159-ab4906363235");
    }

    @Test
    public void testWithFindOne() {
        String description = "This an oven with a temperature datastream.";
        Condition predicate = StaEntity.THING.DESCRIPTION.eq(description);
        QueryOptions options = null;
        Class<ThingDTO> entityClass = ThingDTO.class;
        Optional<ThingDTO> result = Optional.empty();
        try {
            result = thingDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "33ed6521-b484-495e-8159-ab4906363235";
        QueryOptions options = null;
        Class<ThingDTO> entityClass = ThingDTO.class;
        Optional<ThingDTO> result = Optional.empty();
        try {
            result = thingDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithFindAll() {
        Condition predicate = StaEntity.THING_PROPERTIES.NAME.eq("thing_category");
        QueryOptions options = null;
        Class<ThingDTO> entityClass = ThingDTO.class;
        List<ThingDTO> result = List.of();
        try {
            result = thingDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getProperties().findValue("thing_category").toString(), "\"08.2024\"");
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "33ed6521-b484-495e-8159-ab4906363235";
        Class<ThingDTO> entityClass = ThingDTO.class;
        boolean result = false;
        try {
            result = thingDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }

}
