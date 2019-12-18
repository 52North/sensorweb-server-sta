package org.n52.sta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.exception.DataException;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.exception.STAInvalidQueryException;
import org.n52.sta.exception.STAInvalidUrlException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.PersistenceException;

/**
 * Class used to customize Exception serialization to json.
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@ControllerAdvice
public class ControllerConfig {

    final ObjectMapper mapper;
    final HttpHeaders headers;

    public ControllerConfig(ObjectMapper mapper) {
        this.mapper = mapper;
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
    }

    @ExceptionHandler(value = PersistenceException.class)
    public ResponseEntity<Object> persistenceException(PersistenceException exception) {
        String msg = "";
        if (exception.getCause() instanceof DataException) {
            msg = ((DataException) exception.getCause()).getSQLException().toString();
        } else {
            msg = exception.getCause().toString();
        }

        return new ResponseEntity<>(
                createErrorMessage(exception.getClass().getName(), msg),
                headers,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = STACRUDException.class)
    public ResponseEntity<Object> staCrudException(STACRUDException exception) {
        return new ResponseEntity<>(
                createErrorMessage(exception.getClass().getName(), exception.getMessage()),
                headers,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(value = STAInvalidUrlException.class)
    public ResponseEntity<Object> staInvalidUrlException(STAInvalidUrlException exception) {
        return new ResponseEntity<>(
                createErrorMessage(exception.getClass().getName(), exception.getMessage()),
                headers,
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = STAInvalidQueryException.class)
    public ResponseEntity<Object> staInvalidQuery(STAInvalidQueryException exception) {
        return new ResponseEntity<>(
                createErrorMessage(exception.getClass().getName(), exception.getMessage()),
                headers,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = STAInvalidFilterExpressionException.class)
    public ResponseEntity<Object> staInvalidFilterExpressionException(STAInvalidFilterExpressionException exception) {
        return new ResponseEntity<>(
                createErrorMessage(exception.getClass().getName(), exception.getMessage()),
                headers,
                HttpStatus.BAD_REQUEST);
    }


    private String createErrorMessage(String error, String message) {
        ObjectNode root = mapper.createObjectNode();
        root.put("timestamp", System.currentTimeMillis());
        root.put("error", error);
        root.put("message", message);
        return root.toString();
    }
}
