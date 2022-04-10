package org.n52.sta.api.dto.plus;

import org.n52.sta.api.dto.vanilla.DatastreamDTO;

public interface PlusDatastreamDTO extends DatastreamDTO {

    LicenseDTO getLicense();

    void setLicense(LicenseDTO license);

    ProjectDTO getProject();

    void setProject(ProjectDTO project);

    PartyDTO getParty();

    void setParty(PartyDTO party);

}
