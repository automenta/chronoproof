package test.ws.prova.test2;

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Inference;
import ws.prova.kernel2.Results;
import ws.prova.kernel2.Rule;
import ws.prova.parser2.ProvaParserImpl;
import ws.prova.parser2.ProvaParsingException;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.DefaultResults;

public class ProvaFileParserTest {

	@Test
	public void consult() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/consult.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/consult.prova");
			// Run each goal.
			// Note that the first goal (eval) has no solutions which is always the case for eval
			//   but the second goal (solve) has one solution as the facts a/1 are defined
			//   in the rulebase consulted by the eval in the first goal.
			int[] numSolutions = new int[] {0,1};
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
			e.printStackTrace();
		}
	}

	@Test
	public void func_001() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/func_001.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/func_001.prova");
			// Run each goal
			int[] numSolutions = new int[] {0,1,1,1,1,1,1,1,1,1,1,1,1};
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
			e.printStackTrace();
		}
	}

	@Test
	public void yale4() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/yale4.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/yale4.prova");
			// Run each goal
			int[] numSolutions = new int[] {7,3,1,0,4,2,1,0,1};
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
			e.printStackTrace();
		}
	}

	@Test
	public void typedVariables() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/test002.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/test002.prova");
			// Run each goal
			int[] numSolutions = new int[] {3,1,1,0,1};
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
			e.printStackTrace();
		}
	}

	@Test
	public void queens() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/queens001.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/queens001.prova");
			// Run each goal
			int[] numSolutions = new int[] {724};
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
			e.printStackTrace();
		}
	}

	@Test
	public void list_tail() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/list_tail.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/list_tail.prova");
			// Run each goal
			int[] numSolutions = new int[] {1};
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
			e.printStackTrace();
		}
	}

//	@Test
	/*
	 * This test will require tabling support as it otherwise it results in infinite loop
	 */
	public void binary_link() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/binary_link.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/binary_link.prova");
			// Run each goal
			int[] numSolutions = new int[] {1};
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
			e.printStackTrace();
		}
	}

//	@Test
	public void queens_sir() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/queens.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/queens.prova");
			// Run each goal
			int[] numSolutions = new int[] {724};
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
			e.printStackTrace();
		}
	}

}
