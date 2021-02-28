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

package org.n52.sta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryError;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.api.dto.impl.Datastream;
import org.n52.sta.api.dto.impl.FeatureOfInterest;
import org.n52.sta.api.dto.impl.HistoricalLocation;
import org.n52.sta.api.dto.impl.Location;
import org.n52.sta.api.dto.impl.Observation;
import org.n52.sta.api.dto.impl.ObservedProperty;
import org.n52.sta.api.dto.impl.Sensor;
import org.n52.sta.api.dto.impl.Thing;
import org.n52.sta.utils.TimeUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Translates between STA DTO Entities and Entities used by the dao-postgres module internally
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class DTOTransformer<R extends StaDTO, S extends HibernateRelations.HasId> {

    private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
    private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public R toDTO(Object raw, QueryOptions queryOptions) throws STAInvalidQueryError {
        switch (raw.getClass().getSimpleName()) {
            case "PlatformEntity": {
                return (R) toThingDTO((PlatformEntity) raw, queryOptions);
            }
            case "LocationEntity": {
                return (R) toLocationDTO((LocationEntity) raw, queryOptions);
            }
            case "HistoricalLocationEntity": {
                return (R) toHistoricalLocationDTO((HistoricalLocationEntity) raw, queryOptions);
            }
            case "AbstractDatasetEntity":
            case "DatasetEntity":
            case "DatasetAggregationEntity": {
                return (R) toDatastreamDTO((AbstractDatasetEntity) raw, queryOptions);
            }
            case "ProcedureEntity": {
                return (R) toSensorDTO((ProcedureEntity) raw, queryOptions);
            }
            case "StaFeatureEntity": {
                return (R) toFeatureOfInterestDTO((StaFeatureEntity) raw, queryOptions);
            }
            case "PhenomenonEntity": {
                return (R) toObservedPropertyDTO((PhenomenonEntity) raw, queryOptions);
            }
            default:
                // As we have many different types we unwrap them all here
                if (raw instanceof DataEntity) {
                    return (R) toObservationDTO((DataEntity<?>) raw, queryOptions);
                }
                throw new STAInvalidQueryError(String.format("Could not parse entity %s to DTO. Unknown type!",
                                                             raw.getClass().getName()));
        }
    }

    private DatastreamDTO toDatastreamDTO(AbstractDatasetEntity raw, QueryOptions queryOptions) {
        DatastreamDTO datastream = new Datastream();
        datastream.setAndParseQueryOptions(queryOptions);

        datastream.setId(raw.getStaIdentifier());
        datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getSamplingTimeStart()),
                                                         TimeUtil.createDateTime(raw.getSamplingTimeEnd())));
        datastream.setDescription(raw.getDescription());
        datastream.setObservedArea(raw.getGeometry());
        datastream.setObservationType(raw.getOMObservationType().getFormat());
        datastream.setProperties(parseProperties(raw));
        datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getResultTimeStart()),
                                                     TimeUtil.createDateTime(raw.getResultTimeEnd())));

        datastream.setUnitOfMeasurement(new DatastreamDTO.UnitOfMeasurement(
            raw.getUnit().getSymbol(),
            raw.getUnit().getName(),
            raw.getUnit().getDescription())
        );
        if (datastream.getFieldsToExpand().containsKey(StaConstants.OBSERVED_PROPERTY)) {
            datastream.setObservedProperty(
                toObservedPropertyDTO(raw.getPhenomenon(),
                                      datastream.getFieldsToExpand().get(StaConstants.OBSERVED_PROPERTY)));
        }
        if (datastream.getFieldsToExpand().containsKey(StaConstants.SENSOR)) {
            datastream.setSensor(
                toSensorDTO(raw.getProcedure(),
                            datastream.getFieldsToExpand().get(StaConstants.SENSOR)));
        }
        if (datastream.getFieldsToExpand().containsKey(StaConstants.THING)) {
            datastream.setThing(
                toThingDTO(raw.getPlatform(),
                           datastream.getFieldsToExpand().get(StaConstants.THING)));
        }

        if (datastream.getFieldsToExpand().containsKey(StaConstants.OBSERVATIONS)) {
            datastream.setObservations(
                raw.getObservations().stream()
                    .map(o -> toObservationDTO(o, datastream.getFieldsToExpand().get(StaConstants.OBSERVATIONS)))
                    .collect(Collectors.toSet())
            );
        }
        return datastream;
    }

    private ThingDTO toThingDTO(PlatformEntity platform, QueryOptions queryOptions) {
        ThingDTO thing = new Thing();
        thing.setAndParseQueryOptions(queryOptions);
        thing.setId(platform.getStaIdentifier());
        thing.setName(platform.getName());

        if (thing.getFieldsToExpand().containsKey(StaConstants.DATASTREAMS)) {
            thing.setDatastreams(platform.getDatasets()
                                     .stream()
                                     .map(o -> this.toDatastreamDTO(
                                         o,
                                         thing.getFieldsToExpand().get(StaConstants.DATASTREAMS)))
                                     .collect(Collectors.toSet()));
        }

        if (thing.getFieldsToExpand().containsKey(StaConstants.LOCATIONS)) {
            thing.setLocations(platform.getLocations()
                                   .stream()
                                   .map(o -> this.toLocationDTO(
                                       o,
                                       thing.getFieldsToExpand().get(StaConstants.LOCATIONS)))
                                   .collect(Collectors.toSet()));
        }

        if (thing.getFieldsToExpand().containsKey(StaConstants.HISTORICAL_LOCATIONS)) {
            thing.setHistoricalLocations(platform.getHistoricalLocations()
                                             .stream()
                                             .map(o -> this.toHistoricalLocationDTO(
                                                 o,
                                                 thing.getFieldsToExpand().get(StaConstants.HISTORICAL_LOCATIONS)))
                                             .collect(Collectors.toSet()));
        }
        return thing;
    }

    private ObservationDTO toObservationDTO(DataEntity<?> raw, QueryOptions queryOptions) {
        ObservationDTO observation = new Observation();
        observation.setAndParseQueryOptions(queryOptions);

        observation.setId(raw.getStaIdentifier());
        observation.setResult(parseObservationResult(raw));

        observation.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getPhenomenonTimeStart()),
                                                          TimeUtil.createDateTime(raw.getPhenomenonTimeEnd())));
        observation.setValidTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getValidTimeStart()),
                                                     TimeUtil.createDateTime(raw.getValidTimeStart())));
        observation.setResultTime(new TimeInstant(raw.getResultTime()));

        observation.setParameters(parseProperties(raw));
        if (observation.getFieldsToExpand().containsKey(StaConstants.FEATURE_OF_INTEREST)) {
            observation.setFeatureOfInterest(toFeatureOfInterestDTO((StaFeatureEntity) raw.getFeature(),
                                                                    observation.getFieldsToExpand()
                                                                        .get(StaConstants.FEATURE_OF_INTEREST)));
        }
        if (observation.getFieldsToExpand().containsKey(StaConstants.DATASTREAM)) {
            observation.setDatastream(toDatastreamDTO(raw.getDataset(),
                                                      observation.getFieldsToExpand().get(StaConstants.DATASTREAM)));

        }
        return observation;
    }

    private ObservedPropertyDTO toObservedPropertyDTO(PhenomenonEntity raw, QueryOptions queryOptions) {
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setAndParseQueryOptions(queryOptions);
        observedProperty.setId(raw.getStaIdentifier());
        observedProperty.setName(raw.getName());
        observedProperty.setDefinition(raw.getIdentifier());
        observedProperty.setProperties(parseProperties(raw));

        if (observedProperty.getFieldsToExpand().containsKey(StaConstants.OBSERVED_PROPERTY)) {
            observedProperty.setDatastreams(raw.getDatasets()
                                                .stream()
                                                .map(e -> toDatastreamDTO(e,
                                                                          observedProperty.getFieldsToExpand()
                                                                              .get(StaConstants.OBSERVED_PROPERTY)))
                                                .collect(Collectors.toSet()));
        }
        return observedProperty;
    }

    private FeatureOfInterestDTO toFeatureOfInterestDTO(StaFeatureEntity raw, QueryOptions queryOptions) {
        FeatureOfInterestDTO featureOfInterest = new FeatureOfInterest();
        featureOfInterest.setAndParseQueryOptions(queryOptions);

        featureOfInterest.setId(raw.getStaIdentifier());
        featureOfInterest.setName(raw.getName());
        featureOfInterest.setFeature(raw.getGeometry());
        featureOfInterest.setDescription(raw.getDescription());
        featureOfInterest.setProperties(parseProperties(raw));
        featureOfInterest.setEncodingType(ENCODINGTYPE_GEOJSON);

        if (featureOfInterest.getFieldsToExpand().containsKey(StaConstants.OBSERVATIONS)) {
            featureOfInterest.setObservations((Set<ObservationDTO>) raw.getObservations()
                .stream()
                .map(e -> toObservationDTO((DataEntity<?>) e,
                                           featureOfInterest.getFieldsToExpand()
                                               .get(StaConstants.OBSERVATIONS)))
                .collect(Collectors.toSet()));
        }
        return featureOfInterest;
    }

    private SensorDTO toSensorDTO(ProcedureEntity raw, QueryOptions queryOptions) {
        SensorDTO sensor = new Sensor();
        sensor.setAndParseQueryOptions(queryOptions);

        sensor.setId(raw.getStaIdentifier());
        sensor.setName(raw.getName());
        sensor.setProperties(parseProperties(raw));
        sensor.setDescription(raw.getDescription());
        sensor.setMetadata(raw.getDescriptionFile());

        String format = raw.getFormat().getFormat();
        if (format.equalsIgnoreCase(SENSORML_2)) {
            format = STA_SENSORML_2;
        }
        sensor.setEncodingType(format);
        if (sensor.getFieldsToExpand().containsKey(StaConstants.DATASTREAMS)) {
            sensor.setDatastreams(raw.getDatasets()
                                      .stream()
                                      .map(e -> toDatastreamDTO(e,
                                                                sensor.getFieldsToExpand()
                                                                    .get(StaConstants.DATASTREAMS)))
                                      .collect(Collectors.toSet()));
        }
        return sensor;
    }

    private HistoricalLocationDTO toHistoricalLocationDTO(HistoricalLocationEntity raw, QueryOptions queryOptions) {
        HistoricalLocationDTO historicalLocation = new HistoricalLocation();
        historicalLocation.setAndParseQueryOptions(queryOptions);

        historicalLocation.setId(raw.getStaIdentifier());
        historicalLocation.setTime(new TimeInstant(raw.getTime()));

        if (historicalLocation.getFieldsToExpand().containsKey(StaConstants.LOCATIONS)) {
            historicalLocation.setLocations(
                raw.getLocations()
                    .stream()
                    .map(e -> toLocationDTO(e, historicalLocation.getFieldsToExpand().get(StaConstants.LOCATIONS)))
                    .collect(Collectors.toSet())
            );
        }

        if (historicalLocation.getFieldsToExpand().containsKey(StaConstants.THING)) {
            historicalLocation.setThing(toThingDTO(raw.getThing(),
                                                   historicalLocation.getFieldsToExpand().get(StaConstants.THING)));
        }
        return historicalLocation;
    }

    private LocationDTO toLocationDTO(LocationEntity raw, QueryOptions queryOptions) {
        LocationDTO location = new Location();
        location.setAndParseQueryOptions(queryOptions);

        location.setId(raw.getStaIdentifier());
        location.setDescription(raw.getDescription());
        location.setGeometry(raw.getGeometry());
        location.setName(raw.getName());
        location.setProperties(parseProperties(raw));

        if (location.getFieldsToExpand().containsKey(StaConstants.HISTORICAL_LOCATIONS)) {
            location.setHistoricalLocations(
                raw.getHistoricalLocations()
                    .stream()
                    .map(e -> toHistoricalLocationDTO(e,
                                                      location.getFieldsToExpand()
                                                          .get(StaConstants.HISTORICAL_LOCATIONS)))
                    .collect(Collectors.toSet()));
        }

        if (location.getFieldsToExpand().containsKey(StaConstants.THINGS)) {
            location.setThings(raw.getThings()
                                   .stream()
                                   .map(e -> toThingDTO(e, location.getFieldsToExpand().get(StaConstants.THINGS)))
                                   .collect(Collectors.toSet()));
        }
        return location;
    }

    public S fromDTO(R type) {
        switch (type.getClass().getSimpleName()) {

            default:
                //TODO: expand error message
                throw new RuntimeException(type.toString());
        }
    }

    private ObjectNode parseProperties(HibernateRelations.HasParameters raw) {

        //        if (includeDatastreamCategory) {
        //            // Add Category to parameters
        //            gen.writeNumberField(categoryPrefix + "Id",
        //                                 datastream.getCategory().getId());
        //            gen.writeStringField(categoryPrefix + "Name",
        //                                 datastream.getCategory().getName());
        //            gen.writeStringField(categoryPrefix + "Description",
        //                                 datastream.getCategory().getDescription());
        //        }
        return MAPPER.createObjectNode();
    }

    @SuppressWarnings("unchecked")
    protected HashSet<ParameterEntity<?>> convertParameters(JsonNode parameters,
                                                            ParameterFactory.EntityType entityType) {
        // parameters
        if (parameters != null) {
            HashSet<ParameterEntity<?>> parameterEntities = new HashSet<>();
            // Check that structure is correct
            Iterator<String> it = parameters.fieldNames();
            while (it.hasNext()) {
                String key = it.next();
                JsonNode value = parameters.get(key);

                ParameterEntity parameterEntity;
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
                        parameterEntity = ParameterFactory.from(entityType, ParameterFactory.ValueType.JSON);
                        parameterEntity.setValue(value.asText());
                        break;
                    case BINARY:
                        // fallthru
                    case BOOLEAN:
                        parameterEntity = ParameterFactory.from(entityType, ParameterFactory.ValueType.BOOLEAN);
                        parameterEntity.setValue(value.asBoolean());
                        break;
                    case NUMBER:
                        parameterEntity = ParameterFactory.from(entityType, ParameterFactory.ValueType.QUANTITY);
                        parameterEntity.setValue(BigDecimal.valueOf(value.asDouble()));
                        break;
                    case STRING:
                        parameterEntity = ParameterFactory.from(entityType, ParameterFactory.ValueType.TEXT);
                        parameterEntity.setValue(value.asText());
                        break;
                    default:
                        throw new RuntimeException("Could not identify value type of parameters!");
                }
                parameterEntity.setName(key);
                parameterEntities.add(parameterEntity);
            }
            return parameterEntities;
        } else {
            return null;
        }
    }


    /*
    public JSONObservation parseParameters(Map<String, String> propertyMapping) {
        if (parameters != null) {
            for (Map.Entry<String, String> mapping : propertyMapping.entrySet()) {
                Iterator<String> keyIt = parameters.fieldNames();
                while (keyIt.hasNext()) {
                    String paramName = keyIt.next();
                    if (paramName.equals(mapping.getValue())) {
                        JsonNode jsonNode = parameters.get(paramName);
                        switch (mapping.getKey()) {
                            case "samplingGeometry":
                                // Add as samplingGeometry to enable interoperability with SOS
                                GeometryFactory factory =
                                    new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
                                GeoJsonReader reader = new GeoJsonReader(factory);
                                try {
                                    GeometryEntity geometryEntity = new GeometryEntity();
                                    geometryEntity.setGeometry(reader.read(jsonNode.toString()));
                                    self.setGeometryEntity(geometryEntity);
                                } catch (ParseException e) {
                                    Assert.notNull(null, "Could not parse" + e.getMessage());
                                }
                                continue;
                            case "verticalFrom":
                                // Add as verticalTo to enable interoperability with SOS
                                self.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            case "verticalTo":
                                // Add as verticalTo to enable interoperability with SOS
                                self.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            case "verticalFromTo":
                                // Add as verticalTo to enable interoperability with SOS
                                self.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                                self.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            default:
                                throw new RuntimeException("Unable to parse Parameters!");
                        }
                    }
                }
            }
        }
        return this;
    }
    */

    private Object parseObservationResult(DataEntity<?> raw) {
        return MAPPER.createObjectNode();
    }
}
