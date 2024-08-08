package org.n52.sta.data.cloudnative.service;

import org.jooq.Field;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.data.cloudnative.dao.FeatureOfInterestDao;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public abstract class FeatureOfInterestService extends AbstractSensorThingsEntityServiceImpl<
        FeatureOfInterestDao,
        FeatureOfInterestDTO> {
    public FeatureOfInterestService(FeatureOfInterestDao dao, Class entityClass) {
        super(dao, entityClass);
    }
    @Override
    Field<String> getStaEntityId() {
        return null;
    }
}
