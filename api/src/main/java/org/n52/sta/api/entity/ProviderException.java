package org.n52.sta.api.entity;

public class ProviderException extends Exception {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }

}
