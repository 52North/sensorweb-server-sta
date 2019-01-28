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
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.n52.sta.mqtt.core.MQTTEventHandler;
import org.n52.sta.mqtt.core.MQTTSubscription;
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
        localClient.addSubscription(new MQTTSubscription(msg.getTopicFilter(), parser, edm));
    }

    private void processUnsubscribeMessage(InterceptUnsubscribeMessage msg) throws UriParserException, UriValidationException {
        localClient.removeSubscription(new MQTTSubscription(msg.getTopicFilter(), parser, edm));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        OData odata = OData.newInstance();

        edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
    }

}
