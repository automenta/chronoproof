package test.ws.prova.test2;

import org.junit.Test;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaGoalImpl;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaGoalTest {

	/**
	 * Unify
	 *		goal:
	 *			pred1(X,1,Z).
	 *		rules:
	 *			pred1(1,1,2).
	 *			pred1(X,Y,Z):-pred2(X,Y,1),pred2(X,Z,X).
	 *
	 *			pred2(1,3,X).
	 *			pred2(1,X,1).
	 */
	@SuppressWarnings("unused")
	@Test
	public void simpleItrationTest1() {
		KB kb = new DefaultKB();
		
		Constant c1 = ProvaConstantImpl.create(1);
		Variable x = ProvaVariableImpl.create("X");
		Variable z = ProvaVariableImpl.create("Z");
		PList l1 = ProvaListImpl.create(new PObj[] {x,c1,z});
		Literal query = kb.newLiteral("pred1",l1);
		Rule goalRule = kb.newGoal(new Literal[] {query});

		Constant c2 = ProvaConstantImpl.create(2);
		Constant c3 = ProvaConstantImpl.create(3);
		PList l2 = ProvaListImpl.create(new PObj[] {c1,c1,c2});
		Literal lit2 = kb.newLiteral("pred1",l2);
		Rule rule1 = kb.newRule(lit2, null);

		Variable x1 = ProvaVariableImpl.create("X");
		Variable y1 = ProvaVariableImpl.create("Y");
		Variable z1 = ProvaVariableImpl.create("Z");
		PList l3 = ProvaListImpl.create(new PObj[] {x1,y1,z1});
		Literal lit3 = kb.newLiteral("pred1",l3);
		PList l4 = ProvaListImpl.create(new PObj[] {x1,y1,c1});
		Literal lit4 = kb.newLiteral("pred2",l4);
		PList l4a = ProvaListImpl.create(new PObj[] {x1,z1,x1});
		Literal lit4a = kb.newLiteral("pred2",l4a);
		Rule rule2 = kb.newRule(lit3, new Literal[] {lit4,lit4a});

		Variable x2 = ProvaVariableImpl.create("X");
		PList l5 = ProvaListImpl.create(new PObj[] {c1,c3,x2});
		Literal lit5 = kb.newLiteral("pred2",l5);
		Rule rule3 = kb.newRule(lit5, null);
		
		Variable x3 = ProvaVariableImpl.create("X");
		PList l6 = ProvaListImpl.create(new PObj[] {c1,x3,c1});
		Literal lit6 = kb.newLiteral("pred2",l6);
		Rule rule4 = kb.newRule(lit6, null);
		
		Goal goal = new ProvaGoalImpl(goalRule);
		
		Unification unification = goal.nextUnification(kb);
		boolean result = unification.unify();
		org.junit.Assert.assertTrue(result);

		// Recover actual substitutions resulting from the unification
		Literal[] newGoals1 = unification.rebuildNewGoals();
		
		unification = goal.nextUnification(kb);
		result = unification.unify();
		org.junit.Assert.assertTrue(result);
		
		int countSourceSubstitutions = 0;
		for( Variable var : unification.getSourceVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countSourceSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countSourceSubstitutions,0);

		int countTargetSubstitutions = 0;
		for( Variable var : unification.getTargetVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countTargetSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countTargetSubstitutions,3);

		// Recover actual substitutions resulting from the unification
		Literal[] newGoals2 = unification.rebuildNewGoals();

		org.junit.Assert.assertNotNull(newGoals2);
		// There is one actual goal: pred2(X,Y,1)
		org.junit.Assert.assertEquals(newGoals2.length,2);
		// The goal literal has arity of 3
		org.junit.Assert.assertEquals(newGoals2[0].getTerms().computeSize(),3);
		// The goal literal has a fixed part of length 2
		org.junit.Assert.assertEquals(newGoals2[0].getTerms().getFixed().length,3);
	}

}
