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

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
//@Component
public class DatastreamEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_DATASTREAM_NAME = "Datastream";
    public static final FullQualifiedName ET_DATASTREAM_FQN = new FullQualifiedName(NAMESPACE, ET_DATASTREAM_NAME);

    // Entity Set Name
    public static final String ES_DATASTREAMS_NAME = "Datastreams";

    @Override
    protected CsdlEntityType createEntityType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected CsdlEntitySet createEntitySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_DATASTREAM_FQN;
    }

}
