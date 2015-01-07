package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.kernel2.Rule;

public class ProvaMkListImpl extends ProvaBuiltinImpl {

	public ProvaMkListImpl(KB kb) {
		super(kb,"mklist");
	}

	/**
	 * Append a tail to a list that has no tail
	 */
	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=2 )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof PList) )
			return false;
		PObj a1 = data[1];
		if( a1 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a1;
			a1 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		PList args = (PList) lt;
		PObj[] fixed = args.getFixed();
		if( fixed.length!=2 )
			return false;
		if( !(fixed[0] instanceof PList) )
			return false;
		PList prefix = (PList) fixed[0];
		PObj prefixTail = prefix.getTail();
		// Only allow adding a tail if there is no tail in the input list
		if( prefixTail!=null )
			return false;
		PList newTerms = ProvaListImpl.create(prefix.getFixed(), fixed[1]);
		if( a1 instanceof Variable ) {
			((Variable) a1).setAssigned(newTerms);
			return true;
		}
		Predicate pred = new ProvaPredicateImpl("",1,kb);
		PList ls = ProvaListImpl.create(new PObj[] {a1} );
		Literal lit = new ProvaLiteralImpl(pred,ls);
		Rule clause = Rule.createVirtualRule(1, lit, null);
		pred.addClause(clause);
		PList ltls = ProvaListImpl.create(new PObj[] {newTerms} );
		Literal newLiteral = new ProvaLiteralImpl(pred,ltls);
		newLiterals.add(newLiteral);
		return true;
	}

}
