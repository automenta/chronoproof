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

public class ProvaAssertAImpl extends ProvaBuiltinImpl {

	public ProvaAssertAImpl(KB kb) {
		super(kb, "asserta");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=1 || !(data[0] instanceof PList) )
			return false;
		String symbol = ((Constant) ((PList) data[0]).getFixed()[0]).getObject().toString();
		Literal lit = kb.newLiteral(symbol, ((PList) data[0]).getFixed(), 1);
		// This automatically adds the rule to the respective predicate in the knowledge base
		kb.newRuleA(lit, new Literal[] {});
		return true;
	}

}
