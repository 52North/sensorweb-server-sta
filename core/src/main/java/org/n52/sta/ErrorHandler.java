/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */

package org.n52.sta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.PersistenceException;

/**
 * Class used to customize Exception serialization to json.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@ControllerAdvice
public class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    private final ObjectMapper mapper;
    private final HttpHeaders headers;

    public ErrorHandler(ObjectMapper mapper) {
        this.mapper = mapper;
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
    }

    @ExceptionHandler(value = PersistenceException.class)
    public ResponseEntity<Object> persistenceException(PersistenceException exception) {
        String msg = "";
        if (exception.getCause() instanceof DataException) {
            msg = ((DataException) exception.getCause()).getSQLException().toString();
        } else if (exception.getCause() instanceof ConstraintViolationException) {
            msg = exception.getCause().getCause().getMessage();
        } else {
            msg = exception.getMessage();
        }

        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(
            createErrorMessage(exception.getClass().getName(), msg),
            headers,
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = STACRUDException.class)
    public ResponseEntity<Object> staCrudException(STACRUDException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.valueOf(exception.getResponseStatus().getCode()));
    }

    @ExceptionHandler(value = STANotFoundException.class)
    public ResponseEntity<Object> staNotFoundException(STANotFoundException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = STAInvalidUrlException.class)
    public ResponseEntity<Object> staInvalidUrlException(STAInvalidUrlException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = STAInvalidQueryException.class)
    public ResponseEntity<Object> staInvalidQuery(STAInvalidQueryException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MismatchedInputException.class)
    public ResponseEntity<Object> jacksonMismatchedInputException(MismatchedInputException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = STAInvalidFilterExpressionException.class)
    public ResponseEntity<Object> staInvalidFilterExpressionException(STAInvalidFilterExpressionException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> staIllegalArgumentException(IllegalArgumentException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<Object> staIllegalStateException(IllegalStateException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = HttpMessageNotWritableException.class)
    public ResponseEntity<Object> staHttpMessageNotWritableException(HttpMessageNotWritableException exception) {
        String msg = createErrorMessage(exception.getClass().getName(), exception.getMessage());
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
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
