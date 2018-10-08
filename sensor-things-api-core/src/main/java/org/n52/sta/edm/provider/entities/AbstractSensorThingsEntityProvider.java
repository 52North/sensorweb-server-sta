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
package org.n52.sta.edm.provider.entities;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import static org.n52.sta.edm.provider.SensorThingsEdmConstants.NAMESPACE;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public abstract class AbstractSensorThingsEntityProvider implements InitializingBean {

    public static final String CONTROL_ANNOTATION_PREFIX = "@" + NAMESPACE;

    public static final String ID_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".id";

    public static final String SELF_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".selfLink";

    public static final String NAVIGATION_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".navigationLink";
    
    public static final String NEXT_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".nextLink";

    // Entity Property Names
    public static final String PROP_DEFINITION = "definition";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_ENCODINGTYPE = "encodingType";
    public static final String PROP_FEATURE = "feature";
    public static final String PROP_LOCATION = "location";
    public static final String PROP_NAME = "name";
    public static final String PROP_OBSERVATION_TYPE = "observationType";
    public static final String PROP_OBSERVED_AREA = "observedArea";
    public static final String PROP_PARAMETERS = "parameters";
    public static final String PROP_PHENOMENON_TIME = "phenomenonTime";
    public static final String PROP_PROPERTIES = "properties";
    public static final String PROP_RESULT = "result";
    public static final String PROP_RESULT_QUALITY = "resultQuality";
    public static final String PROP_RESULT_TIME = "resultTime";
    public static final String PROP_TIME = "time";
    public static final String PROP_UOM = "unitOfMeasurement";
    public static final String PROP_VALID_TIME = "validTime";
    public static final String PROP_METADATA = "metadata";

    private CsdlEntityType entityType;
    private CsdlEntitySet entitySet;

    @Override
    public void afterPropertiesSet() throws Exception {
        entityType = createEntityType();
        entitySet = createEntitySet();
    }

    public CsdlEntityType getEntityType() {
        return entityType;
    }

    public CsdlEntitySet getEntitySet() {
        return entitySet;
    }

    public abstract FullQualifiedName getFullQualifiedTypeName();

    protected abstract CsdlEntityType createEntityType();

    protected abstract CsdlEntitySet createEntitySet();

}
