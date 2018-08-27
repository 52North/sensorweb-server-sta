/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider;
import org.n52.sta.edm.provider.entities.LocationEntityProvider;
import org.n52.sta.edm.provider.entities.SensorEntityProvider;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.mapping.HistoricalLocationMapper;
import org.n52.sta.mapping.LocationMapper;
import org.n52.sta.mapping.SensorMapper;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class DummyEntityCreator {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private HistoricalLocationMapper historicalLocationMapper;

    @Autowired
    private SensorMapper sensorMapper;

    @Autowired
    private ThingMapper thingMapper;

    public Entity createEntity(String type, String id) {
        Entity entity = null;

        if (type.equals(ThingEntityProvider.ET_THING_NAME)) {
            entity = createThingEntityForId(id);
        } else if (type.equals(LocationEntityProvider.ET_LOCATION_NAME)) {
            entity = createLocationEntityForId(id);
        } else if (type.equals(HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_NAME)) {
            entity = createHistoricalLocationEntityForId(id);
        } else if (type.equals(SensorEntityProvider.ET_SENSOR_NAME)) {
            entity = createLocationEntityForId(id);
        }

        return entity;
    }

    public EntityCollection createEntityCollection(String type) {
        EntityCollection entityCollection = null;

        if (type.equals(ThingEntityProvider.ET_THING_NAME)) {
            entityCollection = createThingEntityCollection();
        } else if (type.equals(LocationEntityProvider.ET_LOCATION_NAME)) {
            entityCollection = createLocationEntityCollection();
        } else if (type.equals(HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_NAME)) {
            entityCollection = createHistoricalLocationEntityCollection();
        } else if (type.equals(SensorEntityProvider.ET_SENSOR_NAME)) {
            entityCollection = createSensorEntityCollection();
        }

        return entityCollection;
    }

    private List<LocationEntity> createLocationEntities() {
        List<LocationEntity> locations = new ArrayList<>();

        LocationEntity loc1 = new LocationEntity();
        loc1.setId(42L);
        loc1.setName("Demo Name 1");
        loc1.setDescription("Demo Location 1");
        loc1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));

        loc1.setLocationEncodings(createEncoding());

        locations.add(loc1);
        return locations;
    }

    private LocationEncodingEntity createEncoding() {
        LocationEncodingEntity encoding = new LocationEncodingEntity();
        encoding.setId(43L);
        encoding.setEncodingType("DemoEncoding");
        return encoding;
    }

    private EntityCollection createLocationEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();

        createLocationEntities().forEach(t -> retEntitySet.getEntities().add(locationMapper.createLocationEntity(t)));
        return retEntitySet;
    }

    private Entity createLocationEntityForId(String id) {
        LocationEntity loc1 = new LocationEntity();
        loc1.setId(Long.parseLong(id));
        loc1.setName("Demo Name 1");
        loc1.setDescription("Demo Location 1");
        loc1.setGeometry(new GeometryFactory().createPoint(new Coordinate(Math.random() * 90, Math.random() * 180)));

        return locationMapper.createLocationEntity(loc1);
    }

    private Entity createHistoricalLocationEntityForId(String id) {
        HistoricalLocationEntity loc = new HistoricalLocationEntity();
        loc.setTime(new Date());
        loc.setId(Long.parseLong(id));
        loc.setLocationEntity((LocationEntity) createLocationEntities().get(0));
        return historicalLocationMapper.createHistoricalLocationEntity(loc);
    }

    private EntityCollection createHistoricalLocationEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();
        List<HistoricalLocationEntity> locations = new ArrayList<>();

        HistoricalLocationEntity loc = new HistoricalLocationEntity();
        loc.setTime(new Date());
        loc.setId(44L);
        loc.setLocationEntity((LocationEntity) createLocationEntities().get(0));
        locations.add(loc);

        locations.forEach(t -> retEntitySet.getEntities().add(historicalLocationMapper.createHistoricalLocationEntity(t)));

        return retEntitySet;
    }

    private EntityCollection createSensorEntityCollection() {
        String name;
        EntityCollection retEntitySet = new EntityCollection();
        List<ProcedureEntity> things = new ArrayList<>();

        ProcedureEntity e1 = new ProcedureEntity();
        name = "Sensor 1";
        e1.setId(48L);
        e1.setName(name);
        e1.setDescription("Nice Sensor");

        things.add(e1);

        things.forEach(t -> retEntitySet.getEntities().add(sensorMapper.createSensorEntity(t)));

        return retEntitySet;
    }

    private Entity createSensorEntityForId(String id) {
        String name;
        ProcedureEntity e1 = new ProcedureEntity();
        name = "Sensor 1";
        e1.setId(Long.parseLong(id));
        e1.setName(name);
        e1.setDescription("Nice Sensor");
        return sensorMapper.createSensorEntity(e1);
    }

    private EntityCollection createThingEntityCollection() {
        String name;
        EntityCollection retEntitySet = new EntityCollection();
        List<ThingEntity> things = new ArrayList<>();

        ThingEntity e1 = new ThingEntity();
        name = "Oven";
        e1.setId(3l);
        e1.setName(name);
        e1.setDescription("Nice oven");
        e1.setProperties("{}");
        e1.setLocationEntities(new HashSet(createLocationEntities()));

        ThingEntity e2 = new ThingEntity();
        name = "Mower";
        e2.setId(2l);
        e2.setName(name);
        e2.setDescription("Awesome mower");
        e2.setProperties("{}");
        e2.setLocationEntities(new HashSet(createLocationEntities()));

        things.add(e1);
        things.add(e2);

        things.forEach(t -> retEntitySet.getEntities().add(thingMapper.createThingEntity(t)));

        return retEntitySet;
    }

    private Entity createThingEntityForId(String id) {
        String name;
        ThingEntity e1 = new ThingEntity();
        name = "Oven";
        e1.setId(Long.parseLong(id));
        e1.setName(name);
        e1.setDescription("Nice oven");
        e1.setProperties("{}");
        e1.setLocationEntities(new HashSet(createLocationEntities()));
        return thingMapper.createThingEntity(e1);
    }

}
