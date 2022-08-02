package org.n52.sta.data.editor;

import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.exception.EditorException;

/**
 * Interface for Entity Editor Implementations that can be delegated to.
 * @param <T> Type of Entity
 */
interface EntityEditorDelegate<T extends Identifiable, V extends T> extends EntityEditor<T> {

    /**
     * Gets the Database Entity if it exists, saves it otherwise.
     * @param entity Entity to be handled
     * @return Persisted Entity. May be wrapped
     * @throws EditorException if an error occurred
     */
    V getOrSave(T entity) throws EditorException;
}
