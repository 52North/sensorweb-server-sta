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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to annotate Entities
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityAnnotator {

    /**
     * Annotates an entity with it's self link and navigation links
     *
     * @param entity       the Entity to annotate
     * @param entityType   the type of the entity to annotate
     * @param baseUri      the baseUri for the service deployed
     * @param selectOption select options
     * @return the annotated Entity
     */
    public Entity annotateEntity(Entity entity, EdmEntityType entityType, String baseUri, SelectOption selectOption) {
        // Do not annotate if there is nothing to annotate
        if (entity == null) {
            return null;
        }

        // Only annotate with selected Annotations if present
        List<String> selector = new ArrayList<>();
        boolean hasSelectOption = selectOption != null;
        if (hasSelectOption) {
            selectOption.getSelectItems().forEach(
                elem -> selector.add(elem.getResourcePath().getUriResourceParts().get(0).getSegmentValue())
            );
        }

        if (!hasSelectOption) {
            String selfLinkValue = String.join("/", baseUri, entity.getId().getRawPath());
            Link selfLink = new Link();
            selfLink.setTitle(SensorThingsEdmConstants.SELF_LINK_ANNOTATION);
            selfLink.setHref(selfLinkValue);
            entity.setSelfLink(selfLink);
        }

        entityType.getNavigationPropertyNames().forEach(np -> {
            if (!hasSelectOption || selector.contains(np)) {
                EdmNavigationProperty navProp = entityType.getNavigationProperty(np);

                String navigationAnnotationValue = String.join(
                        "/",
                        baseUri,
                        entity.getId().getRawPath(),
                        navProp.getName());

                Link link = new Link();
                link.setTitle(np);
                link.setHref(navigationAnnotationValue);
                entity.getNavigationLinks().add(link);
            }
        });

        return entity;
    }

}
