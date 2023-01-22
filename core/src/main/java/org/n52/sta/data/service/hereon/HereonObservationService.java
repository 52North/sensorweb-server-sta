/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.service.hereon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Observation;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.CountResponse;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.MetadataResponse;
import org.n52.sensorweb.server.helgoland.adapters.web.ArcgisRestHttpClient;
import org.n52.sensorweb.server.helgoland.adapters.web.ProxyHttpClientException;
import org.n52.sensorweb.server.helgoland.adapters.web.response.ArcgisErrorResponse;
import org.n52.sensorweb.server.helgoland.adapters.web.response.Response;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.service.DatastreamService;
import org.n52.sta.data.service.ObservationService;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile("hereon")
@Primary
public class HereonObservationService extends ObservationService {

    private final ObjectMapper OM = new ObjectMapper();
    private final String not_supported = "not supported for HEREON backend!";
    private final Observation observationMapping;
    private final String dataServiceUrl;
    private final Map<String, ArcgisRestHttpClient> featureServiceConnectors = new HashMap<>(5);
    private final HereonConfig config;
    private final QueryOptionsFactory QOF = new QueryOptionsFactory();

    public HereonObservationService(HereonConfig config) {
        this.config = config;
        this.observationMapping = config.getMapping().getObservation();
        this.dataServiceUrl = config.getMapping().getGeneral().getDataServiceUrl();
    }

    @Override
    public boolean existsEntity(String id) {
        return true;
    }

    @Override
    public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    public int getFeatureCount(QueryOptions queryOptions,
                               String featureServiceUrl,
                               String relatedId) throws STACRUDException {

        // Request total entity Count from API
        int totalCount = -1;
        if (queryOptions.hasCountFilter()) {
            String url = config.createDataServiceUrl(featureServiceUrl);
            ArcgisRestHttpClient client = getClient(url);
            Response countResponse = null;
            try {
                countResponse = client.execute(url, new GetFeatureCountRequest(relatedId));
                CountResponse count = encodeResponse(countResponse.getEntity(), CountResponse.class);
                totalCount = count.getCount();
            } catch (ProxyHttpClientException | DecodingException | JsonProcessingException e) {
                throw new STACRUDException("error retrieving observation count", e);
            }
        }
        return totalCount;
    }

    public ArcgisRestHttpClient getClient(String url) {
        //TODO: This can be refactored into a single client when all featureServices are integrated into
        // a single portal
        String tokenUrl = config.createTokenUrl(url);
        ArcgisRestHttpClient client = this.featureServiceConnectors.get(tokenUrl);
        if (client == null) {
            client = new ArcgisRestHttpClient(
                    config.getCredentials().getUsername(),
                    config.getCredentials().getPassword(),
                    tokenUrl);
            this.featureServiceConnectors.put(tokenUrl, client);
        }
        return client;
    }

    private String getFeatureServiceUrl(String relatedId) throws STACRUDException {
        // Fetch datastream to lookup url_data_service
        DatastreamService datastreamService = getDatastreamService();
        AbstractDatasetEntity dataset = (AbstractDatasetEntity) datastreamService.getEntity(
                relatedId,
                QOF.createDummy()).getEntity();

        String featureServiceUrl = null;
        for (ParameterEntity<?> parameterEntity : dataset.getParameters()) {
            if (parameterEntity.getName().equals(dataServiceUrl)) {
                featureServiceUrl = String.valueOf(parameterEntity.getValue());
                break;
            }
        }
        if (featureServiceUrl == null) {
            throw new STACRUDException("Could not find observations. " +
                                               "Datastream has no linked " + dataServiceUrl);
        }
        return featureServiceUrl;
    }

    private MetadataResponse getCollectionFromFeatureService(String featureServiceUrl,
                                                             String relatedId,
                                                             String relatedType,
                                                             QueryOptions queryOptions) throws STACRUDException {
        if (StaConstants.DATASTREAMS.equals(relatedType)) {
            try {
                ArcgisRestHttpClient client = getClient(featureServiceUrl);
                Response response = client.execute(featureServiceUrl, new GetFeaturesRequest(queryOptions,
                                                                                             relatedId,
                                                                                             config));
                return encodeResponse(response.getEntity(), MetadataResponse.class);
            } catch (ProxyHttpClientException | DecodingException | JsonProcessingException e) {
                throw new STACRUDException("error retrieving observations", e);
            }
        }
        throw new STACRUDException(not_supported);

    }

    @Override
    protected Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                         String relatedType,
                                                         QueryOptions queryOptions)
            throws STACRUDException {

        String featureServiceUrl = config.createDataServiceUrl(getFeatureServiceUrl(relatedId));
        MetadataResponse responseCollection = getCollectionFromFeatureService(featureServiceUrl,
                                                                              relatedId,
                                                                              relatedType,
                                                                              queryOptions);
        // Page is always $top large
        Pageable pageable = Pageable.ofSize(Math.toIntExact(queryOptions.getTopFilter().getValue()));

        return new PageImpl(
                ObservationMapper.toDataEntity(observationMapping, responseCollection.getFeatures()),
                pageable,
                0);
    }

    @Override
    public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                String relatedType,
                                                                QueryOptions queryOptions)
            throws STACRUDException {

        String featureServiceUrl = config.createDataServiceUrl(getFeatureServiceUrl(relatedId));
        MetadataResponse responseCollection = getCollectionFromFeatureService(featureServiceUrl,
                                                                              relatedId,
                                                                              relatedType,
                                                                              queryOptions);
        return new CollectionWrapper(getFeatureCount(queryOptions, featureServiceUrl, relatedId),
                                     ObservationMapper.toElementWithQO(observationMapping,
                                                                      responseCollection.getFeatures(),
                                                                      queryOptions),
                                     responseCollection.getExceededTransferLimit());
    }

    private <T> T encodeResponse(String response, Class<T> clazz) throws
            JsonProcessingException, DecodingException {
        try {
            return OM.readValue(response, clazz);
        } catch (JsonProcessingException e) {
            if (!clazz.isInstance(ArcgisErrorResponse.class)) {
                ArcgisErrorResponse errorResponse = encodeResponse(response, ArcgisErrorResponse.class);
                throw new DecodingException(errorResponse.toString());
            } else {
                throw e;
            }
        }
    }

    @Override
    public DataEntity<?> getEntityByIdRaw(Long id, QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(not_supported);

    }

    @Override
    protected DataEntity<?> fetchExpandEntitiesWithFilter(DataEntity<?> returned, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        throw new STACRUDException(not_supported);

    }

    @Override
    public Specification<DataEntity<?>> byRelatedEntityFilter(String relatedId, String relatedType, String ownId) {
        return null;
    }

    @Override
    public DataEntity<?> createOrfetch(DataEntity<?> entity) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    public DataEntity<?> updateEntity(String id, DataEntity<?> entity, HttpMethod method) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    public DataEntity<?> createOrUpdate(DataEntity<?> entity) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    public Specification<DataEntity<?>> getFilterPredicate(QueryOptions queryOptions) {
        return null;
    }

    @Override
    public DataEntity<?> merge(DataEntity<?> existing, DataEntity<?> toMerge) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    public void delete(String identifier) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

}
