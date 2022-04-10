package org.n52.sta.api.dto.plus;

import org.n52.sta.api.dto.vanilla.ThingDTO;

import java.util.Set;

public interface PlusThingDTO extends ThingDTO {

    Set<PlusDatastreamDTO> getPlusDatastream();

    void setPlusDatastreams(Set<PlusDatastreamDTO> datastream);
}
