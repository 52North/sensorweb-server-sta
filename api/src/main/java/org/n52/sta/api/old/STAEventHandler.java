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
package org.n52.sta.api.old;

import java.util.Map;
import java.util.Set;

import org.n52.sta.api.old.dto.common.StaDTO;

/**
 * Interface to be implemented by Handlers responding to Entity Creation (e.g. Handler for MQTT Subscription).
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface STAEventHandler {

    /**
     * Handles a Create/Update Event emitted by the Database.
     *
     * @param entity             base entity
     * @param entityType         java class name of entity
     * @param differenceMap      names of properties that changed. null if all properties changed (e.g. new entity)
     * @param relatedCollections List of related Collections
     */
    void handleEvent(StaDTO entity,
                     String entityType,
                     Set<String> differenceMap,
                     Map<String, Set<String>> relatedCollections);

    /**
     * Lists all Entity types that are monitored by this Handler. Directly matched with getJavaType().getName().
     *
     * @return Set of all watched Entity Types. Empty if Handler is inactive.
     */
    Set<String> getWatchedEntityTypes();

    void setServiceRepository(EntityServiceLookup serviceRepository);
}
