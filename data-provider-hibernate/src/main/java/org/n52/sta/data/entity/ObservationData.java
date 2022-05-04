package org.n52.sta.data.entity;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.old.utils.TimeUtil;

public class ObservationData<T> extends StaData<DataEntity<T>> implements Observation<T> {

    protected ObservationData(DataEntity<T> data) {
        super(data);
    }

    @Override
    public Time getPhenomenonTime() {
        Date samplingTimeStart = data.getSamplingTimeStart();
        Date samplingTimeEnd = data.getSamplingTimeEnd();
        Optional<DateTime> sStart = Optional.ofNullable(samplingTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> sEnd = Optional.ofNullable(samplingTimeEnd).map(TimeUtil::createDateTime);
        return sStart.map(start -> TimeUtil.createTime(start, sEnd.orElse(null))).orElse(null);
    }

    @Override
    public Time getResultTime() {
        return toTime(data.getResultTime());
    }

    @Override
    public T getResult() {
        return data.getValue();
    }

    @Override
    public Object getResultQuality() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Time getValidTime() {
        Date samplingTimeStart = data.getSamplingTimeStart();
        Date samplingTimeEnd = data.getSamplingTimeEnd();
        return toTimeInterval(samplingTimeStart, samplingTimeEnd);
    }

    @Override
    public Map<String, Object> getParameters() {
        return toMap(data.getParameters());
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return new FeatureOfInterestData(data.getFeature());
    }

    @Override
    public Datastream getDatastream() {
        return new DatastreamData(data.getDataset());
    }

}
