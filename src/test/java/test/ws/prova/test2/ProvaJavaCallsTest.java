package test.ws.prova.test2;

import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import ws.prova.api2.ProvaCommunicator;
import ws.prova.api2.Communicator;
import ws.prova.exchange.ProvaSolution;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Inference;
import ws.prova.kernel2.Results;
import ws.prova.kernel2.Rule;
import ws.prova.parser2.ProvaParserImpl;
import ws.prova.parser2.ProvaParsingException;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.DefaultResults;

public class ProvaJavaCallsTest {

	static final String kAgent = "prova";

	static final String kPort = null;

	private ProvaCommunicator prova;
	
	@After
	public void shutdown() {
		if( prova!=null ) {
			prova.shutdown();
			prova = null;
		}
	}
	
	@Test
	public void retract_with_java_types() {
		final String rulebase = "rules/reloaded/retract_with_java_types.prova";
		
		prova = new Communicator(kAgent,kPort,rulebase);
		final int numSolutions[] = {3,2,3};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(numSolutions.length,solutions.size());
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",numSolutions[i],solutions.get(i).length);
	}

	@Test
	public void instanceJavaFunctions() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/typed_constants.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/typed_constants.prova");
			// Run each goal
			int[] numSolutions = new int[] {1,0,0};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(numSolutions[i++],resultSet.getSolutions().size());
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Test
	public void staticJavaFunctions() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/static_method_call.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/static_method_call.prova");
			// Run each goal
			int[] numSolutions = new int[] {1,1,0,0};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(numSolutions[i++],resultSet.getSolutions().size());
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void element_matching() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/element_matching.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/element_matching.prova");
			// Run each goal
			int[] numSolutions = new int[] {3,3,1,3};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(numSolutions[i++],resultSet.getSolutions().size());
					if( i==0 )
						org.junit.Assert.assertEquals("2",resultSet.getSolutions().get(0).getNv("X").toString());
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
