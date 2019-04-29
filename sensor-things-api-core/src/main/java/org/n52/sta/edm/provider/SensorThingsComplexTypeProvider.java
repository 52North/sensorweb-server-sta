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
package org.n52.sta.edm.provider;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.n52.sta.edm.provider.complextypes.ComplexTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provider for ComplexTypes that should be defined in the CsdlSchema and can be
 * used by the EntityTypes for their PropertyTypes
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsComplexTypeProvider {

    @Autowired
    private ComplexTypeRepository complexTypeRepository;

    /**
     * Creates and delivers the ComplexTypes
     *
     * @return List of ComplexTypes
     */
    public List<CsdlComplexType> getComplexTypes() {
        return complexTypeRepository.getComplexTypes().stream()
                .map(ct -> ct.createComplexType())
                .collect(Collectors.toList());
    }

    /**
     * Creates and delivers the ComplexType that matches the given
     * FullQualifiedName
     *
     * @param complexTypeName FullQualifiedNamed of the target ComplexType
     * @return the ComplexType that matches the given FullQualified Name
     */
    CsdlComplexType getComplexType(FullQualifiedName complexTypeName) {
        return complexTypeRepository.getComplexType(complexTypeName)
                .createComplexType();
    }

}
