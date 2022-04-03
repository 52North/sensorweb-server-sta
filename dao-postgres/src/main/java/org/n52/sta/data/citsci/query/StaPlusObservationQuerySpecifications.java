package org.n52.sta.data.citsci.query;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.series.db.beans.sta.plus.StaPlusDataEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.sta.data.common.query.EntityQuerySpecifications;
import org.n52.sta.data.vanilla.query.ObservationQuerySpecifications;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class StaPlusObservationQuerySpecifications extends EntityQuerySpecifications<StaPlusDataEntity<?>> {

    ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

    public static Specification<StaPlusDataEntity<?>> withObservationGroupStaIdentifier(
        final String obsGroupIdentifier) {
        return (root, query, builder) -> {
            final Join<StaPlusDataEntity, GroupEntity> join =
                root.join(StaPlusDataEntity.PROPERTY_GROUPS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), obsGroupIdentifier);
        };
    }

    public static Specification<StaPlusDataEntity<?>> withObservationRelationStaIdentifierAsSubject(
        final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<StaPlusDataEntity, RelationEntity> join =
                root.join(StaPlusDataEntity.PROPERTY_SUBJECTS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    public static Specification<StaPlusDataEntity<?>> withObservationRelationStaIdentifierAsObject(
        final String relationIdentifier) {
        return (root, query, builder) -> {
            final Join<StaPlusDataEntity, RelationEntity> join =
                root.join(StaPlusDataEntity.PROPERTY_OBJECTS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), relationIdentifier);
        };
    }

    @Override protected Specification<StaPlusDataEntity<?>> handleRelatedPropertyFilter(String propertyName,
                                                                                        Specification<?> propertyValue) {
        throw new RuntimeException("not implemented yet");
    }

    @Override protected Specification<StaPlusDataEntity<?>> handleDirectPropertyFilter(String propertyName,
                                                                                       Expression<?> propertyValue,
                                                                                       FilterConstants.ComparisonOperator operator,
                                                                                       boolean switched) {
        throw new RuntimeException("not implemented yet");
    }
}
