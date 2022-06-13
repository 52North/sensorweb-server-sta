
package org.n52.sta.http.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;

/**
 * Holds a URI referencing an STA entity.
 */
public class StaPath<T extends Identifiable> implements SelectPath<T> {

    private final Class<T> entityType;

    private final SelectPath.PathType pathType;

    private final List<PathSegment> pathSegments;

    private final Function<SerializationContext, StaBaseSerializer<T>> serializerFactory;

    private boolean isRef;

    public StaPath(PathType pathType,
                   PathSegment pathSegment,
                   Function<SerializationContext, StaBaseSerializer<T>> serializerFactory,
                   Class<T> type) {

        this.entityType = type;
        this.pathType = pathType;
        this.serializerFactory = serializerFactory;
        this.pathSegments = new ArrayList<>();
        this.pathSegments.add(pathSegment);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public PathType getPathType() {
        return pathType;
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return pathSegments;
    }

    void addPathSegment(PathSegment pathSegment) {
        if (pathSegment != null) {
            pathSegments.add(pathSegment);
        }
    }

    public StaBaseSerializer<T> createSerializer(SerializationContext serializationContext) {
        return serializerFactory.apply(serializationContext);
    }

    @Override
    public boolean isRef() {
        return isRef;
    }

    public void setRef(boolean ref) {
        isRef = ref;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StaPath {\ntype: ")
               .append(pathType)
               .append("\npath:\n");
        for (PathSegment seg : pathSegments) {
            builder.append("    ")
                   .append(seg.toString())
                   .append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

}
