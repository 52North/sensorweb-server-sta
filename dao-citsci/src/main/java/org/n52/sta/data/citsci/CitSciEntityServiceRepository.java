/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
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

package org.n52.sta.data.citsci;

import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.STAEventHandler;
import org.n52.sta.data.vanilla.service.AbstractSensorThingsEntityServiceImpl;
import org.n52.sta.data.vanilla.service.EntityServiceRepository;
import org.n52.sta.data.vanilla.service.ServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class CitSciEntityServiceRepository extends EntityServiceRepository implements EntityServiceFactory {

    private Map<CitSciEntityTypes, ServiceFacade<?, ?>> entityServices = new LinkedHashMap<>();

    @Autowired
    private CitSciServiceFacade.ObservationGroupServiceFacade obsGroupService;

    @Autowired
    private CitSciServiceFacade.ObservationRelationServiceFacade obsRelationService;

    @Autowired
    private CitSciServiceFacade.LicenseServiceFacade licenseService;

    @Autowired
    private CitSciServiceFacade.PartyServiceFacade partyService;

    @Autowired
    private CitSciServiceFacade.ProjectServiceFacade projectService;

    @Autowired
    private STAEventHandler mqttSubscriptionEventHandler;

    @PostConstruct
    public void postConstruct() {
        super.postConstruct();
        entityServices.put(CitSciEntityTypes.ObservationGroup, obsGroupService);
        entityServices.put(CitSciEntityTypes.ObservationGroups, obsGroupService);

        entityServices.put(CitSciEntityTypes.ObservationRelation, obsRelationService);
        entityServices.put(CitSciEntityTypes.ObservationRelations, obsRelationService);

        entityServices.put(CitSciEntityTypes.Objects, obsRelationService);
        entityServices.put(CitSciEntityTypes.Object, obsRelationService);

        entityServices.put(CitSciEntityTypes.Subjects, obsRelationService);
        entityServices.put(CitSciEntityTypes.Subject, obsRelationService);

        entityServices.put(CitSciEntityTypes.License, licenseService);
        entityServices.put(CitSciEntityTypes.Licenses, licenseService);

        entityServices.put(CitSciEntityTypes.Party, partyService);
        entityServices.put(CitSciEntityTypes.Parties, partyService);

        entityServices.put(CitSciEntityTypes.Project, projectService);
        entityServices.put(CitSciEntityTypes.Projects, projectService);

        entityServices.forEach(
            (t, e) -> e.getServiceImpl().setServiceRepository(this)
        );
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService<?> getEntityService(String entityTypeName) {
        for (CitSciEntityTypes value : CitSciEntityTypes.values()) {
            if (entityTypeName.equals(value.name())) {
                return entityServices.get(CitSciEntityTypes.valueOf(entityTypeName));
            }
        }
        return super.getEntityService(entityTypeName);
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityServiceImpl getEntityServiceRaw(CitSciEntityTypes entityTypeName) {
        return entityServices.get(entityTypeName).getServiceImpl();
    }

    public enum CitSciEntityTypes {
        ObservationGroup, ObservationGroups, Subject, Subjects, Object, Objects, ObservationRelation,
        ObservationRelations, License, Licenses, Party, Parties, Project, Projects
    }
}
