package org.n52.sta.data.entity;

import java.util.Map;
import java.util.Set;

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.ObservedProperty;

public class ObservedPropertyData extends StaData<PhenomenonEntity> implements ObservedProperty {

    protected ObservedPropertyData(PhenomenonEntity dataEntity) {
        super(dataEntity);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public String getDefinition() {
        return data.getIdentifier();
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(data.getDatasets(), DatastreamData::new);
    }

}
