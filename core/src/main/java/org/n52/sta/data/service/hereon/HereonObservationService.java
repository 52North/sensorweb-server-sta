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
import org.n52.sensorweb.server.helgoland.adapters.connector.response.ErrorResponse;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.MetadataResponse;
import org.n52.sensorweb.server.helgoland.adapters.web.ArcgisRestHttpClient;
import org.n52.sensorweb.server.helgoland.adapters.web.ProxyHttpClientException;
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
    private Map<String, ArcgisRestHttpClient> featureServiceConnectors = new HashMap<>(5);
    private final HereonConfig config;
    private final QueryOptionsFactory QOF = new QueryOptionsFactory();

    public HereonObservationService(HereonConfig config) {
        this.config = config;
        this.observationMapping = config.getMapping().getObservation();
        this.dataServiceUrl = config.getMapping().getGeneral().getDataServiceUrl();
    }

    @Override
    public ElementWithQueryOptions getEntity(String id, QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    public CollectionWrapper getEntityCollection(QueryOptions queryOptions) throws STACRUDException {
        throw new STACRUDException(not_supported);
    }

    @Override
    protected Page getEntityCollectionByRelatedEntityRaw(String relatedId,
                                                         String relatedType,
                                                         QueryOptions queryOptions)
            throws STACRUDException {
        throw new STACRUDException(not_supported);

    }

    @Override
    public CollectionWrapper getEntityCollectionByRelatedEntity(String relatedId,
                                                                String relatedType,
                                                                QueryOptions queryOptions)
            throws STACRUDException {
        switch (relatedType) {
            case StaConstants.DATASTREAMS:
                // Fetch datastream to lookup url_data_service
                DatastreamService datastreamService = getDatastreamService();
                AbstractDatasetEntity dataset = (AbstractDatasetEntity) datastreamService.getEntity(
                        relatedId,
                        QOF.createDummy()).getEntity();

                String url_data_service = null;
                for (ParameterEntity<?> parameterEntity : dataset.getParameters()) {
                    if (parameterEntity.getName().equals(dataServiceUrl)) {
                        url_data_service = String.valueOf(parameterEntity.getValue());
                        break;
                    }
                }
                if (url_data_service == null) {
                    throw new STACRUDException("Could not find observations. " +
                            "Datastream has no linked " + dataServiceUrl);
                }

                try {
                    String url = config.createDataServiceUrl(url_data_service);

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
                    Response response = client.execute(url, new GetFeaturesRequest(relatedId));
                    MetadataResponse responseCollection = encodeResponse(response.getEntity(), MetadataResponse.class);

                    return new CollectionWrapper(responseCollection.getFeatures().size(),
                            ObservationMapper.toDataEntities(observationMapping, responseCollection.getFeatures()),
                            responseCollection.getExceededTransferLimit());
                } catch (ProxyHttpClientException | DecodingException | JsonProcessingException e) {
                    throw new STACRUDException("error retrieving observations", e);
                }
            default:
                throw new STACRUDException(not_supported);
        }
    }

    private <T> T encodeResponse(String response, Class<T> clazz) throws JsonProcessingException, DecodingException {
        try {
            return OM.readValue(response, clazz);
        } catch (JsonProcessingException e) {
            if (!clazz.isInstance(ErrorResponse.class)) {
                ErrorResponse errorResponse = encodeResponse(response, ErrorResponse.class);
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
