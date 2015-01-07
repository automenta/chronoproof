package ws.prova.reference2.builtins;

import java.util.Arrays;
import java.util.Collections;
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

public class ProvaReverseImpl extends ProvaBuiltinImpl {

	public ProvaReverseImpl(KB kb) {
		super(kb,"reverse");
	}

	/**
	 * Find the reverse of a rest-less list.
	 * If the output list is not a free variable, unify it against the reversed list in the input list.
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
			VariableIndex ltPtr = (VariableIndex) lt;
			lt = variables.get(ltPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof PList) )
			return false;
		PList list = (PList) lt;
		if( list.getTail() instanceof Variable )
			return false;
		
		PObj out = data[1];
		if( out instanceof VariableIndex ) {
			VariableIndex outPtr = (VariableIndex) out;
			out = variables.get(outPtr.getIndex()).getRecursivelyAssigned();
		}
		if( out instanceof Variable ) {
			List<PObj> jlist = Arrays.asList(list.getFixed());
			Collections.reverse(jlist);
			((Variable) out).setAssigned(ProvaListImpl.create(jlist));
			return true;
		}
		if( out instanceof PList ) {
			PList other = (PList) out;
			if( other.getTail()!=null )
				return false;
			List<PObj> jlist = Arrays.asList(list.getFixed());
			Collections.reverse(jlist);
			// Unify the reversed first argument list with the second argument list
			Predicate pred = new ProvaPredicateImpl("",1,kb);
			PList ls = ProvaListImpl.create(new PObj[] {ProvaListImpl.create(jlist)} );
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = Rule.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			PList outls = ProvaListImpl.create(new PObj[] {other} );
			Literal newLiteral = new ProvaLiteralImpl(pred,outls);
			newLiterals.add(newLiteral);
			return true;
		}
		return false;
	}

}
