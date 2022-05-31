package org.n52.sta.api.path;

import java.util.List;

public interface Path {

    PathType getType();

    List<PathSegment> getPath();

    enum PathType {
        collection,
        entity,
        ref,
        property
    }
}
