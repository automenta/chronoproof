package ws.prova.reference2.operators;

import java.util.List;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;

public class ProvaBinaryNotEqualsImpl implements ProvaBinaryOperator {

	@Override
	public boolean evaluate( KB kb, List<Literal> newLiterals, PObj o1, Computable a2 ) {
		if( o1 instanceof Variable ) {
			throw new RuntimeException("Variable "+o1+" used in '!='");
		}
		Object n2 = a2.computeIfExpression();
		if( n2.getClass()==ProvaConstantImpl.class )
			n2 = ((Constant) n2).getObject();
		// TODO: what to do if the lhs contains a ProvaList?
		return !((Constant) o1).getObject().equals(n2);
	}

        @Override
	public String toString() {
		return "!=";
	}

}
