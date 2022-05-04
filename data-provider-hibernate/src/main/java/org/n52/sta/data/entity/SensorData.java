package org.n52.sta.data.entity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Sensor;

public class SensorData extends StaData<ProcedureEntity> implements Sensor {

    public SensorData(ProcedureEntity data) {
        super(data);
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
    public String getEncodingType() {
        FormatEntity format = data.getFormat();
        return format.getFormat();
    }

    @Override
    public String getMetadata() {
        String file = data.getDescriptionFile();
        return useLinkToFile(file).orElse(getXml(data).orElse(null));
    }

    @Override
    public Map<String, Object> getProperties() {
        return toMap(data.getParameters());
    }

    @Override
    public Set<Datastream> getDatastreams() {
        return toSet(data.getDatasets(), DatastreamData::new);
    }

    private Optional<String> useLinkToFile(String file) {
        return Optional.ofNullable(file).filter(f -> !f.isEmpty());
    }

    private Optional<String> getXml(ProcedureEntity entity) {
        Set<ProcedureHistoryEntity> history = entity.getProcedureHistory();
        return findCurrentFrom(history).map(ProcedureHistoryEntity::getXml).findFirst();
    }

    private Stream<ProcedureHistoryEntity> findCurrentFrom(Set<ProcedureHistoryEntity> history) {
        return Streams.stream(history).filter(isLatest());
    }

    private Predicate<? super ProcedureHistoryEntity> isLatest() {
        return entity -> entity.getEndTime() == null;
    }

}
