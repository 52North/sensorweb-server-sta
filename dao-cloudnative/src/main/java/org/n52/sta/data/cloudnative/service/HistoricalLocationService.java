package org.n52.sta.data.cloudnative.service;

import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.data.cloudnative.dao.HistoricalLocationDao;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public abstract class HistoricalLocationService
        extends AbstractSensorThingsEntityServiceImpl<
        HistoricalLocationDao,
        HistoricalLocationDTO> {
    public HistoricalLocationService(HistoricalLocationDao dao, Class entityClass) {
        super(dao, entityClass);
    }
}
