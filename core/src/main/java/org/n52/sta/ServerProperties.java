/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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
package org.n52.sta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds 52N-STA specific configuration properties
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Configuration
@ConfigurationProperties(prefix = "server.feature")
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ServerProperties {

    private final BuildProperties buildProperties;
    @Value("${project.version}")
    private String projectVersion;

    @Value("${git.build.time}")
    private String buildTime;

    @Value("${git.remote.origin.url}")
    private String repository;

    @Value("${git.branch}")
    private String branch;

    @Value("${git.commit.id.full}")
    private String commitId;

    @Value("${git.commit.time}")
    private String commitTime;

    @Value("${git.commit.message.short}")
    private String commitMessage;

    private Boolean escapeId;
    private Boolean implicitExpand;
    private Boolean updateFOI;
    private Set<String> mqttPublishTopics;
    private Boolean variableEncodingType;
    private Boolean isMobile;
    private Boolean mqttReadOnly;
    private Boolean httpReadOnly;
    private Map<String, String> observation;

    public ServerProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

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

    public Set<String> getMqttPublishTopics() {
        return mqttPublishTopics;
    }

    public void setMqttPublishTopics(Set<String> mqttPublishTopics) {
        this.mqttPublishTopics = mqttPublishTopics;
    }

    public Boolean getVariableEncodingType() {
        return variableEncodingType;
    }

    public void setVariableEncodingType(Boolean variableEncodingType) {
        this.variableEncodingType = variableEncodingType;
    }

    public Boolean getIsMobile() {
        return isMobile;
    }

    public void setIsMobile(Boolean mobile) {
        isMobile = mobile;
    }

    public Boolean getMqttReadOnly() {
        return mqttReadOnly;
    }

    public void setMqttReadOnly(Boolean mqttReadOnly) {
        this.mqttReadOnly = mqttReadOnly;
    }

    public Boolean getHttpReadOnly() {
        return httpReadOnly;
    }

    public void setHttpReadOnly(Boolean httpReadOnly) {
        this.httpReadOnly = httpReadOnly;
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
        props.put("mqttReadOnly", this.getMqttReadOnly());
        props.put("httpReadOnly", this.getHttpReadOnly());
        props.put("mqttPublishTopics", String.join(",", this.getMqttPublishTopics()));
        ObjectNode observationJSON = mapper.createObjectNode();
        this.getObservation().forEach(observationJSON::put);
        props.put("observation", observationJSON);
        return props;
    }

    public ObjectNode getVersionInformation(ObjectMapper mapper) {
        Map<String, String> result = new HashMap<>();
        result.put("project.name", "52North SensorThingsAPI");
        result.put("project.version", buildProperties.getVersion());
        result.put("project.time", buildProperties.getTime().toString());
        result.put("git.builddate", buildTime);
        result.put("git.repository", repository);
        result.put("git.path", branch);
        result.put("git.revision", commitId);
        result.put("git.lastCommitMessage", commitMessage);
        result.put("git.lastCommitDate", commitTime);
        ObjectNode json = mapper.createObjectNode();
        result.forEach(json::put);
        return json;
    }

}
