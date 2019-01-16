
package org.n52.sta.data.query;

import java.util.Collection;

import org.n52.series.db.beans.QDatasetEntity;

import com.querydsl.core.types.dsl.BooleanExpression;

public class DatasetQuerySpecifications {

    /**
     * Matches datasets having offering with given ids.
     *
     * @param id
     *        the id to match
     * @return a boolean expression
     * @see #matchOfferings(Collection)
     */
    public BooleanExpression matchOfferings(final String id) {
        return QDatasetEntity.datasetEntity.offering.id.eq(Long.valueOf(id));
    }

    /**
     * Matches datasets having feature with given id.
     *
     * @param id
     *        the id to match
     * @return a boolean expression
     * @see #matchFeatures(Collection)
     */
    public BooleanExpression matchFeatures(final String id) {
        return QDatasetEntity.datasetEntity.feature.id.eq(Long.valueOf(id));
    }

    /**
     * Matches datasets having procedures with given id.
     *
     * @param id
     *        the id to match
     * @return a boolean expression
     * @see #matchProcedures(Collection)
     */
    public BooleanExpression matchProcedures(final String id) {
        return QDatasetEntity.datasetEntity.procedure.id.eq(Long.valueOf(id));
    }

    /**
     * Matches datasets having phenomena with given id.
     *
     * @param id
     *        the id to match
     * @return a boolean expression
     * @see #matchPhenomena(Collection)
     */
    public BooleanExpression matchPhenomena(final String id) {
        return QDatasetEntity.datasetEntity.phenomenon.id.eq(Long.valueOf(id));
    }
}
