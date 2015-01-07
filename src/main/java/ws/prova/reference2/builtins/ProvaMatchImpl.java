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
import ws.prova.kernel2.RuleSet;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaGoalImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;
import ws.prova.reference2.builtins.target.ProvaTarget;
import ws.prova.reference2.builtins.target.ProvaTargetImpl;

public class ProvaMatchImpl extends ProvaBuiltinImpl {

	private Literal targetLiteral;

	private Rule targetQuery;
	
	public ProvaMatchImpl(KB kb) {
		super(kb,"match");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=3 )
			return false;
		PObj handle = data[2];
		if( handle instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) handle;
			PObj o = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			handle = o;
		}
		PObj[] target = ((PList) data[0]).getFixed();
		String symbol = ((Constant) target[0]).getObject().toString();
		RuleSet clauses = kb.getPredicates(symbol,target.length-1);
		Goal targetGoal = null;
		ProvaTarget ptr = null;
		if( handle instanceof Variable ) {
			targetLiteral = kb.newLiteral(target);
			targetQuery = kb.newGoal(new Literal[] {targetLiteral},variables);
			targetGoal = new ProvaGoalImpl(targetQuery);
			ptr = ProvaTargetImpl.create(targetGoal); 
			((Variable) handle).setAssigned(ProvaConstantImpl.create(ptr));
		} else if( handle instanceof Constant ) {
			ptr = (ProvaTarget) ((Constant) handle).getObject();
			targetGoal = ptr.getTarget();
		}
		Unification unification = clauses.nextMatch(kb,targetGoal);
		if( unification== null )
			return false;
		Rule candidate = unification.getTarget();
		ptr.setCandidate(candidate);
		PObj lt = data[1];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			PObj o = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			lt = o;
		}
		Literal l = (Literal) candidate.getHead().cloneWithVariables(variables);
		PObj[] arr = l.getTerms().getFixed();
		PObj[] newArr = new PObj[1+arr.length];
		System.arraycopy(arr,0,newArr,1,arr.length);
		newArr[0] = ProvaConstantImpl.create(l.getPredicate().getSymbol());
		if( lt instanceof Variable ) {
			((Variable) lt).setAssigned(ProvaListImpl.create(newArr));
			return true;
		} else if( lt instanceof PList ) {
			Predicate pred = new ProvaPredicateImpl("",1,kb);
			Literal lit = new ProvaLiteralImpl(pred,ProvaListImpl.create(newArr));
			Rule clause = ProvaRuleImpl.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			Literal newLiteral = new ProvaLiteralImpl(pred,(PList) lt.cloneWithVariables(variables));
			newLiterals.add(newLiteral);
		} else
			return false;
		return true;
	}

}
