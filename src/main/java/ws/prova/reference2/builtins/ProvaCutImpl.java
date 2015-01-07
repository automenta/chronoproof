package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Rule;

public class ProvaCutImpl extends ProvaBuiltinImpl {

	public ProvaCutImpl(KB kb, String symbol) {
		super(kb, symbol);
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		return false;
	}

	@Override
	public String getSymbol() {
		return "cut";
	}

}
