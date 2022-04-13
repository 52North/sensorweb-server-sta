/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.api.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.impl.ObservedProperty;
import org.n52.sta.api.serdes.common.AbstractJSONEntity;
import org.n52.sta.api.serdes.common.JSONBase;

import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservedProperty extends JSONBase.JSONwithIdNameDescription<ObservedPropertyDTO>
    implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String definition;
    public ObjectNode properties;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    public JSONObservedProperty() {
        self = new ObservedProperty();
    }

    @Override
    public ObservedPropertyDTO parseToDTO(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                assertNotNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                assertNotNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                assertNotNull(definition, INVALID_INLINE_ENTITY_MISSING + "definition");

                self.setId(identifier);
                self.setDefinition(definition);
                self.setName(name);
                self.setDescription(description);

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
                    self.addDatastreams(((JSONDatastream) backReference).getEntity());
                }

                return self;
            case PATCH:
                self.setId(identifier);
                self.setDefinition(definition);
                self.setName(name);
                self.setDescription(description);

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
                assertIsNull(definition, INVALID_REFERENCED_ENTITY);
                assertIsNull(properties, INVALID_REFERENCED_ENTITY);
                assertIsNull(Datastreams, INVALID_REFERENCED_ENTITY);

                self.setId(identifier);
                return self;
            default:
                return null;
        }
    }
}
