package ws.prova.reference2.builtins;

import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaConstantImpl;

public class ProvaMathRemainderImpl extends ProvaBuiltinImpl {

	public ProvaMathRemainderImpl(KB kb) {
		super(kb,"math_remainder");
	}

	@Override
	//TODO: recursive expressions as operands
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=3 )
				return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !((lt instanceof Variable) || (lt instanceof Constant)) )
			return false;
		PObj a1 = data[1];
		if( a1 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a1;
			a1 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(a1 instanceof Constant) )
			return false;
		Object oa1 = ((Constant) a1).getObject();
		if( !(oa1 instanceof Number) )
			return false;
		PObj a2 = data[2];
		if( a2 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a2;
			a2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(a2 instanceof Constant) )
			return false;
		Object oa2 = ((Constant) a2).getObject();
		if( !(oa2 instanceof Number) )
			return false;
		Number na1 = (Number) oa1;
		Number na2 = (Number) oa2;
		Number result;
		if( na1 instanceof Double || na2 instanceof Double )
			result = na1.doubleValue()%na2.doubleValue();
		else if( na1 instanceof Float || na2 instanceof Float )
			result = na1.floatValue()%na2.floatValue();
		else if( na1 instanceof Long || na2 instanceof Long )
			result = na1.longValue()%na2.longValue();
		else if( na1 instanceof Integer || na2 instanceof Integer )
			result = na1.intValue()%na2.intValue();
		else
			result = na1.byteValue()%na2.byteValue();
		if( lt instanceof Constant )
			return ((Constant) lt).getObject()==result;
		((Variable) lt).setAssigned(ProvaConstantImpl.create(result));
		return true;
	}

}
