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

package org.n52.sta.http.util.path;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Vocabulary;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.entity.Identifiable;

public abstract class PathFactory {

    public abstract StaPath< ? extends Identifiable> parse(String url) throws STAInvalidUrlException;

    protected void replaceErrorListener(Recognizer< ? , ? > recognizer) {
        recognizer.removeErrorListeners();
        Vocabulary vocabulary = recognizer.getVocabulary();
        recognizer.addErrorListener(new CustomErrorListener(vocabulary));
    }

    private static final class CustomErrorListener extends BaseErrorListener {

        private final Vocabulary vocabulary;

        private CustomErrorListener(Vocabulary vocabulary) {
            this.vocabulary = vocabulary;
        }

        @Override
        public void syntaxError(Recognizer< ? , ? > recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
            String message = null;
            if (hasOffendingToken(e)) {
                int tokenType = e.getOffendingToken()
                                 .getType();
                String tokenName = vocabulary.getDisplayName(tokenType);
                message = String.format(
                                        "Failed to parse URL due to %s with offending token: %s",
                                        msg,
                                        tokenName);
            } else {
                message = String.format("Failed to parse URL due to error: %s", msg);
            }
            throw new IllegalStateException(message, e);
        }

        private boolean hasOffendingToken(RecognitionException e) {
            return e != null && e.getOffendingToken() != null;
        }
    }
}
