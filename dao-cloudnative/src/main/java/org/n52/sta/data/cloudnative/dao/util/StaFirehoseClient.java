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
package org.n52.sta.data.cloudnative.dao.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.PutRecordRequest;
import software.amazon.awssdk.services.firehose.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class StaFirehoseClient implements FirehoseConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaFirehoseClient.class);

    private final FirehoseClient firehoseClient = FirehoseClient.builder()
            .region(FirehoseConstants.REGION)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private final AtomicLong TS = new AtomicLong();
    private Long getUniqueTimestamp() {
        long micros = System.currentTimeMillis() * 1000;
        for ( ; ; ) {
            long value = TS.get();
            if (micros <= value)
                micros = value + 1;
            if (TS.compareAndSet(value, micros))
                return micros;
        }
    }

    public void icebergMerge(ObjectNode dataNode, String tableName, String operation)
            throws STACRUDException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        rootNode.put(FirehoseConstants.TABLE, tableName);
        rootNode.put(FirehoseConstants.OPERATION, operation);

        rootNode.set(FirehoseConstants.DATA, dataNode);

        try {
            streamToFirehose(mapper.writeValueAsString(rootNode));
        } catch (JsonProcessingException e) {
            throw new STACRUDException("Bad request: cannot parse payload for table " + operation);
        }
    }

    public void icebergMergeParameters(ObjectNode dataNode,
                                 String tableName,
                                 String operation,
                                 String foreignKey,
                                 String foreignKeyVal)
            throws STACRUDException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        rootNode.put(FirehoseConstants.TABLE, tableName);
        rootNode.put(FirehoseConstants.OPERATION, operation);
        ArrayNode dataArray = rootNode.putArray(FirehoseConstants.DATA);

        Iterator<String> it = dataNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            JsonNode value = dataNode.get(key);
            ObjectNode transformedNode = mapper.createObjectNode();
            transformedNode.put("parameter_id", getUniqueTimestamp());
            transformedNode.put("name", key);
            switch (value.getNodeType()) {
                case ARRAY:
                    // fallthru
                case MISSING:
                    // fallthru
                case NULL:
                    // fallthru
                case OBJECT:
                    // fallthru
                case POJO:
                    transformedNode.put("value_text", value.asText());
                    break;
                case BINARY:
                    // fallthru
                case BOOLEAN:
                    transformedNode.put("value_boolean", value.asBoolean());
                    break;
                case NUMBER:
                    transformedNode.put("value_quantity", value.asDouble());
                    break;
                case STRING:
                    transformedNode.put("value_text", value.asText());
                    break;
                default:
                    throw new RuntimeException("Could not identify value type of parameters!");
            }
            transformedNode.put(foreignKey, foreignKeyVal);
            dataArray.add(transformedNode);
        }

        try {
            streamToFirehose(mapper.writeValueAsString(rootNode));
        } catch (JsonProcessingException e) {
            throw new STACRUDException("Bad request: cannot parse payload for table " + operation);
        }
    }

    public void icebergDeleteById(String key, long Id, String tableName) throws STACRUDException {
        ObjectNode rootNode = mapper.createObjectNode();

        rootNode.put(FirehoseConstants.TABLE, tableName);
        rootNode.put(FirehoseConstants.OPERATION, FirehoseConstants.DELETE);

        ObjectNode dataNode = rootNode.putObject(FirehoseConstants.DATA);
        dataNode.put(FirehoseConstants.DELETE_KEY, key);
        dataNode.put(FirehoseConstants.DELETE_VALUE, Id);

        try {
            streamToFirehose(mapper.writeValueAsString(rootNode));
        } catch (JsonProcessingException e) {
            throw new STACRUDException("Bad request: cannot parse payload for table " + FirehoseConstants.DELETE);
        }
    }

    public void icebergDeleteByStaIdentifier(String id, String tableName) throws STACRUDException {
        ObjectNode rootNode = mapper.createObjectNode();

        rootNode.put(FirehoseConstants.TABLE, tableName);
        rootNode.put(FirehoseConstants.OPERATION, FirehoseConstants.DELETE);

        ObjectNode dataNode = rootNode.putObject(FirehoseConstants.DATA);
        dataNode.put(FirehoseConstants.DELETE_KEY, "sta_identifier");
        dataNode.put(FirehoseConstants.DELETE_VALUE, id);

        try {
            streamToFirehose(mapper.writeValueAsString(rootNode));
        } catch (JsonProcessingException e) {
            throw new STACRUDException("Bad request: cannot parse payload for table " + FirehoseConstants.DELETE);
        }
    }

    private void streamToFirehose(String jsonPayload) {
        try {
            Record record = Record.builder()
                    .data(SdkBytes.fromString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            PutRecordRequest putRecordRequest = PutRecordRequest.builder()
                    .deliveryStreamName(FirehoseConstants.DELIVERY_STREAM_NAME)
                    .record(record)
                    .build();

            firehoseClient.putRecord(putRecordRequest);
            LOGGER.debug("Record sent successfully to Firehose.");
        } catch (Exception e) {
            LOGGER.debug("Error sending to Firehose: " + e.getMessage());
        }
    }
}
