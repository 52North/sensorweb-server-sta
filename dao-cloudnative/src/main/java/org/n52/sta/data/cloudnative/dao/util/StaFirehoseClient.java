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
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.PutRecordRequest;
import software.amazon.awssdk.services.firehose.model.PutRecordResponse;
import software.amazon.awssdk.services.firehose.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class StaFirehoseClient implements FirehoseConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaFirehoseClient.class);
    private final FirehoseClient firehoseClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicLong TS = new AtomicLong();

    @Autowired
    public StaFirehoseClient(FirehoseClient firehoseClient) {
        this.firehoseClient = firehoseClient;
    }

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
    /**
     * Upserts STA Entities into Iceberg tables by streaming it into Firehose
     *
     * @param dataNode STA Entity
     * @param tableName    Feature to be used for the new Dataset
     * @param operation  INSERT / UPDATE
     */
    public void icebergMerge(ObjectNode dataNode, String tableName, String operation)
            throws STACRUDException {

        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode adf_metadata = rootNode.putObject(FirehoseConstants.ADF_METADATA);
        ObjectNode otf_metadata = adf_metadata.putObject(FirehoseConstants.OTF_METADATA);
        otf_metadata.put(FirehoseConstants.TABLE, tableName.toLowerCase());
        otf_metadata.put(FirehoseConstants.DATABASE, FirehoseConstants.DATABASE_NAME);
        otf_metadata.put(FirehoseConstants.OPERATION, operation);


        ObjectNode adf_record = rootNode.putObject(FirehoseConstants.ADF_RECORD);
        adf_record.setAll(dataNode);

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
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode adf_metadata = rootNode.putObject(FirehoseConstants.ADF_METADATA);
        ObjectNode otf_metadata = adf_metadata.putObject(FirehoseConstants.OTF_METADATA);

        otf_metadata.put(FirehoseConstants.TABLE, tableName.toLowerCase());
        otf_metadata.put(FirehoseConstants.DATABASE, FirehoseConstants.DATABASE_NAME);
        otf_metadata.put(FirehoseConstants.OPERATION, operation);

        ObjectNode adf_record = rootNode.putObject(FirehoseConstants.ADF_RECORD);

        Iterator<String> it = dataNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            JsonNode value = dataNode.get(key);

            adf_record.put("parameter_id", getUniqueTimestamp());
            adf_record.put("name", key);

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
                    adf_record.put("value_text", value.asText());
                    break;
                case BINARY:
                    // fallthru
                case BOOLEAN:
                    adf_record.put("value_boolean", value.asBoolean());
                    break;
                case NUMBER:
                    adf_record.put("value_quantity", value.asDouble());
                    break;
                case STRING:
                    adf_record.put("value_text", value.asText());
                    break;
                default:
                    throw new RuntimeException("Could not identify value type of parameters!");
            }
            adf_record.put(foreignKey.toLowerCase(), foreignKeyVal);
            try {
                streamToFirehose(mapper.writeValueAsString(rootNode));
            } catch (JsonProcessingException e) {
                throw new STACRUDException("Bad request: cannot parse payload for table " + operation);
            }

        }
    }

    public void icebergDeleteById(String key, long Id, String tableName) throws STACRUDException {
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode adf_metadata = rootNode.putObject(FirehoseConstants.ADF_METADATA);
        ObjectNode otf_metadata = adf_metadata.putObject(FirehoseConstants.OTF_METADATA);
        otf_metadata.put(FirehoseConstants.TABLE, tableName.toLowerCase());
        otf_metadata.put(FirehoseConstants.DATABASE, FirehoseConstants.DATABASE_NAME);
        otf_metadata.put(FirehoseConstants.OPERATION, FirehoseConstants.DELETE);


        ObjectNode adf_record = rootNode.putObject(FirehoseConstants.ADF_RECORD);
        adf_record.put(key.toLowerCase(), Id);

        try {
            streamToFirehose(mapper.writeValueAsString(rootNode));
        } catch (JsonProcessingException e) {
            throw new STACRUDException("Bad request: cannot parse payload for table " + FirehoseConstants.DELETE);
        }
    }



    /*public void icebergDeleteByStaIdentifier(String id, String tableName) throws STACRUDException {
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode adf_metadata = rootNode.putObject(FirehoseConstants.ADF_METADATA);
        ObjectNode otf_metadata = adf_metadata.putObject(FirehoseConstants.OTF_METADATA);
        otf_metadata.put(FirehoseConstants.TABLE, tableName);
        otf_metadata.put(FirehoseConstants.DATABASE, FirehoseConstants.DATABASE_NAME);
        otf_metadata.put(FirehoseConstants.OPERATION, FirehoseConstants.DELETE);


        ObjectNode adf_record = rootNode.putObject(FirehoseConstants.ADF_RECORD);
        adf_record.put("sta_identifier", id);


        try {
            streamToFirehose(mapper.writeValueAsString(rootNode));
        } catch (JsonProcessingException e) {
            throw new STACRUDException("Bad request: cannot parse payload for table " + FirehoseConstants.DELETE);
        }
    }*/

    private void streamToFirehose(String jsonPayload) {
        try {
            Record record = Record.builder()
                    .data(SdkBytes.fromUtf8String(jsonPayload))
                    .build();

            PutRecordRequest putRecordRequest = PutRecordRequest.builder()
                    .deliveryStreamName(FirehoseConstants.DELIVERY_STREAM_NAME)
                    .record(record)
                    .build();

            PutRecordResponse resp = firehoseClient.putRecord(putRecordRequest);
            LOGGER.debug("Record sent successfully to Firehose.");
        } catch (Exception e) {
            LOGGER.debug("Error sending to Firehose: " + e.getMessage());
        }
    }
}
