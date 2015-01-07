package ws.prova.reference2.builtins;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
import ws.prova.reference2.ProvaMapImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.kernel2.Rule;

public class ProvaElementImpl extends ProvaBuiltinImpl {

	public ProvaElementImpl(KB kb) {
		super(kb,"element");
	}

	/**
	 * @TODO: All this assumes that collections or iterators contain only ProvaObject members
	 */
	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		int whereList = 1;
		if( data.length!=2 && data.length!=3 )
				return false;
		PObj specifiedIndex = null;
		if( data.length==3 ) {
			whereList = 2;
			specifiedIndex = data[0];
			if( specifiedIndex instanceof VariableIndex ) {
				VariableIndex varPtr = (VariableIndex) specifiedIndex;
				specifiedIndex = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			}
		}
		PObj lt = data[whereList-1];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			PObj o = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			if( !(o instanceof Variable) )
				lt = o;
		}
//		if( lt instanceof ProvaVariable ) {
////			 // TODO: this is nonsense
////			((ProvaVariable) lt).setAssigned(data[1]);
////			return true;
//			
//		}
		PObj rhs = data[whereList];
		if( rhs instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) rhs;
			rhs = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( rhs instanceof PList ) {
			PObj[] arrays = ((PList) rhs).getFixed();
			rhs = ProvaConstantImpl.create(Arrays.asList(arrays));
		}
		if( rhs instanceof Constant ) {
			Object rhsObject = ((Constant) rhs).getObject();
			if( rhsObject instanceof Iterator<?> ) {
				Iterator<?> rhsIterator = (Iterator<?>) rhsObject;
				Predicate pred = null;
				PList ltls = null;
				if( data.length==2 ) {
					pred = new ProvaPredicateImpl("",1,kb);
					while( rhsIterator.hasNext() ) {
						PObj o = getElement(rhsIterator.next());
						PList ls = ProvaListImpl.create(new PObj[] {o} );
						Literal lit = new ProvaLiteralImpl(pred,ls);
						Rule clause = Rule.createVirtualRule(1, lit, null);
						pred.addClause(clause);
					}
					ltls = ProvaListImpl.create(new PObj[] {lt} );
				} else if( specifiedIndex instanceof Variable ) {
					pred = new ProvaPredicateImpl("",2,kb);
					int index = 0;
					while( rhsIterator.hasNext() ) {
						PObj o = getElement(rhsIterator.next());
						PList ls = ProvaListImpl.create(new PObj[] {ProvaConstantImpl.create(index++), o} );
						Literal lit = new ProvaLiteralImpl(pred,ls);
						Rule clause = Rule.createVirtualRule(1, lit, null);
						pred.addClause(clause);
					}
					ltls = ProvaListImpl.create(new PObj[] {data[0],lt} );
				} else if( specifiedIndex instanceof Constant ) {
					pred = new ProvaPredicateImpl("",1,kb);
					Object o = ((Constant) specifiedIndex).getObject();
					if( !(o instanceof Integer) && !(o instanceof Long) )
						return false;
					Number index = (Number) o;
					if( index.intValue()<0 )
						return false;
					int i = 0;
					while( rhsIterator.hasNext() ) {
						if( i==index.intValue() ) {
							PObj obj = getElement(rhsIterator.next());
							PList ls = ProvaListImpl.create(new PObj[] {obj} );
							Literal lit = new ProvaLiteralImpl(pred,ls);
							Rule clause = Rule.createVirtualRule(1, lit, null);
							pred.addClause(clause);
							ltls = ProvaListImpl.create(new PObj[] {lt} );
							return true;
						}
					}
				}
				Literal newLiteral = new ProvaLiteralImpl(pred,ltls);
				newLiterals.add(newLiteral);
				return true;
			}
			if( !(rhsObject instanceof Collection<?>) )
				return false;
			Collection<?> rhsCollection = (Collection<?>) rhsObject;
			Predicate pred = null;
			PList ltls = null;
			if( data.length==2 ) {
				pred = new ProvaPredicateImpl("",1,kb);
				for( Object o : rhsCollection ) {
					PObj obj = getElement(o);
					PList ls = ProvaListImpl.create(new PObj[] {obj} );
					Literal lit = new ProvaLiteralImpl(pred,ls);
					Rule clause = Rule.createVirtualRule(1, lit, null);
					pred.addClause(clause);
				}
				ltls = ProvaListImpl.create(new PObj[] {lt} );
			} else if( specifiedIndex instanceof Variable ) {
				pred = new ProvaPredicateImpl("",2,kb);
				int index = 0;
				for( Object o : rhsCollection ) {
					PObj obj = getElement(o);
					PList ls = ProvaListImpl.create(new PObj[] {ProvaConstantImpl.create(index++), obj} );
					Literal lit = new ProvaLiteralImpl(pred,ls);
					Rule clause = Rule.createVirtualRule(1, lit, null);
					pred.addClause(clause);
				}
				ltls = ProvaListImpl.create(new PObj[] {data[0],lt} );
			} else if( specifiedIndex instanceof Constant ) {
				pred = new ProvaPredicateImpl("",1,kb);
				Object o = ((Constant) specifiedIndex).getObject();
				if( !(o instanceof Integer) && !(o instanceof Long) )
					return false;
				Number index = (Number) o;
				if( index.intValue()<0 || index.intValue()>=rhsCollection.size() )
					return false;
				Object element = rhsCollection.toArray()[index.intValue()];
				PObj obj = getElement(element);
//				if( element instanceof ProvaObject )
//					obj = (ProvaObject) element;
//				else
//					obj = new ProvaConstantImpl(element);
				PList ls = ProvaListImpl.create(new PObj[] {obj} );
				Literal lit = new ProvaLiteralImpl(pred,ls);
				Rule clause = Rule.createVirtualRule(1, lit, null);
				pred.addClause(clause);
				ltls = ProvaListImpl.create(new PObj[] {lt} );
			}
			Literal newLiteral = new ProvaLiteralImpl(pred,ltls);
			newLiterals.add(newLiteral);
			return true;
		}
		return false;
	}

	private PObj getElement(final Object element) {
		return ProvaMapImpl.wrap(element);
	}

}
