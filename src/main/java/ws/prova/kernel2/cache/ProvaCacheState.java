package ws.prova.kernel2.cache;

import java.util.Collection;
import java.util.List;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.cache.ProvaCacheStateImpl.ProvaCacheAnswerKey;

public interface ProvaCacheState {

	public Collection<PList> getSolutions();

	public void setOpen(boolean open);

	public boolean isOpen();

	public boolean isComplete();

	public Goal getGoal();

	public ProvaCacheAnswerKey getCacheAnswerKey(PList literalList,
			List<Variable> variables);

	boolean addSolution(ProvaCacheAnswerKey key, PList literalList);

	public List<Goal> getGoals();

	public void addGoal(Goal goal);

	public void markCompletion();

}
