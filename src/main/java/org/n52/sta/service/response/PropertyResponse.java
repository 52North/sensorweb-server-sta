/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.response;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmType;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class PropertyResponse {

    private Property property;

    private EdmType edmPropertyType;

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public EdmType getEdmPropertyType() {
        return edmPropertyType;
    }

    public void setEdmPropertyType(EdmType edmPropertyType) {
        this.edmPropertyType = edmPropertyType;
    }

    public EdmEntitySet getResponseEdmEntitySet() {
        return responseEdmEntitySet;
    }

    public void setResponseEdmEntitySet(EdmEntitySet responseEdmEntitySet) {
        this.responseEdmEntitySet = responseEdmEntitySet;
    }

    private EdmEntitySet responseEdmEntitySet;

}
