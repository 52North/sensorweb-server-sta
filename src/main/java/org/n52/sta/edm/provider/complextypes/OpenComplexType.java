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
public class OpenComplexType implements AbstractComplexType {

    public static final String CT_OPEN_TYPE_NAME = "OpenComplexType";
    public static final FullQualifiedName CT_OPEN_TYPE_FQN = new FullQualifiedName(SensorThingsEdmConstants.NAMESPACE, CT_OPEN_TYPE_NAME);

    @Override
    public CsdlComplexType createComplexType() {
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(CT_OPEN_TYPE_FQN.getFullQualifiedNameAsString());
        complexType.setOpenType(true);
        return complexType;
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return CT_OPEN_TYPE_FQN;
    }

}
