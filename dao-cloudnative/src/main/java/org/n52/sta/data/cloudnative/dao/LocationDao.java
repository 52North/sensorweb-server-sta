package org.n52.sta.data.cloudnative.dao;

import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.LocationDTO;

import java.util.List;
import java.util.Set;

public interface LocationDao extends StaNamedEntityDao<LocationDTO> {
    List<LocationDTO> findAllByThingId(Long id, Class<LocationDTO> entityClass) throws STAInvalidQueryException;
}
