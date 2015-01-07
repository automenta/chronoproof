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

public class ProvaGreaterEqualImpl extends ProvaBuiltinImpl {

	public ProvaGreaterEqualImpl(KB kb) {
		super(kb,"ge");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length<2 
				|| !(data[0] instanceof Constant) 
				|| !(((Constant) data[0]).getObject() instanceof Number) )
			return false;
		double left = ((Number) ((Constant) data[0]).getObject()).doubleValue();
		for( int i=1; i<data.length; i++ ) {
			if( !(((Constant) data[i]).getObject() instanceof Number) )
				return false;
			double right = ((Number) ((Constant) data[i]).getObject()).doubleValue();
			if( left<right )
				return false;
		}
		return true;
	}

}
