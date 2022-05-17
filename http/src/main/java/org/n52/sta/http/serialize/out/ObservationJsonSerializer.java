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
package org.n52.sta.http.serialize.out;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Observation;

public class ObservationJsonSerializer extends StaBaseSerializer<Observation> {

    public ObservationJsonSerializer(SerializationContext context) {
        super(context, StaConstants.OBSERVATIONS, Observation.class);
    }

    @Override
    public void serialize(Observation value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        String id = value.getId();

        // entity properties
        writeProperty("id", name -> gen.writeStringField(StaConstants.AT_IOT_ID, id));
        writeStringProperty(StaConstants.AT_IOT_SELFLINK, () -> createSelfLink(id), gen);

        writeObjectProperty(StaConstants.PROP_RESULT_QUALITY, value::getResultQuality, gen);
        writeObjectProperty(StaConstants.PROP_PARAMETERS, value::getParameters, gen);
        writeObjectProperty(StaConstants.PROP_RESULT, value::getResult, gen);
        writeTimeProperty(StaConstants.PROP_RESULT_TIME, value::getResultTime, gen);
        writeTimeProperty(StaConstants.PROP_VALID_TIME, value::getValidTime, gen);
        writeTimeProperty(StaConstants.PROP_PHENOMENON_TIME, value::getPhenomenonTime, gen);

        // entity members
        writeMember(StaConstants.DATASTREAM, id, gen, DatastreamJsonSerializer::new,
                serializer -> serializer.serialize(value.getDatastream(), gen, serializers));

        writeMember(StaConstants.FEATURES_OF_INTEREST, id, gen, FeatureOfInterestJsonSerializer::new,
                serializer -> serializer.serialize(value.getFeatureOfInterest(), gen, serializers));

    }

}
