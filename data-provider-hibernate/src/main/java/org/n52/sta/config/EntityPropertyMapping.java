package org.n52.sta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// TODO probably we can define a general property mapping config?

@Configuration
@ConfigurationProperties(prefix = "server.observation.parameter")
public class EntityPropertyMapping {

    private String samplingGeometry;

    private String verticalFrom;

    private String verticalTo;

    private String verticalFromTo;

    public String getSamplingGeometry() {
        return samplingGeometry;
    }

    public void setSamplingGeometry(String samplingGeometry) {
        this.samplingGeometry = samplingGeometry;
    }

    public String getVerticalFrom() {
        return verticalFrom;
    }

    public void setVerticalFrom(String verticalFrom) {
        this.verticalFrom = verticalFrom;
    }

    public String getVerticalTo() {
        return verticalTo;
    }

    public void setVerticalTo(String verticalTo) {
        this.verticalTo = verticalTo;
    }

    public String getVerticalFromTo() {
        return verticalFromTo;
    }

    public void setVerticalFromTo(String verticalFromTo) {
        this.verticalFromTo = verticalFromTo;
    }

}
