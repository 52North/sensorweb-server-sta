package org.n52.sta.data.editor;

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.sta.api.entity.Identifiable;

/**
 * Interface specifying additional methods that must be supplied by DatastreamEntityEditor Implementations.
 * Used e.g. by ObservationEditors to update Datastreams
 */
public interface DatastreamEditorDelegate<T extends Identifiable, R extends T> extends EntityEditorDelegate<T, R> {


    /**
     * Removes the given observation from first observation field if present there.
     *
     * @param old DataEntity to be removed
     * @return true if dataset was changed
     */
    boolean deleteReferenceFromDatasetFirst(DataEntity<?> old);

    /**
     * Removes the given observation from first observation field if present there.
     *
     * @param old DataEntity to be removed
     * @return true if dataset was changed
     */
    boolean deleteReferenceFromDatasetLast(DataEntity<?> old);

    /**
     * Recalculates a new first/last observation and associated values.
     * Needed when observations are deleted from the database
     *
     * @param datastreamEntity Datastream to be updated
     * @param first            Temporally first observation
     * @param last             Temporally last observation
     */
    void updateDatastreamFirstLast(AbstractDatasetEntity datastreamEntity,
                                   DataEntity<?> first,
                                   DataEntity<?> last);

}
