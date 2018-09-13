/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.edm.provider.complextypes;

import java.util.List;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ComplexTypeRepository {

    @Autowired
    private List<AbstractComplexType> complexTypes;

    public AbstractComplexType getComplexType(FullQualifiedName complexTypeFullQualifiedName) {
        AbstractComplexType provider = complexTypes.stream()
                .filter(p -> p.getFullQualifiedTypeName().getFullQualifiedNameAsString().equals(complexTypeFullQualifiedName.getFullQualifiedNameAsString()))
                .findAny()
                .orElse(null);
        return provider;
    }

    public List<AbstractComplexType> getComplexTypes() {
        return complexTypes;
    }

}
