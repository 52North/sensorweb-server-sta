package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

import java.util.Date;

public class JSONObservation extends JSONBase.JSONwithIdTime<StaDataEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String phenomenonTime;
    public String resultTime;
    public String result;
    public String[] resultQuality;
    public String validTime;
    public JsonNode parameters;

    @JsonManagedReference
    public JSONFeatureOfInterest FeatureOfInterest;
    @JsonManagedReference
    public JSONDatastream Datastream;

    public JSONObservation() {
        self = new StaDataEntity();
    }

    public StaDataEntity toEntity() {
        if (!generatedId && result == null) {

            Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(result, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultQuality, INVALID_REFERENCED_ENTITY);
            Assert.isNull(parameters, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            return self;
        } else {
            Assert.notNull(result, INVALID_INLINE_ENTITY + "result");
            Assert.notNull(resultTime, INVALID_INLINE_ENTITY + "resultTime");

            // phenomenonTime
            if (phenomenonTime != null) {
                Time time = parseTime(phenomenonTime);
                if (time instanceof TimeInstant) {
                    self.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                    self.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    self.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                    self.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            } else {
                // Use time of POST Request as fallback
                Date date = DateTime.now().toDate();
                self.setSamplingTimeStart(date);
                self.setSamplingTimeEnd(date);
            }
            // resultTime
            self.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());

            // validTime
            if (validTime != null) {
                Time time = parseTime(validTime);
                if (time instanceof TimeInstant) {
                    self.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                    self.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    self.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                    self.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            }

            // parameters
            if (parameters != null) {
                //TODO: handle parameters
                //observation.setParameters();
            }
            // result
            self.setValue(result);

            // Link to Datastream
            if (Datastream != null) {
                self.setDatastream(Datastream.toEntity());
            } else if (backReference instanceof JSONDatastream) {
                self.setDatastream(((JSONDatastream) backReference).getEntity());
            } else {
                Assert.notNull(null, INVALID_INLINE_ENTITY + "Datastream");
            }

            // Link to FOI
            if (FeatureOfInterest != null) {
                self.setFeatureOfInterest(FeatureOfInterest.toEntity());
            } else if (backReference instanceof JSONFeatureOfInterest) {
                self.setFeatureOfInterest(((JSONFeatureOfInterest) backReference).getEntity());
            } else {
                Assert.notNull(null, INVALID_INLINE_ENTITY + "FeatureOfInterest");
            }

            return self;
        }
    }
}
