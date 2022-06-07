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

package org.n52.sta.data.provider;

import org.n52.janmayen.stream.Streams;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.ODataPath;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.Request;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.query.FilterQueryParser;
import org.n52.sta.data.query.QuerySpecificationFactory;
import org.n52.sta.data.query.specifications.BaseQuerySpecifications;
import org.n52.sta.data.query.specifications.SpecificationsException;
import org.n52.sta.data.support.GraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;

public abstract class BaseEntityProvider<T extends Identifiable> implements EntityProvider<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntityProvider.class);

    protected final EntityPropertyMapping propertyMapping;

    protected BaseEntityProvider(EntityPropertyMapping propertyMapping) {
        Objects.requireNonNull(propertyMapping, "propertyMapping must not be null!");
        this.propertyMapping = propertyMapping;
    }

    /**
     * Creates an entity graph for unfiltered entity members.
     *
     * @param <E>          the database entity type
     * @param options      the OData query options
     * @param graphBuilder the graph builder adding unfiltered expanded items
     */
    protected <E> void addUnfilteredExpandItems(QueryOptions options, GraphBuilder<E> graphBuilder) {
        if (options.hasExpandFilter()) {
            ExpandFilter expand = options.getExpandFilter();
            Streams.stream(expand.getItems()).forEach(graphBuilder::addUnfilteredExpandItem);
        }
    }

    /**
     * Assert that id is neither null or empty.
     *
     * @param id the id
     * @throws IllegalArgumentException if id is invalid
     */
    protected void assertIdentifier(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Invalid 'id': " + id);
        }
    }

    /**
     * Builds specification based on Request. Parses QueryOptions and Request-Path if present.
     *
     * @param req Request
     * @param qs  QuerySpecifications of requested Entity Type
     * @param <E> Entity Type
     * @return Specification to be used for filtering
     */
    protected <E> Specification<E> buildSpecification(Request req, BaseQuerySpecifications<E> qs) {
        // Parse QueryOptions
        Specification<E> querySpec = FilterQueryParser.parse(req.getQueryOptions(), qs);

        // Parse Path if present
        Specification<E> pathSpec = null;
        pathSpec = parsePath(req.getPath(), qs);

        return (pathSpec != null) ? pathSpec.and(querySpec) : querySpec;
    }

    private <E> Specification<E> parsePath(ODataPath path, BaseQuerySpecifications<E> qs)
        throws ProviderException {
        Specification<E> specification = null;
        try {
            List<PathSegment> segments = path.getPathSegments();

            // Segment of requested Entity
            PathSegment current = segments.get(0);
            if (current.getIdentifier().isPresent()) {
                specification = qs.equalsStaIdentifier(current.getIdentifier().get());
            }

            //TODO: implement handling of this
            if (segments.size() > 3) {
                throw new SpecificationsException("navigation via >1 relations is not implemented yet!");
            }

            if (segments.size() > 1) {
                current = segments.get(1);
                BaseQuerySpecifications<?> bqs =
                    QuerySpecificationFactory.createSpecification(current.getCollection());

                Specification<E> segmentSpec =
                    qs.applyOnMember(current.getCollection(),
                                         bqs.equalsStaIdentifier(current.getIdentifier().orElse(null)));

                return segmentSpec;
            } else {
                return specification;
            }

            // Iterate over preceding Segments and chain specifications
            /*
            BaseQuerySpecifications<?> lastQS = qs;
            Specification<?> stepSpec = specification;
                for (int i = 1; i < segments.size(); i++) {
                current = segments.get(i);

                BaseQuerySpecifications<?> bqs =
                        QuerySpecificationFactory.createSpecification(current.getCollection());

                Specification<?> segmentSpec =
                    lastQS.applyOnMember(current.getCollection(),
                                         bqs.equalsStaIdentifier(current.getIdentifier().orElse(null)));

                stepSpec = stepSpec.and(segmentSpec);
                lastQS = bqs;
                stepSpec = segmentSpec;
            }
            */
        } catch (SpecificationsException | STAInvalidFilterExpressionException e) {
            LOGGER.debug(e.getMessage());
            throw new ProviderException(e.getMessage());
        }
    }
}
