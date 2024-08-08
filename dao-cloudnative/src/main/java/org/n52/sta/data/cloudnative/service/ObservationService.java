package org.n52.sta.data.cloudnative.service;

import org.jooq.Field;
import org.n52.series.db.beans.DataEntity;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.cloudnative.dao.ObservationDao;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public abstract class ObservationService
        extends AbstractSensorThingsEntityServiceImpl<ObservationDao, ObservationDTO> {
    public ObservationService(ObservationDao dao, Class entityClass) {
        super(dao, entityClass);
    }
    @Override
    Field<String> getStaEntityId() {
        return null;
    }
}
