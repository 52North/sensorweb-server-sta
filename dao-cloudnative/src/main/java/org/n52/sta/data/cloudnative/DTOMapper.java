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

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.sta.api.dto.*;
import org.n52.sta.api.dto.impl.*;

import org.n52.sta.data.cloudnative.condition.EntityQueryConstants;
import org.n52.sta.utils.TimeUtil;

import org.springframework.util.Assert;

import java.sql.Time;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * jOOQ Record Mapper for mapping Result Sets to DTO Entities
 *
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class DTOMapper implements RecordMapperProvider {

    private static final WKBReader WKBreader = new WKBReader();

    private static <T extends HasNameAndDescription> void setStaDescription (T entity, Record record, String alias){
        String fieldName = alias.concat(EntityQueryConstants.STA_DESCRIPTION_FIELD);

        if (record.field(DSL.field(fieldName, String.class)) != null) {
            entity.setDescription(record.get(DSL.field(fieldName, String.class)));
        }
    }

    private static <T extends HasNameAndDescription> void setStaName (T entity, Record record, String alias) {
        String fieldName = alias.concat(EntityQueryConstants.STA_NAME_FIELD);

        if (record.field(DSL.field(fieldName, String.class)) != null) {
            entity.setName(record.get(DSL.field(fieldName, String.class)));
        }
    }

    private static <T extends StaDTO> void setStaIdentifier (T entity, Record record, String alias){
        String fieldName = alias.concat(EntityQueryConstants.STA_IDENTIFIER_FIELD);

        if (record.field(DSL.field(fieldName, String.class)) != null) {
            entity.setId(record.get(DSL.field(fieldName, String.class)));
        }
    }

    private static <T extends HasPhenomenonTime> void setPhenomenonTime(T entity, Record record, String alias) {
        String fieldStart = alias.concat(EntityQueryConstants.DATASTREAM_PHENOMENONTIME_START_FIELD);
        String fieldEnd = alias.concat(EntityQueryConstants.DATASTREAM_PHENOMENONTIME_END_FIELD);

        if (record.field(DSL.field(fieldStart)) != null && record.field(DSL.field(fieldEnd, String.class)) != null) {
            entity.setPhenomenonTime(TimeUtil.createTime(
                    TimeUtil.createDateTime(record.get(DSL.field(fieldStart, Date.class))),
                    TimeUtil.createDateTime(record.get(DSL.field(fieldEnd, Date.class))))
            );
        }
    }

    private static <T extends StaDTO> void setProperties (T entity, Record record, String alias) {

    }

    @Override
    public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType,
                                                                           Class<? extends E> type) {
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
        public Datastream map(Record record) {

            Datastream datastream = new Datastream();
            final String alias = EntityQueryConstants.DATASTREAM_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(datastream, record, alias);
            setStaName(datastream, record, alias);
            setStaDescription(datastream, record, alias);
            setProperties(datastream, record, alias);
            setPhenomenonTime(datastream, record, alias);
            setObservedArea(datastream, record, alias);
            setResultTime(datastream, record, alias);
            setObservationType(datastream, record);
            setUnitOfMeasurement(datastream, record);
            setThing(datastream, record);
            setSensor(datastream, record);
            setObservedProperty(datastream, record);

            return datastream;
        }

        private void setResultTime(Datastream datastream, Record record, String alias) {
            String fieldStart = alias.concat(EntityQueryConstants.DATASTREAM_RESULTTIME_START_FIELD);
            String fieldEnd = alias.concat(EntityQueryConstants.DATASTREAM_RESULTTIME_END_FIELD);

            if (record.field(DSL.field(fieldStart)) != null && record.field(DSL.field(fieldEnd)) != null) {
                datastream.setResultTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(record.get(DSL.field(fieldStart, Date.class))),
                        TimeUtil.createDateTime(record.get(DSL.field(fieldEnd, Date.class))))
                );
            }
        }

        private void setObservedArea (Datastream datastream, Record record, String alias){
            String fieldName = alias.concat(EntityQueryConstants.DATASTREAM_OBS_AREA_FIELD);

            if (record.field(DSL.field(fieldName, Geometry.class)) != null) {
                try {
                    datastream.setObservedArea(WKBreader.read(record.get(DSL.field(fieldName), byte[].class)));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }

            }
        }

            private void setObservationType (Datastream datastream, Record record){
                String fieldName = EntityQueryConstants.FORMAT_TABLE
                        .concat(EntityQueryConstants.UNDERSCORE
                                .concat(EntityQueryConstants.STA_DEFINITION_FIELD));

                if (record.field(DSL.field(fieldName, String.class)) != null) {
                    datastream.setObservationType(record.get(DSL.field(fieldName, String.class)));
                }
            }

        private void setUnitOfMeasurement (Datastream datastream, Record record){
            String alias = EntityQueryConstants.UNIT_TABLE.concat(EntityQueryConstants.UNDERSCORE);
            String fieldName = alias.concat(EntityQueryConstants.UNIT_NAME_FIELD);
            String fieldSymbol = alias.concat(EntityQueryConstants.UNIT_SYMBOL_FIELD);
            String fieldLink = alias.concat(EntityQueryConstants.UNIT_LINK_FIELD);

            DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
            if (record.field(DSL.field(fieldName)) != null) {
                uom.setName(record.get(DSL.field(fieldName, String.class)));
            }
            if (record.field(DSL.field(fieldSymbol)) != null) {
                uom.setSymbol(record.get(DSL.field(fieldSymbol, String.class)));
            }
            if (record.field(DSL.field(fieldLink)) != null) {
                uom.setDefinition(record.get(DSL.field(fieldLink, String.class)));
            }
            datastream.setUnitOfMeasurement(uom);
        }

        private void setObservedProperty (Datastream datastream, Record record) {
            ObservedPropertyDTO observedProperty = (new ObservedPropertyRecordMapper().map(record));
            datastream.setObservedProperty(observedProperty);
        }

        private void setSensor (Datastream datastream, Record record) {
            SensorDTO sensor = (new SensorRecordMapper().map(record));
            datastream.setSensor(sensor);
        }

        private void setThing (Datastream datastream, Record record) {
            ThingDTO thing = (new ThingRecordMapper().map(record));
            datastream.setThing(thing);
        }
    }

    public static class LocationRecordMapper implements RecordMapper<Record, Location> {
        @Override
        public Location map(Record record) {
            Location location = new Location();
            String alias = EntityQueryConstants.LOCATION_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(location, record, alias);
            setStaName(location, record, alias);
            setStaDescription(location, record, alias);
            setProperties(location, record, alias);
            setGeometry(location, record, alias);
            setThings(location, record);
            setHistoricalLocations(location, record);

            return location;
        }

        private void setHistoricalLocations(Location location, Record record) {
            Set<HistoricalLocationDTO> historicalLocations = new HashSet<>();
            historicalLocations.add(new HistoricalLocationRecordMapper().map(record));
            location.setHistoricalLocations(historicalLocations);
        }

        private void setThings(Location location, Record record) {
            Set<ThingDTO> things = new HashSet<>();
            things.add(new ThingRecordMapper().map(record));
            location.setThings(things);
        }

        private void setGeometry(Location location, Record record, String alias) {
            String fieldName = alias.concat(EntityQueryConstants.LOCATION_GEOM_FIELD);

            if (record.field(fieldName) != null) {
                try {
                    location.setGeometry(WKBreader.read((record.get(DSL.field(fieldName), byte[].class))));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            }
        }
    }

    public static class ThingRecordMapper implements RecordMapper<Record, Thing> {
        @Override
        public Thing map(Record record) {
            Thing thing = new Thing();
            String alias = EntityQueryConstants.THING_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(thing, record, alias);
            setStaName(thing, record, alias);
            setStaDescription(thing, record, alias);
            setProperties(thing, record, alias);
            setDatastreams(thing, record);
            setLocations(thing, record);
            setHistoricalLocations(thing, record);

            return thing;
        }

        private void setDatastreams(Thing thing, Record record) {
            Set<DatastreamDTO> datastreams = new HashSet<>();
            datastreams.add(new DatastreamRecordMapper().map(record));
            thing.setDatastreams(datastreams);
        }

        private void setLocations(Thing thing, Record record) {
            Set<LocationDTO> locations = new HashSet<>();
            locations.add(new LocationRecordMapper().map(record));
            thing.setLocations(locations);
        }

        private void setHistoricalLocations(Thing thing, Record record) {
            Set<HistoricalLocationDTO> historicalLocations = new HashSet<>();
            historicalLocations.add(new HistoricalLocationRecordMapper().map(record));
            thing.setHistoricalLocations(historicalLocations);
        }
    }

    public static class HistoricalLocationRecordMapper implements RecordMapper<Record, HistoricalLocation> {
        @Override
        public HistoricalLocation map(Record record) {
            HistoricalLocation historicalLocation = new HistoricalLocation();
            String alias = EntityQueryConstants.HISTORICAL_LOCATION_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(historicalLocation, record, alias);
            setTime(historicalLocation, record, alias);
            setLocations(historicalLocation, record);
            setThing(historicalLocation, record);

            return historicalLocation;
        }

        private void setTime(HistoricalLocation historicalLocation, Record record, String alias) {
            String fieldName = alias.concat(EntityQueryConstants.HISTORICAL_LOCATION_TIME_FIELD);

            if(record.field(DSL.field(fieldName)) != null) {
                historicalLocation.setTime(new TimeInstant(record.get(DSL.field(fieldName, Time.class))));
            }
        }

        private void setLocations(HistoricalLocation historicalLocation, Record record) {
            Set<LocationDTO> locations = new HashSet<>();
            locations.add(new LocationRecordMapper().map(record));
            historicalLocation.setLocations(locations);
        }

        private void setThing(HistoricalLocation historicalLocation, Record record) {
            ThingDTO things = new ThingRecordMapper().map(record);
            historicalLocation.setThing(things);
        }
    }

    public static class SensorRecordMapper implements RecordMapper<Record, Sensor> {
        @Override
        public Sensor map(Record record) {
            Sensor sensor = new Sensor();
            String alias = EntityQueryConstants.SENSOR_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(sensor, record, alias);
            setStaName(sensor, record, alias);
            setStaDescription(sensor, record, alias);
            setProperties(sensor, record, alias);
            setMetadata(sensor, record, alias);
            setEncodingType(sensor, record);
            setDatastreams(sensor, record);

            return sensor;
        }

        private void setMetadata(Sensor sensor, Record record, String alias) {
            String fieldName = alias.concat(EntityQueryConstants.SENSOR_METADATA_FIELD);

            if (record.field(DSL.field(fieldName)) != null) {
                sensor.setMetadata(record.get(DSL.field(EntityQueryConstants.SENSOR_METADATA_FIELD, String.class)));
            }
        }

        private void setEncodingType(Sensor sensor, Record record) {
            String alias = EntityQueryConstants.FORMAT_TABLE.concat(EntityQueryConstants.UNDERSCORE);
            String fieldName = alias.concat(EntityQueryConstants.STA_DEFINITION_FIELD);

            if (record.field(DSL.field(fieldName)) != null) {
                sensor.setEncodingType(record.get(DSL.field(fieldName, String.class)));
            }
        }

        private void setDatastreams(Sensor sensor, Record record) {
            Set<DatastreamDTO> datastreams = new HashSet<>();
            datastreams.add(new DatastreamRecordMapper().map(record));
            sensor.setDatastreams(datastreams);
        }
    }

    public static class ObservedPropertyRecordMapper implements RecordMapper<Record, ObservedProperty> {
        @Override
        public ObservedProperty map(Record record) {
            ObservedProperty observedProperty = new ObservedProperty();
            String alias = EntityQueryConstants.OBSERVED_PROPERTY_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(observedProperty, record, alias);
            setStaName(observedProperty, record, alias);
            setStaDescription(observedProperty, record, alias);
            setProperties(observedProperty, record, alias);
            setDefinition(observedProperty, record, alias);
            setDatastreams(observedProperty, record);

            return observedProperty;
        }

        private void setDefinition(ObservedProperty observedProperty, Record record, String alias) {
            String fieldName = alias.concat(EntityQueryConstants.STA_DEFINITION_FIELD);

            if (record.field(DSL.field(fieldName)) != null) {
                observedProperty.setDefinition(record.get(DSL.field(fieldName, String.class)));
            }
        }

        private void setDatastreams(ObservedProperty observedProperty, Record record) {
            Set<DatastreamDTO> datastreams = new HashSet<>();
            datastreams.add(new DatastreamRecordMapper().map(record));
            observedProperty.setDatastreams(datastreams);
        }
    }

    public static class ObservationRecordMapper implements RecordMapper<Record, Observation> {
        @Override
        public Observation map(Record record) {
            Observation observation = new Observation();
            String alias = EntityQueryConstants.OBSERVATION_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(observation, record, alias);
            setProperties(observation, record, alias);
            setProperties(observation, record, alias);
            setPhenomenonTime(observation, record, alias);
            setResultTime(observation, record, alias);
            setResult(observation, record, alias);
            setValidTime(observation, record, alias);
            setDatastreams(observation, record);
            setFeatureOfInterest(observation, record);

            return observation;
        }

        private void setFeatureOfInterest(Observation observation, Record record) {
            observation.setFeatureOfInterest(new FeatureOfInterestRecordMapper().map(record));
        }

        private void setDatastreams(Observation observation, Record record) {
            observation.setDatastream(new DatastreamRecordMapper().map(record));
        }

        private void setResult(Observation observation, Record record, String alias) {
            String fieldCount = alias.concat(EntityQueryConstants.OBSERVATION_VALUE_COUNT_FIELD);
            String fieldText = alias.concat(EntityQueryConstants.OBSERVATION_VALUE_TEXT_FIELD);
            String fieldQuantity = alias.concat(EntityQueryConstants.OBSERVATION_VALUE_QUANTITY_FIELD);
            String fieldBoolean = alias.concat(EntityQueryConstants.OBSERVATION_VALUE_BOOLEAN_FIELD);
            String fieldCategory = alias.concat(EntityQueryConstants.OBSERVATION_VALUE_CATEGORY_FIELD);

            if (record.field(DSL.field(fieldCount)) != null) {
                observation.setResult(record.get(DSL.field(fieldCount)));
            } else if (record.field(DSL.field(fieldText)) != null) {
                observation.setResult(record.get(DSL.field(fieldText)));
            } else if (record.field(DSL.field(fieldQuantity)) != null) {
                observation.setResult(record.get(DSL.field(fieldQuantity)));
            } else if (record.field(DSL.field(fieldBoolean)) != null) {
                observation.setResult(record.get(DSL.field(fieldBoolean)));
            } else if (record.field(DSL.field(fieldCategory)) != null) {
                observation.setResult(record.get(DSL.field(fieldCategory)));
            }
        }

        private void setValidTime(Observation observation, Record record, String alias) {
            String fieldStart = alias.concat(EntityQueryConstants.OBSERVATION_VALIDTIME_START_FIELD);
            String fieldEnd = alias.concat(EntityQueryConstants.OBSERVATION_VALIDTIME_END_FIELD);

            if (record.field(DSL.field(fieldStart)) != null) {
                observation.setPhenomenonTime(TimeUtil.createTime(
                        TimeUtil.createDateTime(record.get(DSL.field(fieldStart, Date.class))),
                        TimeUtil.createDateTime(record.get(DSL.field(fieldEnd, Date.class))))
                );
            }
        }

        private void setResultTime(Observation observation, Record record, String alias) {
            String fieldName = alias.concat(EntityQueryConstants.OBSERVATION_RESULT_TIME_FIELD);

            if (record.field(DSL.field(fieldName)) != null) {
               observation.setResultTime(new TimeInstant(record.get(DSL.field(fieldName, Time.class))));
            }
        }

    }

    public static class FeatureOfInterestRecordMapper implements RecordMapper<Record, FeatureOfInterest> {
        @Override
        public FeatureOfInterest map(Record record) {
            FeatureOfInterest featureOfInterest = new FeatureOfInterest();
            String alias = EntityQueryConstants.FEATURE_TABLE.concat(EntityQueryConstants.UNDERSCORE);

            setStaIdentifier(featureOfInterest, record, alias);
            setStaName(featureOfInterest, record, alias);
            setStaDescription(featureOfInterest, record, alias);
            setProperties(featureOfInterest, record, alias);

            setFeature(featureOfInterest, record, alias);
            setEncodingType(featureOfInterest, record);

            setObservations(featureOfInterest, record);
            return featureOfInterest;
        }

        private void setObservations(FeatureOfInterest featureOfInterest, Record record) {
            Set<ObservationDTO> observations = new HashSet<>();
            observations.add(new ObservationRecordMapper().map(record));
            featureOfInterest.setObservations(observations);
        }

        private void setEncodingType(FeatureOfInterest featureOfInterest, Record record) {
            String alias = EntityQueryConstants.FORMAT_TABLE.concat(EntityQueryConstants.UNDERSCORE);
            String fieldName = alias.concat(EntityQueryConstants.STA_DEFINITION_FIELD);

            if (record.field(DSL.field(fieldName)) != null) {
                featureOfInterest.setEncodingType(record.get(DSL.field(fieldName, String.class)));
            }
        }

        private void setFeature(FeatureOfInterest featureOfInterest, Record record, String alias) {
            String fieldName = alias.concat(EntityQueryConstants.FEATURE_GEOM_FIELD);

            if (record.field(DSL.field(fieldName)) != null) {
                try {
                    featureOfInterest.setFeature(WKBreader.read(record.get(DSL.field(fieldName, byte[].class))));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse to WKB" + e.getMessage());
                }
            }
        }
    }
}


