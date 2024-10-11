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
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.condition.ObservedPropertyQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.ObservedPropertyDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class ObservedPropertyDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private ObservedPropertyDaoImpl observedPropertyDao;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose = new StaFirehoseClient(firehoseClient);
    private final DatastreamQueryConditions dsQC = new DatastreamQueryConditions();
    private final ObservedPropertyQueryConditions opQC = new ObservedPropertyQueryConditions();

    @BeforeEach
    public void setUp() {
        dsQC.setDslContext(ctx);
        opQC.setDslContext(ctx);
        observedPropertyDao = new ObservedPropertyDaoImpl(ctx, staFirehose, dsQC, opQC);
    }

    @Test
    public void testWithExistsByName() {
        String name = "DewPoint Temperature";
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        boolean result = false;
        try {
            result = observedPropertyDao.existsByName(name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result);
    }

    @Test
    public void testWithFindByName() {
        String name = "DewPoint Temperature";
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        Optional<ObservedPropertyDTO> result = Optional.empty();
        try {
            result = observedPropertyDao.findByName(name, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getName(), name);
    }

    @Test
    public void testWithGetColumn() {
        String name = "description";
        String value = "The dewpoint temperature is the temperature .";
        Condition condition = DSL.field(name).eq(value);
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = observedPropertyDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 1L;
        QueryOptions options = null;
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        Optional<ObservedPropertyDTO> result = Optional.empty();
        try {
            result = observedPropertyDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "1");
    }

    @Test
    public void testWithFindOne() {
        String definition = "http://sweet.jpl.nasa.gov/ontology/property.owl#DewPointTemperature";
        Condition predicate = StaEntity.OBSERVED_PROPERTY.IDENTIFIER.eq(definition);
        QueryOptions options = null;
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        Optional<ObservedPropertyDTO> result = Optional.empty();
        try {
            result = observedPropertyDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDefinition(), definition);
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "1";
        QueryOptions options = null;
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        Optional<ObservedPropertyDTO> result = Optional.empty();
        try {
            result = observedPropertyDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithFindAll() {
        Condition predicate = StaEntity.OBSERVED_PROPERTY_PROPERTIES.NAME.eq("op_code");
        QueryOptions options = null;
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        List<ObservedPropertyDTO> result = List.of();
        try {
            result = observedPropertyDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getProperties().findValue("op_code").toString(), "\"ob.001.sample\"");
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "1";
        Class<ObservedPropertyDTO> entityClass = ObservedPropertyDTO.class;
        boolean result = false;
        try {
            result = observedPropertyDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }
}
