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

package org.n52.sta.data;

import java.util.Objects;
import java.util.Optional;

import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.filter.OrderByFilter;
import org.n52.shetland.filter.OrderProperty;
import org.n52.shetland.filter.SkipTopFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.FilterConstants.SortOrder;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Implements {@code AbstractPageRequest} to support offset and limit instead of page and size.
 *
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 */
public final class StaPageRequest extends AbstractPageRequest {

    private static final long serialVersionUID = -5115884285975519825L;

    private final Sort sort;

    /**
     * Creates a new {@link StaPageRequest} with sort parameters applied.
     *
     * @param offset
     *        zero-based offset index.
     * @param limit
     *        the size of the page to be returned.
     * @param sort
     *        can be null.
     */
    private StaPageRequest(int offset, int limit, Sort sort) {
        super(offset, limit);
        this.sort = sort;
    }

    public static StaPageRequest create(QueryOptions queryOptions) {
        return StaPageRequestFactory.create(queryOptions);
    }

    @Deprecated
    public static StaPageRequest create(int offset, int limit, Sort sort) {
        return new StaPageRequest(offset, limit, sort);
    }

    @Override
    public int getPageNumber() {
        return (int) (getOffset() / getPageSize());
    }

    @Override
    public long getOffset() {
        return super.getPageNumber();
    }

    @Override
    public Pageable next() {
        return new StaPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort());
    }

    @Override
    public Pageable previous() {
        return hasPrevious()
                ? new StaPageRequest((int) getOffset() - getPageSize(), getPageSize(), getSort())
                : this;
    }

    @Override
    public Pageable first() {
        return new StaPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public int hashCode() {
        final int prime = 7;
        int result = super.hashCode();
        result = prime * result + sort.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            StaPageRequest other = (StaPageRequest) obj;
            return this.sort == other.sort;
        }
        return false;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    private static final class StaPageRequestFactory {

        private static StaPageRequest create(QueryOptions queryOptions) {
            Objects.requireNonNull(queryOptions, "queryOptions must not be null!");
            return new StaPageRequestFactory().createPageRequest(queryOptions);
        }

        private StaPageRequest createPageRequest(QueryOptions queryOptions) {
            int offset = getOffset(queryOptions);
            int limit = getLimit(queryOptions);
            Sort sort = getSort(queryOptions);
            return new StaPageRequest(offset, limit, sort);
        }

        private int getLimit(QueryOptions queryOptions) {
            return queryOptions.getTopFilter()
                               .getValue()
                               .intValue();
        }

        private int getOffset(QueryOptions queryOptions) {
            Optional<SkipTopFilter> optionalSkip = Optional.ofNullable(queryOptions.getSkipFilter());
            return optionalSkip.map(f -> f.getValue()
                                          .intValue())
                               .orElse(0);
        }

        private Sort getSort(QueryOptions queryOptions) {
            if (!queryOptions.hasOrderByFilter()) {
                return Sort.by(Sort.Direction.ASC, "staIdentifier");
            }
            Sort sort = Sort.unsorted();
            OrderByFilter orderBy = queryOptions.getOrderByFilter();
            for (OrderProperty sortProperty : orderBy.getSortProperties()) {
                Sort.Direction direction = getSortDirection(sortProperty);

                String property = sortProperty.getValueReference();
                sort = sort.and(getSort(property, direction));
            }
            return sort;
        }

        private Sort getSort(String property, Direction direction) {
            return "result".equals(property)
                    ? getResultSort(direction)
                    // TODO is there a need to check for valid property
                    : Sort.by(direction, property);
        }

        private Sort.Direction getSortDirection(OrderProperty sortProperty) {
            if (!sortProperty.isSetSortOrder()) {
                return Sort.Direction.ASC;
            }
            SortOrder sortOrder = sortProperty.getSortOrder();
            return FilterConstants.SortOrder.DESC.equals(sortOrder)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
        }

        private Sort getResultSort(Sort.Direction direction) {
            return Sort.unsorted()
                       .and(Sort.by(direction, DataEntity.PROPERTY_VALUE_BOOLEAN))
                       .and(Sort.by(direction, DataEntity.PROPERTY_VALUE_CATEGORY))
                       .and(Sort.by(direction, DataEntity.PROPERTY_VALUE_COUNT))
                       .and(Sort.by(direction, DataEntity.PROPERTY_VALUE_TEXT))
                       .and(Sort.by(direction, DataEntity.PROPERTY_VALUE_QUANTITY));
        }

    }

}
