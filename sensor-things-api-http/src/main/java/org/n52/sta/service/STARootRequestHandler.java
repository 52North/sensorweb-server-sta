package org.n52.sta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
public class STARootRequestHandler {

    private final String rootUrl;
    private final ObjectMapper mapper;
    private final String rootResponse;

    public STARootRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                 ObjectMapper mapper) {
        this.rootUrl = rootUrl;
        this.mapper = mapper;
        this.rootResponse = createRootResponse();
    }

    /**
     * Matches the request to the root resource
     * e.g. /
     */
    @GetMapping(
            value = "/",
            produces = "application/json"
    )
    public String returnRootResponse() {
        return rootResponse;
    }

    private String createRootResponse() {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (String collection : STAEntityDefinition.allCollections) {
            ObjectNode node = mapper.createObjectNode();
            node.put("name", collection);
            node.put("url", rootUrl + collection);
            arrayNode.add(node);
        }

        ObjectNode node = mapper.createObjectNode();
        node.put("value", arrayNode);
        return node.toString();
    }


}
