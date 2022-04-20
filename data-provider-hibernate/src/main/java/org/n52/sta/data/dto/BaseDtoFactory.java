package org.n52.sta.data.dto;

import java.util.Set;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.sta.api.dto.BaseDto;

@SuppressWarnings("unchecked")
public abstract class BaseDtoFactory<T extends BaseDto, F extends BaseDtoFactory<T, F>> {

    private final T dto;

    protected BaseDtoFactory(T dto) {
        this.dto = dto;
    }

    protected F withMetadata(DescribableEntity entity) {
        return withMetadata(entity.getStaIdentifier(), entity.getName(), entity.getDescription());
    }

    public F withMetadata(String id, String name, String description) {
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        return (F) this;
    }

    protected F setProperties(DescribableEntity entity) {
        Set<ParameterEntity<?>> parameters = entity.getParameters();
        Streams.stream(parameters).forEach(this::addProperty);
        return (F) this;
    }

    protected F addProperty(ParameterEntity<?> entity) {
        addProperty(entity.getName(), entity.getValue());
        return (F) this;
    }

    public F addProperty(String key, Object value) {
        dto.addProperty(key, value);
        return (F) this;
    }

    public T get() {
        return dto;
    }

}
