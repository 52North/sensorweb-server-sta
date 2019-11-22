package org.n52.sta.serdes.json;

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JSONObservedProperty extends JSONBase.JSONwithIdNameDescription implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String definition;
    public JSONDatastream[] Datastreams;

    public JSONObservedProperty() {
    }

    public PhenomenonEntity toEntity() {
        ObservablePropertyEntity phenomenon = new ObservablePropertyEntity();

        if (!generatedId && name != null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(definition, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            phenomenon.setIdentifier(identifier);
            return phenomenon;
        } else {

            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(definition, INVALID_INLINE_ENTITY + "definition");

            phenomenon.setStaIdentifier(identifier);
            phenomenon.setName(name);
            phenomenon.setDescription(description);
            phenomenon.setIdentifier(definition);

            if (Datastreams != null) {
                phenomenon.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }
            return phenomenon;
        }
    }
}
