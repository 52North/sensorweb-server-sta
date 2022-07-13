package org.n52.sta.data.query.specifications;

import javax.persistence.criteria.Expression;

import org.n52.shetland.ogc.filter.FilterConstants;
import org.springframework.data.jpa.domain.Specification;

public interface PropertyComparator<R, T> {

    Specification<R> compareToRight(Expression<?> right, FilterConstants.ComparisonOperator operator);

    Specification<R> compareToLeft(Expression<?> left, FilterConstants.ComparisonOperator operator);

}
