
package org.n52.sta.api.path;

import java.util.List;

public interface SelectPath {

    boolean isRef();

    PathType getPathType();

    List<PathSegment> getPathSegments();

    enum PathType {
        collection,
        entity,
        property
    }
}
