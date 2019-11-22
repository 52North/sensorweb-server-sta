package org.n52.sta.serdes.json;

import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.exception.ParsingException;
import org.n52.sta.utils.TimeUtil;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class JSONHistoricalLocation extends JSONBase.JSONwithId implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public Object time;
    public JSONThing Thing;
    public JSONLocation[] Locations;

    private Date date;

    public JSONHistoricalLocation() {
    }

    public HistoricalLocationEntity toEntity() {
        HistoricalLocationEntity entity = new HistoricalLocationEntity();

        if (!generatedId && time != null) {
            Assert.isNull(time, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Thing, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Locations, INVALID_REFERENCED_ENTITY);

            entity.setIdentifier(identifier);
            return entity;
        } else {
            Assert.notNull(time, INVALID_INLINE_ENTITY + "time");
            Assert.notNull(Thing, INVALID_INLINE_ENTITY + "Thing");

            entity.setIdentifier(identifier);
            entity.setTime(date);
            entity.setThing(Thing.toEntity());

            if (Locations != null) {
                entity.setLocations(Arrays.stream(Locations)
                        .map(JSONLocation::toEntity)
                        .collect(Collectors.toSet()));
            }
            return entity;
        }
    }

    /**
     * Wrapper around rawTime property called by jackson while deserializing.
     *
     * @param rawTime raw Time
     */
    public void setTime(Object rawTime) throws ParsingException {
        Time time = TimeUtil.parseTime(rawTime);
        if (time instanceof TimeInstant) {
            date = ((TimeInstant) time).getValue().toDate();
        } else if (time instanceof TimePeriod) {
            date = ((TimePeriod) time).getEnd().toDate();
        } else {
            //TODO: refine error message
            throw new ParsingException("Invalid time format.");
        }
    }
}
