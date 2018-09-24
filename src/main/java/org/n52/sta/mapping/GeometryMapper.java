/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mapping;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.sta.edm.provider.complextypes.FeatureComplexType;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class GeometryMapper {

    private static final String LOCATION_TYPE = "Feature";

    public ComplexValue resolveGeometry(GeometryEntity geometry) {
        //TODO: geometry creation dependend on the GeometryType
        ComplexValue value = null;
        if (geometry.getGeometry() != null) {
            value = new ComplexValue();
            Point point = new Point(Geospatial.Dimension.GEOMETRY, null);
            point.setX(geometry.getGeometry().getCoordinate().x);
            point.setY(geometry.getGeometry().getCoordinate().y);

            value.getValue().add(new Property(null, FeatureComplexType.PROP_TYPE, ValueType.PRIMITIVE, LOCATION_TYPE));
            value.getValue().add(new Property(null, FeatureComplexType.PROP_GEOMETRY, ValueType.GEOSPATIAL, point));
        }
        return value;
    }

}
