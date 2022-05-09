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

    /*
     * TODO this is rather ugly but might pave the path to introduce kind of a
     * configurable/extensible entity graph later on
     */
    public void addUnfilteredExpandItem(ExpandItem expandItem) {
        if (isNestedQueryOrExpand(expandItem)) {
            // unsupported -> no expand
            LOGGER.debug("Skip adding $expand to entity graph: {}", expandItem);
        } else {
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
