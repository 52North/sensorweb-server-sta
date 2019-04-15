
package org.n52.sta.data.repositories;

import org.n52.series.db.beans.DatasetEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface DatasetRepository<T extends DatasetEntity> extends ParameterDataRepository<T> {

//    /**
//     * Qualifies a 'not_initialized' dataset with the given value type. Once set, no update is possible
//     * anymore.
//     *
//     * @param valueType
//     *        the value type to qualify dataset with
//     * @param id
//     *        the dataset id
//     */
//    @Modifying(clearAutomatically = true)
//    @Query("Update DatasetEntity d set d.valueType = :valueType where d.id = :id and valueType = '"
//            + NotInitializedDatasetEntity.DATASET_TYPE + "'")
//    void initValueType(@Param("valueType") String valueType, @Param("id") Long id);

}
