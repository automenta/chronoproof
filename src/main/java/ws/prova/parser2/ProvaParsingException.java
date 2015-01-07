package ws.prova.parser2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProvaParsingException extends RuntimeException {

    private static final long serialVersionUID = -1079902987548313008L;

    private String src;

    private String desc;

    private final Map<String, String> errors;

    public ProvaParsingException(Map<String,String> errors) {
        super("ProvaParsingException");
        this.errors = errors;
    }

    @Override
    public String toString() {
        return src;
    }

    public Map<String, String> errors() {
        if (errors == null) {
            return Collections.EMPTY_MAP;
        }
        return errors;
    }

    public String errorsToString() {
        StringBuilder sb = new StringBuilder(256);
        for (Iterator<String> kit = errors.keySet().iterator(); kit.hasNext();) {
            final Object key = kit.next();
            sb.append("Line ");
            sb.append(key);
            sb.append(" > ");
            sb.append(errors.get(key));
            sb.append('\n');
        }
        return sb.toString();
    }

    public String getSource() {
        return src;
    }

    public void setSource(final String src) {
        this.src = src;
    }


    public void setDescription(String desc) {
        this.desc = desc;
    }

    public String getDescription() {
        return desc;
    }

    @Override
    public Exception getCause() {
        return null;
    }
}
