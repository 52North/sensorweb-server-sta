
package org.n52.sta.data.query.specifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.Request;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.data.query.FilterQueryParser;
import org.n52.sta.data.query.QuerySpecificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

public abstract class QuerySpecification<T> implements BaseQuerySpecifications<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySpecification.class);

    protected final Map<String, MemberFilter<T>> filterByMember;
    protected final Map<String, PropertyComparator<T, ? >> entityPathByProperty;

    protected QuerySpecification() {
        this.filterByMember = new HashMap<>();
        this.entityPathByProperty = new HashMap<>();
        this.entityPathByProperty.put(StaConstants.PROP_ID,
                                      new SimplePropertyComparator<>(DescribableEntity.PROPERTY_STA_IDENTIFIER));
    }

    public Specification<T> buildSpecification(QueryOptions queryOptions) {
        return FilterQueryParser.parse(queryOptions, this);
    }

    /**
     * Builds specification based on Request. Parses QueryOptions and Request-Path if present.
     *
     * @param req
     *        Request
     * @return Specification to be used for filtering
     */
    public Specification<T> buildSpecification(Request req) {
        Optional<SelectPath< ? extends Identifiable>> path = req.getPath();
        QueryOptions queryOptions = req.getQueryOptions();
        Specification<T> querySpec = buildSpecification(queryOptions);
        return path.map(SelectPath::getPathSegments)
                   .map(p -> querySpec.and(parsePath(p)))
                   .orElse(querySpec);
    }

    private Specification<T> parsePath(List<PathSegment> segments) throws ProviderException {
        Specification<T> specification = null;
        try {
            // Segment of requested Entity
            PathSegment current = segments.get(0);
            if (current.getIdentifier()
                       .isPresent()) {
                specification = equalsStaIdentifier(current.getIdentifier()
                                                           .get());
            }

            // TODO: implement handling of this
            if (segments.size() > 3) {
                throw new SpecificationsException("navigation via >1 relations is not implemented yet!");
            }

            if (segments.size() > 1) {
                current = segments.get(1);
                String currentCollection = current.getCollection();
                BaseQuerySpecifications< ? > bqs = QuerySpecificationFactory.createSpecification(currentCollection);

                String id = current.getIdentifier()
                                   .orElse(null);
                Specification< ? > equalsStaIdentifier = bqs.equalsStaIdentifier(id);
                Specification<T> segmentSpec = applyOnMember(currentCollection,
                                                             equalsStaIdentifier);

                return segmentSpec;
            } else {
                return specification;
            }

            // Iterate over preceding Segments and chain specifications
            /*
             * BaseQuerySpecifications<?> lastQS = qs; Specification<?> stepSpec = specification; for (int i =
             * 1; i < segments.size(); i++) { current = segments.get(i); BaseQuerySpecifications<?> bqs =
             * QuerySpecificationFactory.createSpecification(current.getCollection()); Specification<?>
             * segmentSpec = lastQS.applyOnMember(current.getCollection(),
             * bqs.equalsStaIdentifier(current.getIdentifier().orElse(null))); stepSpec =
             * stepSpec.and(segmentSpec); lastQS = bqs; stepSpec = segmentSpec; }
             */
        } catch (SpecificationsException | STAInvalidFilterExpressionException e) {
            LOGGER.debug(e.getMessage());
            throw new ProviderException(e.getMessage());
        }
    }

    @Override
    public Specification<T> compareProperty(String property,
            FilterConstants.ComparisonOperator operator,
            Expression< ? > rightExpr)
            throws SpecificationsException {
        assertAvailableProperty(property);
        PropertyComparator<T, ? > comparator = entityPathByProperty.get(property);
        return comparator.compareToRight(rightExpr, operator);
    }

    @Override
    public Specification<T> compareProperty(Expression< ? > leftExpr,
            FilterConstants.ComparisonOperator operator,
            String property)
            throws SpecificationsException {
        assertAvailableProperty(property);
        PropertyComparator<T, ? > comparator = entityPathByProperty.get(property);
        return comparator.compareToLeft(leftExpr, operator);
    }

    @Override
    public Specification<T> applyOnMember(String member, Specification< ? > specification)
            throws SpecificationsException {
        assertAvailableMember(member);
        MemberFilter<T> filter = filterByMember.get(member);
        return filter.apply(specification);
    }

    private void assertAvailableMember(String member) throws SpecificationsException {
        if (!filterByMember.containsKey(member)) {
            throw new SpecificationsException("invalid member '" + member + "'");
        }
    }

    private void assertAvailableProperty(String property) throws SpecificationsException {
        if (!entityPathByProperty.containsKey(property)) {
            throw new SpecificationsException("invalid property '" + property + "'");
        }
    }

    @Override
    public <Y extends Comparable< ? super Y>> Specification<T> compare(Expression< ? extends Y> left,
            Expression< ? extends Y> right,
            FilterConstants.ComparisonOperator operator) {
        return (root, query, builder) -> compare(left, right, operator, builder);
    }

    @Override
    public <Y extends Comparable< ? super Y>> Predicate compare(Expression< ? extends Y> left,
            Expression< ? extends Y> right,
            FilterConstants.ComparisonOperator operator,
            CriteriaBuilder builder) {
        switch (operator) {
            case PropertyIsEqualTo:
                return builder.equal(left, right);
            case PropertyIsNotEqualTo:
                return builder.notEqual(left, right);
            case PropertyIsLessThan:
                return builder.lessThan(left, right);
            case PropertyIsLessThanOrEqualTo:
                return builder.lessThanOrEqualTo(left, right);
            case PropertyIsGreaterThan:
                return builder.greaterThan(left, right);
            case PropertyIsGreaterThanOrEqualTo:
                return builder.greaterThanOrEqualTo(left, right);
            case PropertyIsBetween:
                // unsupported between
            default:
                return null;
        }
    }

    @Override
    public Specification<T> equalsName(String value) {
        return (root, query, builder) -> {
            String property = HibernateRelations.HasName.PROPERTY_NAME;
            return builder.equal(root.get(property), value);
        };
    }

    @Override
    public Specification<T> equalsStaIdentifier(String value) {
        return (root, query, builder) -> {
            String property = DescribableEntity.PROPERTY_STA_IDENTIFIER;
            return builder.equal(root.get(property), value);
        };
    }

    @Override
    public Specification<T> equalsOneOfStaIdentifiers(String... values) {
        return (root, query, builder) -> {
            if (values == null || values.length == 0) {
                return null;
            }
            String property = DescribableEntity.PROPERTY_IDENTIFIER;
            return builder.in(root.get(property))
                          .value(values);
        };
    }

    @Override
    public EntityQuery createQuery(String onMember, Class< ? > ofEntity) {
        return (specification, query, builder) -> {
            PreparedSubquery< ? > subquery = selectOnSubquery(onMember, ofEntity);
            return subquery.where(specification, query, builder);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> PreparedSubquery<E> selectOnSubquery(String property, Class<E> entityType) {
        return (specification, query, builder) -> {
            Subquery<E> subquery = query.subquery(entityType);
            Root<E> member = subquery.from(entityType);

            Specification<E> where = (Specification<E>) specification;
            return subquery.select(member.get(property))
                           .where(where.toPredicate(member, query, builder));
        };
    }
}
