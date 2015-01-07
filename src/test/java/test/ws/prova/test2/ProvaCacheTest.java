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

public class ProvaCacheTest {

//	@Test
	// Limitation: Cut does not work correctly with cache
	public void cache_loop10() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop10.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop10.prova");
			// Run each goal
			int[] numSolutions = new int[] {0};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	@Test
	// To be fixed in version 3.1
	public void cache_loop8() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop8.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop8.prova");
			// Run each goal
			int[] numSolutions = new int[] {4};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop7() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop7.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop7.prova");
			// Run each goal
			int[] numSolutions = new int[] {8};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop6() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop6.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop6.prova");
			// Run each goal
			int[] numSolutions = new int[] {2};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop5() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop5.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop5.prova");
			// Run each goal
			int[] numSolutions = new int[] {4};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop.prova");
			// Run each goal
			int[] numSolutions = new int[] {2};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop2() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop2.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop2.prova");
			// Run each goal
			int[] numSolutions = new int[] {3};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop3() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop3.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop3.prova");
			// Run each goal
			int[] numSolutions = new int[] {4};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void cache_loop4() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/loop4.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/loop4.prova");
			// Run each goal
			int[] numSolutions = new int[] {8};
			int i = 0;
			for( Rule rule : rules ) {
				if( rule.getHead()==null ) {
					Inference engine = new DefaultInference(kb, rule);
					engine.run();

					org.junit.Assert.assertEquals(resultSet.getSolutions().size(),numSolutions[i++]);
					resultSet.getSolutions().clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
