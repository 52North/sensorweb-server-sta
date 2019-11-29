///*
// * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
// * Software GmbH
// *
// * This program is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 2 as published
// * by the Free Software Foundation.
// *
// * If the program is linked with libraries which are licensed under one of
// * the following licenses, the combination of the program with the linked
// * library is not considered a "derivative work" of the program:
// *
// *     - Apache License, version 2.0
// *     - Apache Software License, version 1.0
// *     - GNU Lesser General Public License, version 3
// *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
// *     - Common Development and Distribution License (CDDL), version 1.0
// *
// * Therefore the distribution of the program linked with libraries licensed
// * under the aforementioned licenses, is permitted by the copyright holders
// * if the distribution is compliant with both the GNU General Public
// * License version 2 and the aforementioned licenses.
// *
// * This program is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// * Public License for more details.
// */
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.n52.sta.mqtt.handler;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import org.apache.olingo.commons.api.data.ContextURL;
//import org.apache.olingo.commons.api.data.Entity;
//import org.apache.olingo.commons.api.edm.EdmEntitySet;
//import org.apache.olingo.commons.api.edm.EdmEntityType;
//import org.apache.olingo.commons.api.format.ContentType;
//import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
//import org.apache.olingo.server.api.serializer.SerializerException;
//import org.apache.olingo.server.api.serializer.SerializerResult;
//import org.apache.olingo.server.api.uri.queryoption.SelectOption;
//import org.n52.sta.service.serializer.SensorThingsSerializer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * Payload serializer for Entities
// *
// * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
// */
//@Component
//public class PayloadSerializer {
//
//    private final EntityAnnotator annotator;
//    private final String rootUrl;
//
//    @Autowired
//    public PayloadSerializer(EntityAnnotator annotator,
//                             @Value("${server.rootUrl}") String rootUrl) {
//        this.annotator = annotator;
//        this.rootUrl = rootUrl;
//    }
//
//    public ByteBuf encodeEntity(Entity entity,
//                                EdmEntityType entityType,
//                                EdmEntitySet entitySet,
//                                SelectOption selectOption)
//            throws SerializerException, IOException {
//        InputStream payload = createResponseContent(entity, entityType, entitySet, rootUrl, selectOption);
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        int index;
//        byte[] data = new byte[1024];
//        while ((index = payload.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, index);
//        }
//        buffer.flush();
//        return Unpooled.copiedBuffer(buffer.toByteArray());
//    }
//
//    private InputStream createResponseContent(Entity original,
//                                              EdmEntityType entityType,
//                                              EdmEntitySet entitySet,
//                                              String baseUrl,
//                                              SelectOption selectOption) throws SerializerException {
//        SensorThingsSerializer serializer = new SensorThingsSerializer(ContentType.JSON_NO_METADATA);
//        Entity entity = annotator.annotateEntity(original, entityType, baseUrl, selectOption);
//
//        ContextURL.Builder contextUrlBuilder = ContextURL.with()
//                .entitySet(entitySet)
//                .suffix(ContextURL.Suffix.ENTITY);
////        contextUrlBuilder.selectList(queryOptionsHandler.getSelectListFromSelectOption(
////                entityType, new ExpandOptionImpl(), selectOption));
//        ContextURL contextUrl = contextUrlBuilder.build();
//
//        EntitySerializerOptions opts = EntitySerializerOptions.with()
//                .contextURL(contextUrl)
//                .select(selectOption)
//                .build();
//
//        SerializerResult serializerResult = serializer.entity(null, entityType, entity, opts);
//        InputStream serializedContent = serializerResult.getContent();
//
//        return serializedContent;
//    }
//
//}
