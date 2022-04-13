package org.n52.sta.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StaJpaRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    
}
