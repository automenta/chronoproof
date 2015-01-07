package ws.prova.reference2.messaging;

import ws.prova.kernel2.PList;
import ws.prova.kernel2.Predicate;

public class RemoveList {

	// Predicate for reaction rule
	private final Predicate p1;
	
	private final Predicate p2;
	
	private final long ruleid;
	
	private PList reaction;
	
	private boolean not;

	private boolean optional;

	public RemoveList(Predicate p1, Predicate p2, long ruleid, PList reaction) {
		this.p1 = p1;
		this.p2 = p2;
		this.ruleid = ruleid;
		this.reaction = reaction;
		this.not = false;
	}

	public Predicate getP1() {
		return p1;
	}

	public Predicate getP2() {
		return p2;
	}

	public long getRuleid() {
		return ruleid;
	}

	public PList getReaction() {
		return reaction;
	}

	public void setReaction( PList reaction ) {
		this.reaction = reaction;
	}

	public void setNot(boolean not) {
		this.not = not;
	}
	
	public boolean isNot() {
		return this.not;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isOptional() {
		return this.optional;
	}

}

