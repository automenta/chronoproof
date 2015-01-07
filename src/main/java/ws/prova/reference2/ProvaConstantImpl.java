package ws.prova.reference2;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaConstantImpl<O> extends ProvaTermImpl implements Constant<O>, Computable {

    private static final long serialVersionUID = -3976583974992460058L;

    protected O object;

    public static <X> ProvaConstantImpl<X> create(X object) {
        return new ProvaConstantImpl(object);
    }

    protected ProvaConstantImpl(O object) {
        this.object = object;
    }

    @Override
    public void setObject(O object) {
        this.object = object;
    }

    @Override
    public O getObject() {
        return object;
    }

    @Override
    public PObj getRecursivelyAssigned() {
        return this;
    }

    @Override
    public int collectVariables(long ruleId, List<Variable> variables) {
        if (object instanceof Map<?, ?>) {
            Map<String, PObj> map = (Map<String, PObj>) object;
            for (Entry<String, PObj> e : map.entrySet()) {
                int r = e.getValue().collectVariables(ruleId, variables);
                if (r != -1) {
                    e.setValue(new ProvaVariablePtrImpl(ruleId, r));
                }
            }
            return 0;
        }
        return -1;
    }

//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset) {
//	}
    @Override
    public int computeSize() {
        return 1;
    }

    @Override
    public boolean matched(Constant target) {
        if (target instanceof ProvaMapImpl) {
            return false;
        }
        return object.equals(target.getObject());
    }

    @Override
    public boolean unify(PObj target, Unification unification) {
        if (target == null) {
            return false;
        }
        if (target instanceof Constant) {
            // The target is a constant
            Constant targetConstant = (Constant) target;
            // TODO: deal with types later
            Object targetObject = targetConstant.getObject();
            return object.equals(targetObject);
        }
        if (target instanceof Variable) {
            return target.unify(this, unification);
        }
        if (target instanceof VariableIndex) {
            return target.unify(this, unification);
        }
        if (object instanceof PObj) {
            return ((PObj) object).unify(target, unification);
        }
        return false;
    }

    @Override
    public String toString() {
//		if( object instanceof String ) {
//			StringBuilder sb = new StringBuilder("\'");
//			sb.append(object);
//			sb.append('\'');
//			return sb.toString();
//		}
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

    @Override
    public void substituteVariables(final VariableIndex[] varsMap) {
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public String toString(List<Variable> variables) {
        return object.toString();
    }

    @Override
    public PObj cloneWithBoundVariables(List<Variable> variables, List<Boolean> isConstant) {
        return this;
    }

    @Override
    public PObj cloneWithVariables(List<Variable> variables) {
        return this;
    }

    @Override
    public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
        return this;
    }

    @Override
    public Number compute() {
        return (Number) object;
    }

    @Override
    public Object computeIfExpression() {
        return object;
    }

    public static PObj wrap(Object o) {
        return (o instanceof PObj) ? (PObj) o : create(o);
    }

    @Override
    public boolean updateGround(List<Variable> variables) {
        return true;
    }

}
