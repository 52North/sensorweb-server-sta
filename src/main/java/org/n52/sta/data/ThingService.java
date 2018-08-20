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

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingService {

    @Autowired
    private ThingMapper thingMapper;

    public EntityCollection getThings() {
        String name;
        EntityCollection retEntitySet = new EntityCollection();
        List<ThingEntity> things = new ArrayList<>();

        ThingEntity e1 = new ThingEntity();
        name = "Oven";
        e1.setId(3l);
        e1.setName(name);
        e1.setDescription("Nice oven");
        e1.setProperties("{}");
        e1.setLocationEntity(createLocation(name));

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

    private LocationEntity createLocation(String name) {
        LocationEntity loc = new LocationEntity();
        loc.setId(42L);
        loc.setDescription("Demo Location for " + name);
        loc.setGeometry(new GeometryFactory().createPoint(new Coordinate(12, 12)));
        return loc;
    }
}
