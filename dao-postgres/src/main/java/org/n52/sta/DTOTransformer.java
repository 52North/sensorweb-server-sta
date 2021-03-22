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
import org.joda.time.DateTime;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.BlobDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
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
import org.n52.sta.data.service.ServiceUtils;
import org.n52.sta.utils.TimeUtil;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Translates between STA DTO Entities and Entities used by the dao-postgres module internally
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class DTOTransformer<R extends StaDTO, S extends HibernateRelations.HasId> {

    private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
    private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
    private static final String PDF = "application/pdf";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Map<String, Object> serialized;

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
            case "FeatureEntity": {
                return (R) toFeatureOfInterestDTO(new StaFeatureEntity((FeatureEntity) raw), queryOptions);
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

    public S fromDTO(R type) {
        serialized = new HashMap<>();
        if (type instanceof ThingDTO) {
            return (S) toPlatformEntity((ThingDTO) type);
        } else if (type instanceof DatastreamDTO) {
            return (S) toDatasetEntity((DatastreamDTO) type);
        } else if (type instanceof ObservationDTO) {
            return (S) toDataEntity((ObservationDTO) type);
        } else if (type instanceof HistoricalLocationDTO) {
            return (S) toHistoricalLocationEntity((HistoricalLocationDTO) type);
        } else if (type instanceof SensorDTO) {
            return (S) toProcedureEntity((SensorDTO) type);
        } else if (type instanceof ObservedPropertyDTO) {
            return (S) toPhenomenonEntity((ObservedPropertyDTO) type);
        } else if (type instanceof FeatureOfInterestDTO) {
            return (S) toAbstractFeatureEntity((FeatureOfInterestDTO) type);
        } else if (type instanceof LocationDTO) {
            return (S) toLocationEntity((LocationDTO) type);
        } else {
            throw new STAInvalidQueryError(String.format("Could not parse entity %s to Database Entity!",
                                                         type.getClass().getName()));
        }

    }

    private LocationEntity toLocationEntity(LocationDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (LocationEntity) serialized.get(raw.getId());
        } else {
            LocationEntity location = new LocationEntity();
            serialized.put(raw.getId(), location);
            location.setIdentifier(raw.getId());
            location.setStaIdentifier(raw.getId());
            location.setName(raw.getName());
            location.setDescription(raw.getDescription());
            location.setLocationEncoding(new FormatEntity().setFormat(raw.getEncodingType()));
            location.setGeometry(raw.getGeometry());
            location.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.LOCATION));

            if (raw.getThings() != null) {
                location.setThings(raw.getThings().stream()
                                       .map(this::toPlatformEntity)
                                       .collect(Collectors.toSet()));
            }

            if (raw.getHistoricalLocations() != null) {
                location.setHistoricalLocations(raw.getHistoricalLocations().stream()
                                                    .map(this::toHistoricalLocationEntity)
                                                    .collect(Collectors.toSet()));
            }
            return location;
        }
    }

    private AbstractFeatureEntity<?> toAbstractFeatureEntity(FeatureOfInterestDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (FeatureEntity) serialized.get(raw.getId());
        } else {
            FeatureEntity feature = new FeatureEntity();
            serialized.put(raw.getId(), feature);
            feature.setIdentifier(raw.getId());
            feature.setStaIdentifier(raw.getId());
            feature.setName(raw.getName());
            feature.setDescription(raw.getDescription());
            feature.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.FEATURE));
            if (raw.getFeature() != null) {
                feature.setGeometry(raw.getFeature());
                feature.setFeatureType(ServiceUtils.createFeatureType(raw.getFeature()));
            }
            return feature;
        }
    }

    private PhenomenonEntity toPhenomenonEntity(ObservedPropertyDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (PhenomenonEntity) serialized.get(raw.getId());
        } else {
            PhenomenonEntity phenomenon = new PhenomenonEntity();
            serialized.put(raw.getId(), phenomenon);
            phenomenon.setIdentifier(raw.getDefinition());
            phenomenon.setStaIdentifier(raw.getId());
            phenomenon.setName(raw.getName());
            phenomenon.setDescription(raw.getDescription());
            phenomenon.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PHENOMENON));

            if (raw.getDatastreams() != null) {
                phenomenon.setDatasets(raw.getDatastreams().stream()
                                           .map(this::toDatasetEntity)
                                           .collect(Collectors.toSet()));
            }
            return phenomenon;
        }
    }

    private ProcedureEntity toProcedureEntity(SensorDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (ProcedureEntity) serialized.get(raw.getId());
        } else {
            ProcedureEntity procedure = new ProcedureEntity();
            serialized.put(raw.getId(), procedure);
            procedure.setIdentifier(raw.getId());
            procedure.setStaIdentifier(raw.getId());
            procedure.setName(raw.getName());
            procedure.setDescription(raw.getDescription());
            procedure.setFormat(new FormatEntity().setFormat(raw.getEncodingType()));
            procedure.setDescriptionFile(raw.getMetadata());
            procedure.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PROCEDURE));

            if (raw.getEncodingType().equalsIgnoreCase(STA_SENSORML_2)) {
                procedure.setFormat(new FormatEntity().setFormat(SENSORML_2));
                ProcedureHistoryEntity procedureHistoryEntity = new ProcedureHistoryEntity();
                procedureHistoryEntity.setProcedure(procedure);
                procedureHistoryEntity.setFormat(procedure.getFormat());
                procedureHistoryEntity.setStartTime(DateTime.now().toDate());
                procedureHistoryEntity.setXml(raw.getMetadata());
                Set<ProcedureHistoryEntity> set = new LinkedHashSet<>();
                set.add(procedureHistoryEntity);
                procedure.setProcedureHistory(set);
            } else if (raw.getEncodingType().equalsIgnoreCase(PDF)) {
                procedure.setFormat(new FormatEntity().setFormat(PDF));
                procedure.setDescriptionFile(raw.getMetadata());
            }
            procedure.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PROCEDURE));

            if (raw.getDatastreams() != null) {
                procedure.setDatasets(raw.getDatastreams().stream()
                                          .map(this::toDatasetEntity)
                                          .collect(Collectors.toSet()));
            }
            return procedure;
        }
    }

    private HistoricalLocationEntity toHistoricalLocationEntity(HistoricalLocationDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (HistoricalLocationEntity) serialized.get(raw.getId());
        } else {
            HistoricalLocationEntity histLoc = new HistoricalLocationEntity();
            serialized.put(raw.getId(), histLoc);
            histLoc.setIdentifier(raw.getId());
            histLoc.setStaIdentifier(raw.getId());

            Time parsed = TimeUtil.parseTime(raw.getTime());
            if (parsed instanceof TimeInstant) {
                histLoc.setTime(((TimeInstant) parsed).getValue().toDate());
            } else if (parsed instanceof TimePeriod) {
                histLoc.setTime(((TimePeriod) parsed).getEnd().toDate());
            }

            if (raw.getThing() != null) {
                histLoc.setThing(this.toPlatformEntity(raw.getThing()));
            }

            if (raw.getLocations() != null) {
                histLoc.setLocations(raw.getLocations().stream()
                                         .map(this::toLocationEntity)
                                         .collect(Collectors.toSet()));
            }
            return histLoc;
        }
    }

    private DataEntity<?> toDataEntity(ObservationDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (BlobDataEntity) serialized.get(raw.getId());
        } else {
            BlobDataEntity dataEntity = new BlobDataEntity();
            serialized.put(raw.getId(), dataEntity);
            dataEntity.setIdentifier(raw.getId());
            dataEntity.setStaIdentifier(raw.getId());

            Time time = raw.getPhenomenonTime();
            if (time instanceof TimeInstant) {
                dataEntity.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                dataEntity.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                dataEntity.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                dataEntity.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
            }

            dataEntity.setValue(raw.getResult());
            dataEntity.setDataset(this.toDatasetEntity(raw.getDatastream()));

            return dataEntity;
        }
    }

    private DatasetEntity toDatasetEntity(DatastreamDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (DatasetEntity) serialized.get(raw.getId());
        } else {
            DatasetEntity dataset = new DatasetEntity();
            serialized.put(raw.getId(), dataset);
            dataset.setIdentifier(raw.getId());
            dataset.setStaIdentifier(raw.getId());

            dataset.setName(raw.getName());
            dataset.setDescription(raw.getDescription());
            dataset.setOMObservationType(new FormatEntity().setFormat(raw.getObservationType()));

            if (raw.getUnitOfMeasurement() != null) {
                UnitEntity unit = new UnitEntity();
                unit.setLink(raw.getUnitOfMeasurement().getDefinition());
                unit.setName(raw.getUnitOfMeasurement().getName());
                unit.setSymbol(raw.getUnitOfMeasurement().getSymbol());
                dataset.setUnit(unit);
            }

            Time time = TimeUtil.parseTime(raw.getResultTime());
            if (time instanceof TimeInstant) {
                dataset.setResultTimeStart(((TimeInstant) time).getValue().toDate());
                dataset.setResultTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                dataset.setResultTimeStart(((TimePeriod) time).getStart().toDate());
                dataset.setResultTimeEnd(((TimePeriod) time).getEnd().toDate());
            }

            dataset.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.DATASET));

            if (raw.getThing() != null) {
                dataset.setThing(toPlatformEntity(raw.getThing()));
            }
            if (raw.getObservedProperty() != null) {
                dataset.setObservableProperty(toPhenomenonEntity(raw.getObservedProperty()));
            }
            if (raw.getSensor() != null) {
                dataset.setProcedure(toProcedureEntity(raw.getSensor()));
            }
            if (raw.getObservations() != null) {
                dataset.setObservations(raw.getObservations()
                                            .stream()
                                            .map(this::toDataEntity)
                                            .collect(Collectors.toSet()));
            }

            return dataset;
        }
    }

    private PlatformEntity toPlatformEntity(ThingDTO raw) {
        if (serialized.containsKey(raw.getId())) {
            return (PlatformEntity) serialized.get(raw.getId());
        } else {
            PlatformEntity platform = new PlatformEntity();
            serialized.put(raw.getId(), platform);
            platform.setIdentifier(raw.getId());
            platform.setStaIdentifier(raw.getId());
            platform.setName(raw.getName());
            platform.setDescription(raw.getDescription());

            platform.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PLATFORM));

            if (raw.getDatastream() != null) {
                platform.setDatasets(raw.getDatastream()
                                         .stream()
                                         .map(this::toDatasetEntity)
                                         .collect(Collectors.toSet()));
            }

            if (raw.getLocations() != null) {
                platform.setLocations(raw.getLocations()
                                          .stream()
                                          .map(this::toLocationEntity)
                                          .collect(Collectors.toSet()));
            }

            if (raw.getHistoricalLocations() != null) {
                platform.setHistoricalLocations(raw.getHistoricalLocations()
                                                    .stream()
                                                    .map(this::toHistoricalLocationEntity)
                                                    .collect(Collectors.toSet()));
            }
            return platform;
        }

    }

    private DatastreamDTO toDatastreamDTO(AbstractDatasetEntity raw, QueryOptions queryOptions) {
        DatastreamDTO datastream = new Datastream();
        datastream.setAndParseQueryOptions(queryOptions);

        datastream.setId(raw.getStaIdentifier());
        datastream.setName(raw.getName());
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
            raw.getUnit().getLink())
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
        if (raw.getValidTimeStart() != null) {
            observation.setValidTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getValidTimeStart()),
                                                         TimeUtil.createDateTime(raw.getValidTimeStart())));
        }
        if (raw.getResultTime() != null) {
            observation.setResultTime(new TimeInstant(raw.getResultTime()));
        }

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
        observedProperty.setDescription(raw.getDescription());
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

        String format = raw.getFormat().getFormat();
        if (format.equalsIgnoreCase(SENSORML_2)) {
            format = STA_SENSORML_2;
        }
        sensor.setEncodingType(format);

        String metadata = "";
        if (raw.getDescriptionFile() != null && !raw.getDescriptionFile().isEmpty()) {
            metadata = raw.getDescriptionFile();
        } else if (raw.hasProcedureHistory()) {
            Optional<ProcedureHistoryEntity> history =
                raw.getProcedureHistory().stream().filter(h -> h.getEndTime() == null).findFirst();
            if (history.isPresent()) {
                metadata = history.get().getXml();
            }
        }
        sensor.setMetadata(metadata);

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
    protected HashSet<ParameterEntity<?>> convertParameters(ObjectNode parameters,
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

    public void parseObservationParameters(DataEntity dataEntity,
                                           ObservationDTO dto,
                                           Map<String, String> propertyMapping) {
        if (dto.getParameters() != null) {
            for (Map.Entry<String, String> mapping : propertyMapping.entrySet()) {
                Iterator<String> keyIt = dto.getParameters().fieldNames();
                while (keyIt.hasNext()) {
                    String paramName = keyIt.next();
                    if (paramName.equals(mapping.getValue())) {
                        JsonNode jsonNode = dto.getParameters().get(paramName);
                        switch (mapping.getKey()) {
                            case "samplingGeometry":
                                // Add as samplingGeometry to enable interoperability with SOS
                                GeometryFactory factory =
                                    new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
                                GeoJsonReader reader = new GeoJsonReader(factory);
                                try {
                                    GeometryEntity geometryEntity = new GeometryEntity();
                                    geometryEntity.setGeometry(reader.read(jsonNode.toString()));
                                    dataEntity.setGeometryEntity(geometryEntity);
                                } catch (ParseException e) {
                                    Assert.notNull(null, "Could not parse" + e.getMessage());
                                }
                                continue;
                            case "verticalFrom":
                                // Add as verticalTo to enable interoperability with SOS
                                dataEntity.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            case "verticalTo":
                                // Add as verticalTo to enable interoperability with SOS
                                dataEntity.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            case "verticalFromTo":
                                // Add as verticalTo to enable interoperability with SOS
                                dataEntity.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                                dataEntity.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                                continue;
                            default:
                                throw new RuntimeException("Unable to parse Parameters!");
                        }
                    }
                }
            }
        }
    }

    private Object parseObservationResult(DataEntity<?> raw) {
        if (raw instanceof QuantityDataEntity) {
            return raw.getValueQuantity();
        }
        //TODO:
        // Handling of Profile/TrajectoryObservation
        return MAPPER.createObjectNode();
    }
}
