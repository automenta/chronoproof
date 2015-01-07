package test.ws.prova.test2;

import org.junit.Test;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Inference;
import ws.prova.kernel2.Results;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.DefaultResults;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaResolutionEngineTest {

	@Test
	@SuppressWarnings("unused")
	public void solveProblem1() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();

		// Define goal (this should be part of the way solve is encoded by the parser)
		Constant c1 = ProvaConstantImpl.create(1);
		Variable x = ProvaVariableImpl.create("X");
		Variable z = ProvaVariableImpl.create("Z");
		PList l1 = ProvaListImpl.create(new PObj[] {x,c1,z});
		Literal query = kb.newLiteral("pred1",l1);
		// "solve" works by accepting pairs of variable (name,value) pairs
		Constant cResultSet = ProvaConstantImpl.create(resultSet);
		Constant cx = ProvaConstantImpl.create("X");
		Constant cz = ProvaConstantImpl.create("Z");
		PList lx = ProvaListImpl.create(new PObj[] {cx,x});
		PList lz = ProvaListImpl.create(new PObj[] {cz,z});
		PList ls = ProvaListImpl.create(new PObj[] {cResultSet,lx,lz});
		// "solve" always "fails" forcing backtracking
		Literal solveBuiltin = kb.newLiteral("solve",ls);
		Rule goalRule = kb.newGoal(new Literal[] {query,solveBuiltin});

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

		PList l2a = ProvaListImpl.create(new PObj[] {c2,c1,c1});
		Literal lit2a = kb.newLiteral("pred1",l2a);
		Rule rule1a = kb.newRule(lit2a, null);

		Variable x2 = ProvaVariableImpl.create("X");
		PList l5 = ProvaListImpl.create(new PObj[] {c1,c3,x2});
		Literal lit5 = kb.newLiteral("pred2",l5);
		Rule rule3 = kb.newRule(lit5, null);
		
		Variable x3 = ProvaVariableImpl.create("X");
		PList l6 = ProvaListImpl.create(new PObj[] {c1,x3,c1});
		Literal lit6 = kb.newLiteral("pred2",l6);
		Rule rule4 = kb.newRule(lit6, null);
		
		Inference engine = new DefaultInference(kb, goalRule);
		Derivation result = engine.run();
		
		org.junit.Assert.assertEquals(resultSet.getSolutions().size(),4);
	}

	@Test
	@SuppressWarnings("unused")
	public void solveProblemSamePredicateSymbolsDifferentArities() {
		KB kb = new DefaultKB();
		Results resultSet = new DefaultResults();
		
		// Define goal (this should be part of the way solve is encoded by the parser)
		Constant c1 = ProvaConstantImpl.create(1);
		Constant c4 = ProvaConstantImpl.create(4);
		Variable p = ProvaVariableImpl.create("P");
		PList l1 = ProvaListImpl.create(new PObj[] {c1,p});
		Literal query = kb.newLiteral("queens",l1);
		// "solve" works by accepting pairs of variable (name,value) pairs
		Constant cp = ProvaConstantImpl.create("P");
		Constant cResultSet = ProvaConstantImpl.create(resultSet);
		PList lp = ProvaListImpl.create(new PObj[] {cp,p});
		PList ls = ProvaListImpl.create(new PObj[] {cResultSet,lp});
		Literal solveBuiltin = kb.newLiteral("solve",ls);
		Rule goalRule = kb.newGoal(new Literal[] {query,solveBuiltin});

		Variable n1 = ProvaVariableImpl.create("N");
		Variable qs1 = ProvaVariableImpl.create("Qs");
		PList l3 = ProvaListImpl.create(new PObj[] {n1,qs1});
		Literal lit3 = kb.newLiteral("queens",l3);
		Variable ns1 = ProvaVariableImpl.create("Ns");
		PList l4 = ProvaListImpl.create(new PObj[] {c1,n1,ns1});
		Literal lit4 = kb.newLiteral("range",l4);
		PList lEmpty = ProvaListImpl.create(new PObj[] {});
		PList l4a = ProvaListImpl.create(new PObj[] {ns1,lEmpty,qs1});
		Literal lit4a = kb.newLiteral("queens",l4a);
		Rule rule2 = kb.newRule(lit3, new Literal[] {lit4,lit4a});

		Variable n2 = ProvaVariableImpl.create("N");
		PList l2a = ProvaListImpl.create(new PObj[] {n2});
		PList l2 = ProvaListImpl.create(new PObj[] {n2,n2,l2a});
		Literal lit2 = kb.newLiteral("range",l2);
		Rule rule1 = kb.newRule(lit2, null);

		Variable q5 = ProvaVariableImpl.create("Q");
		PList l5 = ProvaListImpl.create(new PObj[] {q5,lEmpty,q5});
		Literal lit5 = kb.newLiteral("queens",l5);
		Rule rule5 = kb.newRule(lit5, null);

		Inference engine = new DefaultInference(kb, goalRule);
		Derivation result = engine.run();
		
		org.junit.Assert.assertEquals(resultSet.getSolutions().size(),1);
	}

}
