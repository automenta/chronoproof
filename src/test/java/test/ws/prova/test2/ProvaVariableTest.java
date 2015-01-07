package test.ws.prova.test2;

import java.util.Vector;
import org.junit.Test;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaVariableTest {

	@Test
	public void tempVariableIsObjectDefault() {
		Variable v1 = ProvaVariableImpl.create();
		org.junit.Assert.assertTrue(v1.getName() instanceof Long);
	}

	@Test
	public void assignVariableToConstant() {
		Variable v1 = ProvaVariableImpl.create("v1");
		Constant c1 = ProvaConstantImpl.create(12);
		v1.setAssigned(c1);
		Variable v2 = ProvaVariableImpl.create("v2");
		Vector<Variable> variables = new Vector<Variable>();
		v1.collectVariables(0, variables);
		org.junit.Assert.assertEquals(variables.size(),0);
		v2.collectVariables(0, variables);
		org.junit.Assert.assertEquals(variables.size(),1);
	}

}
