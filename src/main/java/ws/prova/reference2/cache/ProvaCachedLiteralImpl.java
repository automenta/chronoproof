package ws.prova.reference2.cache;

import java.util.List;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.cache.ProvaCacheState;
import ws.prova.kernel2.cache.ProvaCachedLiteral;
import ws.prova.kernel2.cache.Answers;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaVariableImpl;

public class ProvaCachedLiteralImpl extends ProvaLiteralImpl implements ProvaCachedLiteral {

	private static final long serialVersionUID = -4320866097994244461L;

	private Goal goal;
	
	private final ProvaCacheState cacheState;

	private final Answers answers;
	
	public ProvaCachedLiteralImpl(Predicate predicate, PList terms,
			ProvaCacheState cacheState, Answers answers) {
		super(predicate,terms);
		this.cacheState = cacheState;
		this.answers = answers;
	}

	@Override
	public Literal rebuildSource(Unification unification) {
		if( ground || terms==null )
			return this;
		PList newTerms = terms.rebuildSource(unification);
		ProvaCachedLiteralImpl cachedLit = new ProvaCachedLiteralImpl(predicate, newTerms, cacheState, answers);
		cachedLit.setGoal(goal);
		return cachedLit;
	}

        @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(predicate.getSymbol());
		sb.append('(');
		sb.append(terms);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public PObj cloneWithVariables(List<Variable> variables) {
		if( terms==null )
			return this;
		if( predicate.getSymbol().equals("cut") ) {
			Variable any1 = ProvaVariableImpl.create();
			PList lany1 = ProvaListImpl.create(new PObj[] {any1});
			return new ProvaLiteralImpl(predicate,lany1);
		}
		PList newTerms = (PList) terms.cloneWithVariables(variables);
		ProvaCachedLiteralImpl newLit = new ProvaCachedLiteralImpl(predicate,newTerms,cacheState,answers);
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		newLit.goal = goal;
		return newLit;
	}

	@Override
	public void setGoal(Goal goal) {
		this.goal = goal;
		if( cacheState!=null ) {
			cacheState.addGoal(goal);
		}
	}

	@Override
	public Goal getGoal() {
		return this.goal;
	}
	
	@Override
	public ProvaCacheState getCacheState() {
		return cacheState;
	}

	@Override
	public Answers getAnswers() {
		return answers;
	}

	@Override
	public void markCompletion() {
		cacheState.markCompletion();
	}

}
