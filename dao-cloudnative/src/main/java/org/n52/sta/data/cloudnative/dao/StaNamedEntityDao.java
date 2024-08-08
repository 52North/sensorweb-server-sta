package org.n52.sta.data.cloudnative.dao;

import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.HasNameAndDescription;
import org.n52.sta.api.dto.StaDTO;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
public interface StaNamedEntityDao<T extends HasNameAndDescription & StaDTO> extends StaEntityDao<T> {

    boolean existsByName(String name, Class<T> entityClass) throws STAInvalidQueryException;

    Optional<T> findByName(String name, Class<T> entityClass) throws STAInvalidQueryException;
}
