package org.n52.sta.api.dto.impl.citsci;

import org.n52.sta.api.dto.impl.Datastream;
import org.n52.sta.api.dto.plus.LicenseDTO;
import org.n52.sta.api.dto.plus.PartyDTO;
import org.n52.sta.api.dto.plus.PlusDatastreamDTO;
import org.n52.sta.api.dto.plus.ProjectDTO;

public class PlusDatastream extends Datastream implements PlusDatastreamDTO {

    private ProjectDTO project;

    private PartyDTO party;

    private LicenseDTO license;

    public PlusDatastream() {

    }

    @Override public ProjectDTO getProject() {
        return project;
    }

    @Override public void setProject(ProjectDTO project) {
        this.project = project;
    }

    @Override public PartyDTO getParty() {
        return party;
    }

    @Override public void setParty(PartyDTO party) {
        this.party = party;
    }

    @Override public LicenseDTO getLicense() {
        return license;
    }

    @Override public void setLicense(LicenseDTO license) {
        this.license = license;
    }

}
