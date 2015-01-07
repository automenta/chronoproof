package ws.prova.reference2.builtins;

import java.util.ArrayList;
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
import ws.prova.kernel2.Rule;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaDeriveImpl extends ProvaBuiltinImpl {

	public ProvaDeriveImpl(KB kb) {
		super(kb,"derive");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms();
		if( terms.getFixed().length==0 )
			return false;
		PObj first = terms.getFixed()[0];
		if( first instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) first;
			first = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(first instanceof PList) )
			return false;
		PList firstList0 = (PList) first;
		if( firstList0==ProvaListImpl.emptyRList )
			return false;
		PObj[] fixed0 = firstList0.getFixed();
		PList firstList = (PList) first; //.cloneWithVariables(variables);
		PObj[] fixed = firstList.getFixed();
		PObj first2 = fixed[0];
		if( first2 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) first2;
			first2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( first2 instanceof PList ) {
			if( first2==ProvaListImpl.emptyRList )
				return false;
			Predicate pred = new ProvaPredicateImpl("",2,kb);
			PObj[] funs = ((PList) first2).getFixed();
			if( funs.length!=0 && funs[0] instanceof PList ) {
				PList funs2 = (PList) funs[0];
				PObj[] fixed3 = funs2.getFixed();
				if( fixed3.length==1 ) {
					if( !(fixed3[0] instanceof PList) )
						return false;
					funs = ((PList) fixed3[0]).getFixed();
				}
				if( fixed3.length==2 && fixed3[0] instanceof PList ) {
					PList first3 = (PList) fixed3[0];
					PObj[] funs3 = first3.getFixed();
					PObj arg = fixed3[1];
					PObj[] funs4 = new PObj[funs3.length];
					System.arraycopy(funs3,0,funs4,0,funs3.length-1);
					funs = funs4;
					int i = funs3.length-1;
					PObj fun = funs3[i];
					if( fun instanceof Constant ) {
						final PObj[] newFixed = new PObj[2];
						funs[i] = ProvaListImpl.create(newFixed);
						newFixed[0] = fun;
						newFixed[1] = arg;
					} else if( fun instanceof PList ) {
						PObj[] complex = ((PList) fun).getFixed();
						final PObj[] newFixed = new PObj[1+complex.length];
						funs[i] = ProvaListImpl.create(newFixed);
						System.arraycopy(complex,0,newFixed,0,complex.length);
						newFixed[complex.length] = arg;
					}
				}
			}
			List<Literal> body = new ArrayList<Literal>();
			PObj temp = null;
			for( int i=0; i<funs.length; i++ ) {
				PObj fun = funs[i];
				// Note that if the fixed part is only 1, the new query will be tail-only
				PObj[] newFixed = new PObj[fixed.length-1];
				System.arraycopy(fixed,1,newFixed,0,fixed.length-1);
				String symbol = null;
				if( fun instanceof VariableIndex ) {
					VariableIndex varPtr = (VariableIndex) fun;
					fun = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
				}
				if( fun instanceof Variable ) {
					return false;
				} else if( fun instanceof Constant ) {
					symbol = (String) ((Constant) fun).getObject();
					if( temp!=null )
						newFixed[0] = temp;
				} else if( fun instanceof PList ) {
					PObj[] complex = ((PList) fun).getFixed();
					symbol = (String) ((Constant) complex[0]).getObject();
					PObj[] attachTo = new PObj[complex.length-1];
					System.arraycopy(complex,1,attachTo,0,complex.length-1);
					PList attachToList = ProvaListImpl.create(attachTo,null);
					PObj attachTemp = ProvaVariableImpl.create();
					PObj[] attachFixed = null;
					if( temp==null ) {
						attachFixed = new PObj[] {attachToList,fixed[1],attachTemp};
					} else {
						attachFixed = new PObj[] {attachToList,temp,attachTemp};
					}
					temp = attachTemp;
					PList attachTerms = ProvaListImpl.create(attachFixed,null);
					body.add(kb.newLiteral("@attach", attachTerms));
					newFixed[0] = attachTemp;
				}
				if( i<funs.length-1 ) {
					temp = ProvaVariableImpl.create();
					newFixed[fixed.length-2] = temp;
				}
				PList newTerms = (PList) ProvaListImpl.create(newFixed,firstList.getTail());//.cloneWithVariables(variables);
				body.add(kb.newLiteral(symbol, newTerms));
			}
			PObj in = ProvaVariableImpl.create();
			PObj out = ProvaVariableImpl.create();
			PList ls = ProvaListImpl.create(new PObj[] {in,out} );
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = Rule.createVirtualRule(1, lit, body.toArray(new Literal[] {}));
			pred.addClause(clause);
			// Note that if the fixed part is only 1, the new query will be tail-only
			PObj[] newFixed = new PObj[fixed0.length-1];
			System.arraycopy(fixed0,1,newFixed,0,fixed0.length-1);
			PList newTerms = ProvaListImpl.create(newFixed,firstList0.getTail());
			Literal newLiteral = new ProvaLiteralImpl(pred,newTerms);
			newLiterals.add(newLiteral);
			return true;
		}
		if( !(first2 instanceof Constant) || !(((Constant) first2).getObject() instanceof String) )
			return false;
		String symbol = (String) ((Constant) first2).getObject();
		// Note that if the fixed part is only 1, the new query will be tail-only
		PObj[] newFixed = new PObj[fixed0.length-1];
		System.arraycopy(fixed0,1,newFixed,0,fixed0.length-1);
		PList newTerms = ProvaListImpl.create(newFixed,firstList.getTail());
		newLiterals.add(kb.newLiteral(symbol, newTerms));
		
		return true;
	}

}
