package org.n52.sta.data.support;

import org.n52.series.db.beans.ProcedureEntity;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;

public class SensorGraphBuilder extends GraphBuilder<ProcedureEntity> {

    public SensorGraphBuilder() {
        super(ProcedureEntity.class);
        addGraphText(GraphText.GRAPH_PARAMETERS);
        addGraphText(GraphText.GRAPH_FORMAT);
        addGraphText(GraphText.GRAPH_PROCEDUREHISTORY);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        if (StaConstants.DATASTREAMS.equals(expandItem.getPath())) {
            addGraphText(GraphText.GRAPH_DATASETS);
        }
    }
    
}
