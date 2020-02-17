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
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.sta.exception.ParsingException;
import org.n52.sta.utils.TimeUtil;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONHistoricalLocation extends JSONBase.JSONwithIdTime<HistoricalLocationEntity>
        implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String time;
    public JSONThing Thing;

    @JsonManagedReference
    public JSONLocation[] Locations;

    private Date date;

    public JSONHistoricalLocation() {
        self = new HistoricalLocationEntity();
    }

    @Override
    public HistoricalLocationEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
            case FULL:
                Assert.notNull(date, INVALID_INLINE_ENTITY + "time");

                self.setIdentifier(identifier);
                self.setTime(date);

                if (Thing != null) {
                    self.setThing(Thing.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
                } else if (backReference instanceof JSONThing) {
                    self.setThing(((JSONThing) backReference).getEntity());
                } else {
                    Assert.notNull(null, INVALID_INLINE_ENTITY + "Thing");
                }

                if (Locations != null) {
                    self.setLocations(Arrays.stream(Locations)
                            .map(loc -> loc.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE))
                            .collect(Collectors.toSet()));
                } else if (backReference instanceof JSONLocation) {
                    self.setLocations(Collections.singleton(((JSONLocation) backReference).getEntity()));
                } else {
                    Assert.notNull(null, INVALID_INLINE_ENTITY + "Location");
                }

                return self;
            case PATCH:
                self.setIdentifier(identifier);
                self.setTime(date);

                if (Thing != null) {
                    self.setThing(Thing.toEntity(JSONBase.EntityType.REFERENCE));
                }

                if (Locations != null) {
                    self.setLocations(Arrays.stream(Locations)
                            .map(loc -> loc.toEntity(JSONBase.EntityType.REFERENCE))
                            .collect(Collectors.toSet()));
                }

                return self;
            case REFERENCE:
                Assert.isNull(time, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Thing, INVALID_REFERENCED_ENTITY);
                Assert.isNull(Locations, INVALID_REFERENCED_ENTITY);

                self.setIdentifier(identifier);
                return self;
            default:
                return null;
        }
    }

    /**
     * Wrapper around rawTime property called by jackson while deserializing.
     *
     * @param rawTime raw Time
     */
    public void setTime(Object rawTime) throws ParsingException {
        Time parsed = TimeUtil.parseTime(rawTime);
        if (parsed instanceof TimeInstant) {
            date = ((TimeInstant) parsed).getValue().toDate();
        } else if (parsed instanceof TimePeriod) {
            date = ((TimePeriod) parsed).getEnd().toDate();
        } else {
            //TODO: refine error message
            throw new ParsingException("Invalid parsed format.");
        }
    }
}
