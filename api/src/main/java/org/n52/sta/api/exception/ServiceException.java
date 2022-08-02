
package org.n52.sta.api.exception;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 2881577578006053038L;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
