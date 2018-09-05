/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.edm.provider;

import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.springframework.stereotype.Component;

/**
 * Provider for ComplexTypes that should be defined in the CsdlSchema and can be
 * used by the EntityTypes for their PropertyTypes
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsComplexTypeProvider {

    /**
     * Creates and delivers the ComplexTypes
     *
     * @return List of ComplexTypes
     */
    public List<CsdlComplexType> getComplexTypes() {
        List<CsdlComplexType> complexTypes = new ArrayList();
        //TODO: define ComplexTypes
        return complexTypes;
    }

}
