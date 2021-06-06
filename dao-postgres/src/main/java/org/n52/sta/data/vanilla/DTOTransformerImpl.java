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

package org.n52.sta.data.vanilla;

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
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservationGroupEntity;
import org.n52.series.db.beans.sta.ObservationRelationEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
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
import org.n52.sta.api.dto.LicenseDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.PartyDTO;
import org.n52.sta.api.dto.ProjectDTO;
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
import org.n52.sta.api.dto.impl.citsci.License;
import org.n52.sta.api.dto.impl.citsci.ObservationGroup;
import org.n52.sta.api.dto.impl.citsci.ObservationRelation;
import org.n52.sta.api.dto.impl.citsci.Party;
import org.n52.sta.api.dto.impl.citsci.Project;
import org.n52.sta.data.vanilla.service.ServiceUtils;
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

// import org.n52.series.db.beans.SensorML20DataEntity;


/**
 * Translates between STA DTO Entities and Entities used by the dao-postgres module internally
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class DTOTransformerImpl<R extends StaDTO, S extends HibernateRelations.HasId> implements DTOTransformer<R, S> {

    protected static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
    protected static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    protected static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
    protected static final String PDF = "application/pdf";
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected final SerDesConfig config;
    protected Map<String, Object> serialized;

    public DTOTransformerImpl(SerDesConfig config) {
        this.config = config;
    }

    protected static String genKey(StaDTO dto) {
        return dto.getId() + dto.getClass().getSimpleName();
    }

    @Override
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
            case "LicenseEntity": {
                return (R) toLicenseDTO((LicenseEntity) raw, queryOptions);
            }
            case "ObservationRelationEntity": {
                return (R) toObsRelDTO((ObservationRelationEntity) raw, queryOptions);
            }
            case "ObservationGroupEntity": {
                return (R) toObsGroupDTO((ObservationGroupEntity) raw, queryOptions);
            }
            case "PartyEntity": {
                return (R) toPartyDTO((PartyEntity) raw, queryOptions);
            }
            case "ProjectEntity": {
                return (R) toProjectDTO((ProjectEntity) raw, queryOptions);
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

    @Override public S fromDTO(R type) {
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
        } else if (type instanceof LicenseDTO) {
            return (S) toLicenseEntity((LicenseDTO) type);
        } else if (type instanceof ObservationRelationDTO) {
            return (S) toObservationRelationEntity((ObservationRelationDTO) type);
        } else if (type instanceof ObservationGroupDTO) {
            return (S) toObservationGroupEntity((ObservationGroupDTO) type);
        } else if (type instanceof PartyDTO) {
            return (S) toPartyEntity((PartyDTO) type);
        } else if (type instanceof ProjectDTO) {
            return (S) toProjectEntity((ProjectDTO) type);
        } else {
            throw new STAInvalidQueryError(String.format("Could not parse entity %s to Database Entity!",
                                                         type.getClass().getName()));
        }
    }

    protected LocationEntity toLocationEntity(LocationDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (LocationEntity) serialized.get(genKey(raw));
        } else {
            LocationEntity location = createLocationEntity(raw);
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

    protected LocationEntity createLocationEntity(LocationDTO raw) {
        LocationEntity location = new LocationEntity();
        serialized.put(genKey(raw), location);
        location.setIdentifier(raw.getId());
        location.setStaIdentifier(raw.getId());
        location.setName(raw.getName());
        location.setDescription(raw.getDescription());
        location.setLocationEncoding(new FormatEntity().setFormat(raw.getEncodingType()));
        if (raw.getGeometry() != null) {
            location.setGeometry(raw.getGeometry());
        }
        location.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.LOCATION));
        return location;
    }

    protected AbstractFeatureEntity<?> toAbstractFeatureEntity(FeatureOfInterestDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (FeatureEntity) serialized.get(genKey(raw));
        } else {
            return createFeatureEntity(raw);
        }
    }

    protected FeatureEntity createFeatureEntity(FeatureOfInterestDTO raw) {
        FeatureEntity feature = new FeatureEntity();
        serialized.put(genKey(raw), feature);
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

    protected PhenomenonEntity toPhenomenonEntity(ObservedPropertyDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (PhenomenonEntity) serialized.get(genKey(raw));
        } else {
            PhenomenonEntity phenomenon = createPhenomenonEntity(raw);

            if (raw.getDatastreams() != null) {
                phenomenon.setDatasets(raw.getDatastreams().stream()
                                           .map(this::toDatasetEntity)
                                           .collect(Collectors.toSet()));
            }
            return phenomenon;
        }
    }

    protected PhenomenonEntity createPhenomenonEntity(ObservedPropertyDTO raw) {
        PhenomenonEntity phenomenon = new PhenomenonEntity();
        serialized.put(genKey(raw), phenomenon);
        phenomenon.setIdentifier(raw.getDefinition());
        phenomenon.setStaIdentifier(raw.getId());
        phenomenon.setName(raw.getName());
        phenomenon.setDescription(raw.getDescription());
        phenomenon.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PHENOMENON));
        return phenomenon;
    }

    protected ProcedureEntity toProcedureEntity(SensorDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (ProcedureEntity) serialized.get(genKey(raw));
        } else {
            ProcedureEntity procedure = createProcedureEntity(raw);

            if (raw.getDatastreams() != null) {
                procedure.setDatasets(raw.getDatastreams().stream()
                                          .map(this::toDatasetEntity)
                                          .collect(Collectors.toSet()));
            }
            return procedure;
        }
    }

    protected ProcedureEntity createProcedureEntity(SensorDTO raw) {
        ProcedureEntity procedure = new ProcedureEntity();
        serialized.put(genKey(raw), procedure);
        procedure.setIdentifier(raw.getId());
        procedure.setStaIdentifier(raw.getId());
        procedure.setName(raw.getName());
        procedure.setDescription(raw.getDescription());
        if (raw.getEncodingType() != null) {
            procedure.setFormat(new FormatEntity().setFormat(raw.getEncodingType()));
        }
        procedure.setDescriptionFile(raw.getMetadata());
        procedure.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PROCEDURE));

        if (raw.getEncodingType() != null) {
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
        }
        procedure.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PROCEDURE));
        return procedure;
    }

    protected HistoricalLocationEntity toHistoricalLocationEntity(HistoricalLocationDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (HistoricalLocationEntity) serialized.get(genKey(raw));
        } else {
            HistoricalLocationEntity histLoc = createHistoricalLocationEntity(raw);

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

    protected HistoricalLocationEntity createHistoricalLocationEntity(HistoricalLocationDTO raw) {
        HistoricalLocationEntity histLoc = new HistoricalLocationEntity();
        serialized.put(genKey(raw), histLoc);
        histLoc.setIdentifier(raw.getId());
        histLoc.setStaIdentifier(raw.getId());

        Time parsed = raw.getTime();
        if (parsed instanceof TimeInstant) {
            histLoc.setTime(((TimeInstant) parsed).getValue().toDate());
        } else if (parsed instanceof TimePeriod) {
            histLoc.setTime(((TimePeriod) parsed).getEnd().toDate());
        }
        return histLoc;
    }

    protected DataEntity<?> toDataEntity(ObservationDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (BlobDataEntity) serialized.get(genKey(raw));
        } else {
            BlobDataEntity dataEntity = createBlobDataEntity(raw);

            if (raw.getLicense() != null) {
                dataEntity.setLicense(toLicenseEntity(raw.getLicense()));
            }

            if (raw.getSubjects() != null) {
                dataEntity.setSubjects(raw.getSubjects()
                                           .stream()
                                           .map(this::toObservationRelationEntity)
                                           .collect(Collectors.toSet()));
            }

            if (raw.getObjects() != null) {
                dataEntity.setObjects(raw.getObjects()
                                          .stream()
                                          .map(this::toObservationRelationEntity)
                                          .collect(Collectors.toSet()));
            }

            if (raw.getObservationGroups() != null) {
                dataEntity.setObservationGroups(raw.getObservationGroups()
                                                    .stream()
                                                    .map(this::toObservationGroupEntity)
                                                    .collect(Collectors.toSet()));
            }
            if (raw.getDatastream() != null) {
                dataEntity.setDataset(this.toDatasetEntity(raw.getDatastream()));
            }
            if (raw.getFeatureOfInterest() != null) {
                dataEntity.setFeature(this.toAbstractFeatureEntity(raw.getFeatureOfInterest()));
            }
            return dataEntity;
        }
    }

    protected BlobDataEntity createBlobDataEntity(ObservationDTO raw) {
        BlobDataEntity dataEntity = new BlobDataEntity();
        serialized.put(genKey(raw), dataEntity);
        dataEntity.setIdentifier(raw.getId());
        dataEntity.setStaIdentifier(raw.getId());

        Time phenomenonTime = raw.getPhenomenonTime();
        if (phenomenonTime instanceof TimeInstant) {
            dataEntity.setSamplingTimeStart(((TimeInstant) phenomenonTime).getValue().toDate());
            dataEntity.setSamplingTimeEnd(((TimeInstant) phenomenonTime).getValue().toDate());
        } else if (phenomenonTime instanceof TimePeriod) {
            dataEntity.setSamplingTimeStart(((TimePeriod) phenomenonTime).getStart().toDate());
            dataEntity.setSamplingTimeEnd(((TimePeriod) phenomenonTime).getEnd().toDate());
        }

        if (raw.getResultTime() != null) {
            dataEntity.setResultTime(((TimeInstant) raw.getResultTime()).getValue().toDate());
        }

        Time validTime = raw.getValidTime();
        if (validTime instanceof TimeInstant) {
            dataEntity.setValidTimeStart(((TimeInstant) validTime).getValue().toDate());
            dataEntity.setValidTimeEnd(((TimeInstant) validTime).getValue().toDate());
        } else if (validTime instanceof TimePeriod) {
            dataEntity.setValidTimeStart(((TimePeriod) validTime).getStart().toDate());
            dataEntity.setValidTimeEnd(((TimePeriod) validTime).getEnd().toDate());
        }

        // if (raw.getResult() instanceof ObjectNode)
        if (raw.getResult() != null) {
            dataEntity.setValue(raw.getResult().toString());
        }

        parseObservationParameters(dataEntity, raw);
        return dataEntity;
    }

    protected DatasetEntity toDatasetEntity(DatastreamDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (DatasetEntity) serialized.get(genKey(raw));
        } else {
            DatasetEntity dataset = createDatasetEntity(raw);

            if (raw.getThing() != null) {
                dataset.setThing(toPlatformEntity(raw.getThing()));
            }
            if (raw.getObservedProperty() != null) {
                dataset.setObservableProperty(toPhenomenonEntity(raw.getObservedProperty()));
            }
            if (raw.getSensor() != null) {
                dataset.setProcedure(toProcedureEntity(raw.getSensor()));
            }
            if (raw.getParty() != null) {
                dataset.setParty(toPartyEntity(raw.getParty()));
            }
            if (raw.getProject() != null) {
                dataset.setProject(toProjectEntity(raw.getProject()));
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

    protected DatasetEntity createDatasetEntity(DatastreamDTO raw) {
        DatasetEntity dataset = new DatasetEntity();
        serialized.put(genKey(raw), dataset);
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
        return dataset;
    }

    protected PlatformEntity toPlatformEntity(ThingDTO raw) {
        if (serialized.containsKey(genKey(raw))) {
            return (PlatformEntity) serialized.get(genKey(raw));
        } else {
            PlatformEntity platform = createPlatformEntity(raw);

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

    protected PlatformEntity createPlatformEntity(ThingDTO raw) {
        PlatformEntity platform = new PlatformEntity();
        serialized.put(genKey(raw), platform);
        platform.setIdentifier(raw.getId());
        platform.setStaIdentifier(raw.getId());
        platform.setName(raw.getName());
        platform.setDescription(raw.getDescription());

        platform.setParameters(convertParameters(raw.getProperties(), ParameterFactory.EntityType.PLATFORM));
        return platform;
    }

    protected DatastreamDTO toDatastreamDTO(AbstractDatasetEntity raw, QueryOptions queryOptions) {
        DatastreamDTO datastream = createDatastreamDTO(new Datastream(), raw, queryOptions);

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

        if (datastream.getFieldsToExpand().containsKey(StaConstants.PARTY)) {
            datastream.setParty(
                toPartyDTO(raw.getParty(),
                           datastream.getFieldsToExpand().get(StaConstants.PARTY)));
        }
        if (datastream.getFieldsToExpand().containsKey(StaConstants.PROJECT)) {
            datastream.setProject(
                toProjectDTO(raw.getProject(),
                             datastream.getFieldsToExpand().get(StaConstants.PROJECT)));
        }
        return datastream;
    }

    protected <T extends DatastreamDTO> T createDatastreamDTO(T datastream,
                                                              AbstractDatasetEntity raw,
                                                              QueryOptions queryOptions) {
        datastream.setAndParseQueryOptions(queryOptions);

        datastream.setId(raw.getStaIdentifier());
        datastream.setName(raw.getName());

        if (raw.getSamplingTimeStart() != null) {
            datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getSamplingTimeStart()),
                                                             TimeUtil.createDateTime(raw.getSamplingTimeEnd())));
        }
        datastream.setDescription(raw.getDescription());
        datastream.setObservedArea(raw.getGeometry());
        datastream.setObservationType(raw.getOMObservationType().getFormat());
        datastream.setProperties(parseProperties(raw));

        if (raw.getResultTimeStart() != null) {
            datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getResultTimeStart()),
                                                         TimeUtil.createDateTime(raw.getResultTimeEnd())));
        }

        datastream.setUnitOfMeasurement(new DatastreamDTO.UnitOfMeasurement(
            raw.getUnit().getSymbol(),
            raw.getUnit().getName(),
            raw.getUnit().getLink())
        );
        return datastream;
    }

    protected ThingDTO toThingDTO(PlatformEntity platform, QueryOptions queryOptions) {
        ThingDTO thing = createThingDTO(platform, queryOptions);

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

    protected ThingDTO createThingDTO(PlatformEntity platform, QueryOptions queryOptions) {
        ThingDTO thing = new Thing();
        thing.setAndParseQueryOptions(queryOptions);
        thing.setId(platform.getStaIdentifier());
        thing.setName(platform.getName());
        thing.setDescription(platform.getDescription());
        thing.setProperties(parseProperties(platform));
        return thing;
    }

    protected ObservationDTO toObservationDTO(DataEntity<?> raw, QueryOptions queryOptions) {
        ObservationDTO observation = createObservationDTO(new Observation(), raw, queryOptions);

        if (observation.getFieldsToExpand().containsKey(StaConstants.FEATURE_OF_INTEREST)) {
            observation.setFeatureOfInterest(toFeatureOfInterestDTO(raw.getFeature(),
                                                                    observation.getFieldsToExpand()
                                                                        .get(StaConstants.FEATURE_OF_INTEREST)));
        }
        if (observation.getFieldsToExpand().containsKey(StaConstants.DATASTREAM)) {
            observation.setDatastream(toDatastreamDTO(raw.getDataset(),
                                                      observation.getFieldsToExpand().get(StaConstants.DATASTREAM)));

        }
        if (observation.getFieldsToExpand().containsKey(StaConstants.NAV_SUBJECTS)) {
            observation.setSubjects(raw.getSubjects()
                                        .stream()
                                        .map(o -> this.toObsRelDTO(o,
                                                                   observation.getFieldsToExpand()
                                                                       .get(StaConstants.NAV_SUBJECTS)))
                                        .collect(Collectors.toSet()));

        }
        if (observation.getFieldsToExpand().containsKey(StaConstants.NAV_OBJECTS)) {
            observation.setSubjects(raw.getObjects()
                                        .stream()
                                        .map(o -> this.toObsRelDTO(o,
                                                                   observation.getFieldsToExpand()
                                                                       .get(StaConstants.NAV_OBJECTS)))
                                        .collect(Collectors.toSet()));

        }
        if (observation.getFieldsToExpand().containsKey(StaConstants.OBSERVATION_GROUPS)) {
            observation.setObservationGroups(
                raw.getObservationGroups()
                    .stream()
                    .map(o -> this.toObsGroupDTO(o,
                                                 observation.getFieldsToExpand()
                                                     .get(StaConstants.OBSERVATION_GROUPS)))
                    .collect(Collectors.toSet()));

        }
        return observation;
    }

    protected <T extends ObservationDTO> T createObservationDTO(T observation,
                                                                DataEntity<?> raw,
                                                                QueryOptions queryOptions) {
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
        return observation;
    }

    protected ObservedPropertyDTO toObservedPropertyDTO(PhenomenonEntity raw, QueryOptions queryOptions) {
        ObservedPropertyDTO observedProperty = createObservedPropertyDTO(raw, queryOptions);

        if (observedProperty.getFieldsToExpand().containsKey(StaConstants.DATASTREAMS)) {
            observedProperty.setDatastreams(raw.getDatasets()
                                                .stream()
                                                .map(e -> toDatastreamDTO(e,
                                                                          observedProperty.getFieldsToExpand()
                                                                              .get(StaConstants.DATASTREAMS)))
                                                .collect(Collectors.toSet()));
        }
        return observedProperty;
    }

    protected ObservedPropertyDTO createObservedPropertyDTO(PhenomenonEntity raw, QueryOptions queryOptions) {
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setAndParseQueryOptions(queryOptions);
        observedProperty.setId(raw.getStaIdentifier());
        observedProperty.setName(raw.getName());
        observedProperty.setDescription(raw.getDescription());
        observedProperty.setDefinition(raw.getIdentifier());
        observedProperty.setProperties(parseProperties(raw));
        return observedProperty;
    }

    protected FeatureOfInterestDTO toFeatureOfInterestDTO(AbstractFeatureEntity raw, QueryOptions queryOptions) {
        FeatureOfInterestDTO featureOfInterest = createFeatureOfInterestDTO(raw, queryOptions);

        if (featureOfInterest.getFieldsToExpand().containsKey(StaConstants.OBSERVATIONS)) {
            if (raw instanceof StaFeatureEntity) {
                featureOfInterest.setObservations((Set<ObservationDTO>) ((StaFeatureEntity) raw).getObservations()
                    .stream()
                    .map(e -> toObservationDTO((DataEntity<?>) e,
                                               featureOfInterest.getFieldsToExpand()
                                                   .get(StaConstants.OBSERVATIONS)))
                    .collect(Collectors.toSet()));
            }
        }
        return featureOfInterest;
    }

    protected FeatureOfInterestDTO createFeatureOfInterestDTO(AbstractFeatureEntity raw, QueryOptions queryOptions) {
        FeatureOfInterestDTO featureOfInterest = new FeatureOfInterest();
        featureOfInterest.setAndParseQueryOptions(queryOptions);

        featureOfInterest.setId(raw.getStaIdentifier());
        featureOfInterest.setName(raw.getName());
        featureOfInterest.setFeature(raw.getGeometry());
        featureOfInterest.setDescription(raw.getDescription());
        featureOfInterest.setProperties(parseProperties(raw));
        featureOfInterest.setEncodingType(ENCODINGTYPE_GEOJSON);
        return featureOfInterest;
    }

    protected SensorDTO toSensorDTO(ProcedureEntity raw, QueryOptions queryOptions) {
        SensorDTO sensor = createSensorDTO(raw, queryOptions);

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

    protected SensorDTO createSensorDTO(ProcedureEntity raw, QueryOptions queryOptions) {
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
        return sensor;
    }

    protected HistoricalLocationDTO toHistoricalLocationDTO(HistoricalLocationEntity raw, QueryOptions queryOptions) {
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

    protected LocationDTO toLocationDTO(LocationEntity raw, QueryOptions queryOptions) {
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

    private ProjectEntity toProjectEntity(ProjectDTO dto) {
        ProjectEntity project = new ProjectEntity();
        project.setStaIdentifier(dto.getId());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setClassification(dto.getClassification());
        project.setTermsOfUse(dto.getTermsOfUse());
        project.setPrivacyPolicy(dto.getPrivacyPolicy());

        Time created = dto.getCreated();
        if (created instanceof TimeInstant) {
            project.setCreatedStart(((TimeInstant) created).getValue().toDate());
            project.setCreatedEnd(((TimeInstant) created).getValue().toDate());
        } else if (created instanceof TimePeriod) {
            project.setCreatedStart(((TimePeriod) created).getStart().toDate());
            project.setCreatedEnd(((TimePeriod) created).getEnd().toDate());
        }

        if (dto.getRuntime() != null) {
            Time runtime = dto.getRuntime();
            if (runtime instanceof TimeInstant) {
                project.setRuntimeStart(((TimeInstant) runtime).getValue().toDate());
                project.setRuntimeEnd(((TimeInstant) runtime).getValue().toDate());
            } else if (runtime instanceof TimePeriod) {
                project.setRuntimeStart(((TimePeriod) runtime).getStart().toDate());
                project.setRuntimeEnd(((TimePeriod) runtime).getEnd().toDate());
            }
        }
        if (dto.getDatastreams() != null) {
            project.setDatasets(dto.getDatastreams()
                                    .stream()
                                    .map(this::toDatasetEntity)
                                    .collect(Collectors.toSet()));
        }
        return project;
    }

    private PartyEntity toPartyEntity(PartyDTO dto) {
        PartyEntity party = new PartyEntity();
        party.setStaIdentifier(dto.getId());
        party.setAuthId(dto.getAuthId());
        party.setNickname(dto.getNickname());
        party.setDescription(dto.getDescription());

        if (dto.getRole() != null) {
            party.setRole(PartyEntity.Role.valueOf(dto.getRole().name()));
        }
        if (dto.getDatastreams() != null) {
            party.setDatasets(dto.getDatastreams()
                                  .stream()
                                  .map(this::toDatasetEntity)
                                  .collect(Collectors.toSet()));
        }
        return party;
    }

    private ProjectDTO toProjectDTO(ProjectEntity raw, QueryOptions queryOptions) {
        ProjectDTO dto = new Project();
        dto.setAndParseQueryOptions(queryOptions);

        dto.setId(raw.getStaIdentifier());
        dto.setName(raw.getName());
        dto.setDescription(raw.getDescription());
        dto.setClassification(raw.getClassification());
        dto.setTermsOfUse(raw.getTermsOfUse());
        dto.setPrivacyPolicy(raw.getPrivacyPolicy());
        if (raw.getCreatedStart() != null) {
            dto.setCreated(TimeUtil.createTime(TimeUtil.createDateTime(raw.getCreatedStart()),
                                               TimeUtil.createDateTime(raw.getCreatedEnd())));
        }
        if (raw.getRuntimeStart() != null) {
            dto.setRuntime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getRuntimeStart()),
                                               TimeUtil.createDateTime(raw.getRuntimeEnd())));
        }
        dto.setUrl(raw.getUrl());

        if (dto.getFieldsToExpand().containsKey(StaConstants.DATASTREAMS)) {
            dto.setDatastreams(raw.getDatasets()
                                   .stream()
                                   .map(o -> toDatastreamDTO(o,
                                                             dto.getFieldsToExpand()
                                                                 .get(StaConstants.DATASTREAMS)))
                                   .collect(Collectors.toSet()));
        }
        return dto;
    }

    private PartyDTO toPartyDTO(PartyEntity raw, QueryOptions queryOptions) {
        PartyDTO dto = new Party();
        dto.setAndParseQueryOptions(queryOptions);

        dto.setId(raw.getStaIdentifier());
        dto.setAuthId(raw.getAuthId());
        dto.setNickname(raw.getNickname());
        dto.setDescription(raw.getDescription());
        dto.setRole(PartyDTO.Role.valueOf(raw.getRole().name()));
        dto.setProperties(parseProperties(raw));

        if (dto.getFieldsToExpand().containsKey(StaConstants.DATASTREAMS)) {
            dto.setDatastreams(raw.getDatasets()
                                   .stream()
                                   .map(o -> toDatastreamDTO(o,
                                                             dto.getFieldsToExpand()
                                                                 .get(StaConstants.DATASTREAMS)))
                                   .collect(Collectors.toSet()));
        }

        return dto;
    }

    private ObservationGroupDTO toObsGroupDTO(ObservationGroupEntity raw, QueryOptions queryOptions) {
        ObservationGroupDTO dto = new ObservationGroup();
        dto.setAndParseQueryOptions(queryOptions);

        dto.setId(raw.getStaIdentifier());
        dto.setName(raw.getName());
        dto.setDescription(raw.getDescription());
        dto.setPurpose(raw.getPurpose());
        if (raw.getRuntimeStart() != null) {
            dto.setRuntime(TimeUtil.createTime(TimeUtil.createDateTime(raw.getRuntimeStart()),
                                               TimeUtil.createDateTime(raw.getRuntimeEnd())));
        }
        if (raw.getCreatedStart() != null) {
            dto.setCreated(TimeUtil.createTime(TimeUtil.createDateTime(raw.getCreatedStart()),
                                               TimeUtil.createDateTime(raw.getCreatedEnd())));
        }

        dto.setProperties(parseProperties(raw));
        return dto;
    }

    private ObservationRelationDTO toObsRelDTO(ObservationRelationEntity raw, QueryOptions queryOptions) {
        ObservationRelationDTO dto = new ObservationRelation();
        dto.setAndParseQueryOptions(queryOptions);

        dto.setId(raw.getStaIdentifier());
        dto.setRole(raw.getRole());
        dto.setNamespace(raw.getNamespace());
        dto.setDescription(raw.getDescription());

        if (dto.getFieldsToExpand().containsKey(StaConstants.NAV_SUBJECT)) {
            dto.setSubject(toObservationDTO(raw.getSubject(),
                                            dto.getFieldsToExpand().get(StaConstants.NAV_OBJECT)));
        }

        if (dto.getFieldsToExpand().containsKey(StaConstants.NAV_OBJECT)) {
            dto.setObject(toObservationDTO(raw.getObject(),
                                           dto.getFieldsToExpand().get(StaConstants.NAV_OBJECT)));
        }

        if (dto.getFieldsToExpand().containsKey(StaConstants.OBSERVATION_GROUPS)) {
            dto.setObservationGroups(raw.getObservationGroups()
                                         .stream()
                                         .map(o -> toObsGroupDTO(o,
                                                                 dto.getFieldsToExpand()
                                                                     .get(StaConstants.OBSERVATION_GROUPS)))
                                         .collect(Collectors.toSet()));
        }
        return dto;
    }

    private ObservationGroupEntity toObservationGroupEntity(ObservationGroupDTO dto) {
        ObservationGroupEntity obsGroup = new ObservationGroupEntity();
        obsGroup.setStaIdentifier(dto.getId());
        obsGroup.setName(dto.getName());
        obsGroup.setDescription(dto.getDescription());
        obsGroup.setPurpose(dto.getPurpose());
        // obsGroup.setParameters();

        Time runtime = dto.getRuntime();
        if (runtime instanceof TimeInstant) {
            obsGroup.setRuntimeStart(((TimeInstant) runtime).getValue().toDate());
            obsGroup.setRuntimeEnd(((TimeInstant) runtime).getValue().toDate());
        } else if (runtime instanceof TimePeriod) {
            obsGroup.setRuntimeStart(((TimePeriod) runtime).getStart().toDate());
            obsGroup.setRuntimeEnd(((TimePeriod) runtime).getEnd().toDate());
        }
        Time created = dto.getCreated();
        if (created instanceof TimeInstant) {
            obsGroup.setCreatedStart(((TimeInstant) created).getValue().toDate());
            obsGroup.setCreatedEnd(((TimeInstant) created).getValue().toDate());
        } else if (created instanceof TimePeriod) {
            obsGroup.setCreatedStart(((TimePeriod) created).getStart().toDate());
            obsGroup.setCreatedEnd(((TimePeriod) created).getEnd().toDate());
        }

        if (dto.getLicense() != null) {
            obsGroup.setLicense(toLicenseEntity(dto.getLicense()));
        }

        if (dto.getObservations() != null) {
            obsGroup.setObservations(dto.getObservations().stream()
                                         .map(this::toDataEntity)
                                         .collect(Collectors.toSet()));
        }

        if (dto.getObservationRelations() != null) {
            obsGroup.setObservationRelations(dto.getObservationRelations().stream()
                                                 .map(this::toObservationRelationEntity)
                                                 .collect(Collectors.toSet()));
        }

        return obsGroup;
    }

    private ObservationRelationEntity toObservationRelationEntity(ObservationRelationDTO dto) {
        ObservationRelationEntity obsRel = new ObservationRelationEntity();
        obsRel.setStaIdentifier(dto.getId());
        obsRel.setName(dto.getName());
        obsRel.setDescription(dto.getDescription());
        obsRel.setRole(dto.getRole());
        obsRel.setNamespace(dto.getNamespace());

        if (dto.getObject() != null) {
            obsRel.setObject(toDataEntity(dto.getObject()));
        }

        if (dto.getSubject() != null) {
            obsRel.setSubject(toDataEntity(dto.getSubject()));
        }

        if (dto.getObservationGroups() != null) {
            obsRel.setObservationGroups(dto.getObservationGroups().stream()
                                            .map(this::toObservationGroupEntity)
                                            .collect(Collectors.toSet()));
        }
        return obsRel;
    }

    private LicenseEntity toLicenseEntity(LicenseDTO type) {
        LicenseEntity licenseEntity = new LicenseEntity();
        licenseEntity.setStaIdentifier(type.getId());
        licenseEntity.setName(type.getName());
        licenseEntity.setDefinition(type.getDefinition());
        licenseEntity.setDescription(type.getDescription());
        licenseEntity.setLogo(type.getLogo());
        return licenseEntity;
    }

    private LicenseDTO toLicenseDTO(LicenseEntity raw, QueryOptions queryOptions) {
        License license = new License();

        license.setId(raw.getStaIdentifier());
        license.setName(raw.getName());
        license.setDescription(raw.getDescription());
        license.setDefinition(raw.getDefinition());
        license.setLogo(raw.getLogo());
        //TODO: implement
        // license.setProperties();
        return license;
    }

    protected ObjectNode parseProperties(HibernateRelations.HasParameters raw) {
        ObjectNode properties = MAPPER.createObjectNode();
        if (raw.hasParameters()) {

            // Handle includeDatastreamCategory
            if (raw instanceof DatasetEntity && config.isIncludeDatastreamCategory()) {
                DatasetEntity ds = (DatasetEntity) raw;
                final String categoryPrefix = "category";
                properties.put(categoryPrefix + "Id",
                               ds.getCategory().getId());
                properties.put(categoryPrefix + "Name",
                               ds.getCategory().getName());
                properties.put(categoryPrefix + "Description",
                               ds.getCategory().getDescription());
            }

            //TODO: check if this is always properly formatting elements
            for (ParameterEntity<?> parameter : raw.getParameters()) {
                properties.put(parameter.getName(), parameter.getValueAsString());
            }
            return properties;
        } else {
            return null;
        }
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

    protected void parseObservationParameters(DataEntity dataEntity,
                                              ObservationDTO dto) {
        if (dto.getParameters() != null) {
            Iterator<String> keyIt = dto.getParameters().fieldNames();
            while (keyIt.hasNext()) {
                String paramName = keyIt.next();
                JsonNode jsonNode = dto.getParameters().get(paramName);

                if (paramName.equals(config.getVerticalFromMapping())) {
                    // Add as verticalTo to enable interoperability with SOS
                    dataEntity.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                    dataEntity.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                } else if (paramName.equals(config.getVerticalToMapping())) {
                    // Add as verticalTo to enable interoperability with SOS
                    dataEntity.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                } else if (paramName.equals(config.getVerticalFromToMapping())) {
                    // Add as verticalTo to enable interoperability with SOS
                    dataEntity.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                } else if (paramName.equals(config.getSamplingGeometryMapping())) {
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
                }

                // Additionally store as normal parameter
                dataEntity.setParameters(convertParameters(dto.getParameters(),
                                                           ParameterFactory.EntityType.OBSERVATION));
            }
        }
    }

    protected Object parseObservationResult(DataEntity<?> raw) {
        //TODO:
        // Handling of Profile/TrajectoryObservation
        //try {
        //if (raw instanceof SensorML20DataEntity) {
        //    return MAPPER.readTree((String) raw.getValue());
        //}
        return raw.getValue();
        //} catch (JsonProcessingException e) {
        //   throw new STAInvalidQueryError(String.format("Could not parse Observation->result on Observation:  %s",
        //                                                raw.getIdentifier()));
        //}
    }
}
