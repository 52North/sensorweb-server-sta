/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.data;

import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Java Bean implementation of {@code AbstractPageRequest} to support offset and
 * limit instead of page and size.
 *
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 1.0.0
 *
 */
public class OffsetLimitBasedPageRequest extends AbstractPageRequest {

    private static final long serialVersionUID = -5115884285975519825L;

    private final Sort sort;

    /**
     * Creates a new {@link OffsetLimitBasedPageRequest}. Pages are zero indexed,
     * thus providing 0 for {@code page} will return the first page.
     *
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the size of the page to be returned.
     */
    public OffsetLimitBasedPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    /**
     * Creates a new {@link OffsetLimitBasedPageRequest} with sort parameters
     * applied.
     *
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the size of the page to be returned.
     * @param direction
     *            the direction of the {@link Sort} to be specified, can be
     *            {@literal null}.
     * @param properties
     *            the properties to sort by, must not be {@literal null} or
     *            empty.
     */
    public OffsetLimitBasedPageRequest(int offset, int limit, Direction direction, String... properties) {
        this(offset, limit, Sort.by(direction, properties));
    }

    /**
     * Creates a new {@link OffsetLimitBasedPageRequest} with sort parameters
     * applied.
     *
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the size of the page to be returned.
     * @param sort
     *            can be {@literal null}.
     */
    public OffsetLimitBasedPageRequest(int offset, int limit, Sort sort) {
        super(offset, limit);
        this.sort = sort;
    }

    @Override
    public long getOffset() {
        return super.getPageNumber();
    }

    @Override
    public int getPageNumber() {
        return (int) super.getOffset() / getPageSize();
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetLimitBasedPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort());
    }

    @Override
    public Pageable previous() {
        return hasPrevious() ? new OffsetLimitBasedPageRequest((int) getOffset() - getPageSize(), getPageSize(), getSort())
                : this;
    }

    @Override
    public Pageable first() {
        return new OffsetLimitBasedPageRequest(0, getPageSize(), getSort());
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
            OffsetLimitBasedPageRequest other = (OffsetLimitBasedPageRequest) obj;
            return this.sort == other.sort;
        }
        return false;
    }

}
