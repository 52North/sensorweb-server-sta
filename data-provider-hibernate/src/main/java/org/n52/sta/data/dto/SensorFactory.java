package org.n52.sta.data.dto;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.sta.api.dto.SensorDto;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Sensor;

public class SensorFactory extends BaseDtoFactory<SensorDto, SensorFactory> {

    public static Sensor create(ProcedureEntity entity) {
        SensorFactory factory = create();
        factory.withMetadata(entity);
        factory.setProperties(entity);
        factory.setSensorMetadata(entity);
        factory.setDatastreams(entity);

        return factory.get();
    }

    public static SensorFactory create() {
        return new SensorFactory(new SensorDto());
    }

    private SensorFactory(SensorDto dto) {
        super(dto);
    }

    private SensorFactory setSensorMetadata(ProcedureEntity entity) {
        FormatEntity format = entity.getFormat();
        String file = entity.getDescriptionFile();
        Optional<String> metadata = useLinkToFile(file).or(getXml(entity));
        return setSensorMetadata(format.getFormat(), metadata.orElse(null));
    }

    private Optional<String> useLinkToFile(String file) {
        return Optional.ofNullable(file).filter(f -> !f.isEmpty());
    }

    private Supplier<Optional<String>> getXml(ProcedureEntity entity) {
        return () -> {
            Set<ProcedureHistoryEntity> history = entity.getProcedureHistory();
            return findCurrentFrom(history).map(ProcedureHistoryEntity::getXml).findFirst();
        };
    }

    private Stream<ProcedureHistoryEntity> findCurrentFrom(Set<ProcedureHistoryEntity> history) {
        return Streams.stream(history).filter(isLatest());
    }

    private Predicate<? super ProcedureHistoryEntity> isLatest() {
        return entity -> entity.getEndTime() == null;
    }

    public SensorFactory setSensorMetadata(String encodingType, String metadata) {
        get().setEncodingType(encodingType);
        get().setMetadata(metadata);
        return this;
    }

    private SensorFactory setDatastreams(ProcedureEntity entity) {
        Set<AbstractDatasetEntity> datasets = entity.getDatasets();
        Streams.stream(datasets).forEach(this::addDatastream);
        return this;
    }

    private SensorFactory addDatastream(AbstractDatasetEntity entity) {
        return addDatastream(DatastreamFactory.create(entity));
    }

    public SensorFactory addDatastream(Datastream datastream) {
        get().addDatastream(datastream);
        return this;
    }

}