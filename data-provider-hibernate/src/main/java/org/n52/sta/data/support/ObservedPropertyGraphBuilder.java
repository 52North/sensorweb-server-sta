package org.n52.sta.data.support;

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;

public class ObservedPropertyGraphBuilder extends GraphBuilder<PhenomenonEntity> {

    public ObservedPropertyGraphBuilder() {
        super(PhenomenonEntity.class);
        addGraphText(GraphText.GRAPH_PARAMETERS);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        if (StaConstants.DATASTREAMS.equals(expandItem.getPath())) {
            addGraphText(GraphText.GRAPH_DATASETS);
        }
    }
    
}
