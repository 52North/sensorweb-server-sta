package org.n52.sta.data.citsci.service;

import org.n52.series.db.beans.HibernateRelations;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.data.common.CommonEntityServiceRepository;
import org.n52.sta.data.common.CommonSTAServiceImpl;
import org.n52.sta.data.common.repositories.StaIdentifierRepository;

import javax.persistence.EntityManager;

public abstract class CitSciSTAServiceImpl<
    T extends StaIdentifierRepository<S>,
    R extends StaDTO,
    S extends HibernateRelations.HasId> extends CommonSTAServiceImpl<T, R, S> {

    public CitSciSTAServiceImpl(T repository,
                                EntityManager em,
                                Class entityClass) {
        super(repository, em, entityClass);
    }

    protected ObservationGroupService getObservationGroupService() {
        return (ObservationGroupService)
            serviceRepository.getEntityServiceRaw(
                CitSciEntityServiceRepository.StaPlusEntityTypes.ObservationGroup.name());
    }

    protected ObservationRelationService getObservationRelationService() {
        return (ObservationRelationService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.ObservationRelation.name());
    }

    protected LicenseService getLicenseService() {
        return (LicenseService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.License.name());
    }

    protected CitSciDatastreamService getStaPlusDatastreamService() {
        return (CitSciDatastreamService)
            serviceRepository.getEntityServiceRaw(CommonEntityServiceRepository.EntityTypes.Datastream.name());
    }

    protected PartyService getPartyService() {
        return (PartyService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.Party.name());
    }

    protected ProjectService getProjectService() {
        return (ProjectService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.Project.name());
    }

}
