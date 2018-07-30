/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.edm.provider.entities;

import java.util.List;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsEntityProviderRepository {

    @Autowired
    private List<AbstractSensorThingsEntityProvider> entityProvider;

    public AbstractSensorThingsEntityProvider getEntityProvider(FullQualifiedName entityTypefullQualifiedName) {
        AbstractSensorThingsEntityProvider provider = entityProvider.stream()
                .filter(p -> p.getFullQualifiedTypeName().getFullQualifiedNameAsString().equals(entityTypefullQualifiedName.getFullQualifiedNameAsString()))
                .findAny()
                .orElse(null);
        return provider;
    }

    public AbstractSensorThingsEntityProvider getEntityProvider(String entitySetName) {
        return entityProvider.stream()
                .filter(p -> p.getEntitySet().getName().equals(entitySetName))
                .findAny()
                .orElse(null);
    }

    public List<AbstractSensorThingsEntityProvider> getEntityProviders() {
        return entityProvider;
    }

}
