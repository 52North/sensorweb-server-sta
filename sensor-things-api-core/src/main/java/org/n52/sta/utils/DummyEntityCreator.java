/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.n52.sta.utils;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//
//import org.apache.olingo.commons.api.data.Entity;
//import org.apache.olingo.commons.api.data.EntityCollection;
//import org.n52.series.db.beans.BlobDataEntity;
//import org.n52.series.db.beans.BooleanDataEntity;
//import org.n52.series.db.beans.CategoryDataEntity;
//import org.n52.series.db.beans.CountDataEntity;
//import org.n52.series.db.beans.DataEntity;
//import org.n52.series.db.beans.FeatureEntity;
//import org.n52.series.db.beans.FormatEntity;
//import org.n52.series.db.beans.GeometryDataEntity;
//import org.n52.series.db.beans.GeometryEntity;
//import org.n52.series.db.beans.PhenomenonEntity;
//import org.n52.series.db.beans.ProcedureEntity;
//import org.n52.series.db.beans.QuantityDataEntity;
//import org.n52.series.db.beans.ReferencedDataEntity;
//import org.n52.series.db.beans.TextDataEntity;
//import org.n52.series.db.beans.UnitEntity;
//import org.n52.series.db.beans.sta.DatastreamEntity;
//import org.n52.series.db.beans.sta.HistoricalLocationEntity;
//import org.n52.series.db.beans.sta.LocationEncodingEntity;
//import org.n52.series.db.beans.sta.LocationEntity;
//import org.n52.series.db.beans.sta.ThingEntity;
//import org.n52.sta.edm.provider.entities.DatastreamEntityProvider;
//import org.n52.sta.edm.provider.entities.FeatureOfInterestEntityProvider;
//import org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider;
//import org.n52.sta.edm.provider.entities.LocationEntityProvider;
//import org.n52.sta.edm.provider.entities.ObservationEntityProvider;
//import org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider;
//import org.n52.sta.edm.provider.entities.SensorEntityProvider;
//import org.n52.sta.edm.provider.entities.ThingEntityProvider;
//import org.n52.sta.mapping.DatastreamMapper;
//import org.n52.sta.mapping.FeatureOfInterestMapper;
//import org.n52.sta.mapping.HistoricalLocationMapper;
//import org.n52.sta.mapping.LocationMapper;
//import org.n52.sta.mapping.ObservationMapper;
//import org.n52.sta.mapping.ObservedPropertyMapper;
//import org.n52.sta.mapping.SensorMapper;
//import org.n52.sta.mapping.ThingMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.GeometryFactory;
//
///**
// *
// * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
// */
//@Component
//public class DummyEntityCreator {
//
//    @Autowired
//    private ThingMapper thingMapper;
//    @Autowired
//    private LocationMapper locationMapper;
//    @Autowired
//    private HistoricalLocationMapper historicalLocationMapper;
//    @Autowired
//    private SensorMapper sensorMapper;
//    @Autowired
//    private DatastreamMapper datastreamMapper;
//    @Autowired
//    private ObservedPropertyMapper observedPropertyMapper;
//    @Autowired
//    private ObservationMapper observationMapper;
//    @Autowired
//    private FeatureOfInterestMapper featureOfInterestMapper;
//
//    public Entity createEntity(String type, String id) {
//        Entity entity = null;
//
//        if (type.equals(ThingEntityProvider.ET_THING_NAME)) {
//            entity = createThingEntityForId(id);
//        } else if (type.equals(LocationEntityProvider.ET_LOCATION_NAME)) {
//            entity = createLocationEntityForId(id);
//        } else if (type.equals(HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_NAME)) {
//            entity = createHistoricalLocationEntityForId(id);
//        } else if (type.equals(SensorEntityProvider.ET_SENSOR_NAME)) {
//            entity = createSensorEntityForId(id);
//        } else if (type.equals(DatastreamEntityProvider.ET_DATASTREAM_NAME)) {
//            entity = createDatastreamEntityForId(id);
//        } else if (type.equals(ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_NAME)) {
//            entity = createObservedPropertyEntityForId(id);
//        } else if (type.equals(ObservationEntityProvider.ET_OBSERVATION_NAME)) {
//            entity = createObservationEntityForId(id);
//        } else if (type.equals(FeatureOfInterestEntityProvider.ET_FEATURE_OF_INTEREST_NAME)) {
//            entity = createFeatureOfInterestEntityForId(id);
//        }
//        return entity;
//    }
//
//    public EntityCollection createEntityCollection(String type) {
//        EntityCollection entityCollection = null;
//
//        if (type.equals(ThingEntityProvider.ET_THING_NAME)) {
//            entityCollection = createThingEntityCollection();
//        } else if (type.equals(LocationEntityProvider.ET_LOCATION_NAME)) {
//            entityCollection = createLocationEntityCollection();
//        } else if (type.equals(HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_NAME)) {
//            entityCollection = createHistoricalLocationEntityCollection();
//        } else if (type.equals(SensorEntityProvider.ET_SENSOR_NAME)) {
//            entityCollection = createSensorEntityCollection();
//        } else if (type.equals(ObservationEntityProvider.ET_OBSERVATION_NAME)) {
//            entityCollection = createObservationEntityCollection();
//        }
//
//        return entityCollection;
//    }
//
//    private Entity createThingEntityForId(String id) {
//        String name;
//        ThingEntity e1 = new ThingEntity();
//        name = "Oven";
//        e1.setId(Long.parseLong(id));
//        e1.setName(name);
//        e1.setDescription("Nice oven");
//        e1.setProperties("{}");
//        e1.setLocationEntities(new HashSet<LocationEntity>(createLocationEntities()));
//        return thingMapper.createThingEntity(e1);
//    }
//
//    private Entity createLocationEntityForId(String id) {
//        LocationEntity loc1 = new LocationEntity();
//        loc1.setId(Long.parseLong(id));
//        loc1.setName("Demo Name 1");
//        loc1.setDescription("Demo Location 1");
//        loc1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));
//        return locationMapper.createLocationEntity(loc1);
//    }
//
//    private Entity createHistoricalLocationEntityForId(String id) {
//        HistoricalLocationEntity loc = new HistoricalLocationEntity();
//        loc.setTime(new Date());
//        loc.setId(Long.parseLong(id));
//        loc.setLocationEntity((LocationEntity) createLocationEntities().get(0));
//        return historicalLocationMapper.createHistoricalLocationEntity(loc);
//    }
//
//    private Entity createSensorEntityForId(String id) {
//        ProcedureEntity e1 = new ProcedureEntity();
//        e1.setId(Long.parseLong(id));
//        e1.setName("Sensor" + id);
//        e1.setDescription("Nice Sensor");
//        e1.setDescriptionFile("Test Sensor Description File");
//        e1.setFormat(new FormatEntity().setFormat("Demo Sensor Format"));
//        return sensorMapper.createSensorEntity(e1);
//    }
//
//    private Entity createDatastreamEntityForId(String id) {
//        String name;
//        DatastreamEntity e1 = new DatastreamEntity();
//        name = "Datastream: " + id;
//        e1.setId(Long.parseLong(id));
//        e1.setName(name);
//        e1.setDescription("Nice Datastream");
//        e1.setObservationType(new FormatEntity().setFormat("Test Format"));
//
//        UnitEntity uom = new UnitEntity();
//        uom.setName("Test UOM");
//        uom.setSymbol("Test UOM Symbol");
//        uom.setLink("Test UOM Link");
//        e1.setUnitOfMeasurement(uom);
//
//        return datastreamMapper.createDatastreamEntity(e1);
//    }
//
//    private Entity createObservedPropertyEntityForId(String id) {
//        String name;
//        PhenomenonEntity e1 = new PhenomenonEntity();
//        name = "Observed Property: " + id;
//        e1.setId(Long.parseLong(id));
//        e1.setName(name);
//        e1.setDescription("Nice Property");
//        e1.setIdentifier("Test Identifier");
//        return observedPropertyMapper.createObservedPropertyEntity(e1);
//    }
//
//    private Entity createObservationEntityForId(String id) {
//        String name;
//        CountDataEntity e1 = new CountDataEntity();
//        name = "Observation: " + id;
//        e1.setId(Long.parseLong(id));
//        e1.setName(name);
//        e1.setResultTime(new Date(52L));
//        e1.setSamplingTimeStart(new Date(5000));
//        e1.setSamplingTimeEnd(new Date(5000));
//        e1.setValue(42);
//        return observationMapper.createObservationEntity(e1);
//    }
//
//    private Entity createFeatureOfInterestEntityForId(String id) {
//        String name;
//        FeatureEntity e1 = new FeatureEntity();
//        name = "Feature of Interest: " + id;
//        e1.setId(Long.parseLong(id));
//        e1.setName(name);
//        e1.setDescription("Nice FoI");
//        e1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));
//        return featureOfInterestMapper.createFeatureOfInterestEntity(e1);
//    }
//
//    private EntityCollection createThingEntityCollection() {
//        String name;
//        EntityCollection retEntitySet = new EntityCollection();
//        List<ThingEntity> things = new ArrayList<>();
//
//        ThingEntity e1 = new ThingEntity();
//        name = "Oven";
//        e1.setId(3l);
//        e1.setName(name);
//        e1.setDescription("Nice oven");
//        e1.setProperties("{}");
//        e1.setLocationEntities(new HashSet<LocationEntity>(createLocationEntities()));
//
//        ThingEntity e2 = new ThingEntity();
//        name = "Mower";
//        e2.setId(2l);
//        e2.setName(name);
//        e2.setDescription("Awesome mower");
//        e2.setProperties("{}");
//        e2.setLocationEntities(new HashSet<LocationEntity>(createLocationEntities()));
//
//        things.add(e1);
//        things.add(e2);
//
//        things.forEach(t -> retEntitySet.getEntities().add(thingMapper.createThingEntity(t)));
//
//        return retEntitySet;
//    }
//
//    private EntityCollection createLocationEntityCollection() {
//        EntityCollection retEntitySet = new EntityCollection();
//
//        createLocationEntities().forEach(t -> retEntitySet.getEntities().add(locationMapper.createLocationEntity(t)));
//        return retEntitySet;
//    }
//
//    private List<LocationEntity> createLocationEntities() {
//        List<LocationEntity> locations = new ArrayList<>();
//
//        LocationEntity loc1 = new LocationEntity();
//        loc1.setId(42L);
//        loc1.setName("Demo Name 1");
//        loc1.setDescription("Demo Location 1");
//        loc1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));
//
//        loc1.setLocationEncoding(createEncoding());
//
//        locations.add(loc1);
//        return locations;
//    }
//
//    private LocationEncodingEntity createEncoding() {
//        LocationEncodingEntity encoding = new LocationEncodingEntity();
//        encoding.setId(43L);
//        encoding.setEncodingType("DemoEncoding");
//        return encoding;
//    }
//
//
//    private EntityCollection createHistoricalLocationEntityCollection() {
//        EntityCollection retEntitySet = new EntityCollection();
//
//        retEntitySet.getEntities().add(this.createHistoricalLocationEntityForId("44"));
//        retEntitySet.getEntities().add(this.createHistoricalLocationEntityForId("45"));
//        retEntitySet.getEntities().add(this.createHistoricalLocationEntityForId("46"));
//
//        return retEntitySet;
//    }
//
//    private EntityCollection createSensorEntityCollection() {
//        EntityCollection retEntitySet = new EntityCollection();
//
//        retEntitySet.getEntities().add(this.createSensorEntityForId("52"));
//        retEntitySet.getEntities().add(this.createSensorEntityForId("53"));
//        return retEntitySet;
//    }
//
//    private EntityCollection createObservationEntityCollection() {
//        EntityCollection retEntitySet = new EntityCollection();
//
//        GeometryEntity geom = new GeometryEntity();
//        geom.setGeometry(new GeometryFactory().createPoint(new Coordinate(58, 58)));
//
//        retEntitySet.getEntities().add(this.createObservationEntity("52", new QuantityDataEntity(), new BigDecimal(52)));
//        retEntitySet.getEntities().add(this.createObservationEntity("53", new BlobDataEntity(), 53));
//        retEntitySet.getEntities().add(this.createObservationEntity("54", new BooleanDataEntity(), true));
//        retEntitySet.getEntities().add(this.createObservationEntity("55", new CategoryDataEntity(), "Category 55"));
//        retEntitySet.getEntities().add(this.createObservationEntity("57", new CountDataEntity(), 57));
//        retEntitySet.getEntities().add(this.createObservationEntity("58", new GeometryDataEntity(), geom));
//        retEntitySet.getEntities().add(this.createObservationEntity("59", new TextDataEntity(), "Text 59"));
//        retEntitySet.getEntities().add(this.createObservationEntity("62", new ReferencedDataEntity(), "62"));
//
//        //TODO: Test complex Data Entites
////      retEntitySet.getEntities().add(this.createObservationEntity("56", new ComplexDataEntity(), ));
////      retEntitySet.getEntities().add(this.createObservationEntity("60", new DataArrayDataEntity(), ));
////      retEntitySet.getEntities().add(this.createObservationEntity("61", new ProfileDataEntity(), ));
//        return retEntitySet;
//    }
//
//    @SuppressWarnings("unchecked")
//	private <T extends DataEntity, V> Entity createObservationEntity(String id, T element, V value) {
//    	Date timestamp = new Date((long) (Math.random() * 100000));
//    	element.setId(Long.parseLong(id));
//    	element.setResultTime(timestamp);
//    	element.setSamplingTimeStart(timestamp);
//    	element.setSamplingTimeEnd(timestamp);
//    	element.setValue(value);
//        return observationMapper.createObservationEntity(element);
//    }
//
//    private EntityCollection createFeatureOfInterestEntityCollection() {
//        EntityCollection retEntitySet = new EntityCollection();
//
//        retEntitySet.getEntities().add(this.createFeatureOfInterestEntityForId("72"));
//        retEntitySet.getEntities().add(this.createFeatureOfInterestEntityForId("73"));
//        return retEntitySet;
//    }
//
//
//}
