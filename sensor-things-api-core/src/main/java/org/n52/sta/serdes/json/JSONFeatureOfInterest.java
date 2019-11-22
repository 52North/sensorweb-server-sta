package org.n52.sta.serdes.json;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.data.service.ServiceUtils;
import org.springframework.util.Assert;

public class JSONFeatureOfInterest extends JSONBase.JSONwithIdNameDescription implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String encodingType;
    public String feature;


    private final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";
    private final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);


    public JSONFeatureOfInterest() {
    }

    public FeatureEntity toEntity() {
        FeatureEntity featureOfInterest = new FeatureEntity();

        if (!generatedId && name != null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(feature, INVALID_REFERENCED_ENTITY);

            featureOfInterest.setIdentifier(identifier);
            return featureOfInterest;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(feature, INVALID_INLINE_ENTITY + "feature");
            Assert.state(encodingType.equals(ENCODINGTYPE_GEOJSON),
                    "Invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported!");

            featureOfInterest.setIdentifier(identifier);
            featureOfInterest.setName(name);
            featureOfInterest.setDescription(description);

            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                featureOfInterest.setGeometry(reader.read(feature));
            } catch (ParseException e) {
                Assert.notNull(null, "Could not parse feature to GeoJSON. Error was:" + e.getMessage());
            }
            featureOfInterest.setFeatureType(ServiceUtils.createFeatureType(featureOfInterest.getGeometry()));

            return featureOfInterest;
        }
    }
}
