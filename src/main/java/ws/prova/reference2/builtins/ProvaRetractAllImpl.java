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
import ws.prova.kernel2.RuleSet;
import ws.prova.kernel2.Variable;

public class ProvaRetractAllImpl extends ProvaBuiltinImpl {

	public ProvaRetractAllImpl(KB kb) {
		super(kb, "retractall");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		// Clone variables since unification is used in this method
		List<Variable> variables = query.cloneVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=1 || !(data[0] instanceof PList) )
			return false;
		data = ((PList) data[0]).getFixed();
		String symbol = ((Constant) data[0]).getObject().toString();
		RuleSet clauses = kb.getPredicates(symbol,data.length-1);
		// TODO: Verify that all cases are covered
		boolean retracted = clauses.removeAllClausesByMatch(kb,data);
		return retracted;
	}

}
