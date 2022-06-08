
package org.n52.sta.http.util.path;

import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.n52.grammar.StaPathGrammar;
import org.n52.grammar.StaPathGrammarBaseVisitor;
import org.n52.sta.api.path.ODataPath;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.http.serialize.out.DatastreamJsonSerializer;
import org.n52.sta.http.serialize.out.FeatureOfInterestJsonSerializer;
import org.n52.sta.http.serialize.out.HistoricalLocationJsonSerializer;
import org.n52.sta.http.serialize.out.LocationJsonSerializer;
import org.n52.sta.http.serialize.out.ObservationJsonSerializer;
import org.n52.sta.http.serialize.out.ObservedPropertyJsonSerializer;
import org.n52.sta.http.serialize.out.SensorJsonSerializer;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;
import org.n52.sta.http.serialize.out.ThingJsonSerializer;

public class StaPathVisitor extends StaPathGrammarBaseVisitor<StaPath> {

    @Override
    public StaPath visitPath(StaPathGrammar.PathContext ctx) {
        StaPath path = this.visitResource(ctx.resource());
        // path ending in $ref
        if (ctx.getToken(StaPathGrammar.REF, 0) != null) {
            path.setRef(true);
        }
        return path;
    }

    @Override
    public StaPath visitResource(StaPathGrammar.ResourceContext ctx) {
        return ctx.getChild(0)
                  .accept(this);
    }

    private StaPath parseEntity(int ttEntity,
                                ParserRuleContext propertyCtx,
                                ParserRuleContext ctx,
                                Function<SerializationContext, StaBaseSerializer< ? >> serializerFactory) {
        TerminalNode entity = ctx.getToken(ttEntity, 0);

        // Parse optional Identifier
        StaPathGrammar.IdentifierContext identifierctx = ctx.getRuleContext(StaPathGrammar.IdentifierContext.class, 0);
        String identifier = null;
        if (identifierctx != null) {
            String text = identifierctx.getText();
            identifier = text.substring(1, text.length() - 1);
        }

        // path ending with entity identified by Id
        if (ctx.getToken(StaPathGrammar.SLASH, 0) == null) {
            return new StaPath(ODataPath.PathType.entity,
                               new PathSegment(entity.getText(), identifier),
                               serializerFactory);
        } else {
            // path ending in property
            if (propertyCtx != null) {
                return new StaPath(
                                   ODataPath.PathType.property,
                                   new PathSegment(entity.getText(),
                                                   identifier,
                                                   propertyCtx.getText()),
                                   serializerFactory);
            }

            // path does not end here but continues. Delegate to next segment
            StaPath path = identifier != null
                    ? this.visit(ctx.getChild(3))
                    : this.visit(ctx.getChild(2));
            path.addPathSegment(new PathSegment(entity.getText(), identifier));
            return path;
        }
    }

    @Override
    public StaPath visitDatastream(StaPathGrammar.DatastreamContext ctx) {
        return parseEntity((ctx.DATASTREAM() != null)
                ? StaPathGrammar.DATASTREAM
                : StaPathGrammar.DATASTREAMS,
                           ctx.datastreamProperty(),
                           ctx,
                           DatastreamJsonSerializer::new);
    }

    @Override
    public StaPath visitObservation(StaPathGrammar.ObservationContext ctx) {
        return parseEntity(StaPathGrammar.OBSERVATIONS,
                           ctx.observationProperty(),
                           ctx,
                           ObservationJsonSerializer::new);
    }

    @Override
    public StaPath visitThing(StaPathGrammar.ThingContext ctx) {
        return parseEntity((ctx.THING() != null)
                ? StaPathGrammar.THING
                : StaPathGrammar.THINGS,
                           ctx.thingProperty(),
                           ctx,
                           ThingJsonSerializer::new);
    }

    @Override
    public StaPath visitLocation(StaPathGrammar.LocationContext ctx) {
        return parseEntity(StaPathGrammar.LOCATIONS,
                           ctx.locationProperty(),
                           ctx,
                           LocationJsonSerializer::new);
    }

    @Override
    public StaPath visitHistoricalLocation(StaPathGrammar.HistoricalLocationContext ctx) {
        return parseEntity(StaPathGrammar.HISTORICAL_LOCATIONS,
                           ctx.historicalLocationProperty(),
                           ctx,
                           HistoricalLocationJsonSerializer::new);
    }

    @Override
    public StaPath visitSensor(StaPathGrammar.SensorContext ctx) {
        return parseEntity((ctx.SENSOR() != null)
                ? StaPathGrammar.SENSOR
                : StaPathGrammar.SENSORS,
                           ctx.sensorProperty(),
                           ctx,
                           SensorJsonSerializer::new);
    }

    @Override
    public StaPath visitObservedProperty(StaPathGrammar.ObservedPropertyContext ctx) {
        return parseEntity(
                           (ctx.OBSERVED_PROPERTY() != null)
                                   ? StaPathGrammar.OBSERVED_PROPERTY
                                   : StaPathGrammar.OBSERVED_PROPERTIES,
                           ctx.observedPropertyProperty(),
                           ctx,
                           ObservedPropertyJsonSerializer::new);
    }

    @Override
    public StaPath visitFeatureOfInterest(StaPathGrammar.FeatureOfInterestContext ctx) {
        return parseEntity(
                           (ctx.FEATURE_OF_INTEREST() != null)
                                   ? StaPathGrammar.FEATURE_OF_INTEREST
                                   : StaPathGrammar.FEATURES_OF_INTEREST,
                           ctx.featureOfInterestProperty(),
                           ctx,
                           FeatureOfInterestJsonSerializer::new);
    }

    @Override
    public StaPath visitDatastreams(StaPathGrammar.DatastreamsContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.DATASTREAMS()
                                              .getText()),
                           DatastreamJsonSerializer::new);
    }

    @Override
    public StaPath visitObservations(StaPathGrammar.ObservationsContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.OBSERVATIONS()
                                              .getText()),
                           ObservationJsonSerializer::new);
    }

    @Override
    public StaPath visitThings(StaPathGrammar.ThingsContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.THINGS()
                                              .getText()),
                           ThingJsonSerializer::new);
    }

    @Override
    public StaPath visitLocations(StaPathGrammar.LocationsContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.LOCATIONS()
                                              .getText()),
                           LocationJsonSerializer::new);
    }

    @Override
    public StaPath visitHistoricalLocations(StaPathGrammar.HistoricalLocationsContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.HISTORICAL_LOCATIONS()
                                              .getText()),
                           HistoricalLocationJsonSerializer::new);
    }

    @Override
    public StaPath visitSensors(StaPathGrammar.SensorsContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.SENSORS()
                                              .getText()),
                           SensorJsonSerializer::new);
    }

    @Override
    public StaPath visitObservedProperties(StaPathGrammar.ObservedPropertiesContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.OBSERVED_PROPERTIES()
                                              .getText()),
                           ObservedPropertyJsonSerializer::new);
    }

    @Override
    public StaPath visitFeaturesOfInterest(StaPathGrammar.FeaturesOfInterestContext ctx) {
        return new StaPath(
                           ODataPath.PathType.collection,
                           new PathSegment(ctx.FEATURES_OF_INTEREST()
                                              .getText()),
                           FeatureOfInterestJsonSerializer::new);
    }
}
