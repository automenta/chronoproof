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
import ws.prova.reference2.builtins.target.ProvaTarget;

public class ProvaUpdateImpl extends ProvaBuiltinImpl {

	public ProvaUpdateImpl(KB kb) {
		super(kb,"update");
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
		ProvaTarget ptr = null;
		Object rt = data[1];
		if( rt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) rt;
			PObj o = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			if( o instanceof Constant )
				rt = o;
		}
		if( !(rt instanceof Constant) )
			return false;
		ptr = (ProvaTarget) ((Constant) rt).getObject();
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			PObj o = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			if( !(o instanceof Variable) )
				lt = o;
		}
		if( !(lt instanceof PList) )
			return false;
		PList newList = (PList) ((PList) lt).cloneWithVariables(variables, 1);
		ptr.getCandidate().getHead().setTerms(newList);
		return true;
	}

}
