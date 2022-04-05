/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;

import java.sql.Timestamp;
import java.util.Date;

public class TimeUtil {

    public static DateTime createDateTime(Date date) {
        return new DateTime(date, DateTimeZone.UTC);
    }

    public static Time createTime(DateTime time) {
        return new TimeInstant(time);
    }

    /**
     * Create {@link Time} from {@link DateTime}s
     *
     * @param start Start {@link DateTime}
     * @param end   End {@link DateTime}
     * @return Resulting {@link Time}
     */
    public static Time createTime(DateTime start, DateTime end) {
        if (start.equals(end)) {
            return createTime(start);
        } else {
            return new TimePeriod(start, end);
        }
    }

    public static Time parseTime(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            return new TimeInstant(timestamp);
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
