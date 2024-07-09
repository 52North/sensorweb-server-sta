package org.n52.sta.data.cndao.dao;

import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import org.n52.sta.data.vanilla.repositories.EntityGraphRepository;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Transactional
public interface StaIdentifierDao<T> extends EntityGraphRepository<T, Long> {

    /**
     * Checks whether Entity with given id exists.
     *
     * @param identifier Identifier of the Entity
     * @return true if Entity exists. false otherwise
     */
    boolean existsByStaIdentifier(String identifier);

    /**
     * Finds Entity by identifier. Fetches Entity and all related Entities given by EntityGraphs
     *
     * @param identifier      Identifier of the wanted Entity
     * @param relatedEntities EntityGraphs describing related Entities to be fetched. All graphs are merged into one
     *                        graph internally. may be null.
     * @return Entity found in Database. Optional.empty() otherwise
     */
    Optional<T> findByStaIdentifier(String identifier, EntityGraphRepository.FetchGraph... relatedEntities);

    /**
     * Deletes Entity with given Identifier
     *
     * @param identifier Identifier of the Entity
     */
    void deleteByStaIdentifier(String identifier);

    T intermediateSave(T entity);

}
