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
package org.n52.sta.api.old.serialize.json;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.n52.sta.api.old.dto.Sensor;
import org.n52.sta.api.old.entity.SensorDTO;
import org.n52.sta.api.old.serialize.common.AbstractJSONEntity;
import org.n52.sta.api.old.serialize.common.JSONBase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONSensor extends JSONBase.JSONwithIdNameDescription<SensorDTO> implements AbstractJSONEntity {

    protected static final String INALID_ENCODING_TYPE = "Invalid encodingType supplied. Only SensorML or PDF allowed.";
    protected static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    protected static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
    protected static final String PDF = "application/pdf";

    // JSON Properties. Matched by Annotation or variable name
    public ObjectNode properties;
    public String encodingType;
    public String metadata;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    public JSONSensor() {
        self = new Sensor();
    }

    @Override
    public SensorDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                assertNotNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                assertNotNull(encodingType, INVALID_INLINE_ENTITY_MISSING + "encodingType");
                assertNotNull(metadata, INVALID_INLINE_ENTITY_MISSING + "metadata");
                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);
                self.setMetadata(metadata);

                handleEncodingType();

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (Datastreams != null) {
                    self.setDatastreams(Arrays.stream(Datastreams)
                                            .map(ds -> ds.parseToDTO(JSONBase.EntityType.FULL,
                                                                     JSONBase.EntityType.REFERENCE))
                                            .collect(Collectors.toSet()));
                }

                // Deal with back reference during deep insert
                if (backReference != null) {
                    self.addDatastream(((JSONDatastream) backReference).getEntity());
                }

                return self;
            case PATCH:
                self.setId(identifier);
                self.setName(name);
                self.setDescription(description);
                self.setMetadata(metadata);

                if (encodingType != null) {
                    handleEncodingType();
                }

                if (properties != null) {
                    self.setProperties(properties);
                }

                if (Datastreams != null) {
                    self.setDatastreams(Arrays.stream(Datastreams)
                                            .map(ds -> ds.parseToDTO(JSONBase.EntityType.REFERENCE))
                                            .collect(Collectors.toSet()));
                }
                return self;
            case REFERENCE:
                assertIsNull(name, INVALID_REFERENCED_ENTITY);
                assertIsNull(description, INVALID_REFERENCED_ENTITY);
                assertIsNull(encodingType, INVALID_REFERENCED_ENTITY);
                assertIsNull(metadata, INVALID_REFERENCED_ENTITY);
                assertIsNull(properties, INVALID_REFERENCED_ENTITY);
                assertIsNull(Datastreams, INVALID_REFERENCED_ENTITY);

                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }

    protected void handleEncodingType() {
        if (encodingType.equalsIgnoreCase(STA_SENSORML_2) || encodingType.equalsIgnoreCase(PDF)) {
            self.setEncodingType(encodingType);
        } else {
            assertNotNull(null, INALID_ENCODING_TYPE);
        }
    }
}
