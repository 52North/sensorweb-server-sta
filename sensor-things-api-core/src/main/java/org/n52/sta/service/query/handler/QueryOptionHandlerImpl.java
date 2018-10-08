/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.service.query.handler;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class QueryOptionHandlerImpl extends AbstractQueryOptionHandler {

    @Override
    public PropertySelectionOptions evaluatePropertySelectionOptions(UriInfo uriInfo, EdmEntityType edmEntityType) throws SerializerException {
        SelectOption selectOption = uriInfo.getSelectOption();
        String selectList = uriHelper.buildContextURLSelectList(edmEntityType,
                null, selectOption);

        PropertySelectionOptions selectOptions = new PropertySelectionOptions();
        selectOptions.setSelectOption(selectOption);
        selectOptions.setSelectionList(selectList);
        return selectOptions;
    }

    @Override
    public CountOptions evaluateCountOptions(UriInfo uriInfo, EntityCollection entityCollection) {
        CountOptions countOptions = new CountOptions();
        CountOption countOption = uriInfo.getCountOption();
        countOptions.setCountOption(countOption);
        countOptions.setCount(entityCollection.getEntities().size());

        if (countOption != null) {
            boolean isCount = countOption.getValue();
            countOptions.setIsCount(isCount);
        } else {
            countOptions.setIsCount(false);
        }
        return countOptions;
    }

}
