package test.ws.prova.test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;
import ws.prova.api2.ProvaCommunicatorImpl;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Inference;
import ws.prova.kernel2.Results;
import ws.prova.kernel2.Rule;
import ws.prova.parser2.ProvaParserImpl;
import ws.prova.parser2.ProvaParsingException;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.DefaultResults;

public class ProvaParserTest {

	static final String kAgent = "prova";

	static final String kPort = null;

	@Test
	public void simpleParse() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		StringReader sr = new StringReader(
				":-solve(a(X,Y)).\n"+
				"a(X,Y):-b(X),!,d(Y).\n"+
				"b(X):-c(X).\n"+
				"c(1).\n"+
				"c(2).\n"+
				"d(3).\n"+
				"d(4).");
		BufferedReader in = new BufferedReader(sr);
		ProvaParserImpl parser = new ProvaParserImpl("inline1", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, in);
			// Run each goal
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),2);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void simpleParseWithTail() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		StringReader sr = new StringReader(
				":-solve(a([X,Y|Z])).\n"+
				"a([X,Y]):-b(X),!,d(Y).\n"+
				"b(X):-c(X).\n"+
				"c(1).\n"+
				"c(2).\n"+
				"d(3).\n"+
				"d(4).");
		BufferedReader in = new BufferedReader(sr);
		ProvaParserImpl parser = new ProvaParserImpl("inline1", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, in);
			// Run each goal
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),2);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void simpleParseWithDerive() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		StringReader sr = new StringReader(
				":-solve(a(X,b(X))).\n"+
				"a(X,Y):-derive(Y).\n"+
				"b(X):-c(X).\n"+
				"c(1).\n"+
				"c(2).");
		BufferedReader in = new BufferedReader(sr);
		ProvaParserImpl parser = new ProvaParserImpl("inline1", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, in);
			// Run each goal
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),2);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void simpleParseWithNotAndFail() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		StringReader sr = new StringReader(
				":-solve(a([X,Y|Z])).\n"+
				"a([X,Y]):-b(X),!,c(Y),not(d(Y)).\n"+
				"not(X):-derive(X),!,fail().\n"+
				"not(X).\n"+
				"b(-1).\n"+
				"b(-2).\n"+
				"c(1).\n"+
				"c(2).\n"+
				"c(3).\n"+
				"d(1).\n"+
				"d(4).");
		BufferedReader in = new BufferedReader(sr);
		ProvaParserImpl parser = new ProvaParserImpl("inline1", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, in);
			// Run each goal
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),2);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  A non-existing class used in a Java call literal causes a ProvaParsingException
	 * @throws ProvaParsingException
	 */
	@Test(expected=ProvaParsingException.class)
	public void incorrect_java_class() throws ProvaParsingException {
		final String rulebase = "rules/reloaded/incorrect_java_class.prova";
		
		try {
			new ProvaCommunicatorImpl(kAgent,kPort,rulebase,ProvaCommunicatorImpl.SYNC);
	
		} catch( RuntimeException ex ) {
			// Note that the parsing exception is shipped out inside a RuntimeException
			if( ex.getCause() instanceof ProvaParsingException )
				throw (ProvaParsingException) ex.getCause();
		}
	}

}
