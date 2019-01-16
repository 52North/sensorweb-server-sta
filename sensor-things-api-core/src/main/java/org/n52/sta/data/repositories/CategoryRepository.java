package org.n52.sta.data.repositories;


import org.n52.series.db.beans.CategoryEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CategoryRepository extends ParameterDataRepository<CategoryEntity> {

}
