package ws.prova.reference2.builtins.target;

import ws.prova.kernel2.Goal;
import ws.prova.kernel2.Rule;

public class ProvaTargetImpl implements ProvaTarget {

	private final Goal target;
	
	private Rule candidate;

	private ProvaTargetImpl(Goal target) {
		this.target = target;
	}

	public static ProvaTarget create(Goal target) {
		return new ProvaTargetImpl(target);
	}

	@Override
	public Goal getTarget() {
		return target;
	}

	@Override
	public Rule getCandidate() {
		return candidate;
	}

	@Override
	public void setCandidate( Rule candidate ) {
		this.candidate = candidate;
	}

}
