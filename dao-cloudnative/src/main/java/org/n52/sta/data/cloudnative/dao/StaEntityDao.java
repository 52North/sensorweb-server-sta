package org.n52.sta.data.cloudnative.dao;

import org.jooq.Condition;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.data.OffsetLimitBasedPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Transactional
public interface StaEntityDao <T extends StaDTO> {

    /**
     * Checks whether Entity with given id exists.
     *
     * @param identifier Identifier of the Entity
     * @return true if Entity exists. false otherwise
     */
    boolean existsByStaIdentifier(String identifier);

    /**
     * Finds Entity by identifier. Fetches Entity and all related Entities given by queryOptions
     *
     * @param identifier      Identifier of the wanted Entity
     * @param queryOptions    describing related Entities to be fetched.
     * @return Entity found in Database. Optional.empty() otherwise
     */
    Optional<T> findByStaIdentifier(String identifier, QueryOptions queryOptions);

    /**
     * Deletes Entity with given Identifier
     *
     * @param identifier Identifier of the Entity
     */
    void deleteByStaIdentifier(String identifier);

    /**
     * Returns a single entity matching the given {@link Condition} or {@link Optional#empty()} if none found.
     * Additionally, fetches all related entities given by the provided queryOptions.
     *
     * @param spec        can be {@literal null}.
     * @param queryOptions    describing related Entities to be fetched.
     * @return Optional possibly wrapping the found entity.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one entity found.
     */
    Optional<T> findOne(@Nullable Condition spec, QueryOptions queryOptions);

    Optional<String> getColumn(Condition condition, String staidentifier);

    Optional<T> findById(Long id, QueryOptions queryOptions);

    long count(@Nullable Condition spec);

    <R extends StaDTO> Page<R> findAll(Condition filterPredicate,
                                       OffsetLimitBasedPageRequest pageableRequest,
                                       QueryOptions queryOptions) throws STAInvalidQueryException;
}
