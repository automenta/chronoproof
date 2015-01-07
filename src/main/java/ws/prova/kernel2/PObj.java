package ws.prova.kernel2;

import java.io.Serializable;
import java.util.List;

/**
 * TODO: Rename to ProvaTerm once all rewrite is complete
 * @author Alex Kozlenkov
 *
 */
public interface PObj extends Serializable {
	
	public PObj getRecursivelyAssigned();
	
	public int collectVariables(long ruleId, List<Variable> variables);

	public int computeSize();

//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset);

	public boolean unify(PObj target, Unification unification);

	public void substituteVariables(final VariableIndex[] varsMap);

	public boolean isGround();

	public String toString(final List<Variable> variables);

	public PObj cloneWithVariables(final List<Variable> variables);

	public PObj cloneWithVariables(final long ruleId,
			final List<Variable> variables);

	public Object computeIfExpression();

	public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant);

	public boolean updateGround(final List<Variable> variables);

}
