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

package org.n52.sta.data.ufzaggregata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.cfg.NotYetImplementedException;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.vanilla.DTOTransformer;
import org.n52.sta.data.vanilla.OffsetLimitBasedPageRequest;
import org.n52.sta.data.vanilla.SerDesConfig;
import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;
import org.n52.sta.data.vanilla.service.ObservationService;
import org.n52.svalbard.odata.core.expr.StringValueExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanBinaryExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;
import org.n52.svalbard.odata.core.expr.temporal.TimeValueExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Service connecting to the UFZ Aggregata API for retrieving Observations
 * Note: This only implements a subset of STA functionality. See Extension documentation for details.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component("ObservationService")
@DependsOn({"springApplicationContext"})
@Transactional
@Primary
public class UfzAggregataObservationService extends ObservationService {

    protected static final String SLASH = "/";
    private static final String NOT_YET_IMPLEMENTED = "not yet implemented";
    private static final Logger LOGGER = LoggerFactory.getLogger(UfzAggregataObservationService.class);
    private static final String TARGET = "target";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SerDesConfig config;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private String baseUrl = "https://webapp.ufz.de/rdm/aggregata/lvl1";

    public UfzAggregataObservationService(SerDesConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("****************************");
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        try {
            checkValidQueryOptions(queryOptions);

            AggregataRequest aggregataRequest = createAggregataRequest(queryOptions);
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(aggregataRequest), headers);
            AggregataResponse[] response =
                restTemplate.postForObject(baseUrl + aggregataRequest.getPath(), request, AggregataResponse[].class);

            List<ObservationDTO> observations = new ArrayList<>();
            DTOTransformer transformer = new DTOTransformer(config);
            for (List<BigDecimal> datapoint : response[0].getDatapoints()) {
                QuantityDataEntity observation = new QuantityDataEntity();
                BigDecimal value = datapoint.get(0);
                Date timestamp = new Date(datapoint.get(1).longValue());
                observation.setPhenomenonTimeStart(timestamp);
                observation.setPhenomenonTimeEnd(timestamp);
                observation.setValue(value);
                observations.add((ObservationDTO) transformer.toDTO(observation, queryOptions));
            }
            return new CollectionWrapper(observations.size(), observations, false);
        } catch (RuntimeException | JsonProcessingException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override
    public DataEntity<?> getEntityByIdRaw(Long id, QueryOptions queryOptions) throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> getEntityByRelatedEntityRaw(String relatedId,
                                                     String relatedType,
                                                     String ownId,
                                                     QueryOptions queryOptions) throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    public Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                      String relatedType,
                                                      QueryOptions queryOptions)
        throws STACRUDException {
        try {
            throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption) {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    protected DataEntity<?> fetchExpandEntitiesWithFilter(DataEntity<?> returned,
                                                          ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public Specification<DataEntity<?>> byRelatedEntityFilter(String relatedId,
                                                              String relatedType,
                                                              String ownId) {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> createOrfetch(DataEntity<?> entity) throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> updateEntity(String id, DataEntity<?> entity, HttpMethod method)
        throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> createOrUpdate(DataEntity<?> entity) throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public Specification<DataEntity<?>> getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public String checkPropertyName(String property) {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> merge(DataEntity<?> existing, DataEntity<?> toMerge)
        throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    /**
     * Handles updating the phenomenonTime field of the associated Datastream when Observation phenomenonTime is
     * updated or deleted
     *
     * @param datastreamEntity DatstreamEntity
     * @param observation      ObservationEntity
     */
    protected void updateDatastreamPhenomenonTimeOnObservationUpdate(AbstractDatasetEntity datastreamEntity,
                                                                     DataEntity<?> observation) {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
        throws STACRUDException {

        try {
            throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    private AggregataRequest createAggregataRequest(QueryOptions queryOptions) {
        AggregataRequest request = new AggregataRequest();

        //TODO: refactor to not rely on specific order of queryOptions
        request.setMaxDataPoints(queryOptions.getTopFilter().getValue().intValue());

        BooleanBinaryExpr expr = (BooleanBinaryExpr) queryOptions.getFilterFilter().getFilter();

        // Datastream/Sensor/id
        String[] sensorIdRaw = ((StringValueExpr)
            (((ComparisonExpr) expr.getLeft()).getRight())).getValue().split(":");
        String sensorId = sensorIdRaw[sensorIdRaw.length - 1];
        // properties/projectId
        String projectId =
            ((StringValueExpr)
                ((ComparisonExpr)
                    (((BooleanBinaryExpr) expr.getRight()).getLeft())).getRight()).getValue();

        request.setPath(SLASH + projectId + SLASH + sensorId + "/query");

        Target target = new Target();
        target.setTarget("Batteriespannung");
        request.setTargets(Collections.singletonList(target));

        TimeValueExpr phenTimeStart =
            (TimeValueExpr)
                ((ComparisonExpr)
                    ((BooleanBinaryExpr)
                        (((BooleanBinaryExpr) expr.getRight()).getRight())).getLeft()).getRight();
        TimeValueExpr phenTimeEnd =
            (TimeValueExpr)
                ((ComparisonExpr)
                    ((BooleanBinaryExpr)
                        (((BooleanBinaryExpr) expr.getRight()).getRight())).getRight()).getRight();
        Range range = new Range();
        range.setFrom(phenTimeStart.toString());
        range.setTo(phenTimeEnd.toString());
        request.setRange(range);
        return request;
    }

    private boolean checkValidQueryOptions(QueryOptions queryOptions) {
        //TODO!
        return true;
    }

    /**
     * We touch DataEntity->value here to make sure it is fetched from the database and not lazy-loaded.
     * We cannot fetch it ourselves as any assignment to entity->value will update the database (issue delete+insert
     * statemenets).
     *
     * @param entity to be loaded
     * @return entity with not-lazy-loaded value and value->parameters
     */
    private DataEntity<?> fetchValueIfCompositeDataEntity(DataEntity<?> entity) {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    private CollectionWrapper getEntityCollectionWrapperByIdentifierList(List<String> identifierList,
                                                                         OffsetLimitBasedPageRequest pageableRequest,
                                                                         QueryOptions queryOptions,
                                                                         Specification<DataEntity<?>> spec) {

        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    private DataEntity<?> saveObservation(DataEntity<?> observation, DatasetEntity dataset)
        throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    /**
     * Updates FirstValue/LastValue, FirstObservation/LastObservation, Geometry of Dataset and DatasetAggregation
     *
     * @param dataset Dataset to be updated
     * @param data    New Observation
     * @return update DatasetEntity
     * @throws STACRUDException if an error occurred
     */
    private AbstractDatasetEntity updateDataset(AbstractDatasetEntity dataset, DataEntity<?> data)
        throws STACRUDException {
        throw new NotYetImplementedException(NOT_YET_IMPLEMENTED);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
                           "range",
                           "targets",
                           "maxDataPoints"
                       })
    static class AggregataRequest {

        @JsonProperty("range")
        private Range range;
        @JsonProperty("targets")
        private List<Target> targets;
        @JsonProperty("maxDataPoints")
        private Integer maxDataPoints;
        @JsonIgnore
        private String path;

        @JsonProperty("range")
        public Range getRange() {
            return range;
        }

        @JsonProperty("range")
        public void setRange(Range range) {
            this.range = range;
        }

        @JsonProperty("targets")
        public List<Target> getTargets() {
            return targets;
        }

        @JsonProperty("targets")
        public void setTargets(List<Target> targets) {
            this.targets = targets;
        }

        @JsonProperty("maxDataPoints")
        public Integer getMaxDataPoints() {
            return maxDataPoints;
        }

        @JsonProperty("maxDataPoints")
        public void setMaxDataPoints(Integer maxDataPoints) {
            this.maxDataPoints = maxDataPoints;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
                           "from",
                           "to"
                       })
    static class Range {

        @JsonProperty("from")
        private String from;
        @JsonProperty("to")
        private String to;

        @JsonProperty("from")
        public String getFrom() {
            return from;
        }

        @JsonProperty("from")
        public void setFrom(String from) {
            this.from = from;
        }

        @JsonProperty("to")
        public String getTo() {
            return to;
        }

        @JsonProperty("to")
        public void setTo(String to) {
            this.to = to;
        }
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
                           "target",
                           "type"
                       })
    static class Target {

        @JsonProperty("target")
        private String target;
        @JsonProperty("type")
        private String type = "timeseries";

        @JsonProperty("target")
        public String getTarget() {
            return target;
        }

        @JsonProperty("target")
        public void setTarget(String target) {
            this.target = target;
        }

        @JsonProperty("type")
        public String getType() {
            return type;
        }
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
                           "target",
                           "datapoints"
                       })
    static class AggregataResponse {

        @JsonProperty("target")
        private String target;
        @JsonProperty("datapoints")
        private List<List<BigDecimal>> datapoints;

        @JsonProperty("target")
        public String getTarget() {
            return target;
        }

        @JsonProperty("target")
        public void setTarget(String target) {
            this.target = target;
        }

        @JsonProperty("datapoints")
        public List<List<BigDecimal>> getDatapoints() {
            return datapoints;
        }

        @JsonProperty("datapoints")
        public void setDatapoints(List<List<BigDecimal>> datapoints) {
            this.datapoints = datapoints;
        }
    }
}
