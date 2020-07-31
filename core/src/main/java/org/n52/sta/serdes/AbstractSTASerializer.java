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

package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.sta.StaRelations;
import org.n52.series.db.beans.sta.mapped.extension.CSDatastream;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.serdes.util.ElementWithQueryOptions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSTASerializer<T extends ElementWithQueryOptions<I>, I extends HibernateRelations.HasId>
        extends StdSerializer<T> {

    private static final String ENCODEDSLASH = "%2F";
    private static final String SLASH = "/";

    protected String rootUrl;
    protected String entitySetName;

    protected Set<String> fieldsToSerialize;
    protected Map<String, QueryOptions> fieldsToExpand = new HashMap<>();
    protected final String[] activeExtensions;
    protected boolean hasSelectOption;
    protected boolean hasExpandOption;

    protected AbstractSTASerializer(Class<T> t, String... activeExtensions) {
        super(t);
        this.activeExtensions = activeExtensions;
    }

    protected I unwrap(T wrapped) {
        I unwrapped = wrapped.getEntity();
        QueryOptions options = wrapped.getQueryOptions();

        if (options != null) {
            if (options.hasSelectFilter()) {
                hasSelectOption = true;
                fieldsToSerialize = options.getSelectFilter().getItems();
            }
            if (options.hasExpandFilter()) {
                hasExpandOption = true;
                for (ExpandItem item : options.getExpandFilter().getItems()) {
                    fieldsToExpand.put(item.getPath(), item.getQueryOptions());
                }
            }
        }
        return unwrapped;
    }

    public void writeSelfLink(JsonGenerator gen, String id) throws IOException {
        String escaped = id.replaceAll(SLASH, ENCODEDSLASH);
        gen.writeStringField("@iot.selfLink", rootUrl + entitySetName + "(" + escaped + ")");
    }

    public void writeId(JsonGenerator gen, String id) throws IOException {
        gen.writeStringField("@iot.id", id);
    }

    public void writeNavigationProp(JsonGenerator gen, String navigationProperty, String id) throws IOException {
        String escaped = id.replaceAll(SLASH, ENCODEDSLASH);
        gen.writeStringField(navigationProperty + "@iot.navigationLink",
                             rootUrl + entitySetName + "(" + escaped + ")/" + navigationProperty);
    }

    protected void writeNestedEntity(Object expandedElement,
                                     QueryOptions queryOptions,
                                     JsonGenerator gen,
                                     SerializerProvider serializers) throws IOException {
        serializers.defaultSerializeValue(ElementWithQueryOptions.from(expandedElement, queryOptions), gen);
    }

    protected void writeNestedCollection(Set<?> expandedElements,
                                         QueryOptions queryOptions,
                                         JsonGenerator gen,
                                         SerializerProvider serializers) throws IOException {
        serializers.defaultSerializeValue(
                expandedElements
                        .stream()
                        .map(d -> ElementWithQueryOptions.from(d, queryOptions))
                        .collect(Collectors.toSet()), gen);
    }

    protected void writeNestedCollectionOfType(Set<?> expandedElements,
                                               Class<?> type,
                                               QueryOptions queryOptions,
                                               JsonGenerator gen,
                                               SerializerProvider serializers) throws IOException {
        serializers.defaultSerializeValue(
                expandedElements
                        .stream()
                        .filter(type::isInstance)
                        .map(d -> ElementWithQueryOptions.from(d, queryOptions))
                        .collect(Collectors.toSet()), gen);
    }

    protected <I extends HibernateRelations.HasStaIdentifier & StaRelations.HasDatastreams>
    void handleExtensions(I entity,
                          JsonGenerator gen,
                          SerializerProvider serializers)
            throws IOException {
        for (String activeExtension : activeExtensions) {
            switch (activeExtension) {
            case "citSciExtension":
                if (!hasExpandOption || fieldsToExpand.get(STAEntityDefinition.CSDATASTREAMS) == null ||
                        entity.getDatastreams() == null) {
                    writeNavigationProp(gen, STAEntityDefinition.CSDATASTREAMS, entity.getStaIdentifier());
                } else {
                    gen.writeFieldName(STAEntityDefinition.CSDATASTREAMS);
                    writeNestedCollectionOfType(Collections.unmodifiableSet(entity.getDatastreams()),
                                                CSDatastream.class,
                                                fieldsToExpand.get(STAEntityDefinition.CSDATASTREAMS),
                                                gen,
                                                serializers);
                }
                break;
            default:
                throw new IllegalStateException("Extension " + activeExtension + " is not valid!");

            }
        }
    }
}
