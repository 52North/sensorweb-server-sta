package org.n52.sta.api.domain.aggregate;

public class InvalidAggregateException extends RuntimeException {

    private static final long serialVersionUID = 4013831859050802148L;

    public InvalidAggregateException(String message) {
        super(message);
    }

    public InvalidAggregateException(String message, Throwable cause) {
        super(message, cause);
    }

}
