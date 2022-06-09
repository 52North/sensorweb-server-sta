
package org.n52.sta.http.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;

/**
 * Holds a URI referencing an STA entity.
 */
public class StaPath implements SelectPath {

    private final SelectPath.PathType type;

    private final List<PathSegment> path;

    private final Function<SerializationContext, StaBaseSerializer< ? >> serializerFactory;

    private boolean isRef;

    public StaPath(PathType type,
                   PathSegment segment,
                   Function<SerializationContext, StaBaseSerializer< ? >> serializerFactory) {
        this.type = type;
        this.serializerFactory = serializerFactory;
        this.path = new ArrayList<>();
        this.path.add(segment);
    }

    @Override
    public PathType getType() {
        return type;
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return path;
    }

    void addPathSegment(PathSegment pathSegment) {
        if (pathSegment != null) {
            path.add(pathSegment);
        }
    }

    public Function<SerializationContext, StaBaseSerializer< ? >> getSerializerFactory() {
        return serializerFactory;
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
