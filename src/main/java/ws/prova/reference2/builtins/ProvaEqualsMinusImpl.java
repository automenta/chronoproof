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
import ws.prova.reference2.ProvaGlobalConstantImpl;

public class ProvaEqualsMinusImpl extends ProvaBuiltinImpl {

	public ProvaEqualsMinusImpl(KB kb) {
		super(kb,"equals_minus");
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
		PObj rhs = data[1];
		if( rhs instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) rhs;
			rhs = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(rhs instanceof Constant) )
			return false;
		Object o = ((Constant) rhs).getObject();
		Object n = null;
		if( !(o instanceof Number) )
			return false;
		if( o instanceof Byte ) {
			byte m = ((Byte) o);
			n = (byte) ~m;
		} else if( o instanceof Integer ) {
			int m = ((Integer) o);
			n = -m;
		} else if( o instanceof Long ) {
			long m = ((Long) o);
			n = -m;
		} else if( o instanceof Float ) {
			float m = ((Float) o);
			n = -m;
		} else if( o instanceof Double ) {
			Double m = ((Double) o);
			n = -m;
		}
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( lt instanceof Variable ) {
			((Variable) lt).setAssigned(ProvaConstantImpl.create(n));
			return true;
		}
		if( lt instanceof Constant ) {
			Constant lhsConstant = (Constant) lt;
			if( lhsConstant instanceof ProvaGlobalConstantImpl ) {
				((ProvaGlobalConstantImpl) lhsConstant).setObject(n);
				return true;
			}
			return lhsConstant.getObject().equals(n);
		}
		return true;
	}

}
