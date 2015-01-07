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
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaListImpl;

public class ProvaAttachImpl extends ProvaBuiltinImpl {

	public ProvaAttachImpl(KB kb) {
		super(kb,"attach");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=3 )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof PList) )
			return false;
		PObj a2 = data[2];
		if( a2 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a2;
			a2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(a2 instanceof Variable) )
			return false;
		PObj a1 = data[1];
		if( a1 instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) a1;
			a1 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		PList prefix = (PList) lt;
		PObj prefixTail = prefix.getTail();
		PObj[] prefixFixed = null;
		if( prefixTail!=null ) {
			if( prefixTail instanceof VariableIndex ) {
				VariableIndex varPtr = (VariableIndex) prefixTail;
				prefixTail = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			}
			if( prefixTail instanceof PList ) {
				prefixFixed = concat(prefix.getFixed(),((PList) prefixTail).getFixed());
			} else
				return false;
		} else
			prefixFixed = prefix.getFixed();
		PObj[] newFixed = null;
		boolean isMonad = false;
		if( a1 instanceof PList && a1!=ProvaListImpl.emptyRList ) {
			Object t = ((PList) a1).getFixed()[0];
			if( t instanceof Constant ) {
				t = ((Constant) t).getObject();
				isMonad = t.equals("state") || t.equals("list") || t.equals("maybe") || t.equals("tree") || t.equals("fact");
			}
		}
		if( a1 instanceof PList && !isMonad ) {
			PList suffix = (PList) a1;
			PObj[] suffixFixed = suffix.getFixed();
			PObj tail = suffix.getTail();
			if( tail!=null ) {
				tail = tail.cloneWithVariables(variables);
			}
			int len = prefixFixed.length+suffixFixed.length;
			if( tail instanceof PList )
				len += ((PList) tail).getFixed().length;
			newFixed = new PObj[len];
			System.arraycopy(prefixFixed,0,newFixed,0,prefixFixed.length);
			System.arraycopy(suffixFixed,0,newFixed,prefixFixed.length,suffixFixed.length);
			if( tail instanceof PList )
				System.arraycopy(((PList) tail).getFixed(),0,newFixed,prefixFixed.length+suffixFixed.length,((PList) tail).getFixed().length);
			if( tail instanceof Variable ) {
				PList newTerms = ProvaListImpl.create(newFixed, tail);
				((Variable) a2).setAssigned(newTerms);
				return true;
			}
		} else if( a1==ProvaListImpl.emptyRList ) {
			newFixed = prefixFixed;
		} else {
			newFixed = new PObj[prefixFixed.length+1];
			System.arraycopy(prefixFixed,0,newFixed,0,prefixFixed.length);
			newFixed[prefixFixed.length] = a1;
		}
		PList newTerms = ProvaListImpl.create(newFixed);
		((Variable) a2).setAssigned(newTerms);
		return true;
	}

	private PObj[] concat(PObj[] left, PObj[] right) { 
		PObj[] out= new PObj[left.length+right.length]; 
		   System.arraycopy(left, 0, out, 0, left.length); 
		   System.arraycopy(right, 0, out, left.length, right.length); 
		   return out; 
		} 
	
}
