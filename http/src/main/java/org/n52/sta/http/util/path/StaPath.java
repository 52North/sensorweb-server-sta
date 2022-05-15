package org.n52.sta.http.util.path;

import java.util.ArrayList;
import java.util.List;

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

    public StaPath(PathType type, PathSegment segment) {
        this.type = type;
        this.path.add(segment);
    }

    public PathType getType() {
        return type;
    }

    public List<PathSegment> getPath() {
        return path;
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
