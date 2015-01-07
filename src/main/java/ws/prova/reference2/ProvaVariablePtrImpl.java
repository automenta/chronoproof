package ws.prova.reference2;

import java.util.List;
import ws.prova.kernel2.PListIndex;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaVariablePtrImpl implements VariableIndex {

	private static final long serialVersionUID = 9041171371747132755L;

	private final long ruleId;
	
	private final int index;
	
	public ProvaVariablePtrImpl(final long ruleId, final int index) {
		this.ruleId = ruleId;
		this.index = index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + (int) (ruleId ^ (ruleId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProvaVariablePtrImpl other = (ProvaVariablePtrImpl) obj;
		if (index != other.index)
			return false;
		return ruleId == other.ruleId;
	}


	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int collectVariables(final long ruleId, final List<Variable> variables) {
		return 0;
	}

//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset) {
//	}

	/**
	 * This method assumes that the variable pointers have been rebuilt and point to unassigned variables
	 */
	@Override
	public int computeSize() {
		return -1;
	}

	@Override
	public PObj getRecursivelyAssigned() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getRuleId() {
		return ruleId;
	}

	@Override
	public boolean unify(final PObj target, final Unification unification) {
		PObj sourceObject = unification.getVariableFromVariablePtr(this).getRecursivelyAssigned();
//		ProvaObject sourceObject = unification.getSourceVariables().get(index).getRecursivelyAssigned();
		PObj targetObject;
		if( target instanceof VariableIndex ) {
			ProvaVariablePtrImpl targetPtr = (ProvaVariablePtrImpl) target;
			targetObject = unification.getVariableFromVariablePtr(targetPtr).getRecursivelyAssigned();
//			targetObject = unification.getTargetVariables().get(targetPtr.index).getRecursivelyAssigned();
		} else if( target instanceof PListIndex ) {
			// Since Prova 3.1.9: variable pointer assigned to a list pointer case handled
			targetObject = ((PListIndex) target).getAssignedWithOffset();
		} else
			targetObject = target;
		return sourceObject.unify(targetObject, unification);
	}

	@Override
	public boolean unifyReverse(final PObj target, final Unification unification) {
		PObj sourceObject = unification.getTargetVariables().get(index).getRecursivelyAssigned();
		PObj targetObject;
		if( target instanceof VariableIndex ) {
			ProvaVariablePtrImpl targetPtr = (ProvaVariablePtrImpl) target;
			targetObject = unification.getTargetVariables().get(targetPtr.index).getRecursivelyAssigned();
		} else
			targetObject = target;
		return sourceObject.unify(targetObject, unification);
	}

        @Override
	public String toString() {
		StringBuilder sb = new StringBuilder("@");
		sb.append(ruleId);
		sb.append(':');
		sb.append(index);
		return sb.toString(); 
	}

	@Override
	public void substituteVariables(VariableIndex[] varsMap) {
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public String toString(final List<Variable> variables) {
		return variables.get(index).toString();
	}

	@Override
	public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant) {
		PObj assigned = variables.get(index).getRecursivelyAssigned();
		if( assigned instanceof Variable ) {
			isConstant.set(0, false);
			return new ProvaVariablePtrImpl(0,this.index);
		}
		return assigned.cloneWithBoundVariables(variables, isConstant);
	}

	@Override
	public PObj cloneWithVariables(final List<Variable> variables) {
		return variables.get(index).getRecursivelyAssigned().cloneWithVariables(variables);
	}

	@Override
	public PObj cloneWithVariables(final long ruleId,
			final List<Variable> variables) {
		if( ruleId==this.ruleId )
			return variables.get(index).getRecursivelyAssigned().cloneWithVariables(variables);
		return this;
	}

	@Override
	public Object computeIfExpression() {
		return this;
	}

	@Override
	public boolean updateGround(final List<Variable> variables) {
		return variables.get(index).getRecursivelyAssigned().updateGround(variables);
	}

}
