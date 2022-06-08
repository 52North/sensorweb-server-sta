
package org.n52.sta.api.path;

import java.util.Objects;
import java.util.Optional;

/**
 * POJO holding Path Information - http://example.org/v1.1/Datastreams(1)/Observations(1)/FeatureOfInterest -
 * http://example.org/v1.1/Datastreams(1)/Thing/Locations
 * SERVICE_ROOT_URI/ENTITY_SET_NAME(ID_OF_THE_ENTITY)/LINK_NAME
 * SERVICE_ROOT_URI/ENTITY_SET_NAME(KEY_OF_THE_ENTITY)/LINK_NAME/$ref
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class PathSegment {

    private final String collection;
    private final Optional<String> identifier;
    private final Optional<String> property;

    public PathSegment(String collection) {
        this(collection, null);
    }

    public PathSegment(String collection, String identifier) {
        this(collection, identifier, null);
    }

    public PathSegment(String collection, String identifier, String property) {
        Objects.requireNonNull(collection, "collection must not be null");
        this.collection = collection;
        this.identifier = Optional.ofNullable(identifier);
        this.property = Optional.ofNullable(property);
    }

    public String getCollection() {
        return collection;
    }

    public Optional<String> getIdentifier() {
        return identifier;
    }

    public Optional<String> getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return "{ collection="
                + collection
                +
                ", identifier="
                + identifier
                +
                ", property="
                + property
                +
                "}";
    }
}
