package org.n52.sta.http.serialize.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class ThingNode extends StaNode implements Thing {

    public ThingNode(JsonNode node, ObjectMapper mapper) {
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
    public Set<HistoricalLocation> getHistoricalLocations() {
        return toSet(StaConstants.HISTORICAL_LOCATIONS, n -> new HistoricalLocationNode(n, mapper));
    }

    @Override
    public Set<Location> getLocations() {
        return toSet(StaConstants.LOCATIONS, n -> new LocationNode(n, mapper));
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(StaConstants.DATASTREAMS, n -> new DatastreamNode(n, mapper));
    }

}
