package org.n52.sta.data.repositories;

import java.util.Optional;

public interface IdentifierRepository<T> {

    boolean existsByIdentifier(String identifier);

    Optional<T> findByIdentifier(String identifier);

    void deleteByIdentifier(String identifier);

    T getOneByIdentifier(String identifier);
}
