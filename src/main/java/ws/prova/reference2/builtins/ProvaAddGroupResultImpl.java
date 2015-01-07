package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Rule;

public class ProvaAddGroupResultImpl extends ProvaBuiltinImpl {

	public ProvaAddGroupResultImpl(KB kb) {
		super(kb, "@add_group_result");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		PList terms = (PList) literal.getTerms();
		/*synchronized(kb)*/ {
			try {
				prova.getMessenger().addGroupResult(terms);
			} catch (Exception e) {
				// @TODO: throw exception, this will only be possible when Prova exceptions are back
				//        in the new version
				return false;
			}
			return true;
		}
	}

}
