package ws.prova.reference2.builtins;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class ProvaCaptureEnumImpl extends ProvaBuiltinImpl {

	public ProvaCaptureEnumImpl(KB kb) {
		super(kb,"capture_enum");
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
		if( !(lt instanceof PList) )
			return false;
		PObj[] args = ((PList) ((PList) lt).cloneWithVariables(variables)).getFixed();
		if( args.length!=2 )
			return false;
		String in = ((Constant) args[0]).toString();
		String regexp = ((Constant) args[1]).toString();
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(in);
		Predicate pred = new ProvaPredicateImpl("",1,kb);
		while (m.find()) {
			PObj[] newFixed = new PObj[m.groupCount()];
			for (int i = 1; i <= m.groupCount(); i++) {
				newFixed[i-1] = ProvaConstantImpl.create(m.group(i));
			}
			PList groups = ProvaListImpl.create( newFixed );
			PList ls = ProvaListImpl.create(new PObj[] {groups} );
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = Rule.createVirtualRule(1, lit, null);
			pred.addClause(clause);
		}		
		PList ltls = ProvaListImpl.create(new PObj[] {data[1]} );
		Literal newLiteral = new ProvaLiteralImpl(pred,ltls);
		newLiterals.add(newLiteral);
		return true;
	}

}
