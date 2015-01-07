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
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaAtImpl extends ProvaBuiltinImpl {

	public ProvaAtImpl(KB kb) {
		super(kb,"at");
	}

	/**
	 * Find element at the specified position in a list.
	 * If the supplied position is -1, assume the request is for the last element in the list.
	 * If the target element is already supplied, unify it against the element we find at the specified position.
	 * If the input list is a free variable, generate a list of free variables that includes the supplied positive position.
	 */
	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=2 )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex ltPtr = (VariableIndex) lt;
			lt = variables.get(ltPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof PList) )
			return false;
		PList args = (PList) lt;
		if( args.getFixed().length!=2 )
			return false;
		PObj ppos = args.getFixed()[1];
		if( ppos instanceof VariableIndex ) {
			VariableIndex pposPtr = (VariableIndex) ppos;
			ppos = variables.get(pposPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(ppos instanceof Constant) )
			return false;
		Object opos = ((Constant) ppos).getObject();
		if( !(opos instanceof Integer) )
			return false;
		int pos = (Integer) opos;
		if( pos < -1 )
			return false;

		// data[1] is not cloned, it keeps the variable pointers intact
		PObj out = data[1];
		if( out instanceof VariableIndex ) {
			VariableIndex outPtr = (VariableIndex) out;
			out = variables.get(outPtr.getIndex()).getRecursivelyAssigned();
		}

		PObj olist = args.getFixed()[0];
		if( olist instanceof VariableIndex ) {
			VariableIndex listPtr = (VariableIndex) olist;
			olist = variables.get(listPtr.getIndex()).getRecursivelyAssigned();
		}
		if( olist instanceof Variable ) {
			if( pos < 0 )
				return false;
			// Generate a list given the requested position
			PObj[] fixed = new PObj[pos+1];
			for( int i=0; i<pos; i++ ) {
				fixed[i] = ProvaVariableImpl.create();
			}
			fixed[pos] = out;
			PList newList = ProvaListImpl.create(fixed, ProvaVariableImpl.create());
			((Variable) olist).setAssigned(newList);
			return true;
		}
		if( !(olist instanceof PList) )
			return false;
		
		PList list = (PList) olist;
		if( pos==-1 ) {
			// Request for the last element
			if( list.getTail()!=null )
				// Assume that if there is a rest, we do not know what is the last element
				return false;
			pos = list.getFixed().length - 1;
		} else if( list.getFixed().length-1 < pos )
			return false;
		
		// The element IS cloned as it will be used for creating a virtual rule 
		final PObj element = list.getFixed()[pos].cloneWithVariables(variables);
		if( out instanceof Variable ) {
			((Variable) out).setAssigned(element);
			return true;
		}
		if( out instanceof Constant || out instanceof PList ) {
			// Unify the element at position pos by creating a single fact virtual predicate
			Predicate pred = new ProvaPredicateImpl("",1,kb);
			PList ls = ProvaListImpl.create(new PObj[] {element} );
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			PList outls = ProvaListImpl.create(new PObj[] {out} );
			Literal newLiteral = new ProvaLiteralImpl(pred,outls);
			newLiterals.add(newLiteral);
			return true;
		}
		return false;
	}

}
