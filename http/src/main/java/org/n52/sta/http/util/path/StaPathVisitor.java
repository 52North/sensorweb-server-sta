package org.n52.sta.http.util.path;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.n52.grammar.STAPathGrammar;
import org.n52.grammar.STAPathGrammarBaseVisitor;

import java.util.Optional;

public class StaPathVisitor extends STAPathGrammarBaseVisitor<StaPath> {

    @Override
    public StaPath visitPath(STAPathGrammar.PathContext ctx) {
        return this.visitResource(ctx.resource());
    }

    @Override
    public StaPath visitResource(STAPathGrammar.ResourceContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    private StaPath parseEntity(int ttEntity, ParserRuleContext propertyCtx, ParserRuleContext ctx) {
        TerminalNode entity = ctx.getToken(ttEntity, 0);

        // Parse optional Identifier
        STAPathGrammar.IdentifierContext identifierctx = ctx.getRuleContext(STAPathGrammar.IdentifierContext.class, 0);
        Optional<String> identifier;
        if (identifierctx != null) {
            identifier = Optional.ofNullable(identifierctx.getText());
        } else {
            identifier = Optional.empty();
        }

        // path ending with entity identified by Id
        if (ctx.getToken(STAPathGrammar.SLASH, 0) == null) {
            return new StaPath(StaPath.PathType.entity,
                               new PathSegment(entity.getText(), identifier)
            );
        } else {
            // path ending in $ref
            if (ctx.getToken(STAPathGrammar.REF, 0) != null) {
                return new StaPath(StaPath.PathType.ref,
                                   new PathSegment(entity.getText(), identifier)
                );
            }
            // path ending in property
            if (propertyCtx != null) {
                return new StaPath(StaPath.PathType.property,
                                   new PathSegment(entity.getText(),
                                                   identifier,
                                                   Optional.ofNullable(propertyCtx.getText()))
                );
            }

            // path does not end here but continues. Delegate to next segment
            StaPath path;
            if (identifier.isPresent()) {
                path = this.visit(ctx.getChild(3));
            } else {
                path = this.visit(ctx.getChild(2));
            }
            path.getPath().add(new PathSegment(entity.getText(),
                                               identifier));
            return path;
        }
    }

    @Override
    public StaPath visitDatastream(STAPathGrammar.DatastreamContext ctx) {
        return parseEntity((ctx.DATASTREAM() != null) ? STAPathGrammar.DATASTREAM : STAPathGrammar.DATASTREAMS,
                           ctx.datastreamProperty(),
                           ctx);
    }

    @Override
    public StaPath visitObservation(STAPathGrammar.ObservationContext ctx) {
        return parseEntity(STAPathGrammar.OBSERVATIONS, ctx.observationProperty(), ctx);
    }

    @Override
    public StaPath visitThing(STAPathGrammar.ThingContext ctx) {
        return parseEntity((ctx.THING() != null) ? STAPathGrammar.THING : STAPathGrammar.THINGS,
                           ctx.thingProperty(),
                           ctx);
    }

    @Override
    public StaPath visitLocation(STAPathGrammar.LocationContext ctx) {
        return parseEntity(STAPathGrammar.LOCATIONS, ctx.locationProperty(), ctx);
    }

    @Override
    public StaPath visitHistoricalLocation(STAPathGrammar.HistoricalLocationContext ctx) {
        return parseEntity(STAPathGrammar.HISTORICAL_LOCATIONS, ctx.historicalLocationProperty(), ctx);
    }

    @Override
    public StaPath visitSensor(STAPathGrammar.SensorContext ctx) {
        return parseEntity((ctx.SENSOR() != null) ? STAPathGrammar.SENSOR : STAPathGrammar.SENSORS,
                           ctx.sensorProperty(),
                           ctx);
    }

    @Override
    public StaPath visitObservedProperty(STAPathGrammar.ObservedPropertyContext ctx) {
        return parseEntity((ctx.OBSERVED_PROPERTY() != null) ?
                               STAPathGrammar.OBSERVED_PROPERTY :
                               STAPathGrammar.OBSERVED_PROPERTIES,
                           ctx.observedPropertyProperty(),
                           ctx);
    }

    @Override
    public StaPath visitFeatureOfInterest(STAPathGrammar.FeatureOfInterestContext ctx) {
        return parseEntity((ctx.FEATURE_OF_INTEREST() != null) ?
                               STAPathGrammar.FEATURE_OF_INTEREST :
                               STAPathGrammar.FEATURES_OF_INTEREST,
                           ctx.featureOfInterestProperty(),
                           ctx);
    }

    @Override
    public StaPath visitDatastreams(STAPathGrammar.DatastreamsContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.DATASTREAMS().getText()));
    }

    @Override
    public StaPath visitObservations(STAPathGrammar.ObservationsContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.OBSERVATIONS().getText()));
    }

    @Override
    public StaPath visitThings(STAPathGrammar.ThingsContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.THINGS().getText()));
    }

    @Override
    public StaPath visitLocations(STAPathGrammar.LocationsContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.LOCATIONS().getText()));
    }

    @Override
    public StaPath visitHistoricalLocations(STAPathGrammar.HistoricalLocationsContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.HISTORICAL_LOCATIONS().getText()));
    }

    @Override
    public StaPath visitSensors(STAPathGrammar.SensorsContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.SENSORS().getText()));
    }

    @Override
    public StaPath visitObservedProperties(STAPathGrammar.ObservedPropertiesContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.OBSERVED_PROPERTIES().getText()));
    }

    @Override
    public StaPath visitFeaturesOfInterest(STAPathGrammar.FeaturesOfInterestContext ctx) {
        return new StaPath(StaPath.PathType.collection, new PathSegment(ctx.FEATURES_OF_INTEREST().getText()));
    }
}
