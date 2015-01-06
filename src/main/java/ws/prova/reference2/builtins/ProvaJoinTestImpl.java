package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.ProvaReagent;
import ws.prova.kernel2.ProvaDerivationNode;
import ws.prova.kernel2.ProvaGoal;
import ws.prova.kernel2.ProvaKnowledgeBase;
import ws.prova.kernel2.ProvaLiteral;
import ws.prova.kernel2.ProvaRule;

public class ProvaJoinTestImpl extends ProvaBuiltinImpl {

	public ProvaJoinTestImpl(ProvaKnowledgeBase kb) {
		super(kb,"join_test");
	}

	@Override
	public boolean process(ProvaReagent prova, ProvaDerivationNode node,
			ProvaGoal goal, List<ProvaLiteral> newLiterals, ProvaRule query) {
		ProvaLiteral literal = goal.getGoal();
		return prova.getWorkflows().join_test(literal, newLiterals, query);
	}

}
