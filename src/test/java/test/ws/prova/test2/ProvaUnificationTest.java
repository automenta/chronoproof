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
import ws.prova.reference2.ProvaUnificationImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaUnificationTest {

	/**
	 * Unify
	 *		goal: pred1(X,[2|A]|Z)
	 *		rule: pred1(V,Y,3,Y,U):-pred2(V,Y|U).
	 */
	@Test
	public void unifyTest1() {
		KB kb = new DefaultKB();
		
		Constant c2 = ProvaConstantImpl.create(2);
		Variable a = ProvaVariableImpl.create("A");
		PList l1 = ProvaListImpl.create(new PObj[] {c2},a);
		Variable x = ProvaVariableImpl.create("X");
		Variable z = ProvaVariableImpl.create("Z");
		PList l2 = ProvaListImpl.create(new PObj[] {x,l1},z);
		Literal query = kb.newLiteral("pred1",l2);
		Rule goal = kb.newGoal(new Literal[] {query});
		
		Variable v = ProvaVariableImpl.create("V");
		Variable y = ProvaVariableImpl.create("Y");
		Constant c4 = ProvaConstantImpl.create(3);
		Variable u = ProvaVariableImpl.create("U");
		PList l3 = ProvaListImpl.create(new PObj[] {v,y,c4,y,u});
		Literal lit1 = kb.newLiteral("pred1",l3);
		PList l4 = ProvaListImpl.create(new PObj[] {v,y}, u);
		Literal lit3 = kb.newLiteral("pred2",l4);
		Rule rule = kb.newRule(lit1, new Literal[] {lit3});
		
		ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
		boolean result = unification.unify();
		
		org.junit.Assert.assertTrue(result);
		
		int countSourceSubstitutions = 0;
		for( Variable var : unification.getSourceVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countSourceSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countSourceSubstitutions,1);

		int countTargetSubstitutions = 0;
		for( Variable var : unification.getTargetVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countTargetSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countTargetSubstitutions,2);

		// Recover actual substitutions resulting from the unification
		Literal[] newGoals = unification.rebuildNewGoals();

		org.junit.Assert.assertNotNull(newGoals);
		// There is one actual goal: pred2(X,[1|A]|U)
		org.junit.Assert.assertEquals(newGoals.length,1);
		// The goal literal has variable arity
		org.junit.Assert.assertEquals(newGoals[0].getTerms().computeSize(),-1);
		// The goal literal has a fixed part of length 2
		org.junit.Assert.assertEquals(newGoals[0].getTerms().getFixed().length,2);
	}

	/**
	 * Unify
	 *		goal: pred1(X,[2|A]|Z)
	 *		rule: pred1(V,Y,3,Y,U):-pred2(V|Y).
	 */
	@Test
	public void unifyTest2() {
		KB kb = new DefaultKB();
		
		Constant c2 = ProvaConstantImpl.create(2);
		Variable a = ProvaVariableImpl.create("A");
		PList l1 = ProvaListImpl.create(new PObj[] {c2},a);
		Variable x = ProvaVariableImpl.create("X");
		Variable z = ProvaVariableImpl.create("Z");
		PList l2 = ProvaListImpl.create(new PObj[] {x,l1},z);
		Literal query = kb.newLiteral("pred1",l2);
		Rule goal = kb.newGoal(new Literal[] {query});
		
		Variable v = ProvaVariableImpl.create("V");
		Variable y = ProvaVariableImpl.create("Y");
		Constant c4 = ProvaConstantImpl.create(3);
		Variable u = ProvaVariableImpl.create("U");
		PList l3 = ProvaListImpl.create(new PObj[] {v,y,c4,y,u});
		Literal lit1 = kb.newLiteral("pred1",l3);
		PList l4 = ProvaListImpl.create(new PObj[] {v}, y);
		Literal lit3 = kb.newLiteral("pred2",l4);
		Rule rule = kb.newRule(lit1, new Literal[] {lit3});
		
		ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
		boolean result = unification.unify();
		
		org.junit.Assert.assertTrue(result);
		
		int countSourceSubstitutions = 0;
		for( Variable var : unification.getSourceVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countSourceSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countSourceSubstitutions,1);

		int countTargetSubstitutions = 0;
		for( Variable var : unification.getTargetVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countTargetSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countTargetSubstitutions,2);

		// Recover actual substitutions resulting from the unification
		Literal[] newGoals = unification.rebuildNewGoals();

		org.junit.Assert.assertNotNull(newGoals);
		// There is one actual goal: pred2(X,2|A)
		org.junit.Assert.assertEquals(newGoals.length,1);
		// The goal literal has variable arity
		org.junit.Assert.assertEquals(newGoals[0].getTerms().computeSize(),-1);
		// The second argument "2" is added to the "fixed" part of the goal literal
		org.junit.Assert.assertEquals(newGoals[0].getTerms().getFixed().length,2);
	}

	/**
	 * Unify
	 *		goal: pred1(X,[2|A],3|Z)
	 *		rule: pred1(V,Y|U):-pred2(V,Y|U).
	 */
	@Test
	public void unifyListPointersOffsetTest() {
		KB kb = new DefaultKB();
		
		Constant c2 = ProvaConstantImpl.create(2);
		Variable a = ProvaVariableImpl.create("A");
		PList l1 = ProvaListImpl.create(new PObj[] {c2},a);
		Variable x = ProvaVariableImpl.create("X");
		Constant c3 = ProvaConstantImpl.create(3);
		Variable z = ProvaVariableImpl.create("Z");
		PList l2 = ProvaListImpl.create(new PObj[] {x,l1,c3},z);
		Literal query = kb.newLiteral("pred1",l2);
		Rule goal = kb.newGoal(new Literal[] {query});
		
		Variable v = ProvaVariableImpl.create("V");
		Variable y = ProvaVariableImpl.create("Y");
		Variable u = ProvaVariableImpl.create("U");
		PList l3 = ProvaListImpl.create(new PObj[] {v,y}, u);
		Literal lit1 = kb.newLiteral("pred1",l3);
		PList l4 = ProvaListImpl.create(new PObj[] {v,y}, u);
		Literal lit3 = kb.newLiteral("pred2",l4);
		Rule rule = kb.newRule(lit1, new Literal[] {lit3});
		
		ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
		boolean result = unification.unify();
		
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
		Literal[] newGoals = unification.rebuildNewGoals();

		org.junit.Assert.assertNotNull(newGoals);
		// There is one actual goal: pred2(X,[2|A],3|Z)
		org.junit.Assert.assertEquals(newGoals.length,1);
		// The goal literal has variable arity
		org.junit.Assert.assertEquals(newGoals[0].getTerms().computeSize(),-1);
		// The third argument "3" is added to the "fixed" part of the goal literal
		org.junit.Assert.assertEquals(newGoals[0].getTerms().getFixed().length,3);
	}

	/**
	 * Unify
	 *		goal: pred1(X,[1|Z]|Z)
	 *		rule: pred1(V,[1,2,3],2,3|U):-pred2(V|U).
	 */
	@Test
	public void unifyEmptyListTest() {
		KB kb = new DefaultKB();
		
		Constant c1 = ProvaConstantImpl.create(1);
		Variable z = ProvaVariableImpl.create("Z");
		PList l1 = ProvaListImpl.create(new PObj[] {c1},z);
		Variable x = ProvaVariableImpl.create("X");
		PList l2 = ProvaListImpl.create(new PObj[] {x,l1},z);
		Literal query = kb.newLiteral("pred1",l2);
		Rule goal = kb.newGoal(new Literal[] {query});
		
		Constant c2 = ProvaConstantImpl.create(2);
		Constant c3 = ProvaConstantImpl.create(3);
		PList l3 = ProvaListImpl.create(new PObj[] {c1,c2,c3} );
		Variable v = ProvaVariableImpl.create("V");
		Variable u = ProvaVariableImpl.create("U");
		PList l4 = ProvaListImpl.create(new PObj[] {v,l3,c2,c3}, u);
		Literal lit1 = kb.newLiteral("pred1",l4);
		PList l5 = ProvaListImpl.create(new PObj[] {v}, u);
		Literal lit2 = kb.newLiteral("pred2",l5);
		Rule rule = kb.newRule(lit1, new Literal[] {lit2});
		
		ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
		boolean result = unification.unify();
		
		org.junit.Assert.assertTrue(result);
		
		int countSourceSubstitutions = 0;
		for( Variable var : unification.getSourceVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countSourceSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countSourceSubstitutions,1);

		int countTargetSubstitutions = 0;
		for( Variable var : unification.getTargetVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countTargetSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countTargetSubstitutions,2);

		// Recover actual substitutions resulting from the unification
		Literal[] newGoals = unification.rebuildNewGoals();

		org.junit.Assert.assertNotNull(newGoals);
		// There is one actual goal: pred2(X)
		org.junit.Assert.assertEquals(newGoals.length,1);
		// The goal literal has fixed arity of 1
		org.junit.Assert.assertEquals(newGoals[0].getTerms().computeSize(),1);
		// The "fixed" part of the goal literal has length equal to 1
		org.junit.Assert.assertEquals(newGoals[0].getTerms().getFixed().length,1);
	}

	/**
	 * Unify
	 *		goal: pred1(X,[1|Z]|Z)
	 *		rule: pred1(V,[1,2,3,4],2,3|U):-pred2(V|U).
	 */
	@Test
	public void unifyNonEmptyListTest() {
		KB kb = new DefaultKB();
		
		Constant c1 = ProvaConstantImpl.create(1);
		Variable z = ProvaVariableImpl.create("Z");
		PList l1 = ProvaListImpl.create(new PObj[] {c1},z);
		Variable x = ProvaVariableImpl.create("X");
		PList l2 = ProvaListImpl.create(new PObj[] {x,l1},z);
		Literal query = kb.newLiteral("pred1",l2);
		Rule goal = kb.newGoal(new Literal[] {query});
		
		Constant c2 = ProvaConstantImpl.create(2);
		Constant c3 = ProvaConstantImpl.create(3);
		Constant c4 = ProvaConstantImpl.create(4);
		PList l3 = ProvaListImpl.create(new PObj[] {c1,c2,c3,c4} );
		Variable v = ProvaVariableImpl.create("V");
		Variable u = ProvaVariableImpl.create("U");
		PList l4 = ProvaListImpl.create(new PObj[] {v,l3,c2,c3}, u);
		Literal lit1 = kb.newLiteral("pred1",l4);
		PList l5 = ProvaListImpl.create(new PObj[] {v}, u);
		Literal lit2 = kb.newLiteral("pred2",l5);
		Rule rule = kb.newRule(lit1, new Literal[] {lit2});
		
		ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
		boolean result = unification.unify();
		
		org.junit.Assert.assertTrue(result);
		
		int countSourceSubstitutions = 0;
		for( Variable var : unification.getSourceVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countSourceSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countSourceSubstitutions,1);

		int countTargetSubstitutions = 0;
		for( Variable var : unification.getTargetVariables() ) {
			PObj to = var.getRecursivelyAssigned();
			if( to!=var ) {
				countTargetSubstitutions++;
			}
		}
		org.junit.Assert.assertEquals(countTargetSubstitutions,2);

		// Recover actual substitutions resulting from the unification
		Literal[] newGoals = unification.rebuildNewGoals();

		org.junit.Assert.assertNotNull(newGoals);
		// There is one actual goal: pred2(X,4)
		org.junit.Assert.assertEquals(newGoals.length,1);
		// The goal literal has fixed arity of 2
		org.junit.Assert.assertEquals(newGoals[0].getTerms().computeSize(),2);
		// The "fixed" part of the goal literal has length equal to 2
		org.junit.Assert.assertEquals(newGoals[0].getTerms().getFixed().length,2);
	}

}
