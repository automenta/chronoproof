package ws.prova.kernel2;

public interface ProvaConstant<O> extends ProvaObject {

	public O getObject();

	public void setObject(O object);

	public boolean matched(ProvaConstant target);

}
