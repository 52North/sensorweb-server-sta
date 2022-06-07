package org.n52.sta.api.path;

import java.util.List;

public interface ODataPath {

    boolean isRef();

    PathType getType();

    List<PathSegment> getPathSegments();

    enum PathType {
        collection,
        entity,
        ref,
        property
    }
}
