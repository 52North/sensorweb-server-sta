/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.sta.serdes.json.extension.JSONCSDatastream;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONSensor extends JSONBase.JSONwithIdNameDescription<SensorEntity> implements AbstractJSONEntity {

    protected static final String INALID_ENCODING_TYPE = "Invalid encodingType supplied. Only SensorML or PDF allowed.";
    protected static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    protected static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
    protected static final String PDF = "application/pdf";

    // JSON Properties. Matched by Annotation or variable name
    public String properties;
    public String encodingType;
    public String metadata;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    public JSONSensor() {
        self = new SensorEntity();
    }

    @Override
    public SensorEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
            Assert.notNull(encodingType, INVALID_INLINE_ENTITY_MISSING + "encodingType");
            Assert.notNull(metadata, INVALID_INLINE_ENTITY_MISSING + "metadata");
            self.setIdentifier(identifier);
            self.setStaIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            handleEncodingType();

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                                          .map(ds -> ds.toEntity(JSONBase.EntityType.FULL,
                                                                 JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
            }

            // Deal with back reference during deep insert
            if (backReference != null) {
                if (backReference instanceof JSONDatastream) {
                    self.addDatastream(((JSONDatastream) backReference).getEntity());
                } else {
                    self.addDatastream(((JSONCSDatastream) backReference).getEntity());
                }
            }

            return self;
        case PATCH:
            self.setIdentifier(identifier);
            self.setStaIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            if (encodingType != null) {
                handleEncodingType();
            } else {
                // Used when PATCHing new metadata
                self.setDescriptionFile(metadata);
            }

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                                          .map(ds -> ds.toEntity(JSONBase.EntityType.REFERENCE))
                                          .collect(Collectors.toSet()));
            }
            return self;
        case REFERENCE:
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(metadata, INVALID_REFERENCED_ENTITY);

            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            self.setStaIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }

    protected void handleEncodingType() {
        if (encodingType.equalsIgnoreCase(STA_SENSORML_2)) {
            self.setFormat(new FormatEntity().setFormat(SENSORML_2));
            ProcedureHistoryEntity procedureHistoryEntity = new ProcedureHistoryEntity();
            procedureHistoryEntity.setProcedure(self);
            procedureHistoryEntity.setFormat(self.getFormat());
            procedureHistoryEntity.setStartTime(DateTime.now().toDate());
            procedureHistoryEntity.setXml(metadata);
            Set<ProcedureHistoryEntity> set = new LinkedHashSet<>();
            set.add(procedureHistoryEntity);
            self.setProcedureHistory(set);
        } else if (encodingType.equalsIgnoreCase(PDF)) {
            self.setFormat(new FormatEntity().setFormat(PDF));
            self.setDescriptionFile(metadata);
        } else {
            Assert.notNull(null, INALID_ENCODING_TYPE);
        }
    }
}
