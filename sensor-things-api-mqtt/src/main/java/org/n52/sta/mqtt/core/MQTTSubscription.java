/*
 * Copyright (C) 2012-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.mqtt.core;

import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class MQTTSubscription {

    private String topic;

    private Set<String> fields;

    private String[][] pattern;

    private String olingoEntityType;

    private boolean isCollection;

    public MQTTSubscription(String topic) throws Exception {
        this.topic = topic;

        // Parse Select Parameter into select List
        String[] select = topic.split("$select=");
        switch(select.length) {
        case 1: break;
        case 2: {
            String[] field = select[1].split(",");
            for (String elem : field) {
                fields.add(elem);
            }
            break;
        }
        default:
            throw new Exception("Invalid topic supplied! Found double '$select' in topic pattern.");
        }

        // Parse Topic into ResourceName+ResourceId Pairs
        String[] rawpattern = select[0].split("/");
        pattern = new String[rawpattern.length][2];
        for (int i = 0; i < rawpattern.length; i++) {
            String[] splitpattern = rawpattern[i].split("\\(|\\)");
            pattern[i][0] = splitpattern[0];
            if (splitpattern.length == 2) {
                pattern[i][1] = splitpattern[1];
            } else if (splitpattern.length > 2){
                throw new Exception("Invalid topic supplied! Cannot Get Resource Ids from Resource Path.");
            }
        }

        // Parse Entitytype
        switch(rawpattern[rawpattern.length-1].split("\\(")[0]) {
        case "Observations":
            isCollection = true;
        case "Observation":
            olingoEntityType = "iot.Observation";
            break;
        }
    }

    public String checkSubscription(Entity entity) {
        return matches(entity) ? topic : null;
    }

    private boolean matches(Entity entity) {
        // Check type and fail-fast on type mismatched
        if (!(entity.getType().equals(olingoEntityType))) {
            return false;
        }

        // Check ID (if not collection) and fail-fast if wrong id is present
        if (!isCollection && !pattern[pattern.length-1][1].equals(entity.getProperty("id").getValue())) {
            return false;
        }

        //TODO: Check for more complex Paths
        // Check Resource Path for complete Match
//        for (String[] elem : pattern) {
//            String resource = elem[0];
//            String resourceId = elem[1];
//
//            Link navLink = entity.getNavigationLink(resource);
//            if (navLink == null) {
//                return false;
//            }
//            navLink.getTitle();
//
//            // navLink.get
//            // Check Id
//        }
        return true;
    }

    public String getEntityType() {
        return olingoEntityType;
    }
    
    public String getTopic() {
        return topic;
    }
    
    @Override
    public boolean equals(Object other) {
        return (other instanceof MQTTSubscription && ((MQTTSubscription)other).getTopic().equals(this.topic));
    }
}
