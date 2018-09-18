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
