package org.n52.sta.http.serialize.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class FeatureOfInterestNode extends StaNode implements FeatureOfInterest {

    public FeatureOfInterestNode(JsonNode node, ObjectMapper mapper) {
        super(node, mapper);
    }

    @Override
    public String getName() {
        return getOrNull(StaConstants.PROP_NAME, JsonNode::asText);
    }

    @Override
    public String getDescription() {
        return getOrNull(StaConstants.PROP_DESCRIPTION, JsonNode::asText);
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(StaConstants.PROP_PROPERTIES);
    }

    @Override
    public Geometry getFeature() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Observation<?>> getObservations() {
        return toSet(StaConstants.OBSERVATIONS, n -> new ObservationNode(n, mapper));
    }

}
