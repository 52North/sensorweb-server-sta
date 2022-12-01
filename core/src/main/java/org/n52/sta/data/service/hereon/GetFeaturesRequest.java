package org.n52.sta.data.service.hereon;

import org.n52.sensorweb.server.helgoland.adapters.connector.request.AbstractHereonRequest;

public class GetFeaturesRequest extends AbstractHereonRequest {

    public GetFeaturesRequest() {
        //TODO: only request fields needed for serializing to reduce payload size
        withOutField("*");

        //TODO: make this dependant on actual QueryOptions
        withResultRecordCount(1000L);

        //TODO: implement $skip
        // withResultOffset()
    }
}
