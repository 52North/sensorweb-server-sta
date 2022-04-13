package org.n52.sta.api.serializer;

@FunctionalInterface
public interface Deserializer<S, T> {

    T deserialize(S toDeserialize);
}
