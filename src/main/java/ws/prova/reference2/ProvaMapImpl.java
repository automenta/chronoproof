package ws.prova.reference2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

/**
 * A wrapper around a Java Map that recursively (for all nested Maps) wraps all
 * Java objects in the Map values in ProvaObject(s). The keys in the original
 * Map are converted to Java String by toString().
 * <p/>
 * The Prova Maps are created using the equivalent {@link #create(Map)} and
 * {@link #wrapValues(Map)}} methods.
 *
 */
public class ProvaMapImpl extends ProvaConstantImpl<Object> {

    private static final long serialVersionUID = -4660675788561894085L;

    public static ProvaMapImpl create(Map<?, ?> m) {
        return ProvaMapImpl.wrapValues(m);
    }

    protected ProvaMapImpl(Object object) {
        super(object);
    }

    @Override
    public int collectVariables(long ruleId, List<Variable> variables) {
        int rc = -1;
        Map<String, PObj> map = (Map<String, PObj>) object;
        for (Entry<String, PObj> e : map.entrySet()) {
            final PObj value = e.getValue();
            if (value instanceof VariableIndex) {
                rc = 0;
                continue;
            }
            int r = value.collectVariables(ruleId, variables);
            if (r != -1 && !(value instanceof PList) && !(value instanceof ProvaMapImpl)) {
                e.setValue(new ProvaVariablePtrImpl(ruleId, r));
                rc = r;
            }
        }
        return rc;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean updateGround(List<Variable> variables) {
        boolean rc = true;
        Map<String, PObj> map = (Map<String, PObj>) object;
        for (Entry<String, PObj> e : map.entrySet()) {
            if (!e.getValue().updateGround(variables)) {
                rc = false;
            }
        }
        return rc;
    }

    @Override
    public boolean unify(PObj target, Unification unification) {
        if (target == null) {
            return false;
        }
        if (target instanceof Variable) {
            return ((Variable) target).unify(this, unification);
        }
        if (target instanceof VariableIndex) {
            return ((VariableIndex) target).unify(this, unification);
        } else if (target instanceof PList) {
            return false;
        } else if (target instanceof ProvaListPtrImpl) {
            return false;
        }
        // The target is a constant
        Constant targetConstant = (Constant) target;
        Object targetObject = targetConstant.getObject();
        if (object instanceof Map<?, ?> && targetObject instanceof Map<?, ?>) {
            Map<?, ?> src = (Map<?, ?>) object;
            Map<?, ?> tgt = (Map<?, ?>) targetObject;
            for (Entry<?, ?> t : tgt.entrySet()) {
                PObj srcValue = (PObj) src.get(t.getKey());
                if (srcValue == null) {
                    return false;
                }
                boolean rc = srcValue.unify((PObj) t.getValue(), unification);
                if (!rc) {
                    return false;
                }
            }
            return true;
        }
        return object.equals(targetObject);
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

    @SuppressWarnings("unchecked")
    @Override
    public void substituteVariables(final VariableIndex[] varsMap) {
        final Map<String, PObj> map = (Map<String, PObj>) object;
        for (Entry<String, PObj> e : map.entrySet()) {
            PObj value = e.getValue();
            if (value instanceof VariableIndex) {
                e.setValue(varsMap[((VariableIndex) value).getIndex()]);
            } else {
                e.getValue().substituteVariables(varsMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isGround() {
        final Map<String, PObj> map = (Map<String, PObj>) object;
        for (Entry<String, PObj> e : map.entrySet()) {
            if (!e.getValue().isGround()) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PObj cloneWithBoundVariables(List<Variable> variables, List<Boolean> isConstant) {
        if (isGround()) {
            return this;
        }
        final Map<String, PObj> map = (Map<String, PObj>) object;
        final Map<String, PObj> newMap = new HashMap<String, PObj>(map.size());
        for (Entry<String, PObj> e : map.entrySet()) {
            if (e.getValue().isGround()) {
                newMap.put(e.getKey(), e.getValue());
            } else {
                newMap.put(e.getKey(), e.getValue().cloneWithBoundVariables(variables, isConstant));
            }
        }
        return new ProvaMapImpl(newMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PObj cloneWithVariables(List<Variable> variables) {
        if (isGround()) {
            return this;
        }
        Map<String, PObj> map = (Map<String, PObj>) object;
        Map<String, PObj> newMap = new HashMap<String, PObj>(map.size());
        for (Entry<String, PObj> e : map.entrySet()) {
            PObj nextValue;
            PObj ev = e.getValue();
            if (ev.isGround()) {
                nextValue = ev;
            } else {
                nextValue = ev.cloneWithVariables(variables);
            }

            newMap.put(e.getKey(), nextValue);
        }
        return new ProvaMapImpl(newMap);
    }


    @SuppressWarnings("unchecked")
    @Override
    public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
        if (isGround()) {
            return this;
        }
        final Map<String, PObj> map = (Map<String, PObj>) object;
        final Map<String, PObj> newMap = new HashMap<String, PObj>(map.size());
        
        for (Entry<String, PObj> e : map.entrySet()) {
            PObj nextValue;
            PObj ev = e.getValue();
            if (ev.isGround()) {
                nextValue = ev;
            } else {
                nextValue = ev.cloneWithVariables(ruleId, variables);
            }

            newMap.put(e.getKey(), nextValue);
        }
        return new ProvaMapImpl(newMap);    }

    @Override
    public Number compute() {
        return (Number) object;
    }

    @Override
    public Object computeIfExpression() {
        return object;
    }

    /**
     * Wrap the map values as ProvaObject(s) while ignoring the already present
     * ProvaObject(s) and recursively wrapping further Map(s). This function is
     * often used for wrapping Maps before passing them as payload to
     * {@link ws.prova.api2.ProvaCommunicator#addMsg(ProvaList)} or
     * {@link ws.prova.api2.ProvaCommunicator#addMsg(String, String, String, Object)}.
     *
     * @param m Map to wrap
     * @return a wrapped Map
     */
    public static ProvaMapImpl wrapValues(Map<?, ?> m) {
        final Map<String, PObj> map = new HashMap<String, PObj>(m.size());
        for (Entry<?, ?> e : m.entrySet()) {
            final Object value = e.getValue();
            PObj store = null;
            if (value instanceof PObj) {
                store = (PObj) value;
            } else if (value instanceof Map) {
                store = wrapValues((Map<?, ?>) value);
            } else {
                store = ProvaConstantImpl.create(value);
            }
            map.put(e.getKey().toString(), store);
        }
        return new ProvaMapImpl(map);
    }

    /**
     * Wrap an object in a ProvaMapImpl or ProvaConstantImpl if it is a Map or
     * any other non-Prova object. Keep it as it is if it is already a
     * ProvaObject.
     *
     * @param o
     * @return
     */
    public static PObj wrap(Object o) {
        return (o instanceof PObj) ? (PObj) o : o instanceof Map ? wrapValues((Map<?, ?>) o) : new ProvaConstantImpl(o);
    }

    @SuppressWarnings("unchecked")
    public PObj rebuild(Unification unification) {
        final Map<String, PObj> map = (Map<String, PObj>) object;
        final Map<String, PObj> newMap = new HashMap<String, PObj>(map.size());
        boolean changed = false;
        for (Entry<String, PObj> e : map.entrySet()) {
            PObj ov = e.getValue();
            if (e.getValue() instanceof VariableIndex) {
                PObj nv = unification.rebuild((VariableIndex) ov);
                newMap.put(e.getKey(), nv);
                if (ov != nv) {
                    changed = true;
                }
            } else if (ov instanceof PList) {
                PObj nv = ((PList) ov).rebuild(unification);
                newMap.put(e.getKey(), nv);
                if (ov != nv) {
                    changed = true;
                }
            } else if (ov instanceof ProvaMapImpl) {
                PObj nv = ((ProvaMapImpl) ov).rebuild(unification);
                newMap.put(e.getKey(), nv);
                if (ov != nv) {
                    changed = true;
                }
            } else {
                newMap.put(e.getKey(), ov);
            }

        }
        return changed ? new ProvaMapImpl(newMap) : this;
    }

    @SuppressWarnings("unchecked")
    public PObj rebuildSource(Unification unification) {
        final Map<String, PObj> map = (Map<String, PObj>) object;
        final Map<String, PObj> newMap = new HashMap<String, PObj>(map.size());
        boolean changed = false;
        for (Entry<String, PObj> e : map.entrySet()) {
            PObj ov = e.getValue();
            if (e.getValue() instanceof VariableIndex) {
                PObj nv = unification.rebuild((VariableIndex) ov);
                newMap.put(e.getKey(), nv);
                if (ov != nv) {
                    changed = true;
                }
            } else if (ov instanceof PList) {
                PObj nv = ((PList) ov).rebuildSource(unification);
                newMap.put(e.getKey(), nv);
                if (ov != nv) {
                    changed = true;
                }
            } else if (ov instanceof ProvaMapImpl) {
                PObj nv = ((ProvaMapImpl) ov).rebuildSource(unification);
                newMap.put(e.getKey(), nv);
                if (ov != nv) {
                    changed = true;
                }
            } else {
                newMap.put(e.getKey(), ov);
            }

        }
        return changed ? new ProvaMapImpl(newMap) : this;
    }

    @SuppressWarnings("unchecked")
    public Object unwrap() {
        final Map<String, PObj> map = (Map<String, PObj>) object;
        final Map<String, Object> newMap = new HashMap<String, Object>(map.size());
        for (Entry<String, PObj> e : map.entrySet()) {
            PObj ov = e.getValue();
            if (ov instanceof Constant) {
                newMap.put(e.getKey(), ((Constant) ov).getObject());
            }
        }
        return newMap;
    }

    @Override
    public boolean matched(Constant target) {
        return target instanceof ProvaMapImpl;
    }

}
