package ws.prova.reference2.operators;

import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PObj;

public class ProvaOperatorFactoryImpl {

	public static ProvaOperator create( String op ) {
		if( "+".equals(op) )
			return new ProvaAddImpl();
		else if( "-".equals(op) )
			return new ProvaSubtractImpl();
		else if( "*".equals(op) )
			return new ProvaMultiplyImpl();
		else if( "/".equals(op) )
			return new ProvaDivideImpl();
		else if( "mod".equals(op) )
			return new ProvaRemainderImpl();
		else if( "neg".equals(op) )
			return new ProvaNegateImpl();
		else throw new RuntimeException("Invalid operator "+op);
	}

	public static ProvaOperator createFunctionCall( PObj otype, PObj omethod ) {
		String type = ((Constant) otype).getObject().toString();
		String method = ((Constant) omethod).getObject().toString();
		return new ProvaFcalcImpl(type,method);
	}

}
