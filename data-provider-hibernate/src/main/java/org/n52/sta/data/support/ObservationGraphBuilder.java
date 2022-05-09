package org.n52.sta.data.support;

import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.filter.ExpandItem;

public class ObservationGraphBuilder extends GraphBuilder<DataEntity<?>> {

    public ObservationGraphBuilder(Class<DataEntity<?>> entityType) {
        super(entityType);
        addGraphText(GraphText.GRAPH_PARAMETERS);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        // no member to expand
    }
    
}
