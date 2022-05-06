package org.n52.sta.http.serialize.out;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.n52.shetland.ogc.sta.StaConstants;

public abstract class StaBaseSerializer<T> extends StdSerializer<T> implements StaSerializer<T> {

    private final String serviceUri;

    private final String collectionName;

    private final transient SerializationContext context;

    protected StaBaseSerializer(SerializationContext context, String collectionName, Class<T> type) {
        super(type);
        Objects.requireNonNull(context, "context must not be null!");
        Objects.requireNonNull(collectionName, "collectionName must not be null!");
        this.serviceUri = removeTrailingSlash(context.getServiceUri());
        this.collectionName = collectionName;
        this.context = context;
        context.register(this);
    }

    private static String removeTrailingSlash(String serviceUri) {
        return serviceUri.endsWith("/")
                ? serviceUri.substring(0, serviceUri.length() - 1)
                : serviceUri;
    }

    @Override
    public Class<T> getType() {
        return handledType();
    }

    protected void writeProperty(String name, ThrowingFieldWriter fieldWriter) throws IOException {
        if (context.isSelected(name)) {
            fieldWriter.writeIfSelected(name);
        }
    }

    protected <E> void writeMember(
            String member,
            String parentId,
            JsonGenerator gen,
            Function<SerializationContext, StaBaseSerializer<E>> serializerFactory,
            ThrowingMemberWriter<E> memberWriter) throws IOException {
        if (context.isSelected(member)) {
            Optional<StaBaseSerializer<E>> serializer = context.getQueryOptionsForExpanded(member)
                    .map(expandQueryOptions -> SerializationContext.create(context, expandQueryOptions))
                    .map(serializerFactory::apply);
            if (serializer.isPresent()) {
                memberWriter.writeIfSelected(serializer.get());
            } else {
                String navLink = createNavigationLink(parentId, member);
                String iotNavLinkProperty = String.format("%s%s", member, StaConstants.AT_IOT_NAVIGATIONLINK);
                gen.writeStringField(iotNavLinkProperty, navLink);
            }
        }
    }

    protected String createSelfLink(String id) {
        return String.format("%s/%s(%s)", serviceUri, collectionName, id);
    }

    protected String createNavigationLink(String id, String member) {
        return String.format("%s/%s", createSelfLink(id), member);
    }

    @FunctionalInterface
    protected interface ThrowingFieldWriter {
        void writeIfSelected(String name) throws IOException;
    }

    @FunctionalInterface
    protected interface ThrowingMemberWriter<T> {
        void writeIfSelected(StaBaseSerializer<T> context) throws IOException;
    }

}
