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
import ws.prova.reference2.ProvaConstantImpl;

public class ProvaConcatImpl extends ProvaBuiltinImpl {

	public ProvaConcatImpl(KB kb) {
		super(kb,"concat");
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
		PObj res = data[1];
		if( res instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) res;
			res = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(res instanceof Variable) && !(res instanceof Constant) )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof PList) )
			return false;
		PObj[] args = ((PList) ((PList) lt).cloneWithVariables(variables)).getFixed();
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<args.length; i++ ) {
			sb.append(args[i].toString());
		}

		if( res instanceof Variable )
			((Variable) res).setAssigned(ProvaConstantImpl.create(sb.toString()));
		else if( res instanceof Constant ) {
			return ((Constant) res).getObject().toString().equals(sb.toString());
		}
		return true;
	}

}
