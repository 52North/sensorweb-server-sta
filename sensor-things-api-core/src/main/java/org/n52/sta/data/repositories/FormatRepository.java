package org.n52.sta.data.repositories;


import org.n52.series.db.beans.FormatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FormatRepository extends JpaRepository<FormatEntity, Long>, JpaSpecificationExecutor<FormatEntity> {

    boolean existsByFormat(String format);

    FormatEntity findByFormat(String format);

}
