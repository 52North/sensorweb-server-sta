package org.n52.sta.data.citsci.service;

import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.sta.plus.LicenseEntity;
import org.n52.series.db.beans.sta.plus.GroupEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.series.db.beans.sta.plus.PartyEntity;
import org.n52.series.db.beans.sta.plus.ProjectEntity;
import org.n52.sta.api.dto.LicenseDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.PartyDTO;
import org.n52.sta.api.dto.ProjectDTO;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.data.common.CommonSTAServiceImpl;
import org.n52.sta.data.common.CommonServiceFacade;
import org.n52.sta.data.vanilla.DaoSemaphore;
import org.n52.sta.data.vanilla.SerDesConfig;
import org.springframework.stereotype.Component;

public class CitSciServiceFacade<R extends StaDTO, S extends HibernateRelations.HasId>
    extends CommonServiceFacade<R, S> {

    public CitSciServiceFacade(CommonSTAServiceImpl<?, R, S> serviceImpl,
                               DaoSemaphore semaphore, SerDesConfig config) {
        super(serviceImpl, semaphore, config);
    }

    @Component
    static class LicenseServiceFacade extends CommonServiceFacade<LicenseDTO, LicenseEntity> {

        LicenseServiceFacade(LicenseService serviceImpl,
                             DaoSemaphore semaphore,
                             SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    static class PartyServiceFacade extends CommonServiceFacade<PartyDTO, PartyEntity> {

        PartyServiceFacade(PartyService serviceImpl,
                           DaoSemaphore semaphore,
                           SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    static class ProjectServiceFacade extends CommonServiceFacade<ProjectDTO, ProjectEntity> {

        ProjectServiceFacade(ProjectService serviceImpl,
                             DaoSemaphore semaphore,
                             SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    static class ObservationRelationServiceFacade
        extends CommonServiceFacade<ObservationRelationDTO, RelationEntity> {

        ObservationRelationServiceFacade(ObservationRelationService serviceImpl,
                                         DaoSemaphore semaphore,
                                         SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }


    @Component
    static class ObservationGroupServiceFacade
        extends CommonServiceFacade<ObservationGroupDTO, GroupEntity> {

        ObservationGroupServiceFacade(ObservationGroupService serviceImpl,
                                      DaoSemaphore semaphore,
                                      SerDesConfig config) {
            super(serviceImpl, semaphore, config);
        }
    }
}
