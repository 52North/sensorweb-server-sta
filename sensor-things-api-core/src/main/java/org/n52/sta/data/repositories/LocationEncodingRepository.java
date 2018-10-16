package org.n52.sta.data.repositories;

import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LocationEncodingRepository extends AbstractStaRepository<LocationEncodingEntity> {

}
