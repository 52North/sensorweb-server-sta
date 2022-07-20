
package org.n52.sta.data.editor;

import java.util.Objects;
import java.util.Optional;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.service.EntityService;

abstract class DatabaseEntityAdapter<T extends DescribableEntity> {

    private final EntityServiceLookup serviceLookup;

    protected DatabaseEntityAdapter(EntityServiceLookup serviceLookup) {
        Objects.requireNonNull(serviceLookup, "serviceLookup must not be null");
        this.serviceLookup = serviceLookup;
    }

    protected <E extends Identifiable> E getOrSaveMandatory(E input, Class<E> type) throws EditorException {
        if (input == null) {
            String typeName = type.getSimpleName();
            throw new EditorException("The input for '" + typeName + "' is null!");
        }
        return getOrSave(input, type);
    }

    protected <E extends Identifiable> Optional<E> getOrSaveOptional(E input, Class<E> type) throws EditorException {
        return input == null
                ? Optional.empty()
                : Optional.of(getOrSave(input, type));
    }

    private <E extends Identifiable> E getOrSave(E input, Class<E> type) throws EditorException {
        EntityService<E> service = getService(type);
        Optional<E> optionalEntity = service.getEntity(input.getId());
        return optionalEntity.isPresent()
                ? optionalEntity.get()
                : service.save(input);
    }

    protected <E extends Identifiable> EntityService<E> getService(Class<E> entityType) {
        return serviceLookup.getService(entityType)
                            .orElseThrow(() -> {
                                String msg = String.format("No registered service found for '%s'",
                                                           entityType.getSimpleName());
                                return new IllegalStateException(msg);
                            });
    }

}
