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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.serializer;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.QueryOptionsHandler;
import org.n52.sta.utils.EntityAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Payload serializer for Entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class PayloadSerializer {

    @Autowired
    private QueryOptionsHandler queryOptionsHandler;

    @Autowired
    EntityAnnotator annotator;

    private ServiceMetadata edm;

    public ByteBuf encodeEntity(ServiceMetadata serviceMetadata, Entity entity, EdmEntityType entityType, EdmEntitySet entitySet, QueryOptions queryOptions, Set<String> watchedProperties) throws SerializerException, IOException {
        //TODO: Actually serialize Object to JSON
        InputStream payload = createResponseContent(edm, entity, entityType, entitySet, queryOptions);

//        if (watchedProperties != null) {
//            // Only return updated property
//        return UnpooledcopiedBuffer(entity.toString().getBytes());
//        } else {
//            // Return normally serialized object with this.fields selectItems
//            return Unpooled.copiedBuffer(entity.toString().getBytes());
//        }
        return Unpooled.copiedBuffer(ByteStreams.toByteArray(payload));
    }

    private InputStream createResponseContent(ServiceMetadata serviceMetadata, Entity entity, EdmEntityType entityType, EdmEntitySet entitySet, QueryOptions queryOptions) throws SerializerException {
        SensorThingsSerializer serializer = new SensorThingsSerializer(ContentType.JSON_NO_METADATA);
        entity = annotator.annotateEntity(entity, entityType, queryOptions.getBaseURI(), queryOptions.getSelectOption());

        ContextURL.Builder contextUrlBuilder = ContextURL.with()
                .entitySet(entitySet)
                .suffix(ContextURL.Suffix.ENTITY);
        contextUrlBuilder.selectList(queryOptionsHandler.getSelectListFromSelectOption(
                entityType, queryOptions.getExpandOption(), queryOptions.getSelectOption()));
        ContextURL contextUrl = contextUrlBuilder.build();

        EntitySerializerOptions opts = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .select(queryOptions.getSelectOption())
                .expand(queryOptions.getExpandOption())
                .build();

        SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }

}
