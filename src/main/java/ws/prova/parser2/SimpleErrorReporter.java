/*
 * Copyright 2015 me.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.prova.parser2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author me
 */
public class SimpleErrorReporter implements ProvaErrorReporter {
    
    private final static Logger log = Logger.getLogger("prova");
    
    private HashMap errors;

    public SimpleErrorReporter() {
        super();
    }
    
    @Override
    public void addError(final String line, final String desc) {
        if (errors == null) {
            errors = new HashMap();
        }
        errors.put(line, desc);
    }
    

    @Override
    public void reportError(String error) {
        //log.error("Error parsing prova: " + error);
        try {
            String[] e = error.split(" ", 3);
            addError(e[1], e[2]);
        }
        catch (Throwable e) {
            addError(error, Long.toString(System.currentTimeMillis()));
        }
            
    }

    @Override
    public Map getErrors() {
        if (errors == null) return Collections.EMPTY_MAP;
        return errors;
    }

    @Override
    public ProvaParsingException newException() {
        return new ProvaParsingException(errors == null ? Collections.EMPTY_MAP : errors);
    }
    
}
