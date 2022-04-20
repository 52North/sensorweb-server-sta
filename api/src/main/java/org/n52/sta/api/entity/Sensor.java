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
package org.n52.sta.api.entity;

import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object for handling a Sensor as specified in 18-088 OGC SensorThingsAPI Part I Section 8.2.5
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface Sensor extends Identifiable {

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getEncodingType();

    void setEncodingType(String encodingType);

    String getMetadata();

    void setMetadata(String metadata);

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    Set<Datastream> getDatastreams();

    void setDatastreams(Set<Datastream> datastreams);

}
