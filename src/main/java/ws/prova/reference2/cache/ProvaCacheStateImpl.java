package ws.prova.reference2.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.kernel2.cache.ProvaCacheState;
import ws.prova.kernel2.cache.ProvaGroundKey;
import ws.prova.reference2.ProvaListImpl;

public class ProvaCacheStateImpl implements ProvaCacheState {

	// No need for this one, really
	private boolean open;
	
	private boolean complete;

	private final Map<ProvaCacheAnswerKey,PList> answers;
	
	private final List<Goal> goals;
	
	public ProvaCacheStateImpl() {
		this.open = false;
		this.complete = false;
		this.answers = new HashMap<ProvaCacheAnswerKey,PList>();
		this.goals = new ArrayList<Goal>();
	}

	@Override
	public List<Goal> getGoals() {
		return goals;
	}
	
	@Override
	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public void addGoal( Goal goal ) {
		goals.add(goal);
	}
	
	@Override
	public boolean isOpen() {
		return open;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	/**
	 * Only add new answers
	 */
	public boolean addSolution(ProvaCacheAnswerKey key, PList literalList) {
		PList oldAnswer = answers.get(key);
		if( oldAnswer!=null )
			return false;
		answers.put(key, literalList);
		return true;
	}
	
	@Override
	public ProvaCacheAnswerKey getCacheAnswerKey(PList literalList, List<Variable> variables) {
		if( literalList==ProvaListImpl.emptyRList )
			return new ProvaCacheAnswerKey(0,null);
		PObj[] fixed = literalList.getFixed();
		final int arity = fixed.length;
		int numBound = 0;
		// Where are the ground terms
		int mask = 0;
		for( int i=0; i<arity; i++ ) {
			PObj o = fixed[i];
			mask <<= 1;
			if( o instanceof VariableIndex ) {
				VariableIndex ptr = (VariableIndex) o;
				Variable var = variables.get(ptr.getIndex());
				o = var.getAssigned();
			}
			if( o instanceof Constant ) {
				numBound++;
				mask |= 1;
			} else if( o instanceof Variable ) {
			} else {
				// TODO: return false?
			}
		}
		final Object[] data = new Object[numBound];
		for( int i=0, j=0; i<arity; i++ ) {
			PObj o = fixed[i];
			if( o instanceof Constant )
				data[j++] = ((Constant) o).getObject();
		}
		ProvaCacheAnswerKey key = new ProvaCacheAnswerKey(mask,data);
		return key;
	}
	
	@Override
	public Collection<PList> getSolutions() {
		return answers.values();
	}

	@Override
	public Goal getGoal() {
		if( goals.isEmpty() )
			return null;
		return this.goals.get(goals.size()-1);
	}
	
	public class ProvaCacheAnswerKey {
		
		private final int mask;
		
		private final ProvaGroundKey groundKey;

		public ProvaCacheAnswerKey(int mask, Object[] data) {
			this.mask = mask;
			this.groundKey = new ProvaGroundKeyImpl(data);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((groundKey == null) ? 0 : groundKey.hashCode());
			result = prime * result + mask;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProvaCacheAnswerKey other = (ProvaCacheAnswerKey) obj;
			if (groundKey == null) {
				if (other.groundKey != null)
					return false;
			} else if (!groundKey.equals(other.groundKey))
				return false;
			return mask == other.mask;
		}

	}

	@Override
	/**
	 * Purge the top goal and mark the subgoal completion if the highest level goal is complete
	 */
	public void markCompletion() {
		goals.remove(goals.size()-1);
		if( goals.isEmpty() ) {
			// It is now open and complete
			complete = true;
		}
	}

}
