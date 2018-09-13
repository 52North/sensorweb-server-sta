/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.edm.provider.complextypes;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class PropertiesComplexType implements AbstractComplexType {

    public static final String CT_PROPERTIES_NAME = "Properties";
    public static final FullQualifiedName CT_PROPERTIES_FQN = new FullQualifiedName(SensorThingsEdmConstants.NAMESPACE, CT_PROPERTIES_NAME);

    @Override
    public CsdlComplexType createComplexType() {
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(CT_PROPERTIES_FQN.getFullQualifiedNameAsString());
        complexType.setOpenType(true);
        return complexType;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return CT_PROPERTIES_FQN;
    }

}
