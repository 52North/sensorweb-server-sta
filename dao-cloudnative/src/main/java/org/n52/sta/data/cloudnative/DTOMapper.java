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
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.Record;

import org.jooq.impl.DSL;
import org.locationtech.jts.io.ParseException;

import org.locationtech.jts.io.WKTReader;
import org.n52.sta.api.dto.*;
import org.n52.sta.api.dto.impl.*;

import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.schema.tables.records.*;
import org.n52.sta.utils.TimeUtil;

import org.springframework.util.Assert;

import java.sql.Timestamp;

/**
 * jOOQ Record Mapper for mapping Result Sets to DTO Entities
 *
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class DTOMapper implements RecordMapperProvider {

    private static final WKTReader WKTReader = new WKTReader();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static <T extends HasNameAndDescription> void setStaDescription (T entity, String description){
        if (description != null) {
            entity.setDescription(description);
        }
    }

    private static <T extends HasNameAndDescription> void setStaName (T entity, String name) {
        if (name != null) {
            entity.setName(name);
        }
    }

    private static <T extends StaDTO> void setStaIdentifier (T entity, String Identifier){
        if (Identifier != null) {
            entity.setId(Identifier);
        }
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

    public static class DatastreamRecordMapper implements RecordMapper<Record, Datastream> {
        @Override
        public Datastream map(Record rec) {
            DatasetRecord record = rec.into(DatasetRecord.class);
            Datastream datastream = new Datastream();

            setStaIdentifier(datastream, record.getStaIdentifier());
            setStaName(datastream, record.getName());
            setStaDescription(datastream, record.getDescription());
            setPhenomenonTime(datastream, record);
            setResultTime(datastream, record);
            setObservationType(datastream, record);
            setUnitOfMeasurement(datastream, rec);
            setObservedArea(datastream, rec);


            return datastream;
        }
        public static class DatastreamParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record rec) {
                DatasetParameterRecord record = rec.into(DatasetParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();

                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key,
                                record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key,
                                record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key,
                                record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key,
                                record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key,
                                record.getValueTemporalTo().toString());
                    }
                }
                return properties;
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setPhenomenonTime(Datastream datastream, DatasetRecord record) {
            if (record.getFirstTime() != null && record.getLastTime() != null) {
                datastream.setPhenomenonTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getFirstTime())),
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getLastTime())))
                );
            }
        }

        private void setResultTime(Datastream datastream, DatasetRecord record) {

            if (record.getResultTimeStart() != null && record.getResultTimeEnd() != null) {
                datastream.setResultTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getResultTimeStart())),
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getResultTimeEnd())))
                );
            }
        }

        private void setObservedArea (Datastream datastream, Record record){

            if (record.field("observed_area") != null) {
                try {
                    datastream.setObservedArea(WKTReader.read(record.get(DSL.field("observed_area", String.class))));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }

            }
        }

        private void setObservationType (Datastream datastream, DatasetRecord record){
            if (record.getObservationType() != null) {
                datastream.setObservationType(record.getObservationType());
            }
        }

        private void setUnitOfMeasurement(Datastream datastream, Record record) {
            datastream.setUnitOfMeasurement(record.map(new DTOMapper.DatastreamRecordMapper.UnitRecordMapper()));
        }

        public static class UnitRecordMapper implements RecordMapper<Record, DatastreamDTO.UnitOfMeasurement> {
            @Override
            public DatastreamDTO.UnitOfMeasurement map(Record record) {
                DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();

                if (record.field(StaEntity.UNIT.NAME) != null) {
                    uom.setName(record.get(StaEntity.UNIT.NAME));
                }
                if (record.field(StaEntity.UNIT.SYMBOL) != null) {
                    uom.setSymbol(record.get(StaEntity.UNIT.SYMBOL));
                }
                if (record.field(StaEntity.UNIT.LINK) != null) {
                    uom.setDefinition(record.get(StaEntity.UNIT.LINK));
                }
                return uom;
            }
        }

    }

    public static class LocationRecordMapper implements RecordMapper<Record, Location> {
        @Override
        public Location map(Record rec) {
            Location location = new Location();
            LocationRecord record = rec.into(LocationRecord.class);

            setStaIdentifier(location, record.getStaIdentifier());
            setStaName(location, record.getName());
            setStaDescription(location, record.getDescription());
            setGeometry(location, rec);

            return location;
        }
        public static class LocationParameterRecordMapper implements RecordMapper<Record, ObjectNode> {
            private ObjectNode setProperties(Record rec) {
                LocationParameterRecord record = rec.into(LocationParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();

                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key,
                                record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key,
                                record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key,
                                record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key,
                                record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key,
                                record.getValueTemporalTo().toString());
                    }
                }

                return properties;
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }


        private void setGeometry(Location location, Record record) {
            if (record.field(DSL.field("GEOM")) != null) {
                try {
                    location.setGeometry(WKTReader.read(record.get(DSL.field("GEOM", String.class))));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            }
        }
    }

    public static class ThingRecordMapper implements RecordMapper<Record, Thing> {
        @Override
        public Thing map(Record rec) {
            Thing thing = new Thing();
            PlatformRecord record = rec.into(PlatformRecord.class);
            setStaIdentifier(thing, record.getStaIdentifier());
            setStaName(thing, record.getName());
            setStaDescription(thing, record.getDescription());
            //setProperties(thing, rec);
            return thing;
        }
        public static class ThingParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record rec) {
                PlatformParameterRecord record = rec.into(PlatformParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();
    //            if (record.getDescription() != null) {
    //                properties.put(StaEntity.THING_PROPERTIES.DESCRIPTION.getName(),
    //                        record.getDescription());
    //            }
    //            if (record.getLastUpdate() != null) {
    //                properties.put(StaEntity.THING_PROPERTIES.LAST_UPDATE.getName(),
    //                        record.getLastUpdate().toString());
    //            }
    //            if (record.getDomain() != null) {
    //                properties.put(StaEntity.THING_PROPERTIES.DOMAIN.getName(),
    //                        record.getDomain());
    //            }
                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key, record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key, record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key, record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key, record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key, record.getValueTemporalTo().toString());
                    }
                }
    //            if (record.getFkUnitId() != null) {
    //                properties.put(StaEntity.THING_PROPERTIES.FK_UNIT_ID.getName(),
    //                        record.getFkUnitId());
    //            }
    //            if (record.getFkPlatformId() != null) {
    //                properties.put(
    //                        StaEntity.THING_PROPERTIES.FK_PLATFORM_ID.getName(),
    //                        record.getFkPlatformId()
    //                );
    //            }
                return properties;
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }
    }

    public static class HistoricalLocationRecordMapper implements RecordMapper<Record, HistoricalLocation> {
        @Override
        public HistoricalLocation map(Record rec) {
            HistoricalLocation historicalLocation = new HistoricalLocation();
            HistoricalLocationRecord record = rec.into(HistoricalLocationRecord.class);

            setStaIdentifier(historicalLocation, record.getStaIdentifier());
            setTime(historicalLocation, record);

            return historicalLocation;
        }

        private void setTime(HistoricalLocation historicalLocation, HistoricalLocationRecord record) {
            if(record.getTime() != null) {
                historicalLocation.setTime(TimeUtil.createTime(new DateTime(Timestamp.valueOf(record.getTime()))));
            }
        }

    }

    public static class SensorRecordMapper implements RecordMapper<Record, Sensor> {

        @Override
        public Sensor map(Record rec) {
            Sensor sensor = new Sensor();
            ProcedureRecord record = rec.into(ProcedureRecord.class);

            setStaIdentifier(sensor, record.getStaIdentifier());
            setStaName(sensor, record.getName());
            setStaDescription(sensor, record.getDescription());
            setEncodingType(sensor, rec);
            setMetadata(sensor, record);

            return sensor;
        }

        public static class SensorParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record rec) {
                ProcedureParameterRecord record = rec.into(ProcedureParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();

                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key,
                                record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key,
                                record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key,
                                record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key,
                                record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key,
                                record.getValueTemporalTo().toString());
                    }
                }
                return properties;
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setMetadata(Sensor sensor, ProcedureRecord record) {
            if (record.getDescriptionFile() != null) {
                sensor.setMetadata(record.getDescriptionFile());
            }
        }

        private void setEncodingType(Sensor sensor, Record rec) {
            FormatRecord record = rec.into(FormatRecord.class);

            if (record.getDefinition() != null) {
                sensor.setEncodingType(record.getDefinition());
            }
        }
    }

    public static class ObservedPropertyRecordMapper implements RecordMapper<Record, ObservedProperty> {
        @Override
        public ObservedProperty map(Record rec) {
            ObservedProperty observedProperty = new ObservedProperty();
            PhenomenonRecord record = rec.into(PhenomenonRecord.class);

            setStaIdentifier(observedProperty, record.getStaIdentifier());
            setStaName(observedProperty, record.getName());
            setStaDescription(observedProperty, record.getDescription());
            setDefinition(observedProperty, record);

            return observedProperty;
        }
        public static class ObservedPropertyParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record rec) {
                PhenomenonParameterRecord record = rec.into(PhenomenonParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();

                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key,
                                record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key,
                                record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key,
                                record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key,
                                record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key,
                                record.getValueTemporalTo().toString());
                    }
                }
                return properties;
            }
            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setDefinition(ObservedProperty observedProperty, PhenomenonRecord record) {
            if (record.getIdentifier() != null) {
                observedProperty.setDefinition(record.getIdentifier());
            }
        }
    }

    public static class ObservationRecordMapper implements RecordMapper<Record, Observation> {
        @Override
        public Observation map(Record rec) {
            Observation observation = new Observation();
            ObservationRecord record = rec.into(ObservationRecord.class);

            setStaIdentifier(observation, record.getStaIdentifier());
            setPhenomenonTime(observation, record);
            setResultTime(observation, record);
            setResult(observation, record);
            setValidTime(observation, record);

            return observation;
        }

        private void setPhenomenonTime(Observation observation, ObservationRecord record) {
            if (record.getSamplingTimeStart() != null && record.getSamplingTimeEnd() != null) {
                observation.setPhenomenonTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getSamplingTimeStart())),
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getSamplingTimeEnd())))
                );
            }
        }
        public static class ObservationParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setParameters(Record rec) {
                ObservationParameterRecord record = rec.into(ObservationParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();

                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key,
                                record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key,
                                record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key,
                                record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key,
                                record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key,
                                record.getValueTemporalTo().toString());
                    }
                }
                return properties;
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
            }
        }

        private void setValidTime(Observation observation, ObservationRecord record) {
            if (record.getValidTimeStart() != null && record.getValidTimeEnd() != null) {
                observation.setPhenomenonTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getValidTimeStart())),
                        TimeUtil.createDateTime(Timestamp.valueOf(record.getValidTimeEnd())))
                );
            }
        }

        private void setResultTime(Observation observation, ObservationRecord record) {
            if (record.getResultTime() != null) {
               observation.setResultTime(TimeUtil.createTime(new DateTime(record.getResultTime())));
            }
        }

    }

    public static class FeatureOfInterestRecordMapper implements RecordMapper<Record, FeatureOfInterest> {
        @Override
        public FeatureOfInterest map(Record rec) {
            FeatureOfInterest featureOfInterest = new FeatureOfInterest();
            FeatureRecord record = rec.into(FeatureRecord.class);

            setStaIdentifier(featureOfInterest, record.getStaIdentifier());
            setStaName(featureOfInterest, record.getName());
            setStaDescription(featureOfInterest, record.getDescription());
            setFeature(featureOfInterest, rec);
            setEncodingType(featureOfInterest, rec);

            return featureOfInterest;
        }
        public static class FeatureParameterRecordMapper implements RecordMapper<Record, ObjectNode> {

            private ObjectNode setProperties(Record rec) {
                FeatureParameterRecord record = rec.into(FeatureParameterRecord.class);
                ObjectNode properties = MAPPER.createObjectNode();

                if (record.getName() != null) {
                    String key = record.getName();

                    if (record.getValueCount() != null) {
                        properties.put(key, record.getValueCount());
                    }
                    if (record.getValueText() != null) {
                        properties.put(key, record.getValueText());
                    }
                    if (record.getValueQuantity() != null) {
                        properties.put(key, record.getValueQuantity());
                    }
                    if (record.getValueBoolean() != null) {
                        properties.put(key, record.getValueBoolean());
                    }
                    if (record.getValueCategory() != null) {
                        properties.put(key,
                                record.getValueCategory());
                    }
                    if (record.getValueXml() != null) {
                        properties.put(key,
                                record.getValueXml());
                    }
                    if (record.getValueJson() != null) {
                        properties.put(key,
                                record.getValueJson());
                    }
                    if (record.getValueTemporalFrom() != null) {
                        properties.put(key,
                                record.getValueTemporalFrom().toString());
                    }
                    if (record.getValueTemporalTo() != null) {
                        properties.put(key,
                                record.getValueTemporalTo().toString());
                    }
                }

                return properties;
            }

            @Override
            public ObjectNode map(Record record) {
                return setProperties(record);
            }
        }

        private void setEncodingType(FeatureOfInterest featureOfInterest, Record rec) {
            FormatRecord record = rec.into(FormatRecord.class);
            if (record.getDefinition() != null) {
                featureOfInterest.setEncodingType(record.getDefinition());
            }
        }

        private void setFeature(FeatureOfInterest featureOfInterest, Record record) {
            if (record.field("GEOM") != null) {
                try {
                    featureOfInterest.setFeature(WKTReader.read(record.get(DSL.field("GEOM", String.class))));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            }
        }
    }
}