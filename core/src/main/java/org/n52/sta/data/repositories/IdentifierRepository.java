/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@NoRepositoryBean
@Transactional
public interface IdentifierRepository<T> extends JpaSpecificationExecutor<T> {

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
     *                        graph internally.
     * @return Entity found in Database. Optional.empty() otherwise
     */
    Optional<T> findByIdentifier(String identifier, String... relatedEntities);

    /**
     * Finds Entity by identifier. Fetches only Entity itself
     *
     * @param identifier Identifier of the wanted Entity
     * @return Entity found in Database. Optional.empty() otherwise
     */
    Optional<T> findByIdentifier(String identifier);

    /**
     * Deletes Entity with given Identifier
     *
     * @param identifier Identifier of the Entity
     */
    void deleteByIdentifier(String identifier);

    /**
     * Gets content of columnName of entity that is specified by spec. Used for fetching only identifier instead of
     * whole Entity
     *
     * @param spec       Specification of Entity
     * @param columnName Name of Column
     * @return Content of the column with columnName if spec matches. Optional.empty() otherwise
     */
    Optional<String> identifier(Specification<T> spec, String columnName);
}
