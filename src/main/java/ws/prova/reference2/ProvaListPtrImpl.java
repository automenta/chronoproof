package ws.prova.reference2;

import java.util.Arrays;
import java.util.List;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PListIndex;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaListPtrImpl implements PListIndex {

	private static final long serialVersionUID = 4407774077342578415L;

	private PList assigned;
	
	private int offset;

	public ProvaListPtrImpl(PList assigned, int offset) {
		this.assigned = assigned;
		this.offset = offset;
	}

	public void setAssigned(PList assigned) {
		this.assigned = assigned;
	}

	@Override
	public PList getAssigned() {
		return assigned;
	}

	@Override
	public PList getAssignedWithOffset() {
		if( offset==0 )
			return assigned;
		// Make sure the tail is copied across as well
		if( offset<assigned.getFixed().length ) {
			PObj[] newFixed = Arrays.copyOfRange(assigned.getFixed(),offset,assigned.getFixed().length);
			return ProvaListImpl.create(newFixed, assigned.getTail());
		} else if( offset==assigned.getFixed().length )
			return ProvaListImpl.create(null, assigned.getTail());
		return ProvaListImpl.emptyRList;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getIndex() {
		return offset;
	}
	
	@Override
	public PObj getRecursivelyAssigned() {
		return this;
	}

	@Override
	public int collectVariables(final long ruleId, final List<Variable> variables) {
		return assigned.collectVariables(ruleId, variables);
	}

//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset) {
//		assigned.collectVariables(ruleId, variables, offset);
//	}

	@Override
	public int computeSize() {
		return assigned.computeSize(offset);
	}

	@Override
	public boolean unify(final PObj target, final Unification unification) {
		return assigned.unify(offset, target, unification);
	}

	@Override
	public PObj rebuild(final Unification unification) {
		return assigned.rebuild(unification, offset);
	}

	@Override
	public PObj rebuildSource(final Unification unification) {
		return assigned.rebuildSource(unification, offset);
	}

        @Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for( int i=offset; i<assigned.getFixed().length; i++ ) {
			if( i!=offset )
				sb.append(',');
			sb.append(assigned.getFixed()[i]);
		}
		if( assigned.getTail()!=null ) {
			sb.append('|');
			sb.append(assigned.getTail());
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public void substituteVariables(final VariableIndex[] varsMap) {
		assigned.substituteVariables(varsMap);
	}

	@Override
	public boolean isGround() {
		return assigned.isGround();
	}

	@Override
	public String toString(final List<Variable> variables) {
		return toString();
	}

	@Override
	public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant) {
		final PObj[] fixed = assigned.getFixed();
		final PObj[] newFixed = new PObj[fixed.length-offset];
		System.arraycopy(fixed, offset, newFixed, 0, newFixed.length);
		return ProvaListImpl.create(newFixed).cloneWithBoundVariables(variables, isConstant);
	}

	@Override
	public PObj cloneWithVariables(final List<Variable> variables) {
		if( assigned!=null && assigned!=this )
			return assigned.cloneWithVariables(variables);
		throw new UnsupportedOperationException();
	}

	@Override
	public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object computeIfExpression() {
		return this;
	}

	@Override
	public boolean updateGround(final List<Variable> variables) {
		return assigned.updateGround(variables);
	}

}
