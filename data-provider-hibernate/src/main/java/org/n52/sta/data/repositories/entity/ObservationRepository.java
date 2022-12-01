/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */

package org.n52.sta.data.repositories.entity;

import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.n52.series.db.beans.DataEntity;
import org.n52.sta.data.repositories.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ObservationRepository extends BaseRepository<DataEntity> {

    /**
     * Gets the (temporally) first observation for given datastream. Used for updating
     * Dataset->lastObservation and associated fields.
     *
     * @param id
     *        id of the datastream
     * @return temporally latest observation. Optional.empty if there are not observations in this dataset
     */
    Optional<DataEntity< ? >> findFirstByDataset_idOrderBySamplingTimeStartAsc(Long id);

    /**
     * Gets the (temporally) last observation for given datastream. Used for updating Dataset->lastObservation
     * and associated fields
     *
     * @param id
     *        id of the datastream
     * @return temporally latest observation. Optional.empty if there are not observations in this dataset
     */
    Optional<DataEntity< ? >> findFirstByDataset_idOrderBySamplingTimeEndDesc(Long id);

    void deleteAllByDatasetIdIn(Set<Long> datasetId);
}
