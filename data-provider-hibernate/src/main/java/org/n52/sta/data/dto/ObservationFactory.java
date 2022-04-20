package org.n52.sta.data.dto;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.sta.api.dto.ObservationDto;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.utils.TimeUtil;

public class ObservationFactory<T> {

    public static <E> Observation<E> create(DataEntity<E> entity) {
        ObservationFactory<E> factory = create();
        factory.setIdentifier(entity.getStaIdentifier());
        factory.setResult(entity.getValue());
        factory.setFeatureOfInterest(entity.getFeature());

        // TODO handle special properties (verticalTo, etc.)
        factory.setProperties(entity);
        factory.setTime(entity);
        return factory.get();
    }

    public static <E> ObservationFactory<E> create() {
        return new ObservationFactory<>(new ObservationDto<>());
    }

    private final ObservationDto<T> dto;

    private ObservationFactory(ObservationDto<T> dto) {
        this.dto = dto;
    }

    public ObservationFactory<T> setIdentifier(String staIdentifier) {
        dto.setId(staIdentifier);
        return this;
    }

    public ObservationFactory<T> setResult(T value) {
        dto.setResult(value);
        return this;
    }

    private ObservationFactory<T> setFeatureOfInterest(AbstractFeatureEntity<?> entity) {
        return setFeatureOfInterest(FeatureOfInterestFactory.create(entity));
    }

    public ObservationFactory<T> setFeatureOfInterest(FeatureOfInterest feature) {
        dto.setFeatureOfInterest(feature);
        return this;
    }

    private ObservationFactory<T> setProperties(DataEntity<?> entity) {
        Set<ParameterEntity<?>> parameters = entity.getParameters();
        Streams.stream(parameters).forEach(this::addProperty);
        return this;
    }

    private ObservationFactory<T> addProperty(ParameterEntity<?> entity) {
        addProperty(entity.getName(), entity.getValue());
        return this;
    }

    public ObservationFactory<T> addProperty(String key, Object value) {
        dto.addProperty(key, value);
        return this;
    }

    private ObservationFactory<T> setTime(DataEntity<?> entity) {
        Date samplingTimeStart = entity.getSamplingTimeStart();
        Date samplingTimeEnd = entity.getSamplingTimeEnd();
        Date validTimeStart = entity.getValidTimeStart();
        Date validTimeEnd = entity.getValidTimeEnd();

        Optional<DateTime> sStart = Optional.ofNullable(samplingTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> sEnd = Optional.ofNullable(samplingTimeEnd).map(TimeUtil::createDateTime);
        Optional<DateTime> vStart = Optional.ofNullable(validTimeStart).map(TimeUtil::createDateTime);
        Optional<DateTime> vEnd = Optional.ofNullable(validTimeEnd).map(TimeUtil::createDateTime);

        Time phenomenonTime = sStart.map(start -> TimeUtil.createTime(start, sEnd.orElse(null))).orElse(null);
        Time validTime = vStart.map(start -> TimeUtil.createTime(start, vEnd.orElse(null))).orElse(null);
        TimeInstant resultTime = Optional.ofNullable(entity.getResultTime()).map(TimeInstant::new).orElse(null);
        return setTime(phenomenonTime, validTime, resultTime);
    }

    public ObservationFactory<T> setTime(Time phenomenonTime, Time validTime, TimeInstant resultTime) {
        dto.setValidTime(phenomenonTime);
        dto.setResultTime(resultTime);
        dto.setPhenomenonTime(validTime);
        return this;
    }

    public Observation<T> get() {
        return dto;
    }

}
