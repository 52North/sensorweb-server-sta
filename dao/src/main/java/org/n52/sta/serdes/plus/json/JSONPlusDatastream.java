package org.n52.sta.serdes.plus.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.sta.api.dto.vanilla.DatastreamDTO;
import org.n52.sta.serdes.vanilla.json.JSONDatastream;
import org.n52.sta.serdes.vanilla.json.JSONObservedProperty;
import org.n52.sta.serdes.vanilla.json.JSONSensor;
import org.n52.sta.serdes.vanilla.json.JSONThing;
import org.springframework.util.Assert;

@SuppressWarnings("checkstyle:VisibilityModifier")
public class JSONPlusDatastream extends JSONDatastream {

    @JsonManagedReference
    public JSONParty Party;
    @JsonManagedReference
    public JSONProject Project;

    public JSONPlusDatastream() {
        //self = new JSONPlusDatastream();
    }

    @Override protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
                case StaConstants.SENSORS:
                    Assert.isNull(Sensor, INVALID_DUPLICATE_REFERENCE);
                    this.Sensor = new JSONSensor();
                    this.Sensor.identifier = referencedFromID;
                    return;
                case StaConstants.OBSERVED_PROPERTIES:
                    Assert.isNull(ObservedProperty, INVALID_DUPLICATE_REFERENCE);
                    this.ObservedProperty = new JSONObservedProperty();
                    this.ObservedProperty.identifier = referencedFromID;
                    return;
                case StaConstants.THINGS:
                    Assert.isNull(Thing, INVALID_DUPLICATE_REFERENCE);
                    this.Thing = new JSONThing();
                    this.Thing.identifier = referencedFromID;
                    return;
                default:
                    throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    protected DatastreamDTO createPostEntity() {
        DatastreamDTO base = super.createPostEntity();

        /*
        if (Party != null) {
            self.setParty(org.n52.sta.api.dto.impl.citsci.Party.parseToDTO(JSONBase.EntityType.FULL,
                                                                           JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONParty) {
            self.setParty(((JSONParty) backReference).self);
        }

        if (Project != null) {
            self.setProject(org.n52.sta.api.dto.impl.citsci.Project.parseToDTO(JSONBase.EntityType.FULL,
                                                                               JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONProject) {
            self.setProject(((JSONProject) backReference).self);
        }
         */

        return self;
    }

    protected DatastreamDTO createPatchEntity() {
        super.createPatchEntity();
        /*
        if (Party != null) {
            self.setParty(org.n52.sta.api.dto.impl.citsci.Party.parseToDTO(JSONBase.EntityType.FULL,
                                                                           JSONBase.EntityType.REFERENCE));
        }

        if (Project != null) {
            self.setProject(org.n52.sta.api.dto.impl.citsci.Project.parseToDTO(JSONBase.EntityType.FULL,
                                                                               JSONBase.EntityType.REFERENCE));
        }
        */
        return self;
    }
}
