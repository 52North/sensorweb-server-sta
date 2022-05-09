package org.n52.sta.data.support;

import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;

public class HistoricalLocationGraphBuilder extends GraphBuilder<HistoricalLocationEntity> {

    public HistoricalLocationGraphBuilder() {
        super(HistoricalLocationEntity.class);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        switch (expandItem.getPath()) {
            case StaConstants.LOCATIONS:
                addGraphText(GraphText.GRAPH_LOCATIONS);
                break;
            case StaConstants.THING:
                // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
                // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as
                // "Thing"
                // We will allow both for now
            case StaConstants.THINGS:
                addGraphText(GraphText.GRAPH_PLATFORM);
                break;
            default:
                // no expand
        }
    }
    
}
