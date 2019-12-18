/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

import java.util.Date;

@SuppressWarnings("VisibilityModifier")
public class JSONObservation extends JSONBase.JSONwithIdTime<StaDataEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String phenomenonTime;
    public String resultTime;
    public String result;
    public String[] resultQuality;
    public String validTime;
    public JsonNode parameters;

    @JsonManagedReference
    public JSONFeatureOfInterest FeatureOfInterest;
    @JsonManagedReference
    public JSONDatastream Datastream;

    public JSONObservation() {
        self = new StaDataEntity();
    }

    public StaDataEntity toEntity(boolean validate) {
        if (!generatedId && result == null && validate) {
            Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(result, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultQuality, INVALID_REFERENCED_ENTITY);
            Assert.isNull(parameters, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            if (validate) {
                Assert.notNull(result, INVALID_INLINE_ENTITY + "result");
            }

            // phenomenonTime
            if (phenomenonTime != null) {
                Time time = parseTime(phenomenonTime);
                if (time instanceof TimeInstant) {
                    self.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                    self.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    self.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                    self.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            } else if (validate) {
                // Use time of POST Request as fallback
                Date date = DateTime.now().toDate();
                self.setSamplingTimeStart(date);
                self.setSamplingTimeEnd(date);
            }

            if (resultTime != null) {
                // resultTime
                self.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());
            } else {
                self.setResultTime(new Date());
            }

            // validTime
            if (validTime != null) {
                Time time = parseTime(validTime);
                if (time instanceof TimeInstant) {
                    self.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                    self.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    self.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                    self.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            }

            // parameters
//            if (parameters != null) {
//                //TODO: handle parameters
//                //observation.setParameters();
//                //throw new NotImplementedException();
//            }
            // result
            self.setValue(result);

            // Link to Datastream
            if (Datastream != null) {
                self.setDatastream(Datastream.toEntity());
            } else if (backReference instanceof JSONDatastream) {
                self.setDatastream(((JSONDatastream) backReference).getEntity());
            } else {
                Assert.notNull(null, INVALID_INLINE_ENTITY + "Datastream");
            }

            // Link to FOI
            if (FeatureOfInterest != null) {
                self.setFeatureOfInterest(FeatureOfInterest.toEntity());
            } else if (backReference instanceof JSONFeatureOfInterest) {
                self.setFeatureOfInterest(((JSONFeatureOfInterest) backReference).getEntity());
            } else {
                Assert.notNull(null, INVALID_INLINE_ENTITY + "FeatureOfInterest");
            }

            return self;
        }
    }
}
