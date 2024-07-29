package org.n52.sta.api.dto;

import java.util.Set;

public interface HasDatastreams {

    Set<DatastreamDTO> getDatastreams();

    void setDatastreams(Set<DatastreamDTO> datastreams);
}
