package ws.prova.reference2.operators;

import java.util.List;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;

public class ProvaBinaryLessThanImpl implements ProvaBinaryOperator {

	@Override
	public boolean evaluate( KB kb, List<Literal> newLiterals, PObj o1, Computable a2 ) {
		if( o1 instanceof Variable ) {
			throw new RuntimeException("Variable "+o1+" used in '<'");
		}
		Object oa1 = ((Constant) o1).getObject();
		Object oa2 = a2.compute();
		if( oa2.getClass()==ProvaConstantImpl.class )
			oa2 = ((Constant) oa2).getObject();
		if( !(oa1 instanceof Number) || !(oa2 instanceof Number) )
			return false;
		Number na1 = (Number) oa1;
		Number na2 = (Number) oa2;
		if( na1 instanceof Double || na2 instanceof Double )
			return na1.doubleValue()<na2.doubleValue();
		else if( na1 instanceof Float || na2 instanceof Float )
			return na1.floatValue()<na2.floatValue();
		else if( na1 instanceof Long || na2 instanceof Long )
			return na1.longValue()<na2.longValue();
		else if( na1 instanceof Integer || na2 instanceof Integer )
			return na1.intValue()<na2.intValue();
		return na1.byteValue()<na2.byteValue();
	}

        @Override
	public String toString() {
		return "<";
	}

}
