/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.HashSet;
import java.util.Set;
import org.apache.olingo.commons.api.data.Entity;
import org.n52.series.db.beans.sta.LocationEncodingEntity;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingService implements AbstractSensorThingsEntityService {

    @Autowired
    private ThingMapper thingMapper;

    private LocationEntity createLocation(String name) {
        LocationEntity loc = new LocationEntity();
        loc.setId(42L);
        loc.setDescription("Demo Location for " + name);
        loc.setGeometry(new GeometryFactory().createPoint(new Coordinate(12, 12)));
        return loc;
    }

    @Override
    public EntityCollection getEntityCollection() {
        String name;
        EntityCollection retEntitySet = new EntityCollection();
        List<ThingEntity> things = new ArrayList<>();

        ThingEntity e1 = new ThingEntity();
        name = "Oven";
        e1.setId(3l);
        e1.setName(name);
        e1.setDescription("Nice oven");
        e1.setProperties("{}");
        e1.setLocationEntities(createLocations());

        ThingEntity e2 = new ThingEntity();
        name = "Mower";
        e2.setId(2l);
        e2.setName(name);
        e2.setDescription("Awesome mower");
        e2.setProperties("{}");
//        e2.setLocationEntity(createLocation(name));

        things.add(e1);
        things.add(e2);

        things.forEach(t -> retEntitySet.getEntities().add(thingMapper.createThingEntity(t)));

        return retEntitySet;
    }

    @Override
    public Entity getEntityForId(String id) {
        String name;
        ThingEntity e1 = new ThingEntity();
        name = "Oven";
        e1.setId(Long.parseLong(id));
        e1.setName(name);
        e1.setDescription("Nice oven");
        e1.setProperties("{}");
        e1.setLocationEntities(createLocations());
        return thingMapper.createThingEntity(e1);
    }

    protected Set<LocationEntity> createLocations() {
        Set<LocationEntity> locations = new HashSet<>();

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
}
