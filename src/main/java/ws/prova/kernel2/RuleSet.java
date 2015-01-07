package ws.prova.kernel2;

import java.util.Iterator;
import java.util.List;

public interface RuleSet {

	public void add(Rule clause);

	public void addA(Rule clause);

	public Object size();

	public String getSymbol();

	public int getArity();

	public void addAll(RuleSet ruleSet);

        public int numClauses();
        
	public List<Rule> getClauses();

	public List<Rule> getClauses(Object key, PObj[] source);

	public void removeClauses(Object key);

	public boolean removeClausesByMatch(KB kb, PObj[] data);

	public boolean removeAllClausesByMatch(KB kb,
			PObj[] data);

	public void addRuleToSrc(Rule rule, String src);

	public void removeClausesBySrc(String src);

	public Unification nextMatch(KB kb, Goal goal);

	public void removeTemporalClause(long key);

    public Iterator<Rule> iterator();

}
