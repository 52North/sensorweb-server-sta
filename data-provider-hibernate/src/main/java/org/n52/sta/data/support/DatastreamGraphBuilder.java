package org.n52.sta.data.support;

import org.n52.series.db.beans.sta.AbstractDatastreamEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;

public class DatastreamGraphBuilder extends GraphBuilder<AbstractDatastreamEntity> {

    public DatastreamGraphBuilder() {
        super(AbstractDatastreamEntity.class);
        addGraphText(GraphText.GRAPH_PARAMETERS);
        addGraphText(GraphText.GRAPH_OM_OBS_TYPE);
        addGraphText(GraphText.GRAPH_UOM);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        if (expandItem != null) {
            switch (expandItem.getPath()) {
                case StaConstants.SENSOR:
                    addGraphText(GraphText.GRAPH_PROCEDURE);
                    break;
                case StaConstants.THING:
                    addGraphText(GraphText.GRAPH_PLATFORM);
                    break;
                case StaConstants.OBSERVED_PROPERTY:
                    addGraphText(GraphText.GRAPH_PHENOMENON);
                    break;
                case StaConstants.OBSERVATIONS:
                default:
                    // no expand
            }
        }
    }
}
