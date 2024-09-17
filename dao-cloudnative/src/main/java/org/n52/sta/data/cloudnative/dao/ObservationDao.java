package org.n52.sta.data.cloudnative.dao;

import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Observation;

import java.util.Set;

public interface ObservationDao extends StaEntityDao<ObservationDTO> {
    Observation findFirstByDatasetIdOrderBySamplingTimeStartAsc(Long datasetIdentifier,
                                                                Class<ObservationDTO> entityClass) throws STAInvalidQueryException;

    Observation findFirstByDatasetIdOrderBySamplingTimeEndDesc(Long datasetIdentifier,
                                                                  Class<ObservationDTO> entityClass) throws STAInvalidQueryException;

    void deleteAllByDatasetIdIn(Set<Long> datasetId) throws STACRUDException, STAInvalidQueryException;
}
