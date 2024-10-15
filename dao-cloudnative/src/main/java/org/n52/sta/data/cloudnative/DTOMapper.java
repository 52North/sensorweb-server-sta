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
package org.n52.sta.data.cloudnative;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.*;
import org.jooq.Record;

import org.jooq.impl.DSL;
import org.locationtech.jts.io.ParseException;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.n52.sta.api.dto.*;
import org.n52.sta.api.dto.impl.*;

import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.schema.tables.records.*;
import org.n52.sta.utils.TimeUtil;

import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * jOOQ Record Mapper for mapping Result Sets to DTO Entities
 *
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class DTOMapper implements RecordMapperProvider {

    private static final WKBReader WKBReader = new WKBReader();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static <T extends HasNameAndDescription> void setStaDescription (T entity, String description){
        entity.setDescription(description);
    }

    private static <T extends HasNameAndDescription> void setStaName (T entity, String name) {
        entity.setName(name);
    }

    private static <T extends StaDTO> void setStaIdentifier (T entity, String Identifier){
        entity.setId(Identifier);
    }

    @Override
    public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
        if (type == DatastreamDTO.class) {
            return (RecordMapper<R, E>) new DatastreamRecordMapper();
        } else if (type == LocationDTO.class) {
            return (RecordMapper<R, E>) new LocationRecordMapper();
        } else if (type == ThingDTO.class) {
            return (RecordMapper<R, E>) new ThingRecordMapper();
        } else if (type == HistoricalLocationDTO.class) {
            return (RecordMapper<R, E>) new HistoricalLocationRecordMapper();
        } else if (type == SensorDTO.class) {
            return (RecordMapper<R, E>) new SensorRecordMapper();
        } else if (type == ObservedPropertyDTO.class) {
            return (RecordMapper<R, E>) new ObservedPropertyRecordMapper();
        } else if(type == ObservationDTO.class) {
            return (RecordMapper<R, E>) new ObservationRecordMapper();
        } else if (type == FeatureOfInterestDTO.class) {
            return (RecordMapper<R, E>) new FeatureOfInterestRecordMapper();
        } else {
            throw new IllegalArgumentException("Unsupported record type: " + type);
        }
    }

    public static Map<Field<?>, Object> mapAliasedFields(Record fetchedRecord, Table<?> table) {
        Map<Field<?>, Object> fieldAliasValueMap = new HashMap<>();
        for (Field<?> field : table.fields()) {
            Field<?> aliasedField = field.as(table.getName() + "_" + field.getName());
            Object value = null;
            if (fetchedRecord.field(aliasedField) != null) {
                value = fetchedRecord.get(aliasedField, field.getType());
            }
            fieldAliasValueMap.put(field, value);

        }
        return fieldAliasValueMap;
    }

    public static void mapEntityRecord(Map<Field<?>, Object> fieldAliasValueMap, Record tableRecord) {
        for (Map.Entry<Field<?>, Object> entry : fieldAliasValueMap.entrySet()) {
            Field<?> field = entry.getKey();
            Object value = entry.getValue();

            try {
                tableRecord.set((Field<Object>)field, value);
            } catch (Exception e) {
                System.err.println("Error setting value for field " + field.getName() + ": " + e.getMessage());
            }
        }
    }

    public static class DatastreamRecordMapper implements RecordMapper<Record, Datastream> {
        @Override
        public Datastream map(Record record) {
            Datastream datastream = new Datastream();
            DatasetRecord datasetRecord = new DatasetRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.DATASTREAM), datasetRecord);

            // aggregate datasets do not have staIdentifier
            setStaIdentifier(datastream, datasetRecord.getDatasetId() == null ? null : datasetRecord.getDatasetId().toString());
            setStaName(datastream, datasetRecord.getName());
            setStaDescription(datastream, datasetRecord.getDescription());
            setPhenomenonTime(datastream, datasetRecord);
            setResultTime(datastream, datasetRecord);
            setObservationType(datastream, record);
            setUnitOfMeasurement(datastream, record);
            setObservedArea(datastream, datasetRecord);

            return datastream;
        }
        public static class DatastreamParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record record) {
                DatasetParameterRecord tableRecord = new DatasetParameterRecord();
                mapEntityRecord(mapAliasedFields(record, StaEntity.DATASTREAM_PROPERTIES), tableRecord);

                ObjectNode properties = MAPPER.createObjectNode();

                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setPhenomenonTime(Datastream datastream, DatasetRecord record) {
            if (record.getFirstTime() != null && record.getLastTime() != null) {
                datastream.setPhenomenonTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.from(
                                record.getFirstTime()
                                        .atZone(ZoneId.of("UTC"))
                                        .toInstant())),
                        TimeUtil.createDateTime(Timestamp.from(
                                record.getLastTime()
                                        .atZone(ZoneId.of("UTC"))
                                        .toInstant())))
                );
            } else {
                datastream.setPhenomenonTime(null);
            }
        }

        private void setResultTime(Datastream datastream, DatasetRecord record) {

            if (record.getResultTimeStart() != null && record.getResultTimeEnd() != null) {
                datastream.setResultTime(
                        TimeUtil.createTime(
                            TimeUtil.createDateTime(Timestamp.from(
                                    record.getResultTimeStart()
                                    .atZone(ZoneId.of("UTC"))
                                    .toInstant())),
                            TimeUtil.createDateTime(Timestamp.from(
                                    record.getResultTimeEnd()
                                            .atZone(ZoneId.of("UTC"))
                                            .toInstant())))
                );
            } else {
                datastream.setResultTime(null);
            }
        }

        private void setObservedArea (Datastream datastream, DatasetRecord record){
            if (record.getObservedArea() != null) {
                try {
                    datastream.setObservedArea(WKBReader.read(record.getObservedArea()));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }

            } else {
                datastream.setObservedProperty(null);
            }
        }

        private void setObservationType (Datastream datastream, Record rec) {
            // Dataset POJO has an observationType which describes the profile of the Datastream
            // Datastream/observationType (O&M) is actually stored in related Format Record
            // Format Record is always fetched (when creating join list in dao)
            Field<String> definition = DSL.field("DATASTREAM_FORMAT_DEFINITION", String.class);
            if (rec.field(definition) != null) {
                datastream.setObservationType(rec.get(definition));
            }
        }

        private void setUnitOfMeasurement(Datastream datastream, Record record) {
            datastream.setUnitOfMeasurement(record.map(new DTOMapper.DatastreamRecordMapper.UnitRecordMapper()));
        }

        public static class UnitRecordMapper implements RecordMapper<Record, DatastreamDTO.UnitOfMeasurement> {
            @Override
            public DatastreamDTO.UnitOfMeasurement map(Record record) {
                DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
                UnitRecord tableRecord = new UnitRecord();

                mapEntityRecord(mapAliasedFields(record, StaEntity.UNIT), tableRecord);

                uom.setName(tableRecord.getName());
                uom.setSymbol(tableRecord.getSymbol());
                uom.setDefinition(tableRecord.getLink());

                return uom;
            }
        }

    }

    public static class LocationRecordMapper implements RecordMapper<Record, Location> {
        @Override
        public Location map(Record record) {
            Location location = new Location();
            LocationRecord tableRecord = new LocationRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.LOCATION), tableRecord);

            setStaIdentifier(location, tableRecord.getStaIdentifier());
            setStaName(location, tableRecord.getName());
            setStaDescription(location, tableRecord.getDescription());
            setGeometry(location, tableRecord);
            // encodingType is static for Location

            return location;
        }

        public static class LocationParameterRecordMapper implements RecordMapper<Record, ObjectNode> {
            private ObjectNode setProperties(Record record) {
                LocationParameterRecord tableRecord = new LocationParameterRecord();
                mapEntityRecord(mapAliasedFields(record, StaEntity.LOCATION_PROPERTIES), tableRecord);
                ObjectNode properties = MAPPER.createObjectNode();

                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }

            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }


        private void setGeometry(Location location, LocationRecord record) {
            if (record.getGeom() != null) {
                try {
                    location.setGeometry(WKBReader.read(record.getGeom()));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            } else {
                location.setGeometry(null);
            }
        }
    }

    public static class ThingRecordMapper implements RecordMapper<Record, Thing> {
        @Override
        public Thing map(Record record) {
            Thing thing = new Thing();
            PlatformRecord tableRecord = new PlatformRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.THING), tableRecord);

            setStaIdentifier(thing, tableRecord.getStaIdentifier());
            setStaName(thing, tableRecord.getName());
            setStaDescription(thing, tableRecord.getDescription());
            return thing;
        }
        public static class ThingParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record record) {
                PlatformParameterRecord tableRecord = new PlatformParameterRecord();

                mapEntityRecord(mapAliasedFields(record, StaEntity.THING_PROPERTIES), tableRecord);

                ObjectNode properties = MAPPER.createObjectNode();
                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }
    }

    public static class HistoricalLocationRecordMapper implements RecordMapper<Record, HistoricalLocation> {
        @Override
        public HistoricalLocation map(Record record) {
            HistoricalLocation historicalLocation = new HistoricalLocation();
            HistoricalLocationRecord tableRecord = new HistoricalLocationRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.HISTORICAL_LOCATION), tableRecord);

            setStaIdentifier(historicalLocation, tableRecord.getStaIdentifier());
            setTime(historicalLocation, tableRecord);

            return historicalLocation;
        }

        private void setTime(HistoricalLocation historicalLocation, HistoricalLocationRecord record) {
            if(record.getTime() != null) {
                historicalLocation.setTime(TimeUtil.createTime(TimeUtil.createDateTime(
                        Timestamp.from(record.getTime().atZone(ZoneId.of("UTC")).toInstant())
                )));
            }
            else {
                historicalLocation.setTime(null);
            }
        }

    }

    public static class SensorRecordMapper implements RecordMapper<Record, Sensor> {

        @Override
        public Sensor map(Record record) {
            Sensor sensor = new Sensor();
            ProcedureRecord tableRecord = new ProcedureRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.SENSOR), tableRecord);

            setStaIdentifier(sensor, tableRecord.getStaIdentifier());
            setStaName(sensor, tableRecord.getName());
            setStaDescription(sensor, tableRecord.getDescription());
            setEncodingType(sensor, record);
            setMetadata(sensor, tableRecord);

            return sensor;
        }

        public static class SensorParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record record) {
                ProcedureParameterRecord tableRecord = new ProcedureParameterRecord();
                ObjectNode properties = MAPPER.createObjectNode();

                mapEntityRecord(mapAliasedFields(record, StaEntity.SENSOR_PROPERTIES), tableRecord);

                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setMetadata(Sensor sensor, ProcedureRecord record) {
            sensor.setMetadata(record.getDescriptionFile());
        }

        private void setEncodingType(Sensor sensor, Record rec) {
            Field<String> definition = DSL.field("SENSOR_FORMAT_DEFINITION", String.class);
            if (rec.field(definition) != null) {
                sensor.setEncodingType(rec.get(definition));
            }
        }
    }

    public static class ObservedPropertyRecordMapper implements RecordMapper<Record, ObservedProperty> {
        @Override
        public ObservedProperty map(Record record) {
            ObservedProperty observedProperty = new ObservedProperty();
            PhenomenonRecord tableRecord = new PhenomenonRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.OBSERVED_PROPERTY), tableRecord);

            setStaIdentifier(observedProperty, tableRecord.getStaIdentifier());
            setStaName(observedProperty, tableRecord.getName());
            setStaDescription(observedProperty, tableRecord.getDescription());
            setDefinition(observedProperty, tableRecord);

            return observedProperty;
        }
        public static class ObservedPropertyParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record record) {
                PhenomenonParameterRecord tableRecord = new PhenomenonParameterRecord();
                ObjectNode properties = MAPPER.createObjectNode();

                mapEntityRecord(mapAliasedFields(record, StaEntity.OBSERVED_PROPERTY_PROPERTIES), tableRecord);

                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setDefinition(ObservedProperty observedProperty, PhenomenonRecord record) {
            observedProperty.setDefinition(record.getIdentifier());
        }
    }

    public static class ObservationRecordMapper implements RecordMapper<Record, Observation> {
        @Override
        public Observation map(Record record) {
            Observation observation = new Observation();
            ObservationRecord tableRecord = new ObservationRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.OBSERVATION), tableRecord);

            setStaIdentifier(observation, tableRecord.getStaIdentifier());
            setPhenomenonTime(observation, tableRecord);
            setResultTime(observation, tableRecord);
            setResult(observation, tableRecord);
            setValidTime(observation, tableRecord);
            setRelatedDatastreamId(observation, tableRecord);
            return observation;
        }

        private void setRelatedDatastreamId(Observation observation, ObservationRecord record) {
            if (record.getFkDatasetId() != null) {
                DatastreamDTO datastream = new Datastream();
                datastream.setId(record.getFkDatasetId().toString());
                observation.setDatastream(datastream);
            }
        }

        private void setPhenomenonTime(Observation observation, ObservationRecord record) {
            if (record.getSamplingTimeStart() != null && record.getSamplingTimeEnd() != null) {
                observation.setPhenomenonTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.from(
                                record.getSamplingTimeStart()
                                        .atZone(ZoneId.of("UTC"))
                                        .toInstant())),
                        TimeUtil.createDateTime(Timestamp.from(
                                record.getSamplingTimeEnd()
                                        .atZone(ZoneId.of("UTC"))
                                        .toInstant())))
                );
            } else {
                observation.setPhenomenonTime(null);
            }
        }
        public static class ObservationParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setParameters(Record record) {
                ObservationParameterRecord tableRecord = new ObservationParameterRecord();
                ObjectNode properties = MAPPER.createObjectNode();

                mapEntityRecord(mapAliasedFields(record, StaEntity.OBSERVATION_PARAMETERS), tableRecord);

                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }
            }
            @Override
            public ObjectNode map(Record record) {
                return setParameters(record);
            }
        }

        private void setResult(Observation observation, ObservationRecord record) {
            if (record.getValueCount() != null) {
                observation.setResult(record.getValueCount());
            } else if (record.getValueText() != null) {
                observation.setResult(record.getValueText());
            } else if (record.getValueQuantity() != null) {
                observation.setResult(record.getValueQuantity());
            } else if (record.getValueBoolean() != null) {
                observation.setResult(record.getValueBoolean());
            } else if (record.getValueCategory() != null) {
                observation.setResult(record.getValueCategory());
            } else {
                observation.setResult(null);
            }

        }

        private void setValidTime(Observation observation, ObservationRecord record) {
            if (record.getValidTimeStart() != null && record.getValidTimeEnd() != null) {
                observation.setValidTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.from(record.getValidTimeStart()
                                .atZone(ZoneId.of("UTC"))
                                .toInstant())),
                        TimeUtil.createDateTime(Timestamp.from(
                                record.getValidTimeEnd()
                                        .atZone(ZoneId.of("UTC"))
                                        .toInstant())))
                );
            } else {
                observation.setValidTime(null);
            }
        }

        private void setResultTime(Observation observation, ObservationRecord record) {
            if (record.getResultTime() != null) {
               observation.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                       Timestamp.from(record.getResultTime().atZone(ZoneId.of("UTC")).toInstant())
               )));
            } else {
                observation.setResultTime(null);
            }
        }

    }

    public static class FeatureOfInterestRecordMapper implements RecordMapper<Record, FeatureOfInterest> {
        @Override
        public FeatureOfInterest map(Record record) {
            FeatureOfInterest featureOfInterest = new FeatureOfInterest();
            FeatureRecord tableRecord = new FeatureRecord();

            mapEntityRecord(mapAliasedFields(record, StaEntity.FEATURE_OF_INTEREST), tableRecord);

            setStaIdentifier(featureOfInterest, tableRecord.getStaIdentifier());
            setStaName(featureOfInterest, tableRecord.getName());
            setStaDescription(featureOfInterest, tableRecord.getDescription());
            setFeature(featureOfInterest, tableRecord);
            setEncodingType(featureOfInterest, record);

            return featureOfInterest;
        }
        public static class FeatureParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record record) {
                FeatureParameterRecord tableRecord = new FeatureParameterRecord();
                ObjectNode properties = MAPPER.createObjectNode();

                mapEntityRecord(mapAliasedFields(record, StaEntity.FEATURE_PROPERTIES), tableRecord);

                if (tableRecord.getName() != null) {
                    String key = tableRecord.getName();

                    if (tableRecord.getValueText() != null) {
                        properties.put(key, tableRecord.getValueText());
                    }
                    if (tableRecord.getValueQuantity() != null) {
                        properties.put(key, tableRecord.getValueQuantity());
                    }
                    if (tableRecord.getValueBoolean() != null) {
                        properties.put(key, tableRecord.getValueBoolean());
                    }
                    return properties;
                } else {
                    return null;
                }
            }

            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setEncodingType(FeatureOfInterest featureOfInterest, Record record) {
            Field<String> definition = DSL.field("FEATURE_FORMAT_DEFINITION", String.class);
            if (record.field(definition) != null) {
                featureOfInterest.setEncodingType(record.get(definition));
            }
        }

        private void setFeature(FeatureOfInterest featureOfInterest, FeatureRecord record) {
            if (record.getGeom() != null) {
                try {
                    featureOfInterest.setFeature(WKBReader.read(record.getGeom()));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            } else {
                featureOfInterest.setFeature(null);
            }
        }
    }
}