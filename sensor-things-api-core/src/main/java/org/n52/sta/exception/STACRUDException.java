package org.n52.sta.exception;

import org.springframework.http.HttpStatus;

public class STACRUDException extends Exception {

    public STACRUDException(String msg) {
        super(msg);
    }

    //TODO: implement
    public STACRUDException(String msg, HttpStatus status) {
        super(msg);
    }
}
