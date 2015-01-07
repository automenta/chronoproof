package ws.prova.reference2;

import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Unification;

public class ProvaDerivationNodeImpl implements Derivation {

    private boolean failed = false;

    private Unification unification;

    private Goal currentGoal;

    private Rule query;

    private Derivation parent;

    private int id = 0;

    private boolean isCut = false;

    private int cutPredicate = -1;

    /**
     * Constructor.
     */
    public ProvaDerivationNodeImpl() {
        super();
    }

    @Override
    public void setCurrentGoal(Goal currentGoal) {
        this.currentGoal = currentGoal;
    }

    /**
     * Get the applied clause.
     *
     * @return the applied clause
     */
    @Override
    public Goal getCurrentGoal() {
        return currentGoal;
    }

    /**
     * Get the parent derivation node.
     *
     * @return the parent node
     */
    @Override
    public Derivation getParent() {
        return parent;
    }

    /**
     * Get the query.
     *
     * @return the query
     */
    @Override
    public Rule getQuery() {
        return query;
    }

    /**
     * Get the state of this node.
     *
     * @return the state, the value is one of the constants defined in org.mandarax.kernel.DerivationNodeImpl
     */
    public int getState() {
        return isFailed()
                ? Derivation.FAILED
                : Derivation.SUCCESS;
    }

    /**
     * Get the unification.
     *
     * @return the unification applied
     */
    public Unification getUnification() {
        return unification;
    }

    /**
     * Indicates whether this derivation step has failed.
     *
     * @return true if this proof step has failed, false otherwise
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Set the failed flag.
     *
     * @param f true if this node has failed, false otherwise
     */
    @Override
    public void setFailed(boolean f) {
//        cachedToString = null;
        failed = f;
    }

    /**
     * Set the query.
     *
     * @param f a query
     */
    @Override
    public void setQuery(Rule f) {
        query = f;
    }

    /**
     * Set the unification.
     *
     * @param u a unification
     */
    public void setUnification(Unification u) {
        unification = u;
    }

    /**
     * Convert the derivation node to a string.
     *
     * @return the string representation of this object
     */
    @Override
    public String toString() {
//        if (cachedToString == null) {
            StringBuffer buf = new StringBuffer();
            buf.append("NODE ");
            buf.append(id);
            buf.append(" : CLAUSE: \"");
            if (getCurrentGoal() == null) {
                buf.append("<NONE>");
            } else
                buf.append(getCurrentGoal().toString());
            buf.append("\" FAILED: ");
            buf.append(isFailed());
            buf.append(" IS_LAST_NODE: ");
//            buf.append(this.isLastNode);
//            cachedToString = buf.toString();
            return buf.toString();
//        }
//        return cachedToString;
    }

    @Override
    public boolean isCut() {
        return isCut;
    }

    @Override
    public void setCut(boolean cut) {
        isCut = cut;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getCutPredicate() {
        return cutPredicate;
    }

    @Override
    public void setCutPredicate(int cutPredicate) {
        this.cutPredicate = cutPredicate;
    }

	/**
	 * Set the parent node.
	 *
	 * @param n a node
	 */
    @Override
	public void setParent(Derivation n) {
		parent = n;
	}

}
