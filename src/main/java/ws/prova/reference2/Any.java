package ws.prova.reference2;

import java.util.List;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class Any extends ProvaTermImpl implements Constant, Computable {

    static final Any the = new Any();

    public static Any any() {
        //return new Any();
        return the;
    }

    @Override
    public void setObject(Object object) {
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public PObj getRecursivelyAssigned() {
        return this;
    }

    @Override
    public int collectVariables(long ruleId, List<Variable> variables) {
        return -1;
    }

    @Override
    public int computeSize() {
        return 1;
    }

    @Override
    public boolean unify(PObj target, Unification unification) {
        return true;
    }

    @Override
    public String toString() {
        return "_";
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
        return "_";
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
        return null;
    }

    @Override
    public Object computeIfExpression() {
        return null;
    }

    @Override
    public boolean matched(Constant target) {
        return true;
    }

    @Override
    public boolean updateGround(List<Variable> variables) {
        return false;
    }

}
