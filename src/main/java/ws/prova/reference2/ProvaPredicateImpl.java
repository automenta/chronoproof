package ws.prova.reference2;

import ws.prova.kernel2.KB;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.RuleSet;

public class ProvaPredicateImpl implements Predicate {

	private String symbol;
	
	private final int arity;

	private final RuleSet clauseSet;
	
	private KB knowledgeBase;
	
	public ProvaPredicateImpl(String symbol, int arity, KB kb) {
		this.symbol = symbol;
		this.arity = arity;
		this.clauseSet = new ProvaRuleSetImpl(symbol, arity);
		this.knowledgeBase = kb;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public void addClause( Rule clause ) {
		clauseSet.add(clause);
	}
	
	@Override
	public void addClauseA( Rule clause ) {
		clauseSet.addA(clause);
	}
	
	@Override
	public RuleSet getClauses() {
		return clauseSet;
	}

	@Override
	public boolean equals( Predicate predicate ) {
		return predicate.getSymbol().equals(symbol) &&
			(predicate.getArity()==arity || predicate.getArity()==-1 || arity==-1 );
	}

        @Override
	public void setKB(KB knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	@Override
	public KB kb() {
		return knowledgeBase;
	}

}
