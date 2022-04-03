package org.n52.sta.data.citsci.service;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.sta.plus.RelationEntity;
import org.n52.series.db.beans.sta.plus.StaPlusDataEntity;
import org.n52.series.db.beans.sta.plus.StaPlusQuantityDataEntity;
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.vanilla.service.ObservationService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
@Profile(StaConstants.STAPLUS)
public class CitSciObservationService extends ObservationService {

    @Override
    protected DataEntity castToConcreteObservationType(DataEntity<?> observation,
                                                       DatasetEntity dataset)
        throws STACRUDException {
        DataEntity data = null;
        Object value = observation.getValue();
        switch (dataset.getOMObservationType().getFormat()) {
            case OmConstants.OBS_TYPE_MEASUREMENT:
                StaPlusQuantityDataEntity quantityObservationEntity = new StaPlusQuantityDataEntity();
                if (value.equals("NaN") || value.equals("Inf") || value.equals("-Inf")) {
                    quantityObservationEntity.setValue(null);
                } else {
                    quantityObservationEntity.setValue(BigDecimal.valueOf(Double.parseDouble((String) value)));
                }
                data = quantityObservationEntity;
                break;
            default:
                throw new STACRUDException(
                    "Unable to handle OMObservation with type: " + dataset.getOMObservationType().getFormat());
        }
        return fillConcreteObservationType(data, observation, dataset);
    }

    /**
     * Hook to add STAPlus-specific things
     *
     * @param observation
     * @param dataset
     * @return
     * @throws STACRUDException
     */
    @Override protected DataEntity<?> saveObservation(DataEntity<?> observation, DatasetEntity dataset)
        throws STACRUDException {
        StaPlusDataEntity<?> obs = (StaPlusDataEntity<?>) observation;

        if (obs.getSubjects() != null) {
            Set<RelationEntity> subjects = new HashSet<>();
            for (RelationEntity subject : obs.getSubjects()) {
                subjects.add(getObservationRelationService().createOrUpdate(subject));
            }
            obs.setSubjects(subjects);
        }

        return super.saveObservation(observation, dataset);
    }

    @Override
    protected DataEntity<?> fillConcreteObservationType(DataEntity<?> data,
                                                        DataEntity<?> observation,
                                                        DatasetEntity dataset) throws STACRUDException {
        StaPlusDataEntity<?> plusData =
            (StaPlusDataEntity<?>) super.fillConcreteObservationType(data, observation, dataset);
        StaPlusDataEntity<?> plusObservation = (StaPlusDataEntity<?>) observation;
        plusData.setSubjects(plusObservation.getSubjects());
        plusData.setObjects(plusObservation.getObjects());
        plusData.setGroups(plusObservation.getGroups());
        return data;
    }

    private LicenseService getLicenseService() {
        return (LicenseService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.License.name());
    }

    private ObservationRelationService getObservationRelationService() {
        return (ObservationRelationService)
            serviceRepository.getEntityServiceRaw(CitSciEntityServiceRepository.StaPlusEntityTypes.ObservationRelation.name());
    }
}
