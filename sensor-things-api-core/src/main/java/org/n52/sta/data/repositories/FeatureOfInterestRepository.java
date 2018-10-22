package org.n52.sta.data.repositories;

import org.n52.series.db.ParameterDataRepository;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FeatureOfInterestRepository extends ParameterDataRepository<AbstractFeatureEntity<?>> {

}
