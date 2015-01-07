package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Operation;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.RuleSet;
import ws.prova.reference2.ProvaRuleSetImpl;

public abstract class ProvaBuiltinImpl implements Operation {

	protected KB kb;
	
	private final ProvaRuleSetImpl clauseSet;

	private final String symbol;
	
	public ProvaBuiltinImpl(KB kb, String symbol) {
		this.kb = kb;
		this.symbol = symbol;
		this.clauseSet = new ProvaRuleSetImpl(symbol);
	}
	
	@Override
	abstract public boolean process(Reagent prova, Derivation node, Goal goal, List<Literal> newLiterals, Rule query);


	@Override
	public void addClause(Rule clause) {
	}

	@Override
	public void addClauseA(Rule clause) {
	}

	@Override
	public boolean equals(Predicate predicate) {
		return predicate==this;
	}

	@Override
	public int getArity() {
		throw new UnsupportedOperationException("Method is not used for built-ins");
	}

	@Override
	public RuleSet getClauses() {
		return clauseSet;
	}

	@Override
	public KB kb() {
		return kb;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public void setKB(KB kb) {
		this.kb = kb;
	}

}
