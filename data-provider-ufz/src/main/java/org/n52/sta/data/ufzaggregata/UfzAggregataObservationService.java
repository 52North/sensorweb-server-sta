/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryError;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.common.StaDTO;
import org.n52.sta.data.common.DTOTransformer;
import org.n52.sta.data.common.DTOTransformerImpl;
import org.n52.sta.data.common.repositories.EntityGraphRepository;
import org.n52.sta.data.common.service.ObservationService;
import org.n52.sta.data.common.service.SensorService;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.n52.svalbard.odata.core.expr.bool.BooleanBinaryExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;
import org.n52.svalbard.odata.core.expr.temporal.TimeValueExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.n52.sta.SerDesConfig;

/**
 * Service connecting to the UFZ Aggregata API for retrieving Observations
 * Note: This only implements a subset of STA functionality. See Extension documentation for details.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component("ObservationService")
@DependsOn({"springApplicationContext"})
@Transactional
public class UfzAggregataObservationService extends ObservationService {

    protected static final String SLASH = "/";
    private static final String NOT_YET_IMPLEMENTED = "not yet implemented for ufzaggregata backend";
    private static final Logger LOGGER = LoggerFactory.getLogger(UfzAggregataObservationService.class);
    private static final String TARGET = "target";
    private final Pattern longNamePattern = Pattern.compile(".+\\s\\((.*)\\).*");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SerDesConfig config;
    private final SensorService sensorService;
    private QueryOptions selectQueryOptions;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private String baseUrl = "https://webapp.ufz.de/rdm/aggregata/lvl1";

    public UfzAggregataObservationService(SerDesConfig config,
                                          SensorService sensorService,
                                          @Value("${server.security.aggregataToken}") String aggregataToken) {
        this.sensorService = sensorService;
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(aggregataToken);

        QueryOptionsFactory factory = new QueryOptionsFactory();

        // Add select filter with filter only returning available properties
        HashSet<FilterClause> filters = new HashSet<>();
        HashSet<String> items = new HashSet<>();
        items.add("phenomenonTime");
        items.add("result");
        filters.add(new SelectFilter(items));
        selectQueryOptions = factory.createQueryOptions(filters);
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    public DataEntity<?> getEntityByIdRaw(Long id, QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> getEntityByRelatedEntityRaw(String relatedId,
                                                     String relatedType,
                                                     String ownId,
                                                     QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    public Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                      String relatedType,
                                                      QueryOptions queryOptions)
        throws STACRUDException {
        try {
            throw new STACRUDException(NOT_YET_IMPLEMENTED);
        } catch (RuntimeException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption) {
        throw new STAInvalidQueryError(NOT_YET_IMPLEMENTED);
    }

    @Override
    protected DataEntity<?> fetchExpandEntitiesWithFilter(DataEntity<?> returned,
                                                          ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public Specification<DataEntity<?>> byRelatedEntityFilter(String relatedId,
                                                              String relatedType,
                                                              String ownId) {
        throw new STAInvalidQueryError(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> createOrfetch(DataEntity<?> entity) throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> updateEntity(String id, DataEntity<?> entity, String method)
        throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> createOrUpdate(DataEntity<?> entity) throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public Specification<DataEntity<?>> getFilterPredicate(Class entityClass, QueryOptions queryOptions) {
        throw new STAInvalidQueryError(NOT_YET_IMPLEMENTED);
    }

    @Override
    public String checkPropertyName(String property) {
        throw new STAInvalidQueryError(NOT_YET_IMPLEMENTED);
    }

    @Override
    public DataEntity<?> merge(DataEntity<?> existing, DataEntity<?> toMerge)
        throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        throw new STACRUDException(NOT_YET_IMPLEMENTED);
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
        throw new STAInvalidQueryError(NOT_YET_IMPLEMENTED);
    }

    @Override public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                          String relatedType,
                                                                          QueryOptions queryOptions)
        throws STACRUDException {
        try {
            checkValidQueryOptions(queryOptions);

            if (relatedType.equals(StaConstants.DATASTREAMS)) {

                ProcedureEntity sensor =
                    sensorService.getEntityByRelatedEntityRaw(relatedId,
                                                              relatedType,
                                                              null,
                                                              new QueryOptions("", null));
                Matcher matcher = longNamePattern.matcher(sensor.getName());
                String target;
                if (matcher.matches()) {
                    target = matcher.group(1);
                } else {
                    throw new STACRUDException("Could not extract target from Sensor name: " + sensor.getName());
                }

                String[] split = sensor.getIdentifier().split(":");
                String sensorId = split[split.length - 2];

                AggregataRequest aggregataRequest = createAggregataRequest(target, sensorId, queryOptions);
                HttpEntity<String> request =
                    new HttpEntity<>(objectMapper.writeValueAsString(aggregataRequest), headers);
                AggregataResponse[] response =
                    restTemplate.postForObject(baseUrl + aggregataRequest.getPath(),
                                               request,
                                               AggregataResponse[].class);
                DTOTransformer transformer = new DTOTransformerImpl(config);
                List<StaDTO> observations = new ArrayList<>();
                for (List<BigDecimal> datapoint : response[0].getDatapoints()) {
                    QuantityDataEntity observation = new QuantityDataEntity();
                    BigDecimal value = datapoint.get(0);
                    Date timestamp = new Date(datapoint.get(1).longValue());
                    observation.setPhenomenonTimeStart(timestamp);
                    observation.setPhenomenonTimeEnd(timestamp);
                    observation.setValue(value);
                    observations.add(transformer.toDTO(observation, selectQueryOptions));
                }
                return new CollectionWrapper(observations.size(), observations, false);
            } else {
                throw new STACRUDException(NOT_YET_IMPLEMENTED);
            }
        } catch (RuntimeException | JsonProcessingException e) {
            throw new STACRUDException(e.getMessage(), e);
        }
    }

    private AggregataRequest createAggregataRequest(String targetId, String sensorId, QueryOptions queryOptions) {
        AggregataRequest request = new AggregataRequest();

        request.setMaxDataPoints(queryOptions.getTopFilter().getValue().intValue());
        request.setPath(SLASH + sensorId + "/query");

        Target target = new Target();
        target.setTarget(targetId);
        request.setTargets(Collections.singletonList(target));

        BooleanBinaryExpr filter = (BooleanBinaryExpr) queryOptions.getFilterFilter().getFilter();
        TimeValueExpr phenTimeStart = (TimeValueExpr) ((ComparisonExpr) filter.getLeft()).getRight();
        TimeValueExpr phenTimeEnd = (TimeValueExpr) ((ComparisonExpr) filter.getRight()).getRight();
        Range range = new Range();
        range.setFrom(phenTimeStart.toString());
        range.setTo(phenTimeEnd.toString());
        request.setRange(range);
        return request;
    }

    private boolean checkValidQueryOptions(QueryOptions queryOptions) {
        return queryOptions.hasTopFilter() && queryOptions.hasFilterFilter();
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
