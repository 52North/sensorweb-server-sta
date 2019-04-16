
package org.n52.sta.data.query;

import java.util.Collection;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.springframework.data.jpa.domain.Specification;

public class DatasetQuerySpecifications {

    /**
     * Matches datasets having offering with given ids.
     *
     * @param id
     *            the id to match
     * @return a specification
     * @see #matchOfferings(Collection)
     */
    public Specification<DatasetEntity> matchOfferings(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, OfferingEntity> join =
                    root.join(DatasetEntity.PROPERTY_OFFERING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), id);
        };
    }

    /**
     * Matches datasets having feature with given id.
     *
     * @param id
     *            the id to match
     * @return a specification
     * @see #matchFeatures(Collection)
     */
    public Specification<DatasetEntity> matchFeatures(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, FeatureEntity> join =
                    root.join(DatasetEntity.PROPERTY_FEATURE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), id);
        };
    }

    /**
     * Matches datasets having procedures with given id.
     *
     * @param id
     *            the id to match
     * @return a specification
     * @see #matchProcedures(Collection)
     */
    public Specification<DatasetEntity> matchProcedures(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, ProcedureEntity> join =
                    root.join(DatasetEntity.PROPERTY_PROCEDURE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), id);
        };
    }

    /**
     * Matches datasets having phenomena with given id.
     *
     * @param id
     *            the id to match
     * @return a specification
     * @see #matchPhenomena(Collection)
     */
    public Specification<DatasetEntity> matchPhenomena(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, PhenomenonEntity> join =
                    root.join(DatasetEntity.PROPERTY_PHENOMENON, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), id);
        };
    }

    /**
     * Matches datasets having platform with given id.
     *
     * @param id
     *            the id to match
     * @return a specification
     * @see #matchPlatform(Collection)
     */
    public Specification<DatasetEntity> matchPlatform(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, PlatformEntity> join =
                    root.join(DatasetEntity.PROPERTY_PLATFORM, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), id);
        };
    }

    /**
     * Matches datasets having catefory with given id.
     *
     * @param id
     *            the id to match
     * @return a specification
     * @see #matchCategory(Collection)
     */
    public Specification<DatasetEntity> matchCategory(final String id) {
        return (root, query, builder) -> {
            final Join<DatasetEntity, CategoryEntity> join =
                    root.join(DatasetEntity.PROPERTY_CATEGORY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), id);
        };
    }
}
