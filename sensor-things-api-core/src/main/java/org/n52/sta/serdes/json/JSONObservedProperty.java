package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JSONObservedProperty extends JSONBase.JSONwithIdNameDescription<ObservablePropertyEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String definition;
    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    public JSONObservedProperty() {
        self = new ObservablePropertyEntity();
    }

    public PhenomenonEntity toEntity() {
        if (!generatedId && name == null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(definition, INVALID_REFERENCED_ENTITY);
            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            self.setStaIdentifier(identifier);
            return self;
        } else {

            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(definition, INVALID_INLINE_ENTITY + "definition");

            self.setStaIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);
            self.setIdentifier(definition);

            if (Datastreams != null) {
                self.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }
            // Deal with back reference during deep insert
            if (backReference != null) {
                self.addDatastream(((JSONDatastream) backReference).getEntity());
            }

            return self;
        }
    }
}
