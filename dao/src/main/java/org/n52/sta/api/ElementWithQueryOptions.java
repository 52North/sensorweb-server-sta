///*
// * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
// * Software GmbH
// *
// * This program is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 2 as published
// * by the Free Software Foundation.
// *
// * If the program is linked with libraries which are licensed under one of
// * the following licenses, the combination of the program with the linked
// * library is not considered a "derivative work" of the program:
// *
// *     - Apache License, version 2.0
// *     - Apache Software License, version 1.0
// *     - GNU Lesser General Public License, version 3
// *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
// *     - Common Development and Distribution License (CDDL), version 1.0
// *
// * Therefore the distribution of the program linked with libraries licensed
// * under the aforementioned licenses, is permitted by the copyright holders
// * if the distribution is compliant with both the GNU General Public
// * License version 2 and the aforementioned licenses.
// *
// * This program is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// * Public License for more details.
// */
//
//package org.n52.sta.api;
//
//import org.n52.shetland.filter.ExpandItem;
//import org.n52.shetland.oasis.odata.query.option.QueryOptions;
//import org.n52.sta.api.dto.DatastreamDTO;
//import org.n52.sta.api.dto.FeatureOfInterestDTO;
//import org.n52.sta.api.dto.HistoricalLocationDTO;
//import org.n52.sta.api.dto.LocationDTO;
//import org.n52.sta.api.dto.ObservationDTO;
//import org.n52.sta.api.dto.ObservedPropertyDTO;
//import org.n52.sta.api.dto.SensorDTO;
//import org.n52.sta.api.dto.ThingDTO;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
// */
//public abstract class ElementWithQueryOptions<P> {
//
//    protected P entity;
//    protected QueryOptions queryOptions;
//    protected Set<String> fieldsToSerialize = new HashSet<>();
//    protected Map<String, QueryOptions> fieldsToExpand = new HashMap<>();
//    protected boolean hasSelectOption;
//    protected boolean hasExpandOption;
//
//    ElementWithQueryOptions(P entity, QueryOptions queryOptions) {
//
//    }
//
//    public QueryOptions getQueryOptions() {
//        return queryOptions;
//    }
//
//    public P getEntity() {
//        return entity;
//    }
//
//    public Set<String> getFieldsToSerialize() {
//        return fieldsToSerialize;
//    }
//
//    public Map<String, QueryOptions> getFieldsToExpand() {
//        return fieldsToExpand;
//    }
//
//    public boolean hasSelectOption() {
//        return hasSelectOption;
//    }
//
//    public boolean hasExpandOption() {
//        return hasExpandOption;
//    }
//
//    public static class ThingWithQueryOptions
//        extends ElementWithQueryOptions<ThingDTO> {
//
//        public ThingWithQueryOptions(ThingDTO entity, QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class SensorWithQueryOptions
//        extends ElementWithQueryOptions<SensorDTO> {
//
//        public SensorWithQueryOptions(SensorDTO entity, QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class ObservedPropertyWithQueryOptions
//        extends ElementWithQueryOptions<ObservedPropertyDTO> {
//
//        public ObservedPropertyWithQueryOptions(ObservedPropertyDTO entity, QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class ObservationWithQueryOptions
//        extends ElementWithQueryOptions<ObservationDTO> {
//
//        public ObservationWithQueryOptions(ObservationDTO entity,
//                                    QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class LocationWithQueryOptions
//        extends ElementWithQueryOptions<LocationDTO> {
//
//        public LocationWithQueryOptions(LocationDTO entity, QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class HistoricalLocationWithQueryOptions
//        extends ElementWithQueryOptions<HistoricalLocationDTO> {
//
//        public HistoricalLocationWithQueryOptions(HistoricalLocationDTO entity,
//                                           QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class FeatureOfInterestWithQueryOptions
//        extends  <FeatureOfInterestDTO> {
//
//        public FeatureOfInterestWithQueryOptions(FeatureOfInterestDTO entity, QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//
//
//    public static class DatastreamWithQueryOptions
//        extends ElementWithQueryOptions<DatastreamDTO> {
//
//        public DatastreamWithQueryOptions(DatastreamDTO entity, QueryOptions queryOptions) {
//            super(entity, queryOptions);
//        }
//    }
//}
