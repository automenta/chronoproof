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

public class ProvaTypeImpl extends ProvaBuiltinImpl {

	public ProvaTypeImpl(KB kb) {
		super(kb,"type");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=2 )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		String className = null;
		if( lt instanceof Constant )
			className = ((Constant) lt).getObject().getClass().getName();
		else if( lt instanceof Variable )
			className = Variable.class.getName();
		else
			className = PList.class.getName();
		PObj out = data[1];
		if( out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) out;
			out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( out instanceof Constant ) {
			return ((Constant) out).getObject().equals(className);
		} else if( out instanceof Variable ) {
			((Variable) out).setAssigned(ProvaConstantImpl.create(className));
		}
		return true;
	}

}
