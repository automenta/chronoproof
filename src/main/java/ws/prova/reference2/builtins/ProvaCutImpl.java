package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.ProvaReagent;
import ws.prova.kernel2.ProvaDerivationNode;
import ws.prova.kernel2.ProvaGoal;
import ws.prova.kernel2.ProvaKnowledgeBase;
import ws.prova.kernel2.ProvaLiteral;
import ws.prova.kernel2.ProvaRule;

public class ProvaCutImpl extends ProvaBuiltinImpl {

	public ProvaCutImpl(ProvaKnowledgeBase kb, String symbol) {
		super(kb, symbol);
	}

	@Override
	public boolean process(ProvaReagent prova, ProvaDerivationNode node,
			ProvaGoal goal, List<ProvaLiteral> newLiterals, ProvaRule query) {
		return false;
	}

	@Override
	public String getSymbol() {
		return "cut";
	}

}
