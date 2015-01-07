package ws.prova.kernel2;

public interface Variable extends PObj, Computable {

    public Object getName();

    public Class<?> getType();

    public void setAssigned(PObj assigned);

	//public String uniqueName();
    public int getIndex();

    public Variable clone();

    public PObj getAssigned();

    public void setRuleId(long ruleId);

    public long getRuleId();

    public void setIndex(int size);

    public Variable clone(long ruleId);

}
