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

import org.hibernate.graph.EntityGraphs;
import org.hibernate.graph.GraphParser;
import org.hibernate.graph.InvalidGraphException;
import org.hibernate.graph.RootGraph;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.series.db.beans.sta.AbstractObservationEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EntityGraphBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityGraphBuilder.class);

    private final Class<T> entityType;

    private final Set<String> graphTexts;

    public EntityGraphBuilder(Class<T> entityType) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        this.entityType = entityType;
        this.graphTexts = new HashSet<>();
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
        return EntityGraphs.merge(em, entityType, graphs.toArray(new EntityGraph[0]));
    }

    /*
     * TODO this is rather ugly but might pave the path to introduce kind of a
     * configurable/extensible entity graph later on
     */
    public EntityGraphBuilder<T> addUnfilteredExpandItem(ExpandItem expandItem) {
        if (isNestedQueryOrExpand(expandItem)) {
            LOGGER.debug("Skip adding $expand to entity graph: {}", expandItem);
            // unsupported -> no expand
            return this;
        }

        handleExpandThing(expandItem);
        handleExpandSensor(expandItem);
        handleExpandLocation(expandItem);
        handleExpandDatastream(expandItem);
        handleExpandObservation(expandItem);
        handleExpandObservedProperty(expandItem);
        handleExpandHistoricalLocation(expandItem);
        handleExpandFeatureOfInterest(expandItem);
        return this;
    }

    private boolean isNestedQueryOrExpand(ExpandItem expandItem) {
        return expandItem != null
                && (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter());
    }

    private RootGraph<T> parseGraph(String path, EntityManager em) {
        return GraphParser.parse(entityType, path, em);
    }

    private void handleExpandDatastream(ExpandItem expandItem) {
        if (entityType.isAssignableFrom(AbstractDatastreamEntity.class)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
            addGraphText(GraphText.GRAPH_OM_OBS_TYPE);
            addGraphText(GraphText.GRAPH_UOM);
            if (expandItem == null) {
                return;
            }
            switch (expandItem.getPath()) {
                case StaConstants.SENSOR:
                    addGraphText(GraphText.GRAPH_PROCEDURE);
                    break;
                case StaConstants.THING:
                    addGraphText(GraphText.GRAPH_PLATFORM);
                    break;
                case StaConstants.OBSERVED_PROPERTY:
                    addGraphText(GraphText.GRAPH_PHENOMENON);
                    break;
                case StaConstants.OBSERVATIONS:
                default:
                    // no expand
            }
        }
    }

    private void handleExpandThing(ExpandItem expandItem) {
        if (PlatformEntity.class.isAssignableFrom(entityType)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
            if (expandItem == null) {
                return;
            }
            switch (expandItem.getPath()) {
                case StaConstants.HISTORICAL_LOCATIONS:
                    addGraphText(GraphText.GRAPH_HIST_LOCATIONS);
                    break;
                case StaConstants.DATASTREAMS:
                    addGraphText(GraphText.GRAPH_DATASETS);
                    break;
                case StaConstants.LOCATIONS:
                    addGraphText(GraphText.GRAPH_LOCATIONS);
                    break;
                default:
                    // no expand
            }
        }
    }

    private void handleExpandSensor(ExpandItem expandItem) {
        if (ProcedureEntity.class.isAssignableFrom(entityType)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
            addGraphText(GraphText.GRAPH_FORMAT);
            addGraphText(GraphText.GRAPH_PROCEDUREHISTORY);
            if (expandItem == null) {
                return;
            }
            if (StaConstants.DATASTREAMS.equals(expandItem.getPath())) {
                addGraphText(GraphText.GRAPH_DATASETS);
            }
        }
    }

    private void handleExpandObservedProperty(ExpandItem expandItem) {
        if (PhenomenonEntity.class.isAssignableFrom(entityType)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
            if (expandItem == null) {
                return;
            }
            if (StaConstants.DATASTREAMS.equals(expandItem.getPath())) {
                addGraphText(GraphText.GRAPH_DATASETS);
            }
        }
    }

    private void handleExpandObservation(ExpandItem expandItem) {
        if (AbstractObservationEntity.class.isAssignableFrom(entityType)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
        }
    }

    private void handleExpandLocation(ExpandItem expandItem) {
        if (LocationEntity.class.isAssignableFrom(entityType)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
            if (expandItem == null) {
                return;
            }
            switch (expandItem.getPath()) {
                case StaConstants.HISTORICAL_LOCATIONS:
                    addGraphText(GraphText.GRAPH_HIST_LOCATIONS);
                    break;
                case StaConstants.THINGS:
                    addGraphText(GraphText.GRAPH_PLATFORMS);
                    break;
                default:
                    // no expand
            }
        }
    }

    private void handleExpandHistoricalLocation(ExpandItem expandItem) {
        if (HistoricalLocationEntity.class.isAssignableFrom(entityType)) {
            if (expandItem == null) {
                return;
            }
            switch (expandItem.getPath()) {
                case StaConstants.LOCATIONS:
                    addGraphText(GraphText.GRAPH_LOCATIONS);
                    break;
                case StaConstants.THING:
                    // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
                    // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as
                    // "Thing"
                    // We will allow both for now
                case StaConstants.THINGS:
                    addGraphText(GraphText.GRAPH_PLATFORM);
                    break;
                default:
                    // no expand
            }
        }
    }

    private void handleExpandFeatureOfInterest(ExpandItem expandItem) {
        if (FeatureEntity.class.isAssignableFrom(entityType)) {
            addGraphText(GraphText.GRAPH_PARAMETERS);
            addGraphText(GraphText.GRAPH_FEATURETYPE);
        }
    }

    private EntityGraphBuilder<T> addGraphText(GraphText graphText) {
        addGraphText(graphText.value());
        return this;
    }

    /**
     * Adds a text representation of a (sub-)graph.
     *
     * @param graphText the textual graph representation
     * @return this instance
     * @deprecated this will become private
     */
    @Deprecated
    public EntityGraphBuilder<T> addGraphText(String graphText) {
        Objects.requireNonNull(graphText, "graphText must not be null");
        graphTexts.add(graphText);
        return this;
    }

}
