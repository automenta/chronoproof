package test.ws.prova.test2;

import org.junit.Test;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaKnowledgeBaseTest {

	@Test
	public void countRulesForPredicate() {
		KB kb = new DefaultKB();
		
		Variable v1 = ProvaVariableImpl.create("v1");
		Variable v2 = ProvaVariableImpl.create("v2");
		Variable v3 = ProvaVariableImpl.create("v3");
		Variable v4 = ProvaVariableImpl.create("v4");
		PList l1 = ProvaListImpl.create(new PObj[] {v2},v3);
		PList l2 = ProvaListImpl.create(new PObj[] {v1,l1,v4});
		
		Literal lit1 = kb.newLiteral("pred1", l2);
		
		// This automatically adds the rule to the respective predicate in the knowledge base
		kb.newRule(lit1, new Literal[] {});
		
		org.junit.Assert.assertEquals(kb.getPredicates("pred1",3).size(),1);
		org.junit.Assert.assertEquals(kb.getPredicates("pred1",2).size(),0);
	}

}
