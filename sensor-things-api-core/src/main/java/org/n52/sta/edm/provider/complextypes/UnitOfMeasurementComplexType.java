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
package org.n52.sta.edm.provider.complextypes;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class UnitOfMeasurementComplexType implements AbstractComplexType {

    public static final String CT_UOM_NAME = "UnitOfMeasurement";
    public static final FullQualifiedName CT_UOM_FQN = new FullQualifiedName(SensorThingsEdmConstants.NAMESPACE, CT_UOM_NAME);
    public static final String PROP_NAME = "name";
    public static final String PROP_SYMBOL = "symbol";
    public static final String PROP_DEFINITION = "definition";

    @Override
    public CsdlComplexType createComplexType() {
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(CT_UOM_FQN.getFullQualifiedNameAsString());

        List<CsdlProperty> properties = new ArrayList();
        properties.add(new CsdlProperty().setName(PROP_NAME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
        properties.add(new CsdlProperty().setName(PROP_SYMBOL).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
        properties.add(new CsdlProperty().setName(PROP_DEFINITION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));

        complexType.setProperties(properties);

        return complexType;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return CT_UOM_FQN;
    }

}
