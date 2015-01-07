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
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.kernel2.Rule;

public class ProvaCloneImpl extends ProvaBuiltinImpl {

	public ProvaCloneImpl(KB kb) {
		super(kb,"clone");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms0 = literal.getTerms();
		PObj[] data = terms0.getFixed();
		if( data.length!=2 )
			return false;
		PObj rt = data[1];
		data[1] = ProvaListImpl.emptyRList;
		PList terms = (PList) terms0.cloneWithVariables(variables);
		Predicate pred = new ProvaPredicateImpl("",1,kb);
		PList ls = ProvaListImpl.create(new PObj[] {terms.getFixed()[0]} );
		Literal lit = new ProvaLiteralImpl(pred,ls);
		Rule clause = Rule.createVirtualRule(1, lit, null);
		pred.addClause(clause);
		PList ltls = ProvaListImpl.create(new PObj[] {rt} );
		Literal newLiteral = new ProvaLiteralImpl(pred,ltls);
		newLiterals.add(newLiteral);
		return true;
	}

}
