
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
        if (TimePeriod.class.isAssignableFrom(time.getClass())) {
            TimePeriod period = (TimePeriod) time;
            DateTime startTime = period.getStart();
            setter.accept(startTime.toDate());
        } else {
            TimeInstant instant = (TimeInstant) time;
            DateTime startTime = instant.getValue();
            setter.accept(startTime.toDate());
        }
    }

    public void setEndTime(Consumer<Date> setter, Time time) {
        if (TimePeriod.class.isAssignableFrom(time.getClass())) {
            TimePeriod period = (TimePeriod) time;
            DateTime endTime = period.getEnd();
            setter.accept(endTime.toDate());
        } else {
            TimeInstant instant = (TimeInstant) time;
            DateTime startTime = instant.getValue();
            setter.accept(startTime.toDate());
        }
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public FormatEntity getOrSaveFormat(String format) {
        Objects.requireNonNull(format, "format must not be null");
        Optional<FormatEntity> entity = formatRepository.findByFormat(format);
        if (entity.isPresent()) {
            return entity.get();
        }

        FormatEntity formatEntity = new FormatEntity();
        formatEntity.setFormat(format);
        return formatRepository.save(formatEntity);
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public UnitEntity getOrSaveUnit(Datastream.UnitOfMeasurement uom) {
        Objects.requireNonNull(uom, "uom must not be null");
        String symbol = uom.getSymbol();
        Optional<UnitEntity> unit = unitRepository.findBySymbol(symbol);
        if (unit.isPresent()) {
            return unit.get();
        }

        UnitEntity unitEntity = new UnitEntity();
        unitEntity.setLink(uom.getDefinition());
        unitEntity.setName(uom.getName());
        unitEntity.setSymbol(symbol);
        return unitRepository.save(unitEntity);
    }

}
