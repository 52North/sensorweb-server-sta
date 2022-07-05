
package org.n52.sta.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.n52.sta.api.entity.Identifiable;

public final class EntityEditorLookup {

    private final Map<Class< ? extends Identifiable>, EntityEditor< ? extends Identifiable>> entityEditorsByType;

    protected EntityEditorLookup() {
        this.entityEditorsByType = new HashMap<>();
    }

    public boolean contains(Class< ? extends Identifiable> type) {
        return entityEditorsByType.containsKey(type);
    }

    public Set<Class< ? >> getRegisteredTypes() {
        return new HashSet<>(entityEditorsByType.keySet());
    }

    @SuppressWarnings("unchecked")
    public <T extends Identifiable> Optional<EntityEditor<T>> getEditor(Class<T> type) {
        if (type == null) {
            return Optional.empty();
        }
        EntityEditor< ? > editor = entityEditorsByType.get(type);
        return Optional.ofNullable((EntityEditor<T>) editor);
    }

    public <T extends Identifiable> void addEntityEditor(Class<T> type, EntityEditor<T> editor) {
        Objects.requireNonNull(type, "type must not be null!");
        Objects.requireNonNull(editor, "editor must not be null!");
        entityEditorsByType.put(type, editor);
    }
}
