package ws.prova.reference2.operators;

import java.util.List;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;

public interface ProvaBinaryOperator {

	public boolean evaluate(KB kb, List<Literal> newLiterals, PObj o1, Computable a2);

}
