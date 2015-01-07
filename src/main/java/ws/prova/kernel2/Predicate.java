package ws.prova.kernel2;

public interface Predicate {

	public String getSymbol();

	public int getArity();

	public void addClause(Rule clause);

	public void addClauseA(Rule clause);

	public boolean equals(Predicate predicate);

	public void setKB(KB provaKnowledgeBaseImpl);

	RuleSet getClauses();

	public KB kb();

}
