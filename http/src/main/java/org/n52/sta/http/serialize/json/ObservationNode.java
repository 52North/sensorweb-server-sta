package org.n52.sta.http.serialize.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class ObservationNode extends StaNode implements Observation<Object> {

    public ObservationNode(JsonNode node, ObjectMapper mapper) {
        super(node, mapper);
    }

    @Override
    public Time getPhenomenonTime() {
        return parseTime(StaConstants.PROP_PHENOMENON_TIME);
    }

    @Override
    public Time getResultTime() {
        return parseTime(StaConstants.PROP_RESULT_TIME);
    }

    @Override
    public Object getResult() {
        // TODO return typed JSON value or object
        return getOrNull(StaConstants.PROP_RESULT, JsonNode::asText);
    }

    @Override
    public Object getResultQuality() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Time getValidTime() {
        return parseTime(StaConstants.PROP_VALID_TIME);
    }

    @Override
    public Map<String, Object> getParameters() {
        return toMap(StaConstants.PROP_PARAMETERS);
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return getOrNull(StaConstants.FEATURE_OF_INTEREST, n -> new FeatureOfInterestNode(n, mapper));
    }

    @Override
    public Datastream getDatastream() {
        return getOrNull(StaConstants.DATASTREAM, n -> new DatastreamNode(n, mapper));
    }
}
