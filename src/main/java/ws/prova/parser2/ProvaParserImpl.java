package ws.prova.parser2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.TreeAdaptor;
import org.apache.log4j.Logger;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Results;
import ws.prova.kernel2.Rule;
import ws.prova.parser.Prova2Lexer;
import ws.prova.parser.Prova2Parser;
import ws.prova.parser.ProvaWalker2;

public class ProvaParserImpl {

    private final static Logger log = Logger.getLogger("prova");

    public static ThreadLocal<Object[]> tlObjects = new ThreadLocal<Object[]>();

    public static ThreadLocal<KB> tlKB = new ThreadLocal<KB>();

    static ThreadLocal<Random> tlRandom = new ThreadLocal<Random>();

    public static ThreadLocal<Results> tlRS = new ThreadLocal<Results>();

    public static ThreadLocal<String> tlSrc = new ThreadLocal<String>();

    // The source being parsed ("reader" or filename)
    private final String src;

    // Java objects passed to the parser
    private final Object[] objects;


    static final TreeAdaptor adaptor = new CommonTreeAdaptor() {
        @Override
        public Object create(Token payload) {
            return new ProvaAST(payload);
        }

        @Override
        public void addChild(Object t1, Object t2) {
            ProvaAST ast1 = (ProvaAST) t1;
            ProvaAST ast2 = (ProvaAST) t2;
            if (ast1.getLine() == 0) {
                ast1.setLine(ast2.getLine());
                ast1.setColumn(ast2.getColumn());
            }
            super.addChild(t1, t2);
        }

        @Override
        public Object errorNode(TokenStream ts, Token t1, Token t2, RecognitionException rex) {
            ProvaErrorNode pen = new ProvaErrorNode(null);
            pen.setErrorNode((CommonErrorNode) super.errorNode(ts, t1, t2, rex));
            return pen;
        }
    };

    public ProvaParserImpl(String src, Object[] objects) {
        this.src = src;
        this.objects = objects;
    }

    @SuppressWarnings("unchecked")
    public List<Rule> parse(final KB kb, final Results resultSet, final BufferedReader in) throws ProvaParsingException, IOException, RecognitionException {
        List<Rule> rules = new ArrayList<Rule>();
        Prova2Lexer lex = new Prova2Lexer(new ANTLRReaderStream(in));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        Prova2Parser parser = new Prova2Parser(tokens);
        
        ProvaErrorReporter errorReporter = new SimpleErrorReporter();
        parser.setErrorReporter(errorReporter);
        parser.setTreeAdaptor(adaptor);

        /*try*/ {
            tlObjects.set(objects);
            tlRandom.set(new Random());
            tlKB.set(kb);
            tlRS.set(resultSet);
            tlSrc.set(src);

            Prova2Parser.rulebase_return r = parser.rulebase();
            if (!errorReporter.newException().errors().isEmpty()) {
                throw errorReporter.newException();
            }
            CommonTree tree = (CommonTree) r.getTree();
            if (log.isDebugEnabled()) {
                log.debug(tree.toStringTree());
            }
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(adaptor, tree);
            ProvaWalker2 walker = new ProvaWalker2(nodes);
            walker.setErrorReporter(errorReporter);
            List<List<?>> results = walker.rulebase();
            for (List result : results) {
                rules.add((Rule) result.get(2));
//				// Accumulate goals and clauses for subsequent commit
//				temp_kb.insert_item(result.toArray());
            }
        /*} catch (Exception e) {
            if (errorReporter.getErrors().isEmpty()) {
                // A walker error
                errorReporter.addError("0", "Prova walker reported a parsing error");
            }
            if (e instanceof RuntimeException && e.getLocalizedMessage() != null) {
                errorReporter.addError("0", e.getLocalizedMessage());
            }
            ProvaParsingException pex = errorReporter.newException();
            pex.setSource(src);
            pex.setDescription("Syntax errors occurred when parsing");
            throw pex;
        } finally {*/
            if (tlSrc != null) {
                tlSrc.remove();
            }
            if (tlObjects != null) {
                tlObjects.remove();
            }
            if (tlRandom != null) {
                tlRandom.remove();
            }
            if (tlKB != null) {
                tlKB.remove();
            }
            if (tlRS != null) {
                tlRS.remove();
            }
        }

        return rules;
    }

    public List<Rule> parse(KB kb,
            Results resultSet, String filename) throws IOException, ProvaParsingException, RecognitionException {
        File file = new File(filename);
        BufferedReader in;
        InputStream is = null;
        try {
            if (!file.exists() || !file.canRead()) {
                try {
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
                    in = new BufferedReader(
                            new InputStreamReader(is));
                } catch (Exception ex1) {
                    try {
                        is = ProvaParserImpl.class.getClassLoader().getResourceAsStream(filename);
                        in = new BufferedReader(
                                new InputStreamReader(is));
                    } catch (Exception ex2) {
                        try {
							// not sure this is ever needed; think the above is preferred.
                            // (this one looks relative to the package in classpath.)
                            is = ProvaParserImpl.class.getResourceAsStream(filename);
                            in = new BufferedReader(
                                    new InputStreamReader(is));
                        } catch (Exception ex3) {
                            try {
								// Added by Adrian :
                                // read KB / module from URL
                                URL url = new URL(filename);
                                in = new BufferedReader(new InputStreamReader(url.openStream()));
                            } catch (Exception ex4) {
                                throw new IOException("Cannot find " + filename);
                            }
                        }
                    }
                }
            } else {
                FileReader fr = new FileReader(file);
                in = new BufferedReader(fr);
            }
            kb.updateContext(filename);
            List<Rule> results = parse(kb, resultSet, in);
            return results;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
