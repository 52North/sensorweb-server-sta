package org.n52.sta.data.cloudnative.service;

import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.data.cloudnative.dao.DatastreamDao;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public abstract class DatastreamService extends AbstractSensorThingsEntityServiceImpl<
        DatastreamDao,
        DatastreamDTO> {
    public DatastreamService(DatastreamDao repository, Class entityClass) {
        super(repository, entityClass);
    }
}
