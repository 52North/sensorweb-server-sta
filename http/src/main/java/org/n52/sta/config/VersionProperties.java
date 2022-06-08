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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VersionProperties {

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

    public ObjectNode getVersionInformation(ObjectMapper mapper) {
        Map<String, String> result = new HashMap<>();
        // TODO make this configurable
        result.put("project.name", "52North SensorThingsAPI");
        // result.put("project.version", buildProperties.getVersion());
        // result.put("project.time", buildProperties.getTime().toString());
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
