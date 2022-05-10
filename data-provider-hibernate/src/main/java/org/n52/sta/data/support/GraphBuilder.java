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
package org.n52.sta.data.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.graph.EntityGraphs;
import org.hibernate.graph.GraphParser;
import org.hibernate.graph.InvalidGraphException;
import org.hibernate.graph.RootGraph;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphBuilder.class);

    private final Class<T> entityType;

    private final Set<String> graphTexts;

    protected GraphBuilder(Class<T> entityType) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        this.entityType = entityType;
        this.graphTexts = new HashSet<>();
    }

    public abstract void addExpanded(ExpandItem expandItem);

    public void addUnfilteredExpandItem(ExpandItem expandItem) {
        if (isNestedQueryOrExpand(expandItem)) {
            // unsupported -> no expand
            LOGGER.debug("Skip adding $expand to entity graph: {}", expandItem);
        } else {

            // TODO do this recursively by using dots
            // this may require some refactoring how the actual member/db-properties
            // are  being mapped (currently via switch statements)

            // DISCUSS adding more entity graphs recursively is an
            // anti pattern as it makes the query bigger, and OData
            // makes it completely worse as it would allow to expand
            // deep entity paths on collections!
            // 
            // Options:
            // - configurable limit of expanded entities (restricts standard)
            // - do n+1 query (db anti pattern)
            // - ...

            addExpanded(expandItem);
        }
    }
    
    /**
     * Builds an entity graph based on all entries added via addGraphText().
     *
     * @param em the entity manager
     * @return an entity graph or null when no members have been added.
     */
    @SuppressWarnings("unchecked")
    public EntityGraph<T> buildGraph(EntityManager em) {
        if (graphTexts.isEmpty()) {
            return null;
        }
        List<EntityGraph<T>> graphs = new ArrayList<>();
        for (String graphText : graphTexts) {
            try {
                graphs.add(parseGraph(graphText, em));
            } catch (InvalidGraphException e) {
                LOGGER.error("Invalid graph text: " + graphText, e);
                throw new STAInvalidQueryError("Invalid expand!");
            }
        }
        EntityManager provider = em.unwrap(SessionImplementor.class);
        return EntityGraphs.merge(provider, entityType, graphs.toArray(new EntityGraph[0]));
    }

    protected boolean isNestedQueryOrExpand(ExpandItem expandItem) {
        return expandItem != null
                && (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter());
    }

    protected GraphBuilder<T> addGraphText(GraphText graphText) {
        addGraphText(graphText.value());
        return this;
    }

    /**
     * Adds a text representation of a (sub-)graph.
     *
     * @param graphText the textual graph representation
     * @return this instance
     */
    protected GraphBuilder<T> addGraphText(String graphText) {
        Objects.requireNonNull(graphText, "graphText must not be null");
        graphTexts.add(graphText);
        return this;
    }

    private RootGraph<T> parseGraph(String path, EntityManager em) {
        SessionImplementor provider = em.unwrap(SessionImplementor.class);
        return GraphParser.parse(entityType, path, provider);
    }


}
