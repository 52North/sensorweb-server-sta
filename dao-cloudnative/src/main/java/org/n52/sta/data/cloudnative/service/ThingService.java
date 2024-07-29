package org.n52.sta.data.cloudnative.service;

import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.dao.ThingDao;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public abstract class ThingService
        extends AbstractSensorThingsEntityServiceImpl<
        ThingDao,
        ThingDTO> {
    public ThingService(ThingDao dao, Class entityClass) {
        super(dao, entityClass);
    }
}
