/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    protected static final String CONTROL_ANNOTATION_PREFIX = "@" + NAMESPACE;

    public static final String ID_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".id";

    public static final String SELF_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".selfLink";

    public static final String NAVIGATION_LINK_ANNOTATION = CONTROL_ANNOTATION_PREFIX + ".navigationLink ";

    // Entity Property Names
    public static final String PROP_NAME = "name";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_PROPERTIES = "properties";

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
