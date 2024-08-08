package org.n52.sta.api.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface HasProperties {

    ObjectNode getProperties();

    void setProperties(ObjectNode properties);
}
