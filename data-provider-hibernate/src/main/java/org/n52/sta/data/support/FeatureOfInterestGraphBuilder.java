package org.n52.sta.data.support;

import org.n52.series.db.beans.sta.StaFeatureEntity;
import org.n52.shetland.filter.ExpandItem;

public class FeatureOfInterestGraphBuilder extends GraphBuilder<StaFeatureEntity<?>> {

    protected FeatureOfInterestGraphBuilder(Class<StaFeatureEntity<?>> entityType) {
        super(entityType);
        addGraphText(GraphText.GRAPH_PARAMETERS);
        addGraphText(GraphText.GRAPH_FEATURETYPE);
    }

    @Override
    public void addExpanded(ExpandItem expandItem) {
        // no member to expand
        
    }
    
}
