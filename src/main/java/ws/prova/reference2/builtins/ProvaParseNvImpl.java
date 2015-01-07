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

public class ProvaParseNvImpl extends ProvaBuiltinImpl {

	public ProvaParseNvImpl(KB kb) {
		super(kb,"parse_nv");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=2 || !(data[0] instanceof PList) || !(data[1] instanceof PList) )
			return false;
		PObj[] out = ((PList) ((PList) data[1]).cloneWithVariables(variables)).getFixed();
		if( out.length!=2 )
			return false;
		PObj n_out = out[0];
		if( n_out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) n_out;
			n_out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(n_out instanceof Variable) && !(n_out instanceof PList))
			return false;
		PObj v_out = out[1];
		if( v_out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) v_out;
			v_out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(v_out instanceof Variable) && !(v_out instanceof PList))
			return false;
		PObj[] args = ((PList) ((PList) data[0]).cloneWithVariables(variables)).getFixed();
		if( args.length!=2 )
			return false;
		PObj lt = args[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof Constant) )
			return false;
		String in = ((Constant) lt).toString();
		PObj exp = args[1];
		if( exp instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) exp;
			exp = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		String regexp = ((Constant) exp).toString();
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(in);
		PObj arr_n[] = new PObj[m.groupCount()];
		PObj arr_v[] = new PObj[m.groupCount()];
		int i = 0;
		while (m.find()) {
			arr_n[i] = ProvaConstantImpl.create(m.group(1));
			arr_v[i++] = ProvaConstantImpl.create(m.group(2));
		}
		PList n = ProvaListImpl.create( arr_n );
		PList v = ProvaListImpl.create( arr_v );
		if( n_out instanceof PList || v_out instanceof PList ) {
			// Make sure the unification is done between the result and the original subgoal
			Predicate pred = new ProvaPredicateImpl("",2,kb);
			PList ls = ProvaListImpl.create(new PObj[] {
							data[0],
							ProvaListImpl.create(new PObj[]{n,v})} );
			Literal lit = new ProvaLiteralImpl(pred,ls);
			Rule clause = Rule.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			Literal newLiteral = new ProvaLiteralImpl(pred,terms);
			newLiterals.add(newLiteral);
			return true;
		}
		((Variable) n_out).setAssigned(n);
		((Variable) v_out).setAssigned(v);
		return true;
	}

}
