package ws.prova.reference2.builtins.target;

import ws.prova.kernel2.Goal;
import ws.prova.kernel2.Rule;

public interface ProvaTarget {

	public Goal getTarget();

	public Rule getCandidate();

	public void setCandidate(Rule candidate);

}
