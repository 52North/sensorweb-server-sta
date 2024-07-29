package org.n52.sta.data.cloudnative.service;

import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.data.cloudnative.dao.ObservedPropertyDao;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public abstract class ObservedPropertyService
        extends AbstractSensorThingsEntityServiceImpl<
        ObservedPropertyDao,
        ObservedPropertyDTO> {
    public ObservedPropertyService(ObservedPropertyDao dao, Class entityClass) {
        super(dao, entityClass);
    }
}
