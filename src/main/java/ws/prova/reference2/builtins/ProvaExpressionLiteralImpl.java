package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.operators.ProvaBinaryOperator;

public class ProvaExpressionLiteralImpl extends ProvaBuiltinImpl {

	public ProvaExpressionLiteralImpl(KB kb) {
		super(kb,"expr_literal");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms(); //.cloneWithVariables(variables);
		terms.updateGround(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=3 )
			return false;
		// Main binary operator
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof Constant) )
			return false;
		Object olt1 = ((Constant) lt).getObject();
		if( !(olt1 instanceof ProvaBinaryOperator) )
			return false;
		ProvaBinaryOperator bo = (ProvaBinaryOperator) olt1;
		// LHS
		PObj a1 = data[1];
		if( a1 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a1;
			a1 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !((a1 instanceof Variable) || (a1 instanceof Constant) || (a1 instanceof PList)) )
			return false;
		// Expression
		PObj a2 = data[2];
		if( a2 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a2;
			a2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		return bo.evaluate(kb, newLiterals, a1, (Computable) a2);
	}

}
