package org.n52.sta.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public abstract class AbstractSTASerializer<T> extends StdSerializer<T> {

    protected String rootUrl;
    protected String entitySetName;

    protected AbstractSTASerializer(Class<T> t) {
        super(t);
    }

    public void writeSelfLink(JsonGenerator gen, String id) throws IOException {
        gen.writeStringField("@iot.selfLink", rootUrl + entitySetName + "(" + id + ")");
    }

    public void writeId(JsonGenerator gen, String id) throws IOException {
        gen.writeStringField("@iot.id", id);
    }

    public void writeNavigationProp(JsonGenerator gen, String navigationProperty, String id) throws IOException {
        gen.writeStringField(navigationProperty + "@iot.navigationLink",
                rootUrl + entitySetName + "(" + id + ")/" + navigationProperty);
    }
}
