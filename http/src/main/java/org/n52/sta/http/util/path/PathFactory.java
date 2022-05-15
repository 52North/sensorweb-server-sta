package org.n52.sta.http.util.path;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

public class PathFactory {

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

    public StaPath parsePath(String query) {
        CodePointCharStream charStream = CharStreams.fromString(query.trim());
        Lexer lexer = lexerFactory.apply(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        Parser grammar = parserFactory.apply(tokenStream);

        try {
            Method firstRuleMethod = grammar.getClass().getDeclaredMethod(firstRule);
            return ((ParserRuleContext) firstRuleMethod.invoke(grammar)).accept(visitorFactory.get());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
