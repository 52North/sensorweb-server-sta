package org.n52.sta.data.query.specifications;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.Expression;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
import org.springframework.data.jpa.domain.Specification;

public class DatastreamQuerySpecification implements BaseQuerySpecifications<AbstractDatasetEntity> {

    private final Map<String, MemberFilter<AbstractDatasetEntity>> filterByMember;

    private final Map<String, PropertyComparator<AbstractDatasetEntity, ?>> entityPathByProperty;
    
    public DatastreamQuerySpecification() {
        this.filterByMember = new HashMap<>();

        this.entityPathByProperty = new HashMap<>();

        // TODO maybe we should refactor the above maps to a super class
        //
        // interface -> abstract class
        // define and add specifications within constructor
        // ThingQuerySpecification has some implementation
        // which would move to the base class then
        // 
        // it is possible that some interface methods can
        // become private then and the subclasses are there
        // just to define property/member comparator specs 

    }

    @Override
    public Specification<AbstractDatasetEntity> compareProperty(String property, ComparisonOperator operator,
            Expression<?> rightExpr) throws SpecificationsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Specification<AbstractDatasetEntity> compareProperty(Expression<?> leftExpr, ComparisonOperator operator,
            String property) throws SpecificationsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Specification<AbstractDatasetEntity> applyOnMember(String member, Specification<?> memberSpec)
            throws SpecificationsException {
        // TODO Auto-generated method stub
        return null;
    }
    
}
