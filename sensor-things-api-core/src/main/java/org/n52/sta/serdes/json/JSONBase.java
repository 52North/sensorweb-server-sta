package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class JSONBase {

    abstract static class JSONwithId <T> {
        @JsonProperty("@iot.id")
        public String identifier = UUID.randomUUID().toString();
        protected boolean generatedId = true;

        // Used for dealing with nested inserts
        protected T self;

        // Deals with linking to parent Objects during deep insert
        @JsonBackReference
        public Object backReference;

        public void setIdentifier(String rawIdentifier) throws UnsupportedEncodingException {
            generatedId = false;
            identifier = URLEncoder.encode(rawIdentifier.replace("\'", ""), "utf-8");
        }

        /**
         * Returns a reference to the result of this classes toEntity() method
         * @return reference to created database entity
         */
        public T getEntity() {
            Assert.notNull(self, "Trying to get Entity prior to creation!");
            return this.self;
        }
    }

    abstract static class JSONwithIdNameDescription<T> extends JSONwithId<T> {
        public String name;
        public String description;
    }

    abstract static class JSONwithIdNameDescriptionTime<T> extends JSONwithIdTime<T> {
        public String name;
        public String description;
    }

    abstract static class JSONwithIdTime<T> extends JSONwithId<T> {

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
