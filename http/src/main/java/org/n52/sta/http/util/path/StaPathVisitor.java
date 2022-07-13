/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */

package org.n52.sta.http.util.path;

import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.n52.grammar.StaPathGrammar;
import org.n52.grammar.StaPathGrammarBaseVisitor;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.SelectPath;
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

public class StaPathVisitor extends StaPathGrammarBaseVisitor<StaPath< ? extends Identifiable>> {

    @Override
    public StaPath< ? extends Identifiable> visitPath(StaPathGrammar.PathContext ctx) {
        StaPath< ? extends Identifiable> path = this.visitResource(ctx.resource());
        if (ctx.getToken(StaPathGrammar.REF, 0) != null) {
            path.setRef(true);
        }
        return path;
    }

    @Override
    public StaPath< ? extends Identifiable> visitResource(StaPathGrammar.ResourceContext ctx) {
        return ctx.getChild(0)
                  .accept(this);
    }

    private <T extends Identifiable> StaPath<T> parseEntity(int ttEntity,
            ParserRuleContext propertyCtx,
            ParserRuleContext ctx,
            Function<SerializationContext, StaBaseSerializer<T>> serializerFactory,
            Class<T> entityType) {
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
            return new StaPath<>(SelectPath.PathType.entity,
                                 new PathSegment(entity.getText(), identifier),
                                 serializerFactory,
                                 entityType);
        } else {
            // path ending in property
            if (propertyCtx != null) {
                SelectPath.PathType type;
                if (propertyCtx.getToken(StaPathGrammar.VALUE, 0) == null) {
                    type = SelectPath.PathType.property;
                } else {
                    type = SelectPath.PathType.value;
                }
                return new StaPath<>(type,
                                     new PathSegment(entity.getText(),
                                                     identifier,
                                                     propertyCtx.getChild(0).getText()),
                                     serializerFactory,
                                     entityType);
            }

            // path does not end here but continues. Delegate to next segment
            StaPath< ? extends Identifiable> path = identifier != null
                    ? this.visit(ctx.getChild(3))
                    : this.visit(ctx.getChild(2));
            path.addPathSegment(new PathSegment(entity.getText(), identifier));

            return (StaPath<T>) path;
        }
    }

    @Override
    public StaPath<Datastream> visitDatastream(StaPathGrammar.DatastreamContext ctx) {
        return parseEntity((ctx.DATASTREAM() != null)
                ? StaPathGrammar.DATASTREAM
                : StaPathGrammar.DATASTREAMS,
                           ctx.datastreamProperty(),
                           ctx,
                           DatastreamJsonSerializer::new,
                           Datastream.class);
    }

    @Override
    public StaPath<Observation> visitObservation(StaPathGrammar.ObservationContext ctx) {
        return parseEntity(StaPathGrammar.OBSERVATIONS,
                           ctx.observationProperty(),
                           ctx,
                           ObservationJsonSerializer::new,
                           Observation.class);
    }

    @Override
    public StaPath<Thing> visitThing(StaPathGrammar.ThingContext ctx) {
        return parseEntity((ctx.THING() != null)
                ? StaPathGrammar.THING
                : StaPathGrammar.THINGS,
                           ctx.thingProperty(),
                           ctx,
                           ThingJsonSerializer::new,
                           Thing.class);
    }

    @Override
    public StaPath<Location> visitLocation(StaPathGrammar.LocationContext ctx) {
        return parseEntity(StaPathGrammar.LOCATIONS,
                           ctx.locationProperty(),
                           ctx,
                           LocationJsonSerializer::new,
                           Location.class);
    }

    @Override
    public StaPath<HistoricalLocation> visitHistoricalLocation(StaPathGrammar.HistoricalLocationContext ctx) {
        return parseEntity(StaPathGrammar.HISTORICAL_LOCATIONS,
                           ctx.historicalLocationProperty(),
                           ctx,
                           HistoricalLocationJsonSerializer::new,
                           HistoricalLocation.class);
    }

    @Override
    public StaPath<Sensor> visitSensor(StaPathGrammar.SensorContext ctx) {
        return parseEntity((ctx.SENSOR() != null)
                ? StaPathGrammar.SENSOR
                : StaPathGrammar.SENSORS,
                           ctx.sensorProperty(),
                           ctx,
                           SensorJsonSerializer::new,
                           Sensor.class);
    }

    @Override
    public StaPath<ObservedProperty> visitObservedProperty(StaPathGrammar.ObservedPropertyContext ctx) {
        return parseEntity(
                           (ctx.OBSERVED_PROPERTY() != null)
                                   ? StaPathGrammar.OBSERVED_PROPERTY
                                   : StaPathGrammar.OBSERVED_PROPERTIES,
                           ctx.observedPropertyProperty(),
                           ctx,
                           ObservedPropertyJsonSerializer::new,
                           ObservedProperty.class);
    }

    @Override
    public StaPath<FeatureOfInterest> visitFeatureOfInterest(StaPathGrammar.FeatureOfInterestContext ctx) {
        return parseEntity(
                           (ctx.FEATURE_OF_INTEREST() != null)
                                   ? StaPathGrammar.FEATURE_OF_INTEREST
                                   : StaPathGrammar.FEATURES_OF_INTEREST,
                           ctx.featureOfInterestProperty(),
                           ctx,
                           FeatureOfInterestJsonSerializer::new,
                           FeatureOfInterest.class);
    }

    @Override
    public StaPath<Datastream> visitDatastreams(StaPathGrammar.DatastreamsContext ctx) {
        TerminalNode segment = ctx.DATASTREAMS();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             DatastreamJsonSerializer::new,
                             Datastream.class);
    }

    @Override
    public StaPath<Observation> visitObservations(StaPathGrammar.ObservationsContext ctx) {
        TerminalNode segment = ctx.OBSERVATIONS();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             ObservationJsonSerializer::new,
                             Observation.class);
    }

    @Override
    public StaPath<Thing> visitThings(StaPathGrammar.ThingsContext ctx) {
        TerminalNode segment = ctx.THINGS();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             ThingJsonSerializer::new,
                             Thing.class);
    }

    @Override
    public StaPath<Location> visitLocations(StaPathGrammar.LocationsContext ctx) {
        TerminalNode segment = ctx.LOCATIONS();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             LocationJsonSerializer::new,
                             Location.class);
    }

    @Override
    public StaPath<HistoricalLocation> visitHistoricalLocations(StaPathGrammar.HistoricalLocationsContext ctx) {
        TerminalNode segment = ctx.HISTORICAL_LOCATIONS();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             HistoricalLocationJsonSerializer::new,
                             HistoricalLocation.class);
    }

    @Override
    public StaPath<Sensor> visitSensors(StaPathGrammar.SensorsContext ctx) {
        TerminalNode segment = ctx.SENSORS();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             SensorJsonSerializer::new,
                             Sensor.class);
    }

    @Override
    public StaPath<ObservedProperty> visitObservedProperties(StaPathGrammar.ObservedPropertiesContext ctx) {
        TerminalNode segment = ctx.OBSERVED_PROPERTIES();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             ObservedPropertyJsonSerializer::new,
                             ObservedProperty.class);
    }

    @Override
    public StaPath<FeatureOfInterest> visitFeaturesOfInterest(StaPathGrammar.FeaturesOfInterestContext ctx) {
        TerminalNode segment = ctx.FEATURES_OF_INTEREST();
        return new StaPath<>(SelectPath.PathType.collection,
                             new PathSegment(segment.getText()),
                             FeatureOfInterestJsonSerializer::new,
                             FeatureOfInterest.class);
    }
}
