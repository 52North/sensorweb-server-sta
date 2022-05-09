package org.n52.sta.data.support;

import org.n52.series.db.beans.PlatformEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;

public class ThingGraphBuilder extends GraphBuilder<PlatformEntity> {

    public ThingGraphBuilder() {
        super(PlatformEntity.class);
        addGraphText(GraphText.GRAPH_PARAMETERS);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        if (expandItem != null) {
            switch (expandItem.getPath()) {
                case StaConstants.HISTORICAL_LOCATIONS:
                    addGraphText(GraphText.GRAPH_HIST_LOCATIONS);
                    break;
                case StaConstants.DATASTREAMS:
                    addGraphText(GraphText.GRAPH_DATASETS);
                    break;
                case StaConstants.LOCATIONS:
                    addGraphText(GraphText.GRAPH_LOCATIONS);
                    break;
                default:
                    // no expand
            }
        }
    }
}
