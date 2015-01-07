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

public class ProvaUnlistenImpl extends ProvaBuiltinImpl {

	public ProvaUnlistenImpl(KB kb) {
		super(kb,"unlisten");
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
		PObj target = data[1];
		if( target instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) target;
			target = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(target instanceof Constant) )
			return false;
		PObj type = data[0];
		if( type instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) type;
			type = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(type instanceof Constant) )
			return false;
		String strType = (String) ((Constant) type).getObject();
		Object objTarget = ((Constant) target).getObject();
		prova.getSwingAdaptor().unlisten(strType,objTarget);
		return true;
	}

}
