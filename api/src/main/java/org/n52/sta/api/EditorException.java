package org.n52.sta.api;

public class EditorException extends Exception {

    private static final long serialVersionUID = 3628980884160012470L;

    public EditorException(String message) {
        super(message);
    }

    public EditorException(Throwable cause) {
        super(cause);
    }

    public EditorException(String message, Throwable cause) {
        super(message, cause);
    }

}
