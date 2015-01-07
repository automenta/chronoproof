package ws.prova.kernel2;

import java.util.List;

public interface Unification {

	public boolean unify();

	public Rule getSource();

	public Rule getTarget();

	public long getSourceRuleId();

	public long getTargetRuleId();

	public List<Variable> getSourceVariables();

	public List<Variable> getTargetVariables();

	public Literal[] rebuildNewGoals();

	public Variable getVariableFromVariablePtr(VariableIndex variablePtr);

	public PObj rebuild(VariableIndex variablePtr);

	public Literal[] rebuildOldGoals(Literal[] body);

	public PObj rebuildSource(VariableIndex variablePtr);

	public Rule generateQuery(String symbol, KB kb, Rule query,
			Derivation node);

	public boolean targetUnchanged();

}
