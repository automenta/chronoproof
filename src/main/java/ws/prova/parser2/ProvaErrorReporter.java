package ws.prova.parser2;

import java.util.Map;

public interface ProvaErrorReporter {

    public Map getErrors();

    public void addError(final String line, final String desc);

    void reportError(String error);

    ProvaParsingException newException();

}
