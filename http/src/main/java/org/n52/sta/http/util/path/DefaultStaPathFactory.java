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

import java.lang.reflect.Method;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.n52.grammar.StaPathGrammar;
import org.n52.grammar.StaPathLexer;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.entity.Identifiable;

public class DefaultStaPathFactory extends PathFactory {

    @Override
    public StaPath< ? extends Identifiable> parse(String url) throws STAInvalidUrlException {
        CodePointCharStream charStream = CharStreams.fromString(url.trim());
        Lexer lexer = new StaPathLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        Parser grammar = new StaPathGrammar(tokenStream);
        replaceErrorListener(lexer);

        try {
            StaPathVisitor visitor = new StaPathVisitor();
            Method firstRuleMethod = grammar.getClass()
                                            .getDeclaredMethod("path");
            return ((ParserRuleContext) firstRuleMethod.invoke(grammar)).accept(visitor);
        } catch (Exception e) {
            throw new STAInvalidUrlException("Invalid URL: " + url);
        }
    }

}
