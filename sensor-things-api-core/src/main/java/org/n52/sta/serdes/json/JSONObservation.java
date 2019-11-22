package org.n52.sta.serdes.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

import java.util.Date;

public class JSONObservation extends JSONBase.JSONwithIdTime implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String phenomenonTime;
    public String resultTime;
    public String result;
    public String[] resultQuality;
    public String validTime;
    public JsonNode parameters;

    public JSONFeatureOfInterest FeatureOfInterest;
    public JSONDatastream Datastream;

    //TODO: check datatypes

    public JSONObservation() {
    }

    public DataEntity<?> toEntity() {
        StaDataEntity observation = new StaDataEntity();

        if (!generatedId && result != null) {

            Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(result, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultQuality, INVALID_REFERENCED_ENTITY);
            Assert.isNull(parameters, INVALID_REFERENCED_ENTITY);

            observation.setIdentifier(identifier);
            return observation;
        } else {
            Assert.notNull(result, INVALID_INLINE_ENTITY + "result");
            Assert.notNull(resultTime, INVALID_INLINE_ENTITY + "resultTime");

            // phenomenonTime
            if (phenomenonTime != null) {
                Time time = parseTime(phenomenonTime);
                if (time instanceof TimeInstant) {
                    observation.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                    observation.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    observation.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                    observation.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            } else {
                // Use time of POST Request as fallback
                Date date = DateTime.now().toDate();
                observation.setSamplingTimeStart(date);
                observation.setSamplingTimeEnd(date);
            }
            // resultTime
            observation.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());

            // validTime
            if (validTime != null) {
                Time time = parseTime(validTime);
                if (time instanceof TimeInstant) {
                    observation.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                    observation.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    observation.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                    observation.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            }

            // parameters
            if (parameters != null) {
                //TODO: handle parameters
                //observation.setParameters();
            }

            observation.setFeatureOfInterest(FeatureOfInterest.toEntity());
            observation.setDatastream(Datastream.toEntity());

            observation.setValue(result);
            return observation;
        }
    }
}
