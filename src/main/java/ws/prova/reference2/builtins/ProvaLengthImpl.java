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
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaLengthImpl extends ProvaBuiltinImpl {

	public ProvaLengthImpl(KB kb) {
		super(kb,"length");
	}

	/**
	 * Find the length of a rest-less list.
	 * If the supplied list is a free variable but the length is given, generate a list of this length.
	 */
	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=2 )
			return false;
		PObj lt = data[0];
		if( lt instanceof Variable ) {
			PObj out = data[1];
			if( !(out instanceof Constant) )
				return false;
			Object olen = ((Constant) out).getObject();
			if( !(olen instanceof Integer) )
				return false;
			int len = (Integer) olen;
			// Generate a list given its length
			PObj[] fixed = new PObj[len];
			for( int i=0; i<len; i++ ) {
				fixed[i] = ProvaVariableImpl.create();
			}
			PList newList = ProvaListImpl.create(fixed, null);
			((Variable) lt).setAssigned(newList);
			return true;
		}
		if( !(lt instanceof PList) )
			return false;
		PList list = (PList) lt;
		if( list.getTail() instanceof Variable )
			return false;
		
		PObj out = data[1];
		if( out instanceof Constant ) {
			Object o = ((Constant) out).getObject();
			if( !(o instanceof Integer) )
				return false;
			return list.getFixed().length == (Integer) o;
		}
		if( out instanceof Variable ) {
			((Variable) out).setAssigned(ProvaConstantImpl.create(list.getFixed().length));
			return true;
		}
		return false;
	}

}
