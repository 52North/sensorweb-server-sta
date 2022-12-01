package org.n52.sta.data.service.hereon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.n52.sensorweb.server.helgoland.adapters.connector.response.Attributes;
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
    private final ArcgisRestHttpClient connectorFactory;
    private final HereonConfig config;

    public HereonObservationService(HereonConfig config) {
        this.connectorFactory = new ArcgisRestHttpClient(config.getCredentials());
        this.config = config;
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
                AbstractDatasetEntity entityByIdRaw = datastreamService.getEntityByIdRaw(
                        Long.valueOf(relatedId),
                        QueryOptionsFactory.createEmpty());

                String url_data_service = null;
                for (ParameterEntity<?> parameterEntity : entityByIdRaw.getParameters()) {
                    if (parameterEntity.getName().equals("url_data_service")) {
                        url_data_service = String.valueOf(parameterEntity.getValue());
                    }
                }
                if (url_data_service == null) {
                    throw new STACRUDException("Could not find observations. " +
                            "Datastream has no linked url_data_service!");
                }

                try {
                    String url = config.createDataServiceUrl(url_data_service);
                    Response response = connectorFactory.execute(url, new GetFeaturesRequest());

                    MetadataResponse responseCollection = encodeResponse(response.getEntity(), MetadataResponse.class);

                    return new CollectionWrapper(responseCollection.getFeatures().size(),
                            ObservationMapper.toDataEntities(responseCollection.getFeatures()),
                            responseCollection.getExceededTransferLimit());
                } catch (ProxyHttpClientException | DecodingException | JsonProcessingException e) {
                    e.printStackTrace();
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
