/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.n52.sta.mqtt.MqttHandlerException;
import org.n52.sta.mqtt.core.AbstractMqttSubscription;
import org.n52.sta.mqtt.core.MqttEventHandler;
import org.n52.sta.mqtt.core.MqttSubscription;
import org.n52.sta.mqtt.core.MqttUtil;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.URIQueryOptions;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.CrudHelper;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.moquette.interception.messages.InterceptMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBufInputStream;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class MqttMessageHandler {

    final Logger LOGGER = LoggerFactory.getLogger(MqttMessageHandler.class);

    @Autowired
    private Parser parser;

    @Autowired
    AbstractEntityCollectionRequestHandler requestHandler;

    @Autowired
    private CrudHelper crudHelper;

    @Autowired
    private MqttEventHandler localClient;

    @Autowired
    private CsdlAbstractEdmProvider provider;

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @SuppressWarnings("unchecked")
    public void processPublishMessage(InterceptPublishMessage msg) throws UriParserException, UriValidationException, ODataApplicationException, DeserializerException {
        UriInfo uriInfo = parser.parseUri(msg.getTopicName(), null, null, "");
        EntityResponse entityResponse = new EntityResponse();
        DeserializerResult deserializeRequestBody = crudHelper.deserializeRequestBody(new ByteBufInputStream(msg.getPayload()), uriInfo);
        if (deserializeRequestBody.getEntity() != null) {
            entityResponse = crudHelper.getCrudEntityHanlder(uriInfo)
                    .handleCreateEntityRequest(deserializeRequestBody.getEntity(), uriInfo.getUriResourceParts());
            LOGGER.info("Creation of Entity {} was succesful", entityResponse.getEntity());
        }
    }

    public void processSubscribeMessage(InterceptSubscribeMessage msg) throws MqttHandlerException {
        localClient.addSubscription(createMqttSubscription(msg.getTopicFilter()), msg.getClientID());
    }

    public void processUnsubscribeMessage(InterceptUnsubscribeMessage msg) throws MqttHandlerException {
        localClient.removeSubscription(createMqttSubscription(msg.getTopicFilter()), msg.getClientID());
    }

    private AbstractMqttSubscription createMqttSubscription(String topic) throws MqttHandlerException {
        UriInfo uriInfo = validateTopicPattern(topic, "");
        validateResource(uriInfo);

        final int lastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 1;
        final UriResource lastPathSegment = uriInfo.getUriResourceParts().get(lastPathSegmentIndex);

        switch(lastPathSegment.getKind()) {
        case complexProperty:
        case primitiveProperty:
            //          public MqttPropertySubscription(EdmEntitySet targetEntitySet,
            //          EdmEntityType entityType,
            //          Long targetId,
            //          EdmProperty watchedProperty,
            //          String topic,
            //          QueryOptions queryOptions,
            //          Set<String> watchedProperties)
            break;
        case entitySet:
            // EntityCollectionSubscription:
            //          MqttEntityCollectionSubscription(String topic,
            //                                           QueryOptions queryOption,
            //                                           EdmEntityType sourceEntityType,
            //                                           Long sourceId,
            //                                           EdmEntitySet targetEntitySet,
            //                                           EdmEntityType entityType)

            // EntitySubscription:
            //          public MqttEntitySubscription(EdmEntityType sourceEntityType,
            //                                        Long sourceId,
            //                                        EdmEntitySet targetEntitySet,
            //                                        EdmEntityType entityType,
            //                                        Long targetId,
            //                                        String topic,
            //                                        QueryOptions queryOptions)
            break;
        default:
            throw new MqttHandlerException("Error while creating MQTT subscription.");
        }
        

        throw new MqttHandlerException("Error while creating MQTT subscription.");


        //        List<SelectItem> fields = new ArrayList();
        //        Set<String> watchedProperties = new HashSet();
        //        String olingoEntityType = null;
        //        boolean isCollection = false;
        //        Long entityId = null;
        //
        //        //TODO set base URI
        //        String baseUri = "";
        //
        //        UriInfo uriInfo = validateTopicPattern(topic, baseUri);
        //        
        //        List<UriResource> pattern = uriInfo.getUriResourceParts();
        //        QueryOptions queryOptions = new URIQueryOptions(uriInfo, baseUri);
        //
        //        if (queryOptions.hasSelectOption()) {
        //            fields = queryOptions.getSelectOption().getSelectItems();
        //        }

        //        Parse select Option if present
        //        
        //            if (uriInfo.getSelectOption() != null) {
        //                fields = uriInfo.getSelectOption().getSelectItems();
        //            }
        //        Parse specifically adressed property if present
        //        UriResource lastResource = pattern.get(pattern.size() - 1);
        //
        //        String propertyResource = null;
        //        if (!(lastResource instanceof UriResourceEntitySet)) {
        //            // Last Resource is property
        //            propertyResource = ((UriResourceProperty) lastResource).getProperty().getName();
        //            lastResource = pattern.get(pattern.size() - 2);
        //        }

        // Parse ID if present
        //        List<UriParameter> idParameter = ((UriResourceEntitySet) lastResource).getKeyPredicates();
        //
        //        EdmEntityType entityType = ((UriResourceEntitySet) lastResource).getEntityType();
        //        EdmEntitySet entitySet = ((UriResourceEntitySet) lastResource).getEntitySet();
        //
        //        if (idParameter.size() == 0) {
        //            isCollection = true;
        //        } else {
        //            entityId = Long.parseLong(idParameter.get(0).getText());
        //        }

        // Parse Entitytype
        //        switch (lastResource.toString()) {
        //            case "Observations":
        //                olingoEntityType = "iot.Observation";
        //                break;
        //            case "Datastreams":
        //                olingoEntityType = "iot.Datastream";
        //                break;
        //            case "FeatureOfInterests":
        //                olingoEntityType = "iot.FeatureOfInterest";
        //                break;
        //            case "HistoricalLocations":
        //                olingoEntityType = "iot.HistoricalLocation";
        //                break;
        //            case "Locations":
        //                olingoEntityType = "iot.Location";
        //                break;
        //            case "ObservedProperties":
        //                olingoEntityType = "iot.ObservedProperty";
        //                break;
        //            case "Sensors":
        //                olingoEntityType = "iot.Sensor";
        //                break;
        //            case "Things":
        //                olingoEntityType = "iot.Thing";
        //                break;
        //            default:
        //                throw new IllegalArgumentException("Invalid topic supplied! Cannot Get Resource Type.");
        //        }

        // Parse STA Property to Database Property after entityType has been determined

        //        return new MQTTSubscription(topic, fields, olingoEntityType, watchedProperties, isCollection, entityId, queryOptions, entitySet, entityType);
    }

    private UriInfo validateTopicPattern(String topic, String baseUri) throws MqttHandlerException {
        try {
            // Validate that Topic is valid URI
            UriInfo uriInfo = parser.parseUri(topic, null, null, baseUri);
            switch (uriInfo.getKind()) {
            case resource:
            case entityId:
                validateResource(uriInfo);
                break;
            default:
                throw new MqttHandlerException("Unsupported MQTT topic pattern.");
            }
            return uriInfo;
        } catch (UriParserException | UriValidationException ex) {
            throw new MqttHandlerException("Error while parsing MQTT topic.", ex);
        }

    }

    private void validateResource(UriInfo uriInfo) throws MqttHandlerException {
        final int lastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 1;
        final UriResource lastPathSegment = uriInfo.getUriResourceParts().get(lastPathSegmentIndex);

        switch (lastPathSegment.getKind()) {

        case entitySet:
        case navigationProperty:

            break;

        case primitiveProperty:

            break;

        case complexProperty:

            break;

        default:
            throw new MqttHandlerException("Unsupported MQTT topic pattern.");
        }
    }

}