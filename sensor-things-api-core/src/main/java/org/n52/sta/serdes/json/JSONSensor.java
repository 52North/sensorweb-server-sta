package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.joda.time.DateTime;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JSONSensor extends JSONBase.JSONwithIdNameDescription<SensorEntity> implements AbstractJSONEntity{

    // JSON Properties. Matched by Annotation or variable name
    public String properties;
    public String encodingType;
    public String metadata;

    @JsonManagedReference
    public JSONDatastream[] Datastreams;

    private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
    private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
    private static final String PDF = "application/pdf";

    public JSONSensor() {
        self = new SensorEntity();
    }

    public SensorEntity toEntity() {
        if (!generatedId && name == null) {
            Assert.isNull(name, INVALID_REFERENCED_ENTITY);
            Assert.isNull(description, INVALID_REFERENCED_ENTITY);
            Assert.isNull(encodingType, INVALID_REFERENCED_ENTITY);
            Assert.isNull(metadata, INVALID_REFERENCED_ENTITY);

            Assert.isNull(Datastreams, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(encodingType, INVALID_INLINE_ENTITY + "encodingType");
            Assert.notNull(metadata, INVALID_INLINE_ENTITY + "metadata");

            self.setIdentifier(identifier);
            self.setName(name);
            self.setDescription(description);

            if (encodingType.equalsIgnoreCase(STA_SENSORML_2)) {
                self.setFormat(new FormatEntity().setFormat(SENSORML_2));
                ProcedureHistoryEntity procedureHistoryEntity = new ProcedureHistoryEntity();
                procedureHistoryEntity.setProcedure(self);
                procedureHistoryEntity.setFormat(self.getFormat());
                procedureHistoryEntity.setStartTime(DateTime.now().toDate());
                procedureHistoryEntity.setXml(metadata);
                Set<ProcedureHistoryEntity> set = new LinkedHashSet<>();
                set.add(procedureHistoryEntity);
                self.setProcedureHistory(set);
            } else if (encodingType.equalsIgnoreCase(PDF)) {
                self.setFormat(new FormatEntity().setFormat(PDF));
                self.setDescriptionFile(metadata);
            } else {
                Assert.notNull(null, "Invalid encodingType supplied. Only SensorML or PDF allowed.");
            }

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
