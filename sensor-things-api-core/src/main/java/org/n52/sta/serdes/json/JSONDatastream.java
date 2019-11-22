package org.n52.sta.serdes.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

public class JSONDatastream extends JSONBase.JSONwithIdNameDescriptionTime implements AbstractJSONEntity {

    static class JSONUnitOfMeasurement {
        public String symbol;
        public String name;
        public String definition;
    }

    // JSON Properties. Matched by Annotation or variable name
    public String observationType;
    public JSONUnitOfMeasurement unitOfMeasurement;
    public JsonNode observedArea;
    public String phenomenonTime;
    public String resultTime;
    public JSONSensor Sensor;
    public JSONThing Thing;
    public JSONObservedProperty ObservedProperty;
    public JSONObservation[] Observations;

    private final GeometryFactory factory =
            new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private final String OM_CategoryObservation =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation";
    private final String OM_CountObservation =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation";
    private final String OM_Measurement =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
    private final String OM_Observation =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";
    private final String OM_TruthObservation =
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation";


    public JSONDatastream() {
    }

    public DatastreamEntity toEntity() {
        DatastreamEntity datastream = new DatastreamEntity();

        if (!generatedId && name != null) {
            Assert.notNull(name, INVALID_REFERENCED_ENTITY);
            Assert.notNull(description, INVALID_REFERENCED_ENTITY);
            Assert.notNull(observationType, INVALID_REFERENCED_ENTITY);
            Assert.notNull(unitOfMeasurement, INVALID_REFERENCED_ENTITY);
            Assert.notNull(observedArea, INVALID_REFERENCED_ENTITY);
            Assert.notNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.notNull(resultTime, INVALID_REFERENCED_ENTITY);

            Assert.notNull(Sensor, INVALID_REFERENCED_ENTITY);
            Assert.notNull(ObservedProperty, INVALID_REFERENCED_ENTITY);
            Assert.notNull(Thing, INVALID_REFERENCED_ENTITY);
            Assert.notNull(Observations, INVALID_REFERENCED_ENTITY);

            datastream.setIdentifier(identifier);
            return datastream;
        } else {
            Assert.notNull(name, INVALID_INLINE_ENTITY + "name");
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.state(
                    observationType.equals(OM_CategoryObservation)
                            || observationType.equals(OM_CountObservation)
                            || observationType.equals(OM_Measurement)
                            || observationType.equals(OM_Observation)
                            || observationType.equals(OM_TruthObservation)
            );
            Assert.notNull(description, INVALID_INLINE_ENTITY + "description");
            Assert.notNull(unitOfMeasurement, INVALID_INLINE_ENTITY + "unitOfMeasurement");
            Assert.notNull(unitOfMeasurement.name, INVALID_INLINE_ENTITY + "unitOfMeasurement->name");
            Assert.notNull(unitOfMeasurement.symbol, INVALID_INLINE_ENTITY + "unitOfMeasurement->symbol");
            Assert.notNull(unitOfMeasurement.definition, INVALID_INLINE_ENTITY + "unitOfMeasurement->definition");

            Assert.notNull(Sensor, INVALID_INLINE_ENTITY + "Sensor");
            Assert.notNull(ObservedProperty, INVALID_INLINE_ENTITY + "ObservedProperty");
            Assert.notNull(Thing, INVALID_INLINE_ENTITY + "Thing");

            datastream.setIdentifier(identifier);
            datastream.setName(name);
            datastream.setDescription(description);
            datastream.setObservationType(new FormatEntity().setFormat(observationType));

            if (observedArea != null) {
                GeoJsonReader reader = new GeoJsonReader(factory);
                try {
                    datastream.setGeometry(reader.read(observedArea.toString()));
                } catch (ParseException e) {
                    Assert.notNull(null, "Could not parse observedArea to GeoJSON. Error was:" + e.getMessage());
                }
            }

            UnitEntity unit = new UnitEntity();
            unit.setLink(unitOfMeasurement.definition);
            unit.setName(unitOfMeasurement.name);
            unit.setSymbol(unitOfMeasurement.symbol);
            datastream.setUnit(unit);

            if (resultTime != null) {
                Time time = parseTime(resultTime);
                if (time instanceof TimeInstant) {
                    datastream.setResultTimeStart(((TimeInstant) time).getValue().toDate());
                    datastream.setResultTimeEnd(((TimeInstant) time).getValue().toDate());
                } else if (time instanceof TimePeriod) {
                    datastream.setResultTimeStart(((TimePeriod) time).getStart().toDate());
                    datastream.setResultTimeEnd(((TimePeriod) time).getEnd().toDate());
                }
            }

            if (phenomenonTime != null) {
                // phenomenonTime (aka samplingTime) is automatically calculated based on associated Observations
                // phenomenonTime parsed from json is therefore ignored.
            }

            datastream.setThing(Thing.toEntity());
            datastream.setObservableProperty(ObservedProperty.toEntity());
            datastream.setProcedure(Sensor.toEntity());
            return datastream;
        }
    }
}
