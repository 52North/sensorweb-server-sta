/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.data.common.repositories;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@NoRepositoryBean
@Transactional
public interface IdentifierRepository<T, I> extends EntityGraphRepository<T, I> {

    /**
     * Checks whether Entity with given id exists.
     *
     * @param identifier Identifier of the Entity
     * @return true if Entity exists. false otherwise
     */
    boolean existsByIdentifier(String identifier);

    /**
     * Finds Entity by identifier. Fetches Entity and all related Entities given by EntityGraphs
     *
     * @param identifier      Identifier of the wanted Entity
     * @param relatedEntities EntityGraphs describing related Entities to be fetched. All graphs are merged into one
     *                        graph internally. may be null.
     * @return Entity found in Database. Optional.empty() otherwise
     */
    Optional<T> findByIdentifier(String identifier, FetchGraph... relatedEntities);

    /**
     * Deletes Entity with given Identifier
     *
     * @param identifier Identifier of the Entity
     */
    void deleteByIdentifier(String identifier);
}
