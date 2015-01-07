package ws.prova.kernel2;

import java.util.List;

public interface PList extends PObj {

	public int computeSize(int offset);

	public PList rebuild(Unification unification);

	public PObj[] getFixed();

	public PObj getTail();

	public PObj rebuild(Unification unification, int offset);

	public boolean unify(int offset, PObj target,
			Unification unification);

	public PList rebuildSource(Unification unification);

	public PObj rebuildSource(Unification unification, int offset);

        @Override
	public boolean isGround();

	public String performative();

	public PList shallowCopy();

	public PObj cloneWithVariables(List<Variable> variables, int offset);

	public PList copyWithVariables(List<Variable> variables);

        @Override
	public PObj cloneWithBoundVariables(List<Variable> variables, List<Boolean> changed);

	public PList copyWithBoundVariables(List<Variable> variables, List<Boolean> changed);

	public void setGround(boolean ground);

}
