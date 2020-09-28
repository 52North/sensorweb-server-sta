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
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.StaConstants;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONProject extends JSONBase.JSONwithIdNameDescriptionTime<ProjectEntity> implements AbstractJSONEntity {

    public String runtime;

    @JsonManagedReference
    @JsonProperty(StaConstants.DATASTREAMS)
    public JSONDatastream[] datastreams;

    @JsonProperty(StaConstants.PROP_CLASSIFICATION)
    public String classification;

    @JsonProperty(StaConstants.PROP_PRIVACY_POLICY)
    public String privacyPolicy;

    @JsonProperty(StaConstants.PROP_TERMS_OF_USE)
    public String termsOfUse;

    public JSONProject() {
        self = new ProjectEntity();
    }

    @Override public ProjectEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                parseReferencedFrom();
                Assert.notNull(name, INVALID_INLINE_ENTITY_MISSING + "name");
                Assert.notNull(description, INVALID_INLINE_ENTITY_MISSING + "description");
                Assert.notNull(runtime, INVALID_INLINE_ENTITY_MISSING + "runtime");
                Assert.notNull(classification, INVALID_INLINE_ENTITY_MISSING + "classification");
                Assert.notNull(privacyPolicy, INVALID_INLINE_ENTITY_MISSING + "privacyPolicy");
                Assert.notNull(termsOfUse, INVALID_INLINE_ENTITY_MISSING + "termsOfUse");
                Assert.notNull(datastreams, INVALID_INLINE_ENTITY_MISSING + "Datastreams");

                return createEntity();
            case PATCH:
                parseReferencedFrom();
                return createEntity();

            case REFERENCE:
                Assert.isNull(runtime, INVALID_REFERENCED_ENTITY);
                Assert.isNull(name, INVALID_REFERENCED_ENTITY);
                Assert.isNull(description, INVALID_REFERENCED_ENTITY);
                Assert.isNull(classification, INVALID_REFERENCED_ENTITY);
                Assert.isNull(privacyPolicy, INVALID_REFERENCED_ENTITY);
                Assert.isNull(termsOfUse, INVALID_REFERENCED_ENTITY);
                self.setStaIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }

    private ProjectEntity createEntity() {
        self.setStaIdentifier(identifier);
        self.setName(name);
        self.setDescription(description);
        self.setClassification(classification);
        self.setTermsOfUse(termsOfUse);
        self.setPrivacyPolicy(privacyPolicy);

        if (runtime != null) {
            Time time = parseTime(runtime);
            if (time instanceof TimeInstant) {
                self.setRuntimeStart(((TimeInstant) time).getValue().toDate());
                self.setRuntimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setRuntimeStart(((TimePeriod) time).getStart().toDate());
                self.setRuntimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        if (datastreams != null) {
            Set<AbstractDatasetEntity> related = new HashSet<>();
            for (JSONDatastream datastream : datastreams) {
                related.add(datastream.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
            }
            self.setDatasets(related);
        } else if (backReference instanceof JSONDatastream) {
            Set<AbstractDatasetEntity> related = new HashSet<>();
            related.add(((JSONDatastream) backReference).getEntity());
            self.setDatasets(related);
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + StaConstants.DATASTREAM);
        }

        return self;
    }
}
