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
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaGlobalConstantImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;

public class ProvaEqualsImpl extends ProvaBuiltinImpl {

	public ProvaEqualsImpl(KB kb) {
		super(kb,"equals");
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
		if( lt instanceof Variable ) {
			((Variable) lt).setAssigned(data[1]);
			return true;
		}
		PObj rhs = data[1];
		if( rhs instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) rhs;
			rhs = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( rhs instanceof Variable ) {
			((Variable) rhs).setAssigned(data[0]);
			return true;
		}
		if( lt instanceof Constant ) {
			Constant lhsConstant = (Constant) lt;
			if( rhs instanceof Constant ) {
				if( lhsConstant instanceof ProvaGlobalConstantImpl ) {
					((ProvaGlobalConstantImpl) lhsConstant).setObject(((Constant) rhs).getObject());
					return true;
				}
				return lhsConstant.getObject().equals(((Constant) rhs).getObject());
			}
			return false;
		}
		// Deal with the case when LHS is a list
		if( lt instanceof PList && rhs instanceof PList ) {
			Predicate pred = new ProvaPredicateImpl("",1,kb);
			Literal lit = new ProvaLiteralImpl(pred, (PList) lt);
			Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			Literal newLiteral = new ProvaLiteralImpl(pred, (PList) rhs);
			newLiterals.add(newLiteral);
			return true;
		}
		return false;
	}

}
