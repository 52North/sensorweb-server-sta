package org.n52.sta.data.citsci.service;

import org.n52.sta.data.common.CommonEntityServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class CitSciEntityServiceRepository extends CommonEntityServiceRepository {

    @Autowired private CitSciServiceFacade.ObservationGroupServiceFacade obsGroupService;

    @Autowired private CitSciServiceFacade.ObservationRelationServiceFacade obsRelationService;

    @Autowired private CitSciServiceFacade.LicenseServiceFacade licenseService;

    @Autowired private CitSciServiceFacade.PartyServiceFacade partyService;

    @Autowired private CitSciServiceFacade.ProjectServiceFacade projectService;

    @PostConstruct
    public void postConstruct() {
        entityServices.put(StaPlusEntityTypes.ObservationGroup.name(), obsGroupService);
        entityServices.put(StaPlusEntityTypes.ObservationGroups.name(), obsGroupService);

        entityServices.put(StaPlusEntityTypes.ObservationRelation.name(), obsRelationService);
        entityServices.put(StaPlusEntityTypes.ObservationRelations.name(), obsRelationService);

        entityServices.put(StaPlusEntityTypes.Objects.name(), obsRelationService);
        entityServices.put(StaPlusEntityTypes.Object.name(), obsRelationService);

        entityServices.put(StaPlusEntityTypes.Subjects.name(), obsRelationService);
        entityServices.put(StaPlusEntityTypes.Subject.name(), obsRelationService);

        entityServices.put(StaPlusEntityTypes.License.name(), licenseService);
        entityServices.put(StaPlusEntityTypes.Licenses.name(), licenseService);

        entityServices.put(StaPlusEntityTypes.Party.name(), partyService);
        entityServices.put(StaPlusEntityTypes.Parties.name(), partyService);

        entityServices.put(StaPlusEntityTypes.Project.name(), projectService);
        entityServices.put(StaPlusEntityTypes.Projects.name(), projectService);
    }

    public enum StaPlusEntityTypes {
        ObservationGroup, ObservationGroups, Subject, Subjects, Object, Objects,
        ObservationRelation, ObservationRelations, License, Licenses, Party, Parties, Project, Projects
    }
}
