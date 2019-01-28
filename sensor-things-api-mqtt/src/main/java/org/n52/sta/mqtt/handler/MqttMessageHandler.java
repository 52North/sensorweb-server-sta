/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.handler;

import io.moquette.interception.messages.InterceptMessage;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.CrudHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBufInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.n52.sta.mqtt.core.MQTTEventHandler;
import org.n52.sta.mqtt.core.MQTTSubscription;
import org.n52.sta.mqtt.core.MQTTUtil;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.URIQueryOptions;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class MqttMessageHandler implements InitializingBean {

    final Logger LOGGER = LoggerFactory.getLogger(MqttMessageHandler.class);

    @Autowired
    private Parser parser;

    @Autowired
    AbstractEntityCollectionRequestHandler requestHandler;

    @Autowired
    private CrudHelper crudHelper;

    @Autowired
    private MQTTEventHandler localClient;

    @Autowired
    private CsdlAbstractEdmProvider provider;

    private ServiceMetadata edm;

    public void processMessage(InterceptMessage msg) throws UriParserException, UriValidationException, ODataApplicationException, DeserializerException {
        if (msg instanceof InterceptPublishMessage) {
            processPublishMessage((InterceptPublishMessage) msg);
        } else if (msg instanceof InterceptSubscribeMessage) {
            processSubscribeMessage((InterceptSubscribeMessage) msg);
        } else if (msg instanceof InterceptUnsubscribeMessage) {
            processUnsubscribeMessage((InterceptUnsubscribeMessage) msg);
        }
    }

    private void processPublishMessage(InterceptPublishMessage msg) throws UriParserException, UriValidationException, ODataApplicationException, DeserializerException {
        UriInfo uriInfo = parser.parseUri(msg.getTopicName(), null, null, "");
        EntityResponse entityResponse = new EntityResponse();
        DeserializerResult deserializeRequestBody = crudHelper.deserializeRequestBody(new ByteBufInputStream(msg.getPayload()), uriInfo);
        if (deserializeRequestBody.getEntity() != null) {
            entityResponse = crudHelper.getCrudEntityHanlder(uriInfo)
                    .handleCreateEntityRequest(deserializeRequestBody.getEntity(), uriInfo.getUriResourceParts());
            LOGGER.info("Creation of Entity {} was succesful", entityResponse.getEntity());
        }
    }

    private void processSubscribeMessage(InterceptSubscribeMessage msg) throws UriParserException, UriValidationException {
        localClient.addSubscription(createMqttSubscription(msg.getTopicFilter()));
    }

    private void processUnsubscribeMessage(InterceptUnsubscribeMessage msg) throws UriParserException, UriValidationException {
        localClient.removeSubscription(createMqttSubscription(msg.getTopicFilter()));
    }

    private MQTTSubscription createMqttSubscription(String topic) throws UriParserException, UriValidationException {

        List<SelectItem> fields = new ArrayList();
        Set<String> watchedProperties = new HashSet();
        String olingoEntityType = null;
        boolean isCollection = false;
        Long entityId = null;

        //TODO set base URI
        String baseUri = "";

        // Validate that Topic is valid URI
        UriInfo uriInfo = parser.parseUri(topic, null, null, baseUri);

//        this.topic = topic;
        List<UriResource> pattern = uriInfo.getUriResourceParts();
//        this.edm = edm;
        QueryOptions queryOptions = new URIQueryOptions(uriInfo, baseUri);

        if (queryOptions.hasSelectOption()) {
            fields = queryOptions.getSelectOption().getSelectItems();
        }

        // Parse select Option if present
//        if (uriInfo.getSelectOption() != null) {
//            fields = uriInfo.getSelectOption().getSelectItems();
//        }
        // Parse specifically adressed property if present
        UriResource lastResource = pattern.get(pattern.size() - 1);

        String propertyResource = null;
        if (!(lastResource instanceof UriResourceEntitySet)) {
            // Last Resource is property
            propertyResource = ((UriResourceProperty) lastResource).getProperty().getName();
            lastResource = pattern.get(pattern.size() - 2);
        }

        // Parse ID if present
        List<UriParameter> idParameter = ((UriResourceEntitySet) lastResource).getKeyPredicates();

        EdmEntityType entityType = ((UriResourceEntitySet) lastResource).getEntityType();
        EdmEntitySet entitySet = ((UriResourceEntitySet) lastResource).getEntitySet();

        if (idParameter.size() == 0) {
            isCollection = true;
        } else {
            entityId = Long.parseLong(idParameter.get(0).getText());
        }

        // Parse Entitytype
        switch (lastResource.toString()) {
            case "Observations":
                olingoEntityType = "iot.Observation";
                break;
            case "Datastreams":
                olingoEntityType = "iot.Datastream";
                break;
            case "FeatureOfInterests":
                olingoEntityType = "iot.FeatureOfInterest";
                break;
            case "HistoricalLocations":
                olingoEntityType = "iot.HistoricalLocation";
                break;
            case "Locations":
                olingoEntityType = "iot.Location";
                break;
            case "ObservedProperties":
                olingoEntityType = "iot.ObservedProperty";
                break;
            case "Sensors":
                olingoEntityType = "iot.Sensor";
                break;
            case "Things":
                olingoEntityType = "iot.Thing";
                break;
            default:
                throw new IllegalArgumentException("Invalid topic supplied! Cannot Get Resource Type.");
        }

        // Parse STA Property to Database Property after entityType has been determined
        if (propertyResource != null) {
            watchedProperties = MQTTUtil.translateSTAtoToDbProperty(olingoEntityType + "." + propertyResource);
        }
        return new MQTTSubscription(topic, fields, pattern, olingoEntityType, watchedProperties, isCollection, entityId, queryOptions, entitySet, entityType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        OData odata = OData.newInstance();

        edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
    }

}
