package org.n52.sta.data.cloudnative.dao;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.StaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;


import java.util.List;
import java.util.Optional;


/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public interface StaEntityDao <T extends StaDTO> {

    String INVALID_EXPAND_OPTION_SUPPLIED =
            "Invalid expandOption supplied. Cannot find %s on Entity of type '%s'";

    /**
     * Gets content of columnName of entity that is specified by spec. Used for fetching only identifier instead of
     * whole Entity
     *
     * @param condition       Predicate of Entity
     * @param columnName Name of Column
     * @return Content of the column with columnName if spec matches. Optional.empty() otherwise
     */
    Optional<String> getColumn(Condition condition,
                               String columnName,
                               Class<T> className);

    /**
     * Gets content of columnName of entity that is specified by spec. Used for fetching only identifier instead of
     * whole Entity
     *
     * @param condition       Predicate of Entity
     * @param pageable   Pagination Specification
     * @param columnName Name of Column
     * @return Content of the column with columnName if spec matches. Optional.empty() otherwise
     */
    List<String> getColumnList(Condition condition,
                               Pageable pageable,
                               String columnName,
                               Class<T> className) throws STAInvalidQueryException;

    /**
     * Retrieves an entity by its id. Additionally fetches all related entities given by the provided EntityGraph.
     * All provided Graphs are merged internally.
     *
     * @param id          must not be {@literal null}.
     * @param queryOptions OData query options
     * @return the entity with the given id or {@literal Optional#empty()} if none found.
     * @throws IllegalArgumentException if {@literal id} is {@literal null}.
     */
    Optional<T> findById(Long id,
                         QueryOptions queryOptions,
                         Class<T> className) throws STAInvalidQueryException;

    /**
     * Returns a single entity matching the given {@link Condition} or {@link Optional#empty()} if none found.
     * Additionally, fetches all related entities given by the provided queryOptions.
     *
     * @param spec        can be {@literal null}.
     * @param queryOptions    OData query options
     * @return Optional possibly wrapping the found entity.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one entity found.
     */
    Optional<T> findOne(@Nullable Condition spec,
                        QueryOptions queryOptions,
                        Class<T> className) throws STAInvalidQueryException;

    /**
     * Returns all entities matching the given {@link Condition}.
     * Additionally fetches all related entities given by the provided queryOptions
     *
     * @param spec        can be {@literal null}.
     * @param queryOptions OData query parameters
     * @return never {@literal null}.
     */
    List<T> findAll(@Nullable Condition spec,
                    QueryOptions queryOptions,
                    Class<T> className) throws STAInvalidQueryException;

    /**
     * Returns a {@link Page} of entities matching the given {@link Condition} and {@link Sort}.
     * Additionally, fetches all related entities given by the provided queryOptions
     *
     * @param spec        can be {@literal null}.
     * @param queryOptions OData query parameters
     * @return never {@literal null}.
     */
    List<T> findAll(@Nullable Condition spec,
                    Sort sort,
                    QueryOptions queryOptions,
                    Class<T> className) throws STAInvalidQueryException;

    /**
     * Returns a {@link Page} of entities matching the given {@link Condition}.
     * Additionally, fetches all related entities given by the provided queryOptions
     *
     * @param filterPredicate        can be {@literal null}.
     * @param queryOptions OData query parameters
     * @return never {@literal null}.
     */
    Page<T> findAll(@Nullable Condition filterPredicate,
                    Pageable pageable,
                    QueryOptions queryOptions,
                    Class<T> className)
            throws STAInvalidQueryException;

    /**
     * Finds Entity by identifier. Fetches Entity and all related Entities given by queryOptions
     *
     * @param identifier      Identifier of the wanted Entity
     * @param queryOptions    OData query options
     * @return Entity found in Database. Optional.empty() otherwise
     */
    Optional<T> findByStaIdentifier(String identifier,
                                    QueryOptions queryOptions,
                                    Class<T> className) throws STAInvalidQueryException;


    /**
     * Checks whether Entity with given id exists.
     *
     * @param identifier Identifier of the Entity
     * @return true if Entity exists. false otherwise
     */
    boolean existsByStaIdentifier(String identifier,
                                  Class<T> className) throws STAInvalidQueryException;

    /**
     * Deletes Entity with given Identifier
     *
     * @param identifier Identifier of the Entity
     */
    void deleteByStaIdentifier(String identifier) throws STACRUDException;


    long count(@Nullable Condition spec,
               Class<T> className) throws STAInvalidQueryException;



    Field<String> getStaEntityId();

    Field<Long> getEntityId();

    Table<?> getEntityTable();

    Field<?> checkAliasedPropertyName(String property);

    Table<?> createJoinList(QueryOptions queryOptions, Table<?> table, List<Field<?>> select)
            throws STAInvalidQueryException;

}
