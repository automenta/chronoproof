package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Rule;

public class ProvaReceiveMsgImpl extends ProvaBuiltinImpl {

	public ProvaReceiveMsgImpl(KB kb) {
		super(kb,"rcvMsg");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		return prova.getMessenger().rcvMsg(goal, newLiterals, query, false);
	}

}
