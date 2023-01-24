package org.n52.sta.api.domain.rules;

import java.util.List;

import org.n52.janmayen.stream.Streams;
import org.n52.sta.api.domain.aggregate.GroupAggregate;
import org.n52.sta.api.entity.Group;

public class ClosedGroupDomainRule implements DomainRule {

    public void assertEditableGroup(List<Group> groups) throws DomainRuleViolationException {
        Streams.stream(groups).forEach(this::assertEditableGroup);
    }

    public void assertEditableGroup(Group group) throws DomainRuleViolationException {
        GroupAggregate aggregate = new GroupAggregate(group);
        if (aggregate.isClosedGroup()) {
            throw new DomainRuleViolationException("Group with id + " + group.getId() + "is closed!");
        }
    }

}
