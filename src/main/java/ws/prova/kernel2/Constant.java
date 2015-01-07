package ws.prova.kernel2;

public interface Constant<O> extends PObj {

	public O getObject();

	public void setObject(O object);

	public boolean matched(Constant target);

}
