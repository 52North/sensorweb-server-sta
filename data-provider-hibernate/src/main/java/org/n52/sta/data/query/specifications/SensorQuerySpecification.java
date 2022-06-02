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

package org.n52.sta.data.query.specifications;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.shetland.ogc.sta.StaConstants;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Subquery;

public class SensorQuerySpecification extends QuerySpecification<ProcedureEntity> {

    public SensorQuerySpecification() {
        super();
        this.filterByMember.put(StaConstants.DATASTREAMS, new SensorQuerySpecification.DatastreamFilter());
    }

    private final class DatastreamFilter extends MemberFilterImpl<ProcedureEntity> {

        protected Specification<ProcedureEntity> prepareQuery(Specification<?> specification) {
            return (root, query, builder) -> {
                EntityQuery memberQuery = createQuery(AbstractDatasetEntity.PROPERTY_PROCEDURE,
                                                      AbstractDatasetEntity.class);
                Subquery<?> subquery = memberQuery.create(specification, query, builder);
                // 1..n
                return builder.in(root.get(IdEntity.PROPERTY_ID)).value(subquery);
            };
        }
    }
}
