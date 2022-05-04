package org.n52.sta.data.entity;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.old.utils.TimeUtil;

public class StaData<T extends DescribableEntity> implements Identifiable {

    protected final T data;

    protected StaData(T dataEntity) {
        Objects.requireNonNull(dataEntity, "dataEntity must not be null!");
        this.data = dataEntity;
    }

    @Override
    public String getId() {
        return data.getStaIdentifier();
    }

    protected Time toTime(Date time) {
        DateTime dateTime = TimeUtil.createDateTime(time);
        return TimeUtil.createTime(dateTime);
    }

    protected Time toTimeInterval(Date start, Date optionalEnd) {
        Optional<DateTime> begin = Optional.ofNullable(start).map(TimeUtil::createDateTime);
        Optional<DateTime> end = Optional.ofNullable(optionalEnd).map(TimeUtil::createDateTime);
        return begin.map(b -> TimeUtil.createTime(b, end.orElse(null))).orElse(null);
    }

    protected <E, U> Set<U> toSet(Collection<E> collection, Function<E, U> mapper) {
        return Streams.stream(collection).map(mapper).collect(Collectors.toSet());
    }

    protected Map<String, Object> toMap(Set<ParameterEntity<?>> parameters) {
        return Streams.stream(parameters)
                .collect(Collectors.toMap(ParameterEntity::getName, ParameterEntity::getValue));
    }

}
