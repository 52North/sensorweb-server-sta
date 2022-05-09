package org.n52.sta.data.support;

import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;

public class LocationGraphBuilder extends GraphBuilder<LocationEntity> {

    public LocationGraphBuilder() {
        super(LocationEntity.class);
        addGraphText(GraphText.GRAPH_PARAMETERS);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        switch (expandItem.getPath()) {
            case StaConstants.HISTORICAL_LOCATIONS:
                addGraphText(GraphText.GRAPH_HIST_LOCATIONS);
                break;
            case StaConstants.THINGS:
                addGraphText(GraphText.GRAPH_PLATFORMS);
                break;
            default:
                // no expand
        }
    }
    
}
