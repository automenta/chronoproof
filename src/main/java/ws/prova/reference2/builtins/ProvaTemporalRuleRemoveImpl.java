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
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;

public class ProvaTemporalRuleRemoveImpl extends ProvaBuiltinImpl {

	public ProvaTemporalRuleRemoveImpl(KB kb) {
		super(kb, "@temporal_rule_remove");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		/*synchronized(kb)*/ {
			PList terms = (PList) literal.getTerms();
			PObj[] data = terms.getFixed();
			if( data.length!=4 )
				return false;
			Predicate predicate = (Predicate) ((Constant) data[0]).getObject();
			Predicate predicate2 = (Predicate) ((Constant) data[1]).getObject();
			long key = (Long) ((Constant) data[2]).getObject();
			List<Variable> variables = query.getVariables();
			PList reaction = (PList) ((PList) data[3]).cloneWithVariables(variables);
			try {
				boolean status = prova.getMessenger().removeTemporalRule(predicate,predicate2,key,true,reaction,literal.getMetadata());
				if( !status )
					return false;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return true;
		}
	}

}
