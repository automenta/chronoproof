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
import ws.prova.kernel2.cache.ProvaCacheState;
import ws.prova.kernel2.cache.ProvaCachedLiteral;
import ws.prova.kernel2.cache.Answers;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.cache.ProvaCacheStateImpl.ProvaCacheAnswerKey;

public class ProvaUpdateCacheImpl extends ProvaBuiltinImpl {

	public ProvaUpdateCacheImpl(KB kb) {
		super(kb,"@update_cache");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		if( terms.getFixed().length!=5 )
			return false;
		PObj first = terms.getFixed()[0];
		if( !(first instanceof Constant) )
			return false;
		ProvaCacheState cacheState = (ProvaCacheState) ((Constant) first).getObject();
		PList literalList = (PList) terms.getFixed()[1];
		if( literalList==ProvaListImpl.emptyRList )
			return false;
		// TODO: make sure the tail is dealt with properly
		PObj[] fixed = literalList.getFixed();
		PObj[] newFixed = new PObj[fixed.length-1];
		System.arraycopy(fixed,1,newFixed,0,fixed.length-1);
		PList newTerms = ProvaListImpl.create(newFixed,null);
		ProvaCacheAnswerKey cacheAnswerKey = cacheState.getCacheAnswerKey(newTerms, variables);
		boolean answerAdded = false;
		if( cacheAnswerKey!=null ) {
			Answers localAnswers = (Answers) ((Constant) terms.getFixed()[2]).getObject();
			boolean solutionAdded = localAnswers.addSolution(cacheAnswerKey, newTerms);
			if( !solutionAdded )
				return false;
			answerAdded = cacheState.addSolution(cacheAnswerKey, newTerms);
		}
		if( answerAdded ) {
			// Add the answer to all equivalent subgoals higher in the call stack (if they exist)
			// The associated goal
			Goal currentGoal = ((ProvaCachedLiteral) terms.getFixed()[3]).getGoal();
			List<Goal> goals = cacheState.getGoals();
			int i = 0;
			for( ; i<goals.size(); i++ ) {
				Goal g = goals.get(i);
				if( node.isCut() )
					g.setCut(true);
				if( g==currentGoal )
					break;
				g.addAnswer(newTerms);
			}
			while( ++i<goals.size() )
				goals.get(i).addOuterAnswer(newTerms);
		}
		return true;
	}

}
