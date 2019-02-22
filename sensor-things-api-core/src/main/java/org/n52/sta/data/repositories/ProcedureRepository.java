package org.n52.sta.data.repositories;


import org.n52.series.db.beans.ProcedureEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProcedureRepository extends ParameterDataRepository<ProcedureEntity> {

}
