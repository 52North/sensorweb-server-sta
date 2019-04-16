package org.n52.sta.data.repositories;

import org.n52.series.db.beans.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DataRepository<T extends DataEntity<?>> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

}
