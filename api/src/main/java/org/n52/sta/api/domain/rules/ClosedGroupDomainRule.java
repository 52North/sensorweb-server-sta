package org.n52.sta.api.domain.rules;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.aggregate.GroupAggregate;
import org.n52.sta.api.entity.Group;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.path.Request;
import org.n52.sta.api.service.EntityService;

public class ClosedGroupDomainRule implements DomainRule {

    private EntityServiceLookup serviceLookup;

    public ClosedGroupDomainRule(EntityServiceLookup serviceLookup) {
        this.serviceLookup = serviceLookup;
    }

    public void assertAddObservation(Observation observation) {
        List<Group> groups = getGroups(observation);
        assertReferencedGroupsAreEditable(groups);
    }

    public void assertUpdateGroup(Group group) {
        assertEditableGroup(group);
    }

    private void assertReferencedGroupsAreEditable(List<Group> groups) throws DomainRuleViolationException {
        Streams.stream(groups).forEach(this::assertEditableGroup);
    }

    private void assertEditableGroup(Group group) throws DomainRuleViolationException {
        GroupAggregate aggregate = new GroupAggregate(group);
        if (aggregate.isClosedGroup()) {
            throw new DomainRuleViolationException("Group with id '" + group.getId() + "' is closed!");
        }
    }

    private List<Group> getGroups(Observation entity) {
        EntityService<Group> groupService = serviceLookup.getService(Group.class).get();
        return Streams.stream(entity.getGroups())
                        .map(group -> Request.createIdRequest(group.getId()))
                        .map(request -> groupService.getEntity(request))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());
    }

}
