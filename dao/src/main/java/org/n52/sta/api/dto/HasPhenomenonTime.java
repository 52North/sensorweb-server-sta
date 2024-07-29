package org.n52.sta.api.dto;

import org.n52.shetland.ogc.gml.time.Time;

public interface HasPhenomenonTime {

    Time getPhenomenonTime();

    void setPhenomenonTime(Time phenomenonTime);
}
