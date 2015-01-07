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

public class ProvaGlobalsTest {

	@Test
	public void globals001() {
		KB kb = new DefaultKB();
		kb.setGlobalConstant("$A","a");
		Results resultSet = new DefaultResults();
		ProvaParserImpl parser = new ProvaParserImpl("rules/reloaded/globals001.prova", new Object[] {});
		try {
			List<Rule> rules = parser.parse(kb, resultSet, "rules/reloaded/globals001.prova");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
