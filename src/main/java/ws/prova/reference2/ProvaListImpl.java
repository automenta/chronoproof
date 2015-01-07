package ws.prova.reference2;

import java.util.Arrays;
import java.util.List;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PListIndex;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.operators.ProvaOperator;

public class ProvaListImpl extends ProvaTermImpl implements PList, Computable {

    private static final long serialVersionUID = -555571145845834681L;

    public static final ProvaListImpl emptyRList = new ProvaListImpl(new PObj[]{});

    public final PObj[] fixed;

    private PObj tail;

    private boolean ground = false;

    public static PList create(final PObj[] fixed) {
        if (fixed.length == 0) {
            return emptyRList;
        }
        return new ProvaListImpl(fixed);
    }

    public static PList create(final List<PObj> list) {
        if (list.isEmpty()) {
            return emptyRList;
        }
        return new ProvaListImpl(list.toArray(new PObj[]{}));
    }

    private ProvaListImpl(final PObj[] fixed) {
        this.fixed = fixed;
    }

    public static PList create(final PObj[] fixed, final PObj tail) {
        if (fixed.length == 0) {
            return emptyRList;
        }
        return new ProvaListImpl(fixed, tail);
    }

    private ProvaListImpl(final PObj[] fixed, final PObj tail) {
        this.fixed = fixed;
        this.tail = tail;
    }

    @Override
    public boolean isGround() {
        return ground;
    }

//	public void setFixed(ProvaObject[] fixed) {
//		this.fixed = fixed;
//	}
    @Override
    public PObj[] getFixed() {
        return fixed;
    }

    public void setTail(PObj tail) {
        this.tail = tail;
    }

    @Override
    public PObj getTail() {
        return tail;
    }

    @Override
    public int computeSize() {
        int size = fixed.length;
        if (tail != null) {
            int tailSize = tail.computeSize();
            if (tailSize == -1) {
                return -1;
            }
            size += tailSize;
        }
        return size;
    }

    @Override
    public PObj getRecursivelyAssigned() {
        return this;
    }

    @Override
    public void substituteVariables(final VariableIndex[] varsMap) {
        if (ground) {
            return;
        }
        for (int i = 0; i < fixed.length; i++) {
            if (fixed[i].getClass() == ProvaConstantImpl.class) {
                continue;
            }
            if (fixed[i] instanceof VariableIndex) {
                fixed[i] = varsMap[((VariableIndex) fixed[i]).getIndex()];
            } else if (fixed[i] instanceof PList || fixed[i] instanceof PListIndex) {
                fixed[i].substituteVariables(varsMap);
            } else if (fixed[i] instanceof Literal) {
                fixed[i].substituteVariables(varsMap);
            } else if (fixed[i] instanceof ProvaMapImpl) {
                fixed[i].substituteVariables(varsMap);
            }
        }
        if (tail != null) {
            if (tail instanceof VariableIndex) {
                tail = varsMap[((VariableIndex) tail).getIndex()];
            } else if (tail instanceof PList || tail instanceof PListIndex) {
                tail.substituteVariables(varsMap);
            }
        }
    }

    @Override
    public PList shallowCopy() {
        final int fixedLength = fixed.length;
        PObj[] newFixed = new PObj[fixedLength];
        newFixed = Arrays.copyOf(fixed, fixedLength);
        return new ProvaListImpl(newFixed, tail);
    }

    @Override
    public PList copyWithVariables(final List<Variable> variables) {
        final int fixedLength = fixed.length;
        PObj[] newFixed = new PObj[fixedLength];
        for (int i = 0; i < fixedLength; i++) {
            newFixed[i] = fixed[i].cloneWithVariables(variables);
        }
        PObj newTail = null;
        if (tail != null) {
            newTail = tail.cloneWithVariables(variables);
        }
        return new ProvaListImpl(newFixed, newTail);
    }

    @Override
    public PList copyWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant) {
        int fixedLength = fixed.length;
        PObj newTail = null;
        if (tail != null) {
            newTail = tail.cloneWithBoundVariables(variables, isConstant);
            if (newTail instanceof PList) {
                fixedLength += newTail.computeSize();
                PObj[] newFixed = new PObj[fixedLength];
                int i = 0;
                for (; i < fixed.length; i++) {
                    newFixed[i] = fixed[i].cloneWithBoundVariables(variables, isConstant);
                }
                PObj[] tailFixed = ((PList) newTail).getFixed();
                for (; i < fixedLength; i++) {
                    newFixed[i] = tailFixed[i - fixed.length];
                }
                return new ProvaListImpl(newFixed, ((PList) newTail).getTail());
            }
        }
        PObj[] newFixed = new PObj[fixedLength];
        int i = 0;
        for (; i < fixed.length; i++) {
            newFixed[i] = fixed[i].cloneWithBoundVariables(variables, isConstant);
        }
        return new ProvaListImpl(newFixed, newTail);
    }

    @Override
    public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> changed) {
        if (ground) {
            return this;
        }
        return copyWithBoundVariables(variables, changed);
    }

    @Override
    public PObj cloneWithVariables(final List<Variable> variables) {
        if (ground) {
            return this;
        }
        return copyWithVariables(variables);
    }

    @Override
    public PObj cloneWithVariables(final List<Variable> variables, final int offset) {
        if (ground) {
            return this;
        }
        final int fixedLength = fixed.length - offset;
        PObj[] newFixed = new PObj[fixedLength];
        if (ground) {
            System.arraycopy(fixed, offset, newFixed, 0, fixedLength);
        } else {
            for (int i = 0; i < fixedLength; i++) {
                newFixed[i] = fixed[i + offset].cloneWithVariables(variables);
            }
        }
        PObj newTail = null;
        if (tail != null) {
            newTail = tail.cloneWithVariables(variables);
        }
        return new ProvaListImpl(newFixed, newTail);
    }

    @Override
    public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
        if (ground) {
            return this;
        }
        final int fixedLength = fixed.length;
        PObj[] newFixed = new PObj[fixedLength];
        for (int i = 0; i < fixedLength; i++) {
            if (fixed[i] == null) {
                throw new RuntimeException("a");
            }
            newFixed[i] = fixed[i].cloneWithVariables(ruleId, variables);
        }
        PObj newTail = null;
        if (tail != null) {
            newTail = tail.cloneWithVariables(ruleId, variables);
        }
        return new ProvaListImpl(newFixed, newTail);
    }

    @Override
    public boolean updateGround(List<Variable> variables) {
        if (ground) {
            return true;
        }
        ground = true;
        for (int i = 0; i < fixed.length; i++) {
            PObj fi = fixed[i];
            if (fi instanceof VariableIndex) {
                PObj o = variables.get(((VariableIndex)fi).getIndex()).getAssigned();
                if (o != null) {
                    fixed[i] = fi = o;
                }
            }
            if (!fi.updateGround(variables)) {
                ground = false;
            }
        }
        if (tail != null) {
            if (!tail.updateGround(variables)) {
                return ground = false;
            }
        }
        return ground;
    }

    @Override
    public int collectVariables(long ruleId, List<Variable> variables) {
        if (ground) {
            return -1;
        }
        ground = true;
        for (int i = 0; i < fixed.length; i++) {
            if (!fixed[i].isGround() && fixed[i].collectVariables(ruleId, variables) >= 0) {
                ground = false;
            }
            if (fixed[i] instanceof Variable) {
                fixed[i] = new ProvaVariablePtrImpl(ruleId, ((Variable) fixed[i]).getIndex());
            }
        }
        if (tail != null) {
            if (!tail.isGround() && tail.collectVariables(ruleId, variables) >= 0) {
                ground = false;
            }
            if (tail instanceof Variable) {
                tail = new ProvaVariablePtrImpl(ruleId, ((Variable) tail).getIndex());
            }
        }
        return ground ? -1 : 0;
    }

//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset ) {
//		if( offset<fixed.length ) {
//			for( int i=offset; i<fixed.length; i++ ) {
//				fixed[i].collectVariables(ruleId, variables);
//				if( fixed[i] instanceof ProvaVariable )
//					fixed[i] = new ProvaVariablePtrImpl(ruleId, ((ProvaVariable) fixed[i]).getIndex());
//			}
//			if( tail!=null ) {
//				tail.collectVariables(ruleId, variables);
//				if( tail instanceof ProvaVariable )
//					tail = new ProvaVariablePtrImpl(ruleId, ((ProvaVariable) tail).getIndex());
//			}
//		} else {
//			if( tail!=null ) {
//				tail.collectVariables(ruleId, variables, offset-fixed.length);
//				if( tail instanceof ProvaVariable )
//					tail = new ProvaVariablePtrImpl(ruleId, ((ProvaVariable) tail).getIndex());
//			}
//		}
//	}
    @Override
    public int computeSize(int offset) {
        return computeSize() - offset;
    }

    @Override
    public boolean unify(PObj target, Unification unification) {
        if (target == null) {
            return false;
        }
        ProvaListImpl targetList = null;
        if (target instanceof VariableIndex) {
            VariableIndex targetVariablePtr = (VariableIndex) target;
            Variable targetVariable = unification.getVariableFromVariablePtr(targetVariablePtr);
            PObj assigned = targetVariable.getRecursivelyAssigned();
            if (assigned instanceof Variable) {
                return ((Variable) assigned).unify(this, unification);
            } else if (assigned instanceof Constant) {
                return false;
            } else if (assigned instanceof PListIndex) {
                return unify(((PListIndex) assigned).getAssignedWithOffset(), unification);
            }
            targetList = (ProvaListImpl) assigned;
        } else if (target instanceof Variable) {
            return ((Variable) target).unify(this, unification);
        } else if (target instanceof Constant) {
            return target instanceof Any;
        } else if (target instanceof PListIndex) {
            return unify(((PListIndex) target).getAssignedWithOffset(), unification);
        } else {
            targetList = (ProvaListImpl) target;
        }
        int minFixed = Math.min(fixed.length, targetList.fixed.length);
        for (int i = 0; i < minFixed; i++) {
            boolean result = fixed[i].unify(targetList.fixed[i], unification);
            if (!result) {
                return false;
            }
        }
        if (fixed.length == targetList.fixed.length) {
            // TODO: Make sure empty tail is handled properly
            if (tail == null && targetList.tail == null) {
                return true;
            }
            if (tail != null && targetList.tail != null) {
                return tail.unify(targetList.tail, unification);
            }
            if (tail != null) {
                return tail.unify(emptyRList, unification);
            }
            return targetList.tail.unify(emptyRList, unification);
        }
        if (fixed.length < targetList.fixed.length) {
            if (tail instanceof PList) {
                return ((PList) tail).unify(0, new ProvaListPtrImpl(targetList, minFixed), unification);
            }
            if (tail == null || !(tail instanceof VariableIndex)) {
                return false;
            }
            return tail.unify(new ProvaListPtrImpl(targetList, minFixed), unification);
        }
        if (targetList.tail == null) {
            return false;
        }
        return targetList.tail.unify(new ProvaListPtrImpl(this, minFixed), unification);
    }

    /**
     * A general case when both unified lists may have offsets
     */
    @Override
    public boolean unify(
            int offset,
            PObj target,
            Unification unification) {
        if (target instanceof VariableIndex) {
            VariableIndex targetVariablePtr = (VariableIndex) target;
            Variable targetVariable = unification.getVariableFromVariablePtr(targetVariablePtr);
            PObj assigned = targetVariable.getRecursivelyAssigned();
            if (assigned instanceof Variable) {
                return ((Variable) assigned).unify(this, unification);
            } else if (assigned instanceof Constant) {
                return false;
            }
        }
        int targetOffset = 0;
        if (target instanceof PListIndex) {
            PListIndex targetListPtr = (PListIndex) target;
            target = targetListPtr.getAssigned();
            targetOffset = targetListPtr.getIndex();
        }
        ProvaListImpl targetList = (ProvaListImpl) target;
        int minFixed = Math.min(fixed.length - offset, targetList.fixed.length - targetOffset);
        for (int i = 0; i < minFixed; i++) {
            boolean result = fixed[i + offset].unify(targetList.fixed[i + targetOffset], unification);
            if (!result) {
                return false;
            }
        }
        if (fixed.length - offset == targetList.fixed.length - targetOffset) {
            if (tail != null) {
                return tail.unify(targetList.tail, unification);
            } else if (targetList.tail != null) {
                return targetList.tail.unify(null, unification);
            } else // Make sure empty tail is handled properly
            {
                return true;
            }
        }
        if (fixed.length - offset < targetList.fixed.length - targetOffset) {
            if (!(tail instanceof VariableIndex)) {
                return false;
            }
            return tail.unify(new ProvaListPtrImpl(targetList, targetOffset + minFixed), unification);
        }
        return targetList.tail != null && targetList.tail.unify(new ProvaListPtrImpl(this, offset + minFixed), unification);
    }

    @Override
    public PList rebuild(Unification unification) {
        if (ground || this == ProvaListImpl.emptyRList) {
            return this;
        }
        // Rebuild the fixed part
        final int fixedLength = fixed.length;
        PObj[] newFixed = new PObj[fixedLength];
        boolean changed = false;
        for (int i = 0; i < fixedLength; i++) {
            if (fixed[i].getClass() == ProvaConstantImpl.class) {
                newFixed[i] = fixed[i];
            } else if (fixed[i] instanceof VariableIndex) {
                newFixed[i] = unification.rebuild((VariableIndex) fixed[i]);
                changed |= newFixed[i] != fixed[i];
            } else if (fixed[i] instanceof PList) {
                newFixed[i] = ((PList) fixed[i]).rebuild(unification);
                changed |= newFixed[i] != fixed[i];
            } else if (fixed[i] instanceof ProvaMapImpl) {
                newFixed[i] = ((ProvaMapImpl) fixed[i]).rebuild(unification);
                changed |= newFixed[i] != fixed[i];
            } else {
                newFixed[i] = fixed[i];
            }
        }

        PObj newTail = null;
        if (tail != null) {
            if (tail instanceof VariableIndex) {
                newTail = unification.rebuild((VariableIndex) tail);
                changed |= newTail != tail;
            } else if (tail instanceof PList) {
                newTail = ((PList) tail).rebuild(unification);
                changed |= newTail != tail;
            } else {
                newTail = tail;
            }

            if (newTail instanceof PList) {
                changed = true;
                if (newTail == ProvaListImpl.emptyRList) {
                    newTail = null;
                } else {
                    final PObj[] tailFixed = ((PList) newTail).getFixed();
                    final PObj[] newFixedExtended = new PObj[fixedLength + tailFixed.length];
                    System.arraycopy(newFixed, 0, newFixedExtended, 0, newFixed.length);
                    System.arraycopy(tailFixed, 0, newFixedExtended, newFixed.length, tailFixed.length);
                    newTail = ((PList) newTail).getTail();
                    newFixed = newFixedExtended;
                }
            }
        }

        if (changed) {
            return new ProvaListImpl(newFixed, newTail);
        } else {
            return this;
        }
    }

    @Override
    public PObj rebuild(Unification unification, int offset) {
        PObj[] newFixed = new PObj[0];
        final int fixedLength = fixed.length;
        if (offset < fixedLength) {
            // Rebuild the fixed part
            newFixed = new PObj[fixedLength - offset];
            for (int i = offset; i < fixedLength; i++) {
                if (fixed[i] instanceof VariableIndex) {
                    newFixed[i - offset] = unification.rebuild((VariableIndex) fixed[i]);
                } else if (fixed[i] instanceof PList) {
                    newFixed[i - offset] = ((PList) fixed[i]).rebuild(unification);
                } else {
                    newFixed[i - offset] = fixed[i];
                }
            }

            PObj newTail = null;
            if (tail != null) {
                if (tail instanceof VariableIndex) {
                    newTail = unification.rebuild((VariableIndex) tail);
                } else if (tail instanceof PList) {
                    newTail = ((PList) tail).rebuild(unification);
                } else {
                    newTail = tail;
                }

                if (newTail instanceof PList) {
                    final PObj[] tailFixed = ((PList) newTail).getFixed();
                    final PObj[] newFixedExtended = new PObj[fixedLength + tailFixed.length];
                    System.arraycopy(newFixed, 0, newFixedExtended, 0, newFixed.length);
                    System.arraycopy(tailFixed, 0, newFixedExtended, newFixed.length, tailFixed.length);
                    newTail = ((PList) newTail).getTail();
                    newFixed = newFixedExtended;
                }
            }
            return new ProvaListImpl(newFixed, newTail);
            // TODO: deal with other cases
        } else if (offset == fixedLength) {

        } else {

        }

        return null;
    }

    @Override
    public PList rebuildSource(Unification unification) {
        if (ground || this == ProvaListImpl.emptyRList) {
            return this;
        }
        // Rebuild the fixed part
        PObj[] newFixed = new PObj[fixed.length];
        for (int i = 0; i < fixed.length; i++) {
            PObj f = fixed[i];
            PObj n;
            
            if (f.getClass() == ProvaConstantImpl.class) {
                n = f;
            } else if (f instanceof Literal) {
                n = ((Literal) f).rebuildSource(unification);
            } else if (f instanceof VariableIndex) {
                n = unification.rebuildSource((VariableIndex) f);
            } else if (f instanceof PList) {
                n = ((PList) f).rebuildSource(unification);
            } else if (f instanceof ProvaMapImpl) {
                n = ((ProvaMapImpl) f).rebuildSource(unification);
            } else {
                n = f;
            }            
            
            newFixed[i] = n;
        }

        PObj newTail = null;
        if (tail != null) {
            if (tail instanceof VariableIndex) {
                newTail = unification.rebuildSource((VariableIndex) tail);
            } else if (tail instanceof PList) {
                newTail = ((PList) tail).rebuildSource(unification);
            } else {
                newTail = tail;
            }

            if (newTail instanceof PList) {
                if (newTail == ProvaListImpl.emptyRList) {
                    newTail = null;
                } else {
                    PObj[] tailFixed = ((PList) newTail).getFixed();
                    PObj[] newFixedExtended = new PObj[fixed.length + tailFixed.length];
                    System.arraycopy(newFixed, 0, newFixedExtended, 0, newFixed.length);
                    System.arraycopy(tailFixed, 0, newFixedExtended, newFixed.length, tailFixed.length);
                    newTail = ((PList) newTail).getTail();
                    newFixed = newFixedExtended;
                }
            }
        }

        return new ProvaListImpl(newFixed, newTail);
    }

    @Override
    public PObj rebuildSource(Unification unification, int offset) {
        PObj[] newFixed;
        final int fixedLength = fixed.length;
        if (offset < fixedLength) {
            // Rebuild the fixed part
            newFixed = new PObj[fixedLength - offset];
            for (int i = offset; i < fixedLength; i++) {
                if (fixed[i] instanceof VariableIndex) {
                    newFixed[i - offset] = unification.rebuildSource((VariableIndex) fixed[i]);
                } else if (fixed[i] instanceof PList) {
                    newFixed[i - offset] = ((PList) fixed[i]).rebuildSource(unification);
                } else {
                    newFixed[i - offset] = fixed[i];
                }
            }

            PObj newTail = null;
            if (tail != null) {
                if (tail instanceof VariableIndex) {
                    newTail = unification.rebuildSource((VariableIndex) tail);
                } else if (tail instanceof PList) {
                    newTail = ((PList) tail).rebuildSource(unification);
                } else {
                    newTail = tail;
                }

                if (newTail instanceof PList) {
                    PObj[] tailFixed = ((PList) newTail).getFixed();
                    PObj[] newFixedExtended = new PObj[fixedLength + tailFixed.length];
                    System.arraycopy(newFixed, 0, newFixedExtended, 0, newFixed.length);
                    System.arraycopy(tailFixed, 0, newFixedExtended, newFixed.length, tailFixed.length);
                    newTail = ((PList) newTail).getTail();
                    newFixed = newFixedExtended;
                }
            }
            return new ProvaListImpl(newFixed, newTail);
            // TODO: deal with other cases
        } else if (offset == fixedLength) {

        } else {

        }

        return null;
    }

    @Override
    public String toString() {
        if (this == emptyRList) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        final int fixedLength = fixed.length;
        for (int i = 0; i < fixedLength; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(fixed[i]);
        }
        if (tail != null) {
            sb.append('|');
            sb.append(tail);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String toString(List<Variable> variables) {
        if (this == emptyRList) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        final int fixedLength = fixed.length;
        for (int i = 0; i < fixedLength; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(fixed[i].toString(variables));
        }
        if (tail != null) {
            sb.append('|');
            sb.append(tail.toString(variables));
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String performative() {
        if (fixed.length < 4 || !(fixed[3] instanceof Constant)) {
            return null;
        }
        return ((Constant) fixed[3]).getObject().toString();
    }

    @Override
    public Object compute() {
        Object[] args = new Object[fixed.length - 1];
        for (int i = 0; i < args.length; i++) {
            args[i] = fixed[i + 1].computeIfExpression();
        }
        return ((ProvaOperator) ((Constant) fixed[0]).getObject()).evaluate(args);
    }

    @Override
    public Object computeIfExpression() {
        if (fixed.length >= 1 && fixed.length <= 3 && fixed[0] instanceof Constant && ((Constant) fixed[0]).getObject() instanceof ProvaOperator) {
            return compute();
        }
        PObj[] newFixed = new PObj[fixed.length];
        for (int i = 0; i < newFixed.length; i++) {
            newFixed[i] = ProvaConstantImpl.wrap(fixed[i].computeIfExpression());
        }
        return ProvaListImpl.create(newFixed);
    }

    @Override
    public void setGround(boolean ground) {
        this.ground = ground;
    }

}
