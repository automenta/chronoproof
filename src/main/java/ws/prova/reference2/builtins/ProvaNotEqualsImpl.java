package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
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

public class ProvaNotEqualsImpl extends ProvaBuiltinImpl {

	public ProvaNotEqualsImpl(KB kb) {
		super(kb,"ne");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=2 )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( lt instanceof Variable ) {
			return false;
		}
		PObj rhs = data[1];
		if( rhs instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) rhs;
			rhs = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( rhs instanceof Variable ) {
			return false;
		}
		if( lt instanceof Constant ) {
			Constant lhsConstant = (Constant) lt;
			if( rhs instanceof Constant ) {
				return !lhsConstant.getObject().equals(((Constant) rhs).getObject());
			}
			return true;
		}
		// TODO Deal with the case when LHS is a list
		return true;
	}

}
