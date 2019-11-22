package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.exception.ParsingException;
import org.n52.sta.utils.TimeUtil;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

public class JSONHistoricalLocation extends JSONBase.JSONwithId<HistoricalLocationEntity>
        implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public Object time;
    public JSONThing Thing;
    @JsonManagedReference
    public JSONLocation[] Locations;

    private Date date;

    public JSONHistoricalLocation() {
    }

    public HistoricalLocationEntity toEntity() {
        self = new HistoricalLocationEntity();

        if (!generatedId && time == null) {
            Assert.isNull(time, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Thing, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Locations, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            Assert.notNull(time, INVALID_INLINE_ENTITY + "time");

            self.setIdentifier(identifier);
            self.setTime(date);

            if (Thing != null) {
                self.setThing(Thing.toEntity());
            } else if (backReference instanceof JSONThing) {
                self.setThing(((JSONThing) backReference).getEntity());
            } else {
                Assert.notNull(null, INVALID_INLINE_ENTITY + "Thing");
            }

            if (Locations != null) {
                self.setLocations(Arrays.stream(Locations)
                        .map(JSONLocation::toEntity)
                        .collect(Collectors.toSet()));
            } else if (backReference instanceof JSONLocation) {
                self.setLocations(Collections.singleton(((JSONLocation)backReference).getEntity()));
            } else {
                Assert.notNull(null, INVALID_INLINE_ENTITY + "Location");
            }

            return self;
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
