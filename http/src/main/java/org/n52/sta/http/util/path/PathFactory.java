package org.n52.sta.http.util.path;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Vocabulary;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;

public abstract class PathFactory {

    public abstract StaPath parse(String url) throws STAInvalidUrlException;

    protected void replaceErrorListener(Recognizer<?, ?> recognizer) {
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
                    "Failed to parse URL due to %s with offending token: %s", msg,
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
