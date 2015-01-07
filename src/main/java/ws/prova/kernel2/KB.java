package ws.prova.kernel2;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import ws.prova.agent2.Reagent;
import ws.prova.exchange.ProvaSolution;
import ws.prova.kernel2.cache.ProvaCacheState;

public interface KB {

	public Predicate newPredicate(String symbol, int arity);

	public RuleSet getPredicates(String symbol);

	public RuleSet getPredicates(String symbol, int arity);

	public ConcurrentMap<String, Predicate> getPredicates();

	public Literal newLiteral(String symbol, PList terms);

	public Rule newRule(Literal head, Literal[] body);

	public Rule newGoal(Literal[] body);

	public Literal newLiteral(String symbol);

	public Rule newRule(Literal head, Literal[] newGoals,
			Literal[] body, int offset);

	public Rule newRuleA(Literal lit, Literal[] provaLiterals);

	public Rule newGoalSolution(Results resultSet, Literal[] body);

	public Literal newCachedLiteral(String symbol, PList terms,
			ProvaCacheState cacheState,
			ws.prova.kernel2.cache.Answers answers);

	public void setPrinter(PrintWriter printWriter);

	public PrintWriter getPrinter();

	public Constant getGlobal(String name);

	public Constant newGlobalConstant(String name);

	public void setGlobalConstant(String name, Object value);

	public void setGlobals(Map<String, Object> globals);

	public List<ProvaSolution[]> consultSyncInternal(Reagent prova, String src, String key,
			Object[] objects);

	public List<ProvaSolution[]> consultSyncInternal(Reagent prova,
			BufferedReader in, String key, Object[] objects);

	public Literal newHeadLiteral(String symbol, PList terms);

	public Literal newLiteral(String symbol, PObj[] data, int offset);

	public Literal newLiteral(PObj[] data);

	public void addCachePredicate(String symbol);

	public boolean isCachePredicate(String symbol);

	public Literal newLiteral(String symbol,
			PList terms, List<Literal> guard);

	public Predicate getOrGeneratePredicate(String symbol, int arity);

	public Predicate getPredicate(String symbol, int arity);

	public void addClauseSetToSrc(RuleSet ruleSet, String src);

	public void unconsultSync(String src);

	public Rule newGoal(Literal[] body,
			List<Variable> variables);

	public Rule newRule(long ruleId, Literal head,
			Literal[] body);

	public Rule newGoal(Unification unification, Derivation node, Literal[] newGoals,
			Literal[] body, int offset, List<Variable> variables);

	public void updateContext(String filename);

//	public ProvaRule generateLocalRule(ProvaReagent prova, long partitionKey,
//			ProvaLiteral head, ProvaLiteral[] array);

}
