package ws.prova.reference2.builtins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import ws.prova.kernel2.cache.ProvaCacheState;
import ws.prova.kernel2.cache.ProvaCacheTablet;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;
import ws.prova.reference2.cache.ProvaCacheTabletImpl;
import ws.prova.reference2.cache.ProvaCachedLiteralImpl;
import ws.prova.reference2.cache.ProvaLocalAnswersImpl;
import ws.prova.reference2.cache.ProvaTabletKeyImpl;

public class ProvaCacheImpl extends ProvaBuiltinImpl {

	private final Map<ProvaTabletKeyImpl,ProvaCacheTablet> caches = new HashMap<ProvaTabletKeyImpl,ProvaCacheTablet>();

	public ProvaCacheImpl(KB kb) {
		super(kb,"cache");
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
		if( !(first instanceof PList) )
			return false;
		PList firstList = (PList) first;
		if( firstList==ProvaListImpl.emptyRList )
			return false;
		PObj[] fixed = firstList.getFixed();
		PObj first2 = fixed[0];
		if( !(first2 instanceof Constant) || !(((Constant) first2).getObject() instanceof String) )
			return false;
		final int arity = fixed.length-1;
		final String symbol = (String) ((Constant) first2).getObject();
//		int numFree = 0;
		int numBound = 0;
		// Where are the ground terms
		int mask = 0;
		for( int i=1; i<=arity; i++ ) {
			PObj o = fixed[i];
			mask <<= 1;
			if( o instanceof VariableIndex ) {
				VariableIndex ptr = (VariableIndex) o;
				Variable var = variables.get(ptr.getIndex());
				o = var.getAssigned();
			}
			if( o instanceof Constant ) {
				numBound++;
				mask |= 1;
			} else if( o instanceof Variable ) {
			} else {
				// TODO: Ignore cache instruction
			}
		}
		final Object[] data = new Object[numBound];
		for( int i=1, j=0; i<=arity; i++ ) {
			PObj o = fixed[i];
			if( o instanceof VariableIndex ) {
				VariableIndex ptr = (VariableIndex) o;
				Variable var = variables.get(ptr.getIndex());
				o = var.getAssigned();
			}
			if( o instanceof Constant )
				data[j++] = ((Constant) o).getObject();
		}
//		numFree = arity-numBound;
		ProvaTabletKeyImpl key = new ProvaTabletKeyImpl(symbol,arity,mask);
		ProvaCacheTablet cacheTablet = caches.get(key);
		if( cacheTablet==null ) {
			cacheTablet = new ProvaCacheTabletImpl(arity);
			caches.put(key, cacheTablet);
		}
		ProvaCacheState cacheState = cacheTablet.open(data);
		final ProvaLocalAnswersImpl localAnswers = new ProvaLocalAnswersImpl();
		if( cacheState.isOpen() ) {
			Collection<PList> answers = cacheState.getSolutions();
			int numAnswers = answers.size();
			Predicate pred = new ProvaPredicateImpl(symbol,arity,kb);
			if( numAnswers!=0 ) {
				// Use the available answers directly as alternatives for this subgoal
				for( PList answer : answers ) {
					PList ls = ProvaListImpl.create( answer.getFixed() );
					Literal lit = new ProvaLiteralImpl(pred,ls);
					Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
					pred.addClause(clause);
				}
			}
			final Goal lastGoal = cacheState.getGoal();
			if( !cacheState.isComplete() && !lastGoal.isCut() ) {
				Iterator<Rule> iter = lastGoal.getIterator();
				while( iter.hasNext() ) {
					final Rule next = iter.next();
					pred.addClause(next);
				}
			}
			if( pred.getClauses().numClauses() == 0 )
				return false;
			// Note that if the fixed part is only 1, the new query will be tail-only
			PObj[] newFixed = new PObj[arity];
			System.arraycopy(fixed,1,newFixed,0,fixed.length-1);
			PList newTerms = ProvaListImpl.create(newFixed,firstList.getTail());
			Literal newLiteral = new ProvaCachedLiteralImpl(pred,newTerms,cacheState,localAnswers);
			newLiterals.add(newLiteral);

			if( !lastGoal.isCut() ) {
				// Add an update_cache literal
				PList ltls = ProvaListImpl.create(fixed);
				PObj[] newFixed2 = new PObj[5];
				newFixed2[0] = ProvaConstantImpl.create(cacheState);
				newFixed2[1] = ltls;
				newFixed2[2] = ProvaConstantImpl.create(localAnswers);
				newFixed2[3] = newLiteral;
				newFixed2[4] = ProvaConstantImpl.create(node);
				PList newTerms2 = ProvaListImpl.create(newFixed2);
				newLiterals.add(kb.newLiteral("@update_cache", newTerms2));
			}
			
			// TODO: Use other unexplored branches as well
			return true;
		}
		cacheState.setOpen(true);

		// Note that if the fixed part is only 1, the new query will be tail-only
		PObj[] newFixed = new PObj[arity];
		System.arraycopy(fixed,1,newFixed,0,fixed.length-1);
		PList newTerms = ProvaListImpl.create(newFixed,firstList.getTail());
		final Literal newLiteral = kb.newCachedLiteral(symbol, newTerms, cacheState, localAnswers);
		newLiterals.add(newLiteral);

		// Add an update_cache literal
		PList ltls = ProvaListImpl.create(fixed);
		PObj[] newFixed2 = new PObj[5];
		newFixed2[0] = ProvaConstantImpl.create(cacheState);
		newFixed2[1] = ltls;
		newFixed2[2] = ProvaConstantImpl.create(localAnswers);
		newFixed2[3] = newLiteral;
		newFixed2[4] = ProvaConstantImpl.create(node);
		PList newTerms2 = ProvaListImpl.create(newFixed2);
		newLiterals.add(kb.newLiteral("@update_cache", newTerms2));
		return true;
	}

}
