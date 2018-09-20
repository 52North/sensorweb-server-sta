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
public class FeatureComplexType implements AbstractComplexType {
    
    public static final String CT_FEATURE_NAME = "Feature";
    public static final FullQualifiedName CT_FEATURE_FQN = new FullQualifiedName(SensorThingsEdmConstants.NAMESPACE, CT_FEATURE_NAME);
    public static final String PROP_TYPE = "type";
    public static final String PROP_GEOMETRY = "geometry";
    
    @Override
    public CsdlComplexType createComplexType() {
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(CT_FEATURE_FQN.getFullQualifiedNameAsString());
        
        List<CsdlProperty> properties = new ArrayList();
        properties.add(new CsdlProperty().setName(PROP_TYPE)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setNullable(false));
        properties.add(new CsdlProperty().setName(PROP_GEOMETRY)
                .setType(EdmPrimitiveTypeKind.Geometry.getFullQualifiedName())
                .setNullable(false));
        
        complexType.setProperties(properties);
        
        return complexType;
    }
    
    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return CT_FEATURE_FQN;
    }
    
}
