package org.n52.sta.data.cloudnative.dao;

import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.ObservationDTO;

import java.util.Set;

public interface ObservationDao extends StaEntityDao<ObservationDTO> {
    ObservationDTO findFirstByDatasetIdOrderBySamplingTimeStartAsc(Long datasetIdentifier,
                                                                   Class<ObservationDTO> entityClass) throws STAInvalidQueryException;

    ObservationDTO findFirstByDatasetIdOrderBySamplingTimeEndDesc(Long datasetIdentifier,
                                                                  Class<ObservationDTO> entityClass) throws STAInvalidQueryException;

    void deleteAllByDatasetIdIn(Set<Long> datasetId);
}
