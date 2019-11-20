package org.n52.sta.data.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class STASerdesTypes {

    Logger LOGGER = LoggerFactory.getLogger(STASerdesTypes.class);

    static class JSONwithId {
        @JsonProperty("@iot.id") public String identifier;

        public void setIdentifier(String rawIdentifier) throws UnsupportedEncodingException {
            identifier = URLEncoder.encode(rawIdentifier.replace("\'", ""), "utf-8");
        }
    }

    static class JSONwithIdNameDescription extends JSONwithId {
        public String name;
        public String description;
    }
}
