package ws.prova.reference2;

import com.sun.xml.internal.xsom.impl.scd.Iterators;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaGoalImpl implements Goal {

	@SuppressWarnings("unused")
	private final static Logger log = Logger.getLogger("prova");
	
	private Rule query;
	
	private Literal goal;
	
	private Predicate predicate;
	
	private Iterator<Rule> iterator;
	
	private final List<Variable> variables;
	
	private boolean cut = false;
	
	private List<PList> extraAnswers;
	
	private List<PList> outerAnswers;

	private boolean singleClause;
	
	private Rule rule;

	private List<PList> meta;
	
	private Rule lastMatch;

	public ProvaGoalImpl(Rule query) {
		this.query = query;
		this.variables = query.getVariables();
		this.goal = query.getTop();
		this.goal.setGoal(this);
		this.predicate = goal.getPredicate();
//		for( int i=0; i<query.getOffset(); i++ )
//			this.iterator.next();
	}

	public ProvaGoalImpl(List<Variable> variables, Literal goal) {
		this.variables = variables;
		this.goal = goal;
		this.predicate = goal.getPredicate();
		this.iterator = predicate.getClauses().getClauses().iterator();
		for( int i=0; i<query.getOffset(); i++ )
			this.iterator.next();
	}

	@Override
	public void update() {
		this.goal = query.getTop();
		this.predicate = goal.getPredicate();
	}
	
	/*
	 * Creates the iterator on a copy of the iterated collection of clauses
	 */
	private void createIterator() {
		PObj[] fixed = this.goal.getTerms().getFixed();
		if( fixed.length!=0 ) {
			PObj firstObject = fixed[0];
			if( firstObject instanceof VariableIndex ) {
				firstObject = variables.get(((VariableIndex) firstObject).getIndex());
				firstObject = firstObject.getRecursivelyAssigned();
			}
			if( firstObject instanceof Constant && !(firstObject instanceof ProvaMapImpl ) ) {
				Object o = ((Constant) firstObject).getObject();
				final List<Rule> keyClauses = predicate.getClauses().getClauses(o, fixed);
				if( keyClauses!=null ) {
					if( keyClauses.size()==1 ) {
						this.singleClause = true;
					}
					final List<Rule> tempClauses = new ArrayList<Rule>(keyClauses);
					this.iterator = tempClauses.iterator();
				}
				return;
			}
		}
		final List<Rule> clauses = predicate.getClauses().getClauses();
                this.iterator = clauses.iterator();
		if( predicate.getClauses().numClauses() ==1 ) {
//			if( !predicate.getKnowledgeBase().isCachePredicate(predicate.getSymbol()) )
			this.singleClause = true;
			return;
		}
                		
//		final List<ProvaRule> tempClauses = new ArrayList<ProvaRule>(clauses);
//		this.iterator = tempClauses.iterator();
	}

	@Override
	public void updateMetadataGoal() {
		PObj[] fixed = goal.getTerms().getFixed();
		String symbol = (String) ((Constant) fixed[0]).getObject();
		PList terms = (PList) fixed[1];
		Map<String,List<Object>> m = goal.getMetadata();
		meta = new ArrayList<PList>();
		for( int i=2; i<fixed.length; i++ ) {
			meta.add((PList) fixed[i]);
		}
		goal = predicate.kb().newLiteral(symbol, terms, goal.getGuard());
		goal.addMetadata(m);
		query.getBody()[query.getOffset()] = goal;
		predicate = goal.getPredicate();
	}
	
	@Override
	public Rule next() {
		if( extraAnswers!=null ) {
			Predicate pred = new ProvaPredicateImpl(predicate.getSymbol(),predicate.getArity(),predicate.kb());
			for( PList answer : extraAnswers ) {
				PList ls = ProvaListImpl.create( answer.getFixed() );
				Literal lit = new ProvaLiteralImpl(pred,ls);
				Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
				pred.addClause(clause);
			}

                        
                        iterator = pred.getClauses().iterator();
                        
//			query.removeAt(1);
			extraAnswers = null;
		} else if( iterator==null ) {
			createIterator();
		}
		try {
			final Rule nextRule = (iterator!=null && iterator.hasNext()) ? iterator.next() : null;
			if( nextRule!=null && !iterator.hasNext() )
				singleClause = true;
			return nextRule;
		} catch( ConcurrentModificationException ignored ) {
			// TODO: Double-check this: this happens when a temporal reaction rule is removed
		}
		return null;
	}
	
	@Override
	public Unification nextUnification(KB kb) {
		if( cut )
			return null;
		rule = null;
		this.singleClause = false;
//		synchronized(kb) {
			rule = next();
			while( rule!=null && rule.isRemoved() )
				rule = next();
			if( rule==null ) {
				if( outerAnswers!=null ) {
					Predicate pred = new ProvaPredicateImpl(predicate.getSymbol(),predicate.getArity(),predicate.kb());
					for( PList answer : outerAnswers ) {
						PList ls = ProvaListImpl.create( answer.getFixed() );
						Literal lit = new ProvaLiteralImpl(pred,ls);
						Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
						pred.addClause(clause);
					}
                                        if (pred.getClauses().numClauses() == 0)
                                            iterator = Iterators.empty();
                                        else
                                            iterator = pred.getClauses().getClauses().iterator();
					outerAnswers.clear();
					rule = iterator.hasNext() ? iterator.next() : null;
				}
				if( rule==null ) {
					goal.markCompletion();
					return null;
				}
			}
			Rule clone = query.cloneRule(!singleClause);
			final ProvaUnificationImpl unification = new ProvaUnificationImpl(
					clone,
					rule);
			unification.setMeta(meta);
			return unification;
//		}
	}
	
	@Override
	public Literal getGoal() {
		return goal;
	}
	
	@Override
	public Rule getQuery() {
		return query;
	}
	
	public Predicate getPredicate() {
		return predicate;
	}

	public void setIterator(Iterator<Rule> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Iterator<Rule> getIterator() {
		return iterator;
	}

//	public void setVariables(List<ProvaVariable> variables) {
//		this.variables = variables;
//	}

	public List<Variable> getVariables() {
		return variables;
	}

	@Override
	public void setCut(boolean cut) {
		this.cut = cut;
	}

	@Override
	public boolean isCut() {
		return cut;
	}

	@Override
	public void setGoal(Literal goal) {
		this.goal = goal;
	}

	@Override
	public boolean hasNext() {
		return iterator!=null && iterator.hasNext();
	}

	@Override
	public void addAnswer(PList terms) {
		if( extraAnswers==null )
			extraAnswers = new ArrayList<PList>();
		extraAnswers.add(terms);
	}

	@Override
	public void addOuterAnswer(PList terms) {
		if( outerAnswers==null )
			outerAnswers = new ArrayList<PList>();
		outerAnswers.add(terms);
	}

	@Override
	public boolean isSingleClause() {
		return singleClause || (rule!=null && rule.isCut());
	}

	@Override
	public void removeTarget() {
		rule.setRemoved();
		iterator.remove();
	}

	@Override
	public Object lookupMetadata(String reference, List<Variable> variables) {
		if( meta==null )
			return reference;
		for( PList p : meta ) {
			PList pair = (PList) p.cloneWithVariables(variables);
			String name = pair.getFixed()[0].toString();
			if( name.equals(reference) ) {
				final PObj data = pair.getFixed()[1];
				return data instanceof Constant ? ((Constant) data).getObject() : ProvaVariableImpl.create(name, data);
			}
		}
		return reference;
	}

	@Override
	public void setLastMatch(Rule lastMatch) {
		this.lastMatch = lastMatch;
	}

	@Override
	public Rule getLastMatch() {
		return lastMatch;
	}

	@Override
	public void updateGround() {
		goal.updateGround( variables );
	}

}
