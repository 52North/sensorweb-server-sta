/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.config;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Holds 52N-STA specific configuration properties
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Configuration
@ConfigurationProperties(prefix = "server.feature")
public class ServerProperties {

    private Boolean escapeId;
    private Boolean implicitExpand;
    private Boolean updateFOI;
    private Boolean variableEncodingType;
    private Map<String, String> observation;

    @Deprecated
    private Boolean isMobile;

    public Boolean getImplicitExpand() {
        return implicitExpand;
    }

    public void setImplicitExpand(Boolean implicitExpand) {
        this.implicitExpand = implicitExpand;
    }

    public Boolean getUpdateFOI() {
        return updateFOI;
    }

    public void setUpdateFOI(Boolean updateFOI) {
        this.updateFOI = updateFOI;
    }

    public Boolean getVariableEncodingType() {
        return variableEncodingType;
    }

    public void setVariableEncodingType(Boolean variableEncodingType) {
        this.variableEncodingType = variableEncodingType;
    }

    @Deprecated
    public Boolean getIsMobile() {
        return isMobile;
    }

    @Deprecated
    public void setIsMobile(Boolean mobile) {
        isMobile = mobile;
    }

    public Map<String, String> getObservation() {
        return observation;
    }

    public void setObservation(Map<String, String> observation) {
        this.observation = observation;
    }

    public Boolean getEscapeId() {
        return escapeId;
    }

    public void setEscapeId(Boolean escapeId) {
        this.escapeId = escapeId;
    }

    public ObjectNode getFeatureInformation(ObjectMapper mapper) {
        ObjectNode props = mapper.createObjectNode();
        props.put("escapeId", this.getEscapeId());
        props.put("implicitExpand", this.getImplicitExpand());
        props.put("updateFOI", this.getUpdateFOI());
        props.put("variableEncodingType", this.getVariableEncodingType());
        props.put("isMobile", this.getIsMobile());
        ObjectNode observationJSON = mapper.createObjectNode();
        this.getObservation()
            .forEach(observationJSON::put);
        props.set("observation", observationJSON);
        return props;
    }

}
