package org.n52.sta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SerDesConfig {

    private final String samplingGeometryMapping;
    private final String verticalFromMapping;
    private final String verticalToMapping;
    private final String verticalFromToMapping;
    private final boolean includeDatastreamCategory;

    public SerDesConfig(
            @Value("${server.feature.observation.samplingGeometry}") String samplingGeometryMapping,
            @Value("${server.feature.observation.verticalFrom}") String verticalFromMapping,
            @Value("${server.feature.observation.verticalTo}") String verticalToMapping,
            @Value("${server.feature.observation.verticalFromTo}") String verticalFromToMapping,
            @Value("${server.feature.includeDatastreamCategory:false}") boolean includeDatastreamCategory
    ) {
        this.samplingGeometryMapping = samplingGeometryMapping;
        this.verticalFromMapping = verticalFromMapping;
        this.verticalToMapping = verticalToMapping;
        this.verticalFromToMapping = verticalFromToMapping;
        this.includeDatastreamCategory = includeDatastreamCategory;
    }

    public String getSamplingGeometryMapping() {
        return samplingGeometryMapping;
    }

    public String getVerticalFromMapping() {
        return verticalFromMapping;
    }

    public String getVerticalToMapping() {
        return verticalToMapping;
    }

    public String getVerticalFromToMapping() {
        return verticalFromToMapping;
    }

    public boolean isIncludeDatastreamCategory() {
        return includeDatastreamCategory;
    }
}
