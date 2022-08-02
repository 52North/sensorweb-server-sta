package org.n52.sta.api;

import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.exception.EditorException;

/**
 * Interface for Entity Editor Implementations that can be delegated to. Used for assuring that autowired services
 * actually an implementation that can be delegated to.
 * @param <T> Type of Entity
 */
public interface EntityEditorDelegate<T extends Identifiable, V extends T> extends EntityEditor<T> {

    @Override
    V save(T entity) throws EditorException;
}
