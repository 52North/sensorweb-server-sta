package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class JSONBase {

    abstract static class JSONwithId {
        @JsonProperty("@iot.id")
        public String identifier = UUID.randomUUID().toString();
        protected boolean generatedId = true;

        public void setIdentifier(String rawIdentifier) throws UnsupportedEncodingException {
            generatedId = false;
            identifier = URLEncoder.encode(rawIdentifier.replace("\'", ""), "utf-8");
        }
    }

    abstract static class JSONwithIdNameDescription extends JSONwithId {
        public String name;
        public String description;
    }

    abstract static class JSONwithIdNameDescriptionTime extends JSONwithIdTime {
        public String name;
        public String description;
    }

    abstract static class JSONwithIdTime extends JSONwithId {

        protected Time createTime(DateTime time) {
            return new TimeInstant(time);
        }

        /**
         * Create {@link Time} from {@link DateTime}s
         *
         * @param start Start {@link DateTime}
         * @param end   End {@link DateTime}
         * @return Resulting {@link Time}
         */
        protected Time createTime(DateTime start, DateTime end) {
            if (start.equals(end)) {
                return createTime(start);
            } else {
                return new TimePeriod(start, end);
            }
        }

        protected Time parseTime(String input) {
            if (input.contains("/")) {
                String[] split = input.split("/");
                return createTime(DateTime.parse(split[0]),
                        DateTime.parse(split[1]));
            } else {
                return new TimeInstant(DateTime.parse(input));
            }
        }

    }




}
