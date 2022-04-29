package org.n52.sta.http.serialize.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Thing;

public class HistoricalLocationNode extends StaNode implements HistoricalLocation {

    public HistoricalLocationNode(JsonNode node, ObjectMapper mapper) {
        super(node, mapper);
    }

    @Override
    public Time getTime() {
        return parseTime(StaConstants.PROP_TIME);
    }

    @Override
    public Set<Location> getLocations() {
        return toSet(StaConstants.LOCATIONS, n -> new LocationNode(n, mapper));
    }

    @Override
    public Thing getThing() {
        return getOrNull(StaConstants.THING, thing -> new ThingNode(thing, mapper));
    }

}
