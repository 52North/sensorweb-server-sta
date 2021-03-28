/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data.vanilla;

import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class MutexFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MutexFactory.class);

    // MutexMap used for locking during thread-bound in-memory computations on database entities
    private ConcurrentReferenceHashMap<String, Object> lock;

    public MutexFactory() {
        this.lock = new ConcurrentReferenceHashMap<>();
    }

    /**
     * Gets a lock with given name from global lockMap. Name is unique per EntityType.
     * Uses weak references so Map is automatically cleared by GC.
     *
     * @param key name of the lock
     * @return Object used for holding the lock
     * @throws STACRUDException If the lock can not be obtained.
     */
    public synchronized Object getLock(String key) throws STACRUDException {
        if (key != null) {
            LOGGER.trace("Locking:" + key);
            return this.lock.compute(key, (k, v) -> v == null ? new Object() : v);
        } else {
            throw new STACRUDException("Unable to obtain Lock. No name specified!");
        }
    }

}
