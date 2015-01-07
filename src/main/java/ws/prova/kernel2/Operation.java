package ws.prova.kernel2;

import java.util.List;
import ws.prova.agent2.Reagent;

public interface Operation extends Predicate {

	public boolean process(Reagent prova, Derivation node, Goal goal, List<Literal> newLiterals, Rule query);
	
}
