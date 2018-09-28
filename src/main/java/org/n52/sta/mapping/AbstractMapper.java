package org.n52.sta.mapping;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractMapper {
    
    @Autowired
    EntityCreationHelper entityCreationHelper;

    public Time createTime(DataEntity<?> observation) {
        // create time element
        final DateTime start = createDateTime(observation.getSamplingTimeStart());
        DateTime end;
        if (observation.getSamplingTimeEnd() != null) {
            end = createDateTime(observation.getSamplingTimeEnd());
        } else {
            end = start;
        }
        return createTime(start, end);
    }

    public DateTime createDateTime(Date date) {
        return new DateTime(date, DateTimeZone.UTC);
    }

    /**
     * Create {@link Time} from {@link DateTime}s
     *
     * @param start
     *            Start {@link DateTime}
     * @param end
     *            End {@link DateTime}
     * @return Resulting {@link Time}
     */
    public Time createTime(DateTime start, DateTime end) {
        if (start.equals(end)) {
            return new TimeInstant(start);
        } else {
            return new TimePeriod(start, end);
        }
    }
}
