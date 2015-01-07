package ws.prova.kernel2;

public interface VariableIndex extends PObj {

	public boolean unifyReverse(
			PObj target,
			Unification unification);

	public long getRuleId();

	public int getIndex();

}
