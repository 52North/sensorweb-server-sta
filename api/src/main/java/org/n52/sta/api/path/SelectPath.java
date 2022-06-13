
package org.n52.sta.api.path;

import java.util.List;

import org.n52.sta.api.entity.Identifiable;

public interface SelectPath<T extends Identifiable> {

    boolean isRef();

    Class<T> getEntityType();

    PathType getPathType();

    List<PathSegment> getPathSegments();

    enum PathType {
        collection,
        entity,
        property
    }
}
