package org.n52.sta.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;

import java.sql.Timestamp;
import java.util.Date;

public class TimeUtil {

    protected static DateTime createDateTime(Date date) {
        return new DateTime(date, DateTimeZone.UTC);
    }

    protected static Time createTime(DateTime time) {
        return new TimeInstant(time);
    }

    /**
     * Create {@link Time} from {@link DateTime}s
     *
     * @param start Start {@link DateTime}
     * @param end   End {@link DateTime}
     * @return Resulting {@link Time}
     */
    protected static Time createTime(DateTime start, DateTime end) {
        if (start.equals(end)) {
            return createTime(start);
        } else {
            return new TimePeriod(start, end);
        }
    }

    public static Time parseTime(Object object) {
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            return new TimeInstant(new Instant(timestamp.getTime()));
        } else {
            String obj = object.toString();
            if (obj.contains("/")) {
                String[] split = obj.split("/");
                return createTime(DateTime.parse(split[0]),
                        DateTime.parse(split[1]));
            } else {
                return new TimeInstant(DateTime.parse(obj));
            }
        }
    }
}
