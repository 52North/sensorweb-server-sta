package org.n52.sta.http.util.path;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

public class PathFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathFactory.class);

    private final Function<TokenStream, Parser> parserFactory;
    private final Function<CodePointCharStream, Lexer> lexerFactory;
    private final Supplier<ParseTreeVisitor<StaPath>> visitorFactory;
    private final String firstRule;

    public PathFactory(Function<CodePointCharStream, Lexer> lexerFactory,
                       Function<TokenStream, Parser> parserFactory,
                       Supplier<ParseTreeVisitor<StaPath>> visitorFactory,
                       String firstRule) {
        this.parserFactory = parserFactory;
        this.lexerFactory = lexerFactory;
        this.visitorFactory = visitorFactory;
        this.firstRule = firstRule;
    }

    public StaPath parsePath(String url) throws STAInvalidUrlException {
        CodePointCharStream charStream = CharStreams.fromString(url.trim());
        Lexer lexer = lexerFactory.apply(charStream);
        replaceErrorListener(lexer);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        Parser grammar = parserFactory.apply(tokenStream);
        replaceErrorListener(grammar);

        try {
            Method firstRuleMethod = grammar.getClass().getDeclaredMethod(firstRule);
            return ((ParserRuleContext) firstRuleMethod.invoke(grammar)).accept(visitorFactory.get());
        } catch (Exception e) {
            LOGGER.debug("invalid URL requested:" + url);
            throw new STAInvalidUrlException("invalid URL: " + url);
        }
    }

    private void replaceErrorListener(Recognizer<?, ?> recognizer) {
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
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                int charPositionInLine, String msg, RecognitionException e) {
            String message = null;
            if (hasOffendingToken(e)) {
                int tokenType = e.getOffendingToken().getType();
                String tokenName = vocabulary.getDisplayName(tokenType);
                message = String.format(
                    "Failed to parse QueryOptions due to %s with offending token: %s", msg,
                    tokenName);
            } else {
                message = String.format("Failed to parse QueryOptions due to error: %s", msg);
            }
            throw new IllegalStateException(message, e);
        }

        private boolean hasOffendingToken(RecognitionException e) {
            return e != null && e.getOffendingToken() != null;
        }
    }
}
