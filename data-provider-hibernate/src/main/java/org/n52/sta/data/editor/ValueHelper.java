
package org.n52.sta.data.editor;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.joda.time.DateTime;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.data.repositories.value.FormatRepository;
import org.n52.sta.data.repositories.value.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

public class ValueHelper {

    @Autowired
    private FormatRepository formatRepository;

    @Autowired
    private UnitRepository unitRepository;

    public void setStartTime(Consumer<Date> setter, Time time) {
        if (time == null) {
            return;
        } else if (time instanceof TimeInstant) {
            setTime(setter, time);
        } else {
            TimePeriod period = (TimePeriod) time;
            DateTime startTime = period.getStart();
            setter.accept(startTime.toDate());
        }
    }

    public void setEndTime(Consumer<Date> setter, Time time) {
        if (time == null) {
            return;
        } else if (time instanceof TimeInstant) {
            setTime(setter, time);
        } else {
            TimePeriod period = (TimePeriod) time;
            DateTime endTime = period.getEnd();
            setter.accept(endTime.toDate());
        }
    }

    public void setTime(Consumer<Date> setter, Time time) {
        TimeInstant instant = (TimeInstant) time;
        DateTime startTime = instant.getValue();
        setter.accept(startTime.toDate());
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void setFormat(Consumer<FormatEntity> setter, String format) {
        Objects.requireNonNull(format, "format must not be null");
        Optional<FormatEntity> entity = formatRepository.findByFormat(format);
        entity.ifPresentOrElse(
                               setter::accept,
                               () -> {
                                   FormatEntity formatEntity = new FormatEntity();
                                   formatEntity.setFormat(format);
                                   FormatEntity savedEntity = formatRepository.save(formatEntity);
                                   setter.accept(savedEntity);
                               });
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void setUnit(Consumer<UnitEntity> setter, Datastream.UnitOfMeasurement uom) {
        Objects.requireNonNull(uom, "uom must not be null");
        String symbol = uom.getSymbol();
        Optional<UnitEntity> entity = unitRepository.findBySymbol(symbol);
        entity.ifPresentOrElse(
                               setter::accept,
                               () -> {
                                   UnitEntity unitEntity = new UnitEntity();
                                   unitEntity.setLink(uom.getDefinition());
                                   unitEntity.setName(uom.getName());
                                   unitEntity.setSymbol(symbol);
                                   UnitEntity savedUnit = unitRepository.save(unitEntity);
                                   setter.accept(savedUnit);
                               });
    }

}
