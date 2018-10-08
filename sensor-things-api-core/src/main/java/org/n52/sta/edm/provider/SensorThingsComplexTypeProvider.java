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
