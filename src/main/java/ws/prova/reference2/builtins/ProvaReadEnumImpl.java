package ws.prova.reference2.builtins;

import java.io.BufferedReader;
import java.io.IOException;
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

public class ProvaReadEnumImpl extends ProvaBuiltinImpl {

	public ProvaReadEnumImpl(KB kb) {
		super(kb,"read_enum");
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
		if( !(lt instanceof Constant) )
			return false;
		Object o = ((Constant) lt).getObject();
		if( !(o instanceof BufferedReader) )
			return false;
		BufferedReader in = (BufferedReader) o;
		PObj out = data[1];
		if( out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) out;
			out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(out instanceof Variable) && !(out instanceof Constant)  )
			return false;
		String s = null;
		Predicate pred = null;
		if( out instanceof Variable ) {
			// Make sure the unification is done between the result and the original subgoal
			pred = new ProvaPredicateImpl("",2,kb);
		} else if( out instanceof Constant ) {
			s = ((Constant) out).toString();
		}
		String line = "";
		try {
			while( (line = in.readLine()) != null ) {
				if( pred!=null ) {
					PList ls = ProvaListImpl.create(new PObj[] {data[0],ProvaConstantImpl.create(line)} );
					Literal lit = new ProvaLiteralImpl(pred,ls);
					Rule clause = Rule.createVirtualRule(1, lit, null);
					pred.addClause(clause);
				} else if( line.equals(s) ) {
					return true;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Literal newLiteral = new ProvaLiteralImpl(pred,terms);
		newLiterals.add(newLiteral);
		return true;
	}

}
