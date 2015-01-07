package ws.prova.kernel2;

import java.util.List;
import java.util.Map;
import ws.prova.kernel2.cache.ProvaCacheState;
import ws.prova.kernel2.cache.Answers;

public interface Literal extends PObj {

	public Predicate getPredicate();

	public PList getTerms();

	public Literal rebuild(final Unification unification);

	public Literal rebuildSource(final Unification unification);

	public void addClause(Rule clause);

	public void addClauseA(Rule clause);

	public void setGoal(Goal provaGoal);

	public ProvaCacheState getCacheState();

	public Answers getAnswers();

	public void markCompletion();

	public Goal getGoal();

	public String getSourceCode();

	public void setSourceCode(String string);

	public void setMetadata(String property, List<Object> value);

	public List<Object> getMetadata(String property);

	public List<PObj> addMetadata(Map<String, List<Object>> m);

	public Map<String, List<Object>> getMetadata();

	public void setLine(int line);

	public int getLine();

	public List<Literal> getGuard();

	public void setTerms(PList newList);

	public void setGround(boolean ground);

	public Literal cloneWithBoundVariables(Unification unification,
			List<Variable> variables, List<Boolean> isConstant);

}
