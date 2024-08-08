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

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

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

    private static final WKBReader WKBreader = new WKBReader();
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

    private static <T extends HasProperties> void setStaProperties(T entity, Record rec) {
        ObjectNode properties = MAPPER.createObjectNode();

        // Use one ParameterRecord type for all STA entities since all have common attributes
        DatasetParameterRecord record = rec.into(DatasetParameterRecord.class);

        if (record.getName() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.NAME.getName(),
                    record.getName());
        }
        if (record.getDescription() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.DESCRIPTION.getName(),
                    record.getDescription());
        }
        if (record.getLastUpdate() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.LAST_UPDATE.getName(),
                    record.getLastUpdate().toString());
        }
        if (record.getDomain() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.DOMAIN.getName(),
                    record.getDomain());
        }
        if (record.getValueCount() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_COUNT.getName(),
                    record.getValueCount());
        }
        if (record.getValueText() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_TEXT.getName(),
                    record.getValueText());
        }
        if (record.getValueQuantity() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_QUANTITY.getName(),
                    record.getValueQuantity());
        }
        if (record.getValueBoolean() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_BOOLEAN.getName(),
                    record.getValueBoolean());
        }
        if (record.getValueCategory() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_CATEGORY.getName(),
                    record.getValueCategory());
        }
        if (record.getValueXml() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_XML.getName(),
                    record.getValueXml());
        }
        if (record.getValueJson() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_JSON.getName(),
                    record.getValueJson());
        }
        if (record.getValueTemporalFrom() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_TEMPORAL_FROM.getName(),
                    record.getValueTemporalFrom().toString());
        }
        if (record.getValueTemporalTo() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.VALUE_TEMPORAL_TO.getName(),
                    record.getValueTemporalTo().toString());
        }
        if (record.getFkUnitId() != null) {
            properties.put(StaEntity.OBSERVATION_PARAMETERS.FK_UNIT_ID.getName(),
                    record.getFkUnitId());
        }

        entity.setProperties(properties);
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

            setStaIdentifier(datastream, record.getIdentifier());
            setStaName(datastream, record.getName());
            setStaDescription(datastream, record.getDescription());
            setProperties(datastream, record);
            setPhenomenonTime(datastream, record);
            setObservedArea(datastream, record);
            setResultTime(datastream, record);
            setObservationType(datastream, record);
            setUnitOfMeasurement(datastream, rec);

            return datastream;
        }

        private void setProperties(Datastream datastream, Record rec) {
            DatasetParameterRecord record = rec.into(DatasetParameterRecord.class);
            setStaProperties(datastream, record);
            if(record.getFkDatasetId() != null) {
                datastream.getProperties().put(
                        StaEntity.DATASTREAM_PROPERTIES.FK_DATASET_ID.getName(),
                        record.getFkDatasetId()
                );
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

        private void setObservedArea (Datastream datastream, DatasetRecord record){

            if (record.getObservedArea() != null) {
                try {
                    datastream.setObservedArea(WKBreader.read(record.getObservedArea()));
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

        private void setUnitOfMeasurement (Datastream datastream, Record rec){
            UnitRecord record = rec.into(UnitRecord.class);
            DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();

            if (record.getName() != null) {
                uom.setName(record.getName());
            }
            if (record.getSymbol() != null) {
                uom.setSymbol(record.getSymbol());
            }
            if (record.getLink() != null) {
                uom.setDefinition(record.getLink());
            }
            datastream.setUnitOfMeasurement(uom);
        }
    }

    public static class LocationRecordMapper implements RecordMapper<Record, Location> {
        @Override
        public Location map(Record rec) {
            Location location = new Location();
            LocationRecord record = rec.into(LocationRecord.class);

            setStaIdentifier(location, record.getIdentifier());
            setStaName(location, record.getName());
            setStaDescription(location, record.getDescription());
            setProperties(location, record);
            setGeometry(location, record);

            return location;
        }

        private void setProperties(Location location, Record rec) {
            LocationParameterRecord record = rec.into(LocationParameterRecord.class);
            setStaProperties(location, record);
            if (record.getFkLocationId() != null) {
                location.getProperties().put(
                        StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID.getName(),
                        record.getFkLocationId()
                );
            }
        }

        private void setGeometry(Location location, LocationRecord record) {
            if (record.getGeom() != null) {
                try {
                    location.setGeometry(WKBreader.read((record.getGeom())));
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
            setStaIdentifier(thing, record.getIdentifier());
            setStaName(thing, record.getName());
            setStaDescription(thing, record.getDescription());
            setProperties(thing, rec);
            return thing;
        }

        private void setProperties(Thing thing, Record rec) {
            PlatformParameterRecord record = rec.into(PlatformParameterRecord.class);
            setStaProperties(thing, rec);
            if (record.getFkPlatformId() != null) {
                thing.getProperties().put(
                        StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID.getName(),
                        record.getFkPlatformId()
                );
            }
        }
    }

    public static class HistoricalLocationRecordMapper implements RecordMapper<Record, HistoricalLocation> {
        @Override
        public HistoricalLocation map(Record rec) {
            HistoricalLocation historicalLocation = new HistoricalLocation();
            HistoricalLocationRecord record = rec.into(HistoricalLocationRecord.class);

            setStaIdentifier(historicalLocation, record.getIdentifier());
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

            setStaIdentifier(sensor, record.getIdentifier());
            setStaName(sensor, record.getName());
            setStaDescription(sensor, record.getDescription());
            setMetadata(sensor, record);
            setProperties(sensor, rec);
            setEncodingType(sensor, rec);

            return sensor;
        }

        private void setProperties(Sensor sensor, Record rec) {
            ProcedureParameterRecord record = rec.into(ProcedureParameterRecord.class);
            setStaProperties(sensor, rec);
            if (record.getFkProcedureId() != null) {
                sensor.getProperties().put(
                        StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID.getName(),
                        record.getFkProcedureId()
                );
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

            setStaIdentifier(observedProperty, record.getIdentifier());
            setStaName(observedProperty, record.getName());
            setStaDescription(observedProperty, record.getDescription());
            setDefinition(observedProperty, record);
            setProperties(observedProperty, rec);

            return observedProperty;
        }

        private void setProperties(ObservedProperty observedProperty, Record rec) {
            PhenomenonParameterRecord record = rec.into(PhenomenonParameterRecord.class);
            setStaProperties(observedProperty, rec);
            if (record.getFkPhenomenonId() != null) {
                observedProperty.getProperties().put(
                        StaEntity.LOCATION_PROPERTIES.FK_LOCATION_ID.getName(),
                        record.getFkPhenomenonId()
                );
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

            setStaIdentifier(observation, record.getIdentifier());
            setPhenomenonTime(observation, record);
            setParameters(observation, record);
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

        private void setParameters(Observation observation, Record rec) {
            ObservationParameterRecord record = rec.into(ObservationParameterRecord.class);
            ObjectNode properties = MAPPER.createObjectNode();

            if (record.getName() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.NAME.getName(),
                        record.getName());
            }
            if (record.getDescription() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.DESCRIPTION.getName(),
                        record.getDescription());
            }
            if (record.getLastUpdate() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.LAST_UPDATE.getName(),
                        record.getLastUpdate().toString());
            }
            if (record.getDomain() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.DOMAIN.getName(),
                        record.getDomain());
            }
            if (record.getValueCount() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_COUNT.getName(),
                        record.getValueCount());
            }
            if (record.getValueText() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_TEXT.getName(),
                        record.getValueText());
            }
            if (record.getValueQuantity() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_QUANTITY.getName(),
                        record.getValueQuantity());
            }
            if (record.getValueBoolean() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_BOOLEAN.getName(),
                        record.getValueBoolean());
            }
            if (record.getValueCategory() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_CATEGORY.getName(),
                        record.getValueCategory());
            }
            if (record.getValueXml() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_XML.getName(),
                        record.getValueXml());
            }
            if (record.getValueJson() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_JSON.getName(),
                        record.getValueJson());
            }
            if (record.getValueTemporalFrom() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_TEMPORAL_FROM.getName(),
                        record.getValueTemporalFrom().toString());
            }
            if (record.getValueTemporalTo() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.VALUE_TEMPORAL_TO.getName(),
                        record.getValueTemporalTo().toString());
            }
            if (record.getFkUnitId() != null) {
                properties.put(
                        StaEntity.OBSERVATION_PARAMETERS.FK_UNIT_ID.getName(),
                        record.getFkUnitId());
            }
            if (record.getFkObservationId() != null) {
                observation.getParameters().put(
                        StaEntity.OBSERVATION_PARAMETERS.FK_OBSERVATION_ID.getName(),
                        record.getFkObservationId()
                );
            }

            observation.setParameters(properties);
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
            setFeature(featureOfInterest, record);
            setProperties(featureOfInterest, rec);
            setEncodingType(featureOfInterest, rec);

            return featureOfInterest;
        }

        private void setProperties(FeatureOfInterest featureOfInterest, Record rec) {
            FeatureParameterRecord record = rec.into(FeatureParameterRecord.class);
            setStaProperties(featureOfInterest, rec);
            if (record.getFkFeatureId() != null) {
                featureOfInterest.getProperties().put(
                        StaEntity.OBSERVATION_PARAMETERS.FK_OBSERVATION_ID.getName(),
                        record.getFkFeatureId()
                );
            }
        }

        private void setEncodingType(FeatureOfInterest featureOfInterest, Record rec) {
            FormatRecord record = rec.into(FormatRecord.class);
            if (record.getDefinition() != null) {
                featureOfInterest.setEncodingType(record.getDefinition());
            }
        }

        private void setFeature(FeatureOfInterest featureOfInterest, FeatureRecord record) {
            if (record.getGeom() != null) {
                try {
                    featureOfInterest.setFeature(WKBreader.read(record.getGeom()));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            }
        }
    }
}