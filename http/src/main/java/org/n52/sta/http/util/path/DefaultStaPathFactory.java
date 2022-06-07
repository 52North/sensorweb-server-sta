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

public class DefaultStaPathFactory extends PathFactory {

    @Override
    public StaPath parse(String url) throws STAInvalidUrlException {
        CodePointCharStream charStream = CharStreams.fromString(url.trim());
        Lexer lexer = new StaPathLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        Parser grammar = new StaPathGrammar(tokenStream);
        replaceErrorListener(lexer);

        try {
            StaPathVisitor visitor = new StaPathVisitor();
            Method firstRuleMethod = grammar.getClass().getDeclaredMethod("path");
            return ((ParserRuleContext) firstRuleMethod.invoke(grammar)).accept(visitor);
        } catch (Exception e) {
            throw new STAInvalidUrlException("Invalid URL: " + url);
        }
    }
    
}
