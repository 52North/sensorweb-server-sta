package org.n52.sta.serdes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.cfg.NotYetImplementedException;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureHistoryEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.sta.data.service.ServiceUtils;
import org.n52.sta.exception.ParsingException;
import org.n52.sta.utils.TimeUtil;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class STASerdesTypes {

    abstract static class JSONwithId {
        @JsonProperty("@iot.id")
        public String identifier = UUID.randomUUID().toString();
        protected boolean generatedId = true;

        public void setIdentifier(String rawIdentifier) throws UnsupportedEncodingException {
            generatedId = false;
            identifier = URLEncoder.encode(rawIdentifier.replace("\'", ""), "utf-8");
        }
    }

    abstract static class JSONwithIdNameDescription extends JSONwithId {
        public String name;
        public String description;
    }

    static class JSONUnitOfMeasurement {
        public String symbol;
        public String name;
        public String definition;
    }

    static class JSONDatastream extends JSONwithIdNameDescription {

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

            Assert.notNull(name, "name must not be null");
            Assert.notNull(description, "description must not be null");
            Assert.state(
                    observationType.equals(OM_CategoryObservation)
                            || observationType.equals(OM_CountObservation)
                            || observationType.equals(OM_Measurement)
                            || observationType.equals(OM_Observation)
                            || observationType.equals(OM_TruthObservation)
            );
            Assert.notNull(unitOfMeasurement, "unitOfMeasurement must not be null");
            Assert.notNull(unitOfMeasurement.name, "unitOfMeasurement->name must not be null");
            Assert.notNull(unitOfMeasurement.symbol, "unitOfMeasurement->symbol must not be null");
            Assert.notNull(unitOfMeasurement.definition, "unitOfMeasurement->definition must not be null");
            Assert.notNull(Sensor, "Sensor must not be null");
            Assert.notNull(ObservedProperty, "ObservedProperty must not be null");
            Assert.notNull(Thing, "Thing must not be null");

            datastream.setIdentifier(identifier);
            datastream.setName(name);
            datastream.setDescription(description);
            datastream.setObservationType(new FormatEntity().setFormat(observationType));

            if (observedArea != null) {
                GeoJsonReader reader = new GeoJsonReader(factory);
                try {
                    datastream.setGeometry(reader.read(observedArea.toString()));
                } catch (ParseException e) {
                    Assert.notNull(null, "invalid observedArea provided");
                }
            }

            UnitEntity unit = new UnitEntity();
            unit.setLink(unitOfMeasurement.definition);
            unit.setName(unitOfMeasurement.name);
            unit.setSymbol(unitOfMeasurement.symbol);
            datastream.setUnit(unit);

            //TODO: add missing properties (phenomenontime etc.)

            datastream.setThing(Thing.toEntity());
            datastream.setObservableProperty(ObservedProperty.toEntity());
            datastream.setProcedure(Sensor.toEntity());
            return datastream;
        }
    }

    static class JSONThing extends JSONwithIdNameDescription {

        // JSON Properties. Matched by Annotation or variable name
        public String properties;
        public JSONLocation[] Locations;
        public JSONDatastream[] Datastreams;

        public JSONThing() {
        }

        PlatformEntity toEntity() {
            PlatformEntity thing = new PlatformEntity();

            // Check if Entity is only referenced via id and not provided fully
            // More complex since implementation allows custom setting of id by user
            if (!generatedId && name == null) {
                thing.setIdentifier(identifier);
                return thing;
            }

            Assert.notNull(name, "name must not be null");
            Assert.notNull(description, "description must not be null");

            thing.setIdentifier(identifier);
            thing.setName(name);
            thing.setDescription(description);

            //TODO: check if this is correct
            thing.setProperties(properties);

            if (Locations != null) {
                thing.setLocations(Arrays.stream(Locations)
                        .map(JSONLocation::toEntity)
                        .collect(Collectors.toSet()));
            }
            if (Datastreams != null) {
                thing.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }
            return thing;
        }
    }


    static class JSONSensor extends JSONwithIdNameDescription {

        // JSON Properties. Matched by Annotation or variable name
        public String properties;
        public String encodingType;
        public String metadata;
        public JSONDatastream[] Datastreams;

        private static final String STA_SENSORML_2 = "http://www.opengis.net/doc/IS/SensorML/2.0";
        private static final String SENSORML_2 = "http://www.opengis.net/sensorml/2.0";
        private static final String PDF = "application/pdf";

        public JSONSensor() {
        }

        public SensorEntity toEntity() {
            SensorEntity sensor = new SensorEntity();

            Assert.notNull(name, "name must not be null");
            Assert.notNull(description, "description must not be null");
            Assert.notNull(encodingType, "encodingType must not be null");
            Assert.notNull(metadata, "metadata must not be null");

            sensor.setIdentifier(identifier);
            sensor.setName(name);
            sensor.setDescription(description);
            if (encodingType.equalsIgnoreCase(STA_SENSORML_2)) {
                sensor.setFormat(new FormatEntity().setFormat(SENSORML_2));
                ProcedureHistoryEntity procedureHistoryEntity = new ProcedureHistoryEntity();
                procedureHistoryEntity.setProcedure(sensor);
                procedureHistoryEntity.setFormat(sensor.getFormat());
                procedureHistoryEntity.setStartTime(DateTime.now().toDate());
                procedureHistoryEntity.setXml(metadata);
                Set<ProcedureHistoryEntity> set = new LinkedHashSet<>();
                set.add(procedureHistoryEntity);
                sensor.setProcedureHistory(set);
            } else if (encodingType.equalsIgnoreCase("application/pdf")) {
                sensor.setFormat(new FormatEntity().setFormat(PDF));
                sensor.setDescriptionFile(metadata);
            } else {
                Assert.notNull(null, "invalid encodingType supplied. only SensorML or PDF allowed.");
            }

            if (Datastreams != null) {
                sensor.setDatastreams(Arrays.stream(Datastreams)
                        .map(JSONDatastream::toEntity)
                        .collect(Collectors.toSet()));
            }
            return sensor;
        }
    }

    static class JSONObservedProperty extends JSONwithIdNameDescription {

        // JSON Properties. Matched by Annotation or variable name
        public String definition;
        public JSONDatastream[] Datastreams;

        public JSONObservedProperty() {
        }

        public PhenomenonEntity toEntity() {
            ObservablePropertyEntity phenomenon = new ObservablePropertyEntity();

            Assert.notNull(name, "name must not be null");
            Assert.notNull(description, "description must not be null");
            Assert.notNull(definition, "definition must not be null");

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

    static class JSONObservation extends JSONwithIdNameDescription {

        // JSON Properties. Matched by Annotation or variable name
        public String phenomenonTime;
        public String resultTime;
        public String result;
        public String[] resultQuality;
        public String validTime;
        public String[] parameters;

        public JSONFeatureOfInterest FeatureOfInterest;
        public JSONDatastream Datastream;

        //TODO: check datatypes

        public JSONObservation() {
        }

        public DataEntity<?> toEntity() {
            throw new NotYetImplementedException();
        }
    }


    static class JSONLocation extends JSONwithIdNameDescription {

        // JSON Properties. Matched by Annotation or variable name
        public String encodingType;
        public JSONThing[] Things;
        public JsonNode location;

        private final GeometryFactory factory =
                new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

        public JSONLocation() {
        }

        public LocationEntity toEntity() {
            LocationEntity entity = new LocationEntity();

            Assert.notNull(name, "name must not be null");
            Assert.notNull(description, "description must not be null");
            Assert.notNull(location, "location must not be null");

            entity.setIdentifier(identifier);
            entity.setName(name);
            entity.setDescription(description);

            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                entity.setGeometry(reader.read(location.toString()));
            } catch (ParseException e) {
                //TODO: check if this is needed and what purpose it actually serves
                entity.setLocation(location.toString());
            }

            Assert.state(encodingType.equals("application/vnd.geo+json"),
                    "invalid encodingType supplied. Only GeoJSON (application/vnd.geo+json) is supported");
            entity.setLocationEncoding(new FormatEntity().setFormat(encodingType));

            if (Things != null) {
                entity.setThings(Arrays.stream(Things)
                        .map(JSONThing::toEntity)
                        .collect(Collectors.toSet()));
            }
            return entity;
        }
    }

    static class JSONHistoricalLocation extends JSONwithId {

        // JSON Properties. Matched by Annotation or variable name
        public Object time;
        public JSONThing Thing;
        public JSONLocation[] Locations;

        private Date date;

        public JSONHistoricalLocation() {
        }

        /**
         * Wrapper around rawTime property called by jackson while deserializing.
         *
         * @param rawTime raw Time
         */
        public void setTime(Object rawTime) throws ParsingException {
            Time time = TimeUtil.parseTime(rawTime);
            if (time instanceof TimeInstant) {
                date = ((TimeInstant) time).getValue().toDate();
            } else if (time instanceof TimePeriod) {
                date = ((TimePeriod) time).getEnd().toDate();
            } else {
                //TODO: refine error message
                throw new ParsingException("Invalid time format.");
            }
        }

        public HistoricalLocationEntity toEntity() {
            HistoricalLocationEntity entity = new HistoricalLocationEntity();
            entity.setIdentifier(identifier);
            entity.setTime(date);
            entity.setThing(Thing.toEntity());
            if (Locations != null) {
                entity.setLocations(Arrays.stream(Locations)
                        .map(JSONLocation::toEntity)
                        .collect(Collectors.toSet()));
            }
            return entity;
        }
    }

    static class JSONFeatureOfInterest extends JSONwithIdNameDescription {

        // JSON Properties. Matched by Annotation or variable name
        public String encodingType;
        public String feature;

        private final GeometryFactory factory =
                new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);


        public JSONFeatureOfInterest() {
        }

        public FeatureEntity toEntity() {
            FeatureEntity featureOfInterest = new FeatureEntity();

            Assert.notNull(name, "name must not be null");
            Assert.notNull(description, "description must not be null");
            Assert.notNull(feature, "location must not be null");

            featureOfInterest.setIdentifier(identifier);
            featureOfInterest.setName(name);
            featureOfInterest.setDescription(description);

            GeoJsonReader reader = new GeoJsonReader(factory);
            try {
                featureOfInterest.setGeometry(reader.read(feature));
            } catch (ParseException e) {
                Assert.notNull(null, "invalid feature supplied!");
            }
            featureOfInterest.setFeatureType(ServiceUtils.createFeatureType(featureOfInterest.getGeometry()));

            return featureOfInterest;
        }
    }

}
