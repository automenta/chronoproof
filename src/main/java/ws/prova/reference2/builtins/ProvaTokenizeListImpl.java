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
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;

public class ProvaTokenizeListImpl extends ProvaBuiltinImpl {

	public ProvaTokenizeListImpl(KB kb) {
		super(kb,"tokenize_list");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=2 || !(data[0] instanceof PList) )
			return false;
		PObj n_out = data[1];
		if( n_out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) n_out;
			n_out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(n_out instanceof Variable) && !(n_out instanceof PList))
			return false;
		PObj[] args = ((PList) ((PList) data[0]).cloneWithVariables(variables)).getFixed();
		if( args.length!=2 )
			return false;
		PObj lt = args[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof Constant) )
			return false;
		String in = ((Constant) lt).toString();
		PObj exp = args[1];
		if( exp instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) exp;
			exp = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		String sep = ((Constant) exp).toString();
		String[] res = in.split(sep);
		PObj arr_n[] = new PObj[res.length];
		for( int i=0; i<res.length; i++ )
			arr_n[i] = ProvaConstantImpl.create(res[i]);
		PList n = ProvaListImpl.create( arr_n );
		if( n_out instanceof PList ) {
			// Make sure the unification is done between the result and the original subgoal
			Predicate pred = new ProvaPredicateImpl("",2,kb);
			PList ls = ProvaListImpl.create(new PObj[] {data[0],n});
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			Literal newLiteral = new ProvaLiteralImpl(pred,terms);
			newLiterals.add(newLiteral);
			return true;
		}
		((Variable) n_out).setAssigned(n);
		return true;
	}

}
