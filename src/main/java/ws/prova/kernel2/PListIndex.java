package ws.prova.kernel2;

public interface PListIndex extends PObj {

	public PObj rebuild(Unification unification);

	public PList getAssigned();

	public PList getAssignedWithOffset();

	public int getIndex();

	public PObj rebuildSource(Unification unification);

}
