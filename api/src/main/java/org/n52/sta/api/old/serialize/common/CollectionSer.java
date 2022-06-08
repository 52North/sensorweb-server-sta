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

package org.n52.sta.api.old.serialize.common;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.n52.shetland.filter.SkipTopFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.sta.api.old.CollectionWrapper;
import org.n52.sta.api.old.dto.common.StaDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class CollectionSer extends StdSerializer<CollectionWrapper> {

    public CollectionSer(Class<CollectionWrapper> t) {
        super(t);
    }

    @Override
    public void serialize(CollectionWrapper value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();

        if (value.getTotalEntityCount() != -1) {
            gen.writeNumberField("@iot.count", value.getTotalEntityCount());
        }
        // We have multiple pages
        if (value.hasNextPage()
                && !value.getEntities()
                         .isEmpty()) {
            QueryOptions queryOptions = value.getEntities()
                                             .get(0)
                                             .getQueryOptions();
            long oldTop = queryOptions.getTopFilter()
                                      .getValue();
            long oldSkip = queryOptions.hasSkipFilter()
                    ? queryOptions.getSkipFilter()
                                  .getValue()
                    : 0L;
            // Replace old skip Filter with new one
            Set<FilterClause> allFilters = queryOptions.getAllFilters();
            allFilters.remove(queryOptions.getSkipFilter());
            allFilters.add(new SkipTopFilter(FilterConstants.SkipTopOperator.Skip, oldSkip + oldTop));
            gen.writeStringField("@iot.nextLink",
                                 value.getRequestURL()
                                         + "?"
                                         + new QueryOptions(allFilters).toString());
        }

        gen.writeArrayFieldStart("value");
        for (StaDTO element : value.getEntities()) {
            provider.defaultSerializeValue(element, gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
