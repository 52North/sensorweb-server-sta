package org.n52.sta.http.util.path;

import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Holds a URI referencing an STA entity.
 */
public class StaPath {

    public enum PathType {
        collection,
        entity,
        ref,
        property
    }


    private final PathType type;

    private final List<PathSegment> path = new ArrayList<>();

    private final Function<SerializationContext, StaBaseSerializer<?>> serializerFactory;
    public StaPath(PathType type,
                   PathSegment segment,
                   Function<SerializationContext, StaBaseSerializer<?>> serializerFactory) {
        this.type = type;
        this.serializerFactory = serializerFactory;
        this.path.add(segment);
    }

    public PathType getType() {
        return type;
    }

    public List<PathSegment> getPath() {
        return path;
    }

    public Function<SerializationContext, StaBaseSerializer<?>> getSerializerFactory() {
        return serializerFactory;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StaPath {\ntype: ")
            .append(type)
            .append("\npath:\n");
        for (PathSegment seg : path) {
            builder.append("    ")
                .append(seg.toString())
                .append("\n");
        }
        builder.append("}");
        return builder.toString();
    }
}
