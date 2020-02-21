/*
 * Copyright 2015-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.n52.sta.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Repository shadowing JpaSpecificationExecutor methods with additional EntityGraph Parameters.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@NoRepositoryBean
public interface EntityGraphRepository<T, I> extends JpaSpecificationExecutor<T>, JpaRepository<T, I> {

    /**
     * Returns a single entity matching the given {@link Specification} or {@link Optional#empty()} if none found.
     * Additionally fetches all related entities given by the provided EntityGraph. All provided Graphs are merged
     * internally.
     *
     * @param spec        can be {@literal null}.
     * @param fetchGraphs string representation of EntityGraph.
     * @return never {@literal null}.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one entity found.
     */
    Optional<T> findOne(@Nullable Specification<T> spec, FetchGraph... fetchGraphs);

    /**
     * Returns all entities matching the given {@link Specification}.
     * Additionally fetches all related entities given by the provided EntityGraph. All provided Graphs are merged
     * internally.
     *
     * @param spec can be {@literal null}.
     * @return never {@literal null}.
     */
    List<T> findAll(@Nullable Specification<T> spec, FetchGraph... fetchGraphs);

    /**
     * Returns a {@link Page} of entities matching the given {@link Specification}.
     * Additionally fetches all related entities given by the provided EntityGraph. All provided Graphs are merged
     * internally.
     *
     * @param spec     can be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @return never {@literal null}.
     */
    Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable, FetchGraph... fetchGraphs);

    /**
     * Returns all entities matching the given {@link Specification} and {@link Sort}. Additionally fetches all
     * related entities given by the provided EntityGraph. All provided Graphs are merged internally.
     *
     * @param spec can be {@literal null}.
     * @param sort must not be {@literal null}.
     * @return never {@literal null}.
     */
    List<T> findAll(@Nullable Specification<T> spec, Sort sort, FetchGraph... fetchGraphs);

    //TODO: expand if necessary
    enum FetchGraph {
        FETCHGRAPH_DEFAULT("identifier"),
        FETCHGRAPH_THINGS("things"),
        FETCHGRAPH_THING("thing"),
        FETCHGRAPH_THINGLOCATION("thing(locations)"),
        FETCHGRAPH_THINGSHISTLOCATION("things(historicalLocations)"),
        FETCHGRAPH_LOCATION("locations"),
        FETCHGRAPH_LOCATIONHISTLOCATION("locations(historicalLocations)"),
        FETCHGRAPH_HIST_LOCATION("historicalLocations"),
        FETCHGRAPH_DATASTREAMS("datastreams"),
        FETCHGRAPH_UOM("unitOfMeasurement"),
        FETCHGRAPH_OBS_TYPE("observationType"),
        FETCHGRAPH_PARAMETERS("parameters"),
        FETCHGRAPH_FORMAT("format"),
        FETCHGRAPH_FEATURETYPE("featureType"),
        FETCHGRAPH_PROCEDURE("procedure"),
        FETCHGRAPH_OBSERVABLE_PROP("observableProperty"),
        FETCHGRAPH_OM_OBS_TYPE("omObservationType"),
        FETCHGRAPH_DATASETS("datasets"),
        FETCHGRAPH_DATASET("dataset");

        private String val;

        FetchGraph(String val) {
            this.val = val;
        }

        public String value() {
            return val;
        }

    }

}
