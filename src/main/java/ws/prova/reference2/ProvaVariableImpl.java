package ws.prova.reference2;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaVariableImpl extends ProvaTermImpl implements Variable {

    private static final long serialVersionUID = 7501612596168443208L;

    private Object name;

    private Class<?> type;

    // The term that this variable is assigned to
    private PObj assigned;

    // Where this variable is in the rule's variables
    private int index;

    private long ruleId;

    private static AtomicLong incName = new AtomicLong(0);

    public static Variable create() {
        return new ProvaVariableImpl();
    }

    public static Variable create(final String name) {
        return new ProvaVariableImpl(name);
    }

    public static Variable create(final String name, final Class<?> type) {
        return new ProvaVariableImpl(name, type);
    }

    public static ProvaVariableImpl create(final String name, final PObj assigned) {
        return new ProvaVariableImpl(name, assigned);
    }

    private ProvaVariableImpl() {
        this.name = incName.incrementAndGet();
        this.type = Object.class;
        this.index = -1;
    }

    private ProvaVariableImpl(final String name) {
        this.name = "_".equals(name) ? incName.incrementAndGet() : name;
        this.type = Object.class;
        this.index = -1;
    }

    private ProvaVariableImpl(final String name, final Class<?> type) {
        this.name = name;
        this.type = type;
        this.index = -1;
    }

    private ProvaVariableImpl(final Class<?> type, final PObj assigned, final int index,
            final long ruleId) {
        this.name = incName.incrementAndGet();
        this.type = type;
        this.assigned = assigned;
        this.index = index;
        this.ruleId = ruleId;
    }

    private ProvaVariableImpl(final String name, final PObj assigned) {
        this.name = name;
        this.assigned = assigned;
        this.type = Object.class;
        this.index = -1;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getName() {
        return name;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public void setAssigned(final PObj assigned) {
        this.assigned = assigned;
    }

    @Override
    public PObj getAssigned() {
        return assigned;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public PObj getRecursivelyAssigned() {
        if (assigned instanceof Constant) {
            return assigned;
        }
        if (assigned == this) {
            assigned = null;
            return this;
        }
        if (assigned == null) {
            return this;
        }
        PObj recursivelyAssigned = assigned.getRecursivelyAssigned();
        if (assigned != recursivelyAssigned) {
            assigned = recursivelyAssigned;
        }
        return recursivelyAssigned;
    }

    @Override
    public int collectVariables(final long ruleId, final List<Variable> variables) {
        if (assigned != null) {
            assigned.collectVariables(ruleId, variables);
            return -1;
        }
        int foundIndex = variables.indexOf(this);
        if (foundIndex != -1) {
            index = foundIndex;
            return index;
        }
        index = variables.size();
        variables.add(this);
        return index;
    }

    @Override
    public int computeSize() {
        if (assigned != null) {
            return assigned.computeSize();
        }
        return -1;
    }

//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset) {
//		collectVariables(ruleId, variables);
//	}
    @Override
    public Variable clone() {
        return new ProvaVariableImpl(type, assigned, index, ruleId);
    }

    @Override
    public Variable clone(final long ruleId) {
        return new ProvaVariableImpl(type, assigned, index, ruleId);
    }

    @Override
    public boolean unify(final PObj target, final Unification unification) {
        if (target == null) {
            assigned = ProvaListImpl.emptyRList;
            return true;
        }
        if (target instanceof Variable) {
            final Class<?> targetType = ((Variable) target).getType();
            if (targetType.isAssignableFrom(type)) {
                ((Variable) target).setAssigned(this);
                return true;
            }
            if (type.isAssignableFrom(targetType)) {
                assigned = target;
                return true;
            }
            return false;
//			if( !((ProvaVariable) target).getType().isAssignableFrom(type) )
//				return false;
//			((ProvaVariable) target).setAssigned(this);
//			return true;
        }
        if (type != Object.class) {
            if (target instanceof Constant) {
                if (target instanceof Any) {
                    return true;
                }
                if (!type.isInstance(((Constant) target).getObject())) {
                    return false;
                }
            }
        }
        assigned = target;
        return true;
    }

    @Override
    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public long getRuleId() {
        return ruleId;
    }

    @Override
    public String toString() {
        if (assigned == null) {
            StringBuilder sb = new StringBuilder();
            if (type != Object.class) {
                sb.append(type.getCanonicalName());
                sb.append('.');
            }
            String strName = name.toString();
            if (strName.length() != 0 && Character.isDigit(strName.charAt(0))) {
                sb.append("<");
                sb.append(name);
                sb.append('>');
            } else {
                sb.append(name);
            }
            return sb.toString();
        }
        return getRecursivelyAssigned().toString();
    }

    @Override
    public void substituteVariables(final VariableIndex[] varsMap) {
    }

    @Override
    public int hashCode() {
        return name.hashCode();

    }

    @Override
    public boolean equals(Object o) {
        ProvaVariableImpl var = (ProvaVariableImpl) o;
        return var.name.equals(name) && var.type == type;
    }

    @Override
    public boolean isGround() {
        return false;
    }

    @Override
    public String toString(final List<Variable> variables) {
        return toString();
    }

    @Override
    public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant) {
        if (assigned != null) {
            return assigned;
        }
        return this;
    }

    @Override
    public PObj cloneWithVariables(final List<Variable> variables) {
        if (assigned != null) {
            return assigned;
        }
        return this;
    }

    @Override
    public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
        if (assigned != null) {
            return assigned;
        }
        return this;
    }

    @Override
    public Object computeIfExpression() {
        return this;
    }

    @Override
    public Object compute() {
        return this;
    }

    @Override
    public boolean updateGround(final List<Variable> variables) {
        if (assigned != null) {
            return assigned.updateGround(variables);
        }
        return false;
    }

}
