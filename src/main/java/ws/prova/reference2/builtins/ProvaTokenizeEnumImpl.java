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
import ws.prova.kernel2.Rule;

public class ProvaTokenizeEnumImpl extends ProvaBuiltinImpl {

	public ProvaTokenizeEnumImpl(KB kb) {
		super(kb,"tokenize_enum");
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
		PObj n_out = data[1];
		if( n_out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) n_out;
			n_out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof PList) )
			return false;
		PObj[] args = ((PList) ((PList) lt).cloneWithVariables(variables)).getFixed();
		if( args.length!=2 )
			return false;
		String in = ((Constant) args[0]).toString();
		PObj sep = args[1];
		if( sep instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) sep;
			sep = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		String delim = ((Constant) sep).toString();
		String tokens[] = in.split(delim);
		// Make sure the unification is done between the result and the original subgoal
		Predicate pred = new ProvaPredicateImpl("",2,kb);
		for( String token : tokens ) {
			PList ls = ProvaListImpl.create(new PObj[] {data[0],ProvaConstantImpl.create(token)} );
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = Rule.createVirtualRule(1, lit, null);
			pred.addClause(clause);
		}		
		Literal newLiteral = new ProvaLiteralImpl(pred,terms);
		newLiterals.add(newLiteral);
		return true;
	}

}
