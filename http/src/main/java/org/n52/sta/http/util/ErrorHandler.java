/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.http.util;

import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.n52.sta.api.ProviderException;
import org.n52.sta.http.serialize.in.InvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class used to customize Exception serialization to json.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@ControllerAdvice
@SuppressWarnings("checkstyle:linelength")
public class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    private final ObjectMapper mapper;
    private final HttpHeaders headers;

    public ErrorHandler(ObjectMapper mapper) {
        this.mapper = mapper;
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
    }

    @ExceptionHandler(value = STANotFoundException.class)
    public ResponseEntity<Object> staNotFoundException(STANotFoundException exception) {
        String msg = createErrorMessage(exception);
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {
        HttpRequestMethodNotSupportedException.class,
        MismatchedInputException.class,
        STAInvalidFilterExpressionException.class,
        STAInvalidQueryException.class,
        InvalidValueException.class,
        STAInvalidUrlException.class
    })
    public ResponseEntity<Object> staInvalidUrlException(STAInvalidUrlException exception) {
        String msg = createErrorMessage(exception);
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = STACRUDException.class)
    public ResponseEntity<Object> staCrudException(STACRUDException exception) {
        String msg = createErrorMessage(exception);
        LOGGER.debug(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.valueOf(exception.getResponseStatus()
                                                                .getCode()));
    }

    @ExceptionHandler(value = {
        IllegalArgumentException.class,
        IllegalStateException.class,
        Exception.class,
        RuntimeException.class,
        ProviderException.class
    })
    public ResponseEntity<Object> fallbackException(Exception exception) {
        String msg = createErrorMessage(exception);
        LOGGER.error(msg, exception);
        return new ResponseEntity<>(msg,
                                    headers,
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String createErrorMessage(Exception e) {
        ObjectNode err = formatError(e);
        err.put("timestamp", System.currentTimeMillis());
        return err.toPrettyString();
    }

    private ObjectNode formatError(Exception e) {
        ObjectNode root = mapper.createObjectNode();
        root.put("timestamp", System.currentTimeMillis());
        root.put("error", e.getClass().getSimpleName());
        root.put("message", e.getMessage());
        if (e.getCause() != null) {
            root.put("cause", formatError((Exception) e.getCause()));
        }
        return root;
    }
}
