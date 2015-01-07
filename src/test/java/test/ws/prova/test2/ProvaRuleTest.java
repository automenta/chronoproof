package test.ws.prova.test2;

import org.junit.Test;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaListPtrImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaRuleTest {

	@Test
	public void countVariablesInRule() {
		KB kb = new DefaultKB();

		Variable v1 = ProvaVariableImpl.create("v1");
		Variable v2 = ProvaVariableImpl.create("v2");
		Variable v3 = ProvaVariableImpl.create("v3");
		Variable v4 = ProvaVariableImpl.create("v4");
		PList l1 = ProvaListImpl.create(new PObj[] {v2},v3);
		PList l2 = ProvaListImpl.create(new PObj[] {v1,l1,v4});
		
		Literal lit1 = kb.newLiteral("pred1",l2);
		Rule r1 = kb.newRule(lit1, new Literal[] {} );
		
		org.junit.Assert.assertEquals(r1.getVariables().size(),4);
	}

	@Test
	public void countDuplicateVariablesInRule() {
		KB kb = new DefaultKB();

		Variable v1 = ProvaVariableImpl.create("v1");
		Variable v2 = ProvaVariableImpl.create("v2");
		Variable v3 = ProvaVariableImpl.create("v3");
		Constant c1 = ProvaConstantImpl.create(12);
		Variable v4 = ProvaVariableImpl.create("v4");
		PList l1 = ProvaListImpl.create(new PObj[] {v2,c1},v3);
		PList l2 = ProvaListImpl.create(new PObj[] {v1,l1,v2,v4});
		
		Literal lit1 = kb.newLiteral("pred1",l2);
		Rule r1 = kb.newRule(lit1, new Literal[] {} );
		
		org.junit.Assert.assertEquals(r1.getVariables().size(),4);
	}

	@Test
	public void countVariablesInRuleWithListAssigns() {
		KB kb = new DefaultKB();

		Variable v1 = ProvaVariableImpl.create("v1");
		Variable v2 = ProvaVariableImpl.create("v2");
		Variable v3 = ProvaVariableImpl.create("v3");
		Variable v4 = ProvaVariableImpl.create("v4");
		PList l1 = ProvaListImpl.create(new PObj[] {v2},v3);
		PList l2 = ProvaListImpl.create(new PObj[] {v1,l1,v4});
		
		Literal lit1 = kb.newLiteral("pred1",l2);
		Rule r1 = kb.newRule(lit1, new Literal[] {} );
		
		PList l3 = ProvaListImpl.create(new PObj[] {v1,v2});
		v3.setAssigned(new ProvaListPtrImpl(l3,1));
		
		org.junit.Assert.assertEquals(r1.getVariables().size(),4);
	}

}
