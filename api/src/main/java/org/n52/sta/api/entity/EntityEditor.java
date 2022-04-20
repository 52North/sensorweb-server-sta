package org.n52.sta.api.entity;

import org.n52.sta.api.dto.StaDto;

public interface EntityEditor<T, S extends StaDto> {
    
    T create(S entity) throws ProviderException;

    T update(S entity) throws ProviderException;

    void delete(String id) throws ProviderException;
}
