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

package org.n52.sta.data.query;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.mapped.extension.CSDatastream;
import org.n52.series.db.beans.sta.mapped.extension.License;
import org.n52.series.db.beans.sta.mapped.extension.Party;
import org.n52.series.db.beans.sta.mapped.extension.Project;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class CSDatastreamQuerySpecifications extends EntityQuerySpecifications<CSDatastream> {

    public Specification<CSDatastream> withPartyStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<CSDatastream, Party> join =
                    root.join(CSDatastream.PROPERTY_PARTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    public Specification<CSDatastream> withProjectStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<CSDatastream, Project> join =
                    root.join(CSDatastream.PROPERTY_PROJECT, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    public Specification<CSDatastream> withLicenseStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<CSDatastream, License> join =
                    root.join(CSDatastream.PROPERTY_LICENSE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    public Specification<CSDatastream> withThingStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<CSDatastream, DatastreamEntity> join =
                    root.join(CSDatastream.PROPERTY_DATASTREAM, JoinType.INNER);
            return builder.equal(join.get(DatastreamEntity.PROPERTY_THING)
                                     .get(PlatformEntity.STA_IDENTIFIER), identifier);
        };
    }

    public Specification<CSDatastream> withSensorStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<CSDatastream, DatastreamEntity> join =
                    root.join(CSDatastream.PROPERTY_DATASTREAM, JoinType.INNER);
            return builder.equal(join.get(DatastreamEntity.PROPERTY_SENSOR)
                                     .get(ProcedureEntity.STA_IDENTIFIER), identifier);
        };
    }

    public Specification<CSDatastream> withObsPropStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<CSDatastream, DatastreamEntity> join =
                    root.join(CSDatastream.PROPERTY_DATASTREAM, JoinType.INNER);
            return builder.equal(join.get(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY)
                                     .get(ObservablePropertyEntity.STA_IDENTIFIER),
                                 identifier);
        };
    }

    @Override
    protected Specification<CSDatastream> handleRelatedPropertyFilter(String propertyName,
                                                                      Specification<?> propertyValue)
            throws STAInvalidFilterExpressionException {
        return null;
    }

    @Override protected Specification<CSDatastream> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return null;
    }
}
