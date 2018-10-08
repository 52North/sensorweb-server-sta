/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class JsonHelper implements InitializingBean {

    private ObjectMapper mapper;
    private JsonNodeFactory factory;

    @Override
    public void afterPropertiesSet() throws Exception {
        mapper = new ObjectMapper();
        factory = JsonNodeFactory.instance;
    }

    public JsonNode readJsonString(String json) throws IOException {
        return mapper.readTree(json);
    }

    public ObjectNode createEmptyObjectNode() {
        return mapper.createObjectNode();
    }

}
