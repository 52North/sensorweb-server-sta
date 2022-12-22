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

package org.n52.sta.data.editor;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.sta.api.entity.Identifiable;

/**
 * Interface specifying additional methods that must be supplied by DatastreamEntityEditor Implementations.
 * Used e.g. by ObservationEditors to update Datastreams
 */
public interface DatastreamEditorDelegate<T extends Identifiable, R extends T> extends EntityEditorDelegate<T, R> {

    /**
     * Removes the given observation from first observation field if present there.
     *
     * @param old
     *        DataEntity to be removed
     * @return true if dataset was changed
     */
    boolean removeAsFirstObservation(DataEntity< ? > old);

    /**
     * Removes the given observation from first observation field if present there.
     *
     * @param old
     *        DataEntity to be removed
     * @return true if dataset was changed
     */
    boolean removeAsLastObservation(DataEntity< ? > old);

    /**
     * Recalculates a new first/last observation and associated values. Needed when observations are deleted
     * from the database
     *
     * @param datastreamEntity
     *        Datastream to be updated
     * @param first
     *        Temporally first observation
     * @param last
     *        Temporally last observation
     */
    void updateFirstLastObservation(AbstractDatasetEntity datastreamEntity,
            DataEntity< ? > first,
            DataEntity< ? > last);

    void clearFirstObservationLastObservationFeature(DatasetEntity dataset);

    DatasetEntity updateFeature(DatasetEntity datastream, AbstractFeatureEntity<?> featureOfInterestOf);

}
