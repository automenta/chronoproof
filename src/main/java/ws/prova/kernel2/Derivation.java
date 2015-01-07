package ws.prova.kernel2;

public interface Derivation {

    public final static byte UNKNOWN    = 0;
    public final static byte FAILED     = 1;
    public final static byte SUCCESS    = 2;
    public final static byte IRRELEVANT = 3;

    public void setFailed(boolean b);

	public void setQuery(Rule goalRule);

	public void setId(int next);

	public void setCut(boolean b);

	public Rule getQuery();

	public boolean isCut();

	public void setCutPredicate(int intValue);

	public void setCurrentGoal(Goal goal);

	public int getCutPredicate();

	public int getId();

	public void setParent(Derivation n);

	public Derivation getParent();

	public Goal getCurrentGoal();

}
