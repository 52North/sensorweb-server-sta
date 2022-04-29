package org.n52.sta.http.util;

import java.util.Objects;

import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.config.ServerProperties;

public class StaUriValidator {

    private final ServerProperties settings;

    public StaUriValidator(ServerProperties settings) {
        this.settings = settings;
    }

    public void validateRequestPath(String path) throws STAInvalidUrlException {
        Objects.requireNonNull(path, "path must not be null!");
        if (path.isEmpty()) {
            throw new STAInvalidUrlException("'" + path + "' is not a valid resource path");
        }
        String toValidate = trimLeadingSlash(path);

        // TODO sta path grammar
    }

    private String trimLeadingSlash(String path) {
        return path.startsWith("/")
                ? path.substring(1)
                : path;
    }
}
