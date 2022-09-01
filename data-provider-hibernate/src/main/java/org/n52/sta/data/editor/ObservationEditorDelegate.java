package org.n52.sta.data.editor;

import java.util.Set;

import org.n52.sta.api.entity.Identifiable;

/**
 * Interface specifying additional methods that must be supplied by ObservationEntityEditor Implementations.
 * Used e.g. by DatastreamEditor#delete for deletion of entities when deleting dataset
 */
public interface ObservationEditorDelegate<T extends Identifiable, R extends T> extends EntityEditorDelegate<T, R> {

    void deleteObservationsByDatasetId(Set<Long> ids);

}
