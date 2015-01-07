package ws.prova.reference2.operators;

import java.util.List;
import ws.prova.kernel2.Computable;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaGlobalConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;

public class ProvaBinaryEqualsImpl implements ProvaBinaryOperator {

	@Override
	public boolean evaluate( KB kb, List<Literal> newLiterals, PObj o1, Computable a2 ) {
		Object n2 = a2.computeIfExpression();
		if( n2==null )
			return false;
		if( n2.getClass()==ProvaConstantImpl.class )
			n2 = ((Constant) n2).getObject();
		if( o1 instanceof Variable ) {
			((Variable) o1).setAssigned(ProvaConstantImpl.wrap(n2));
			return true;
		}
		if( o1 instanceof ProvaGlobalConstantImpl ) {
			((ProvaGlobalConstantImpl) o1).setObject(n2);
			return true;
		}
		if( a2 instanceof Variable ) {
			((Variable) a2).setAssigned(o1);
			return true;
		}
		if( o1 instanceof PList ) {
			if( !(n2 instanceof PList) )
				return false;
			// Send this to the unification
			final Predicate pred = new ProvaPredicateImpl("", 1, kb);
			final Literal lit = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(new PObj[] {(PObj) o1}));
			final Rule clause = ProvaRuleImpl.createVirtualRule(1, lit,
					null);
			pred.addClause(clause);
			final Literal newLiteral = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(new PObj[] {(PObj) n2}));
			newLiterals.add(newLiteral);
			return true;
		}
		return ((Constant) o1).getObject().equals(n2);
	}

        @Override
	public String toString() {
		return "=";
	}

}
