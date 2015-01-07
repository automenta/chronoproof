package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Rule;

public class ProvaStopPredicateJoinImpl extends ProvaBuiltinImpl {

	public ProvaStopPredicateJoinImpl(KB kb) {
		super(kb,"stop_predicate_join");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		return prova.getWorkflows().stop_predicate_join(literal, newLiterals, query);
	}

}
