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
import ws.prova.reference2.ProvaConstantImpl;

public class ProvaUnescapeImpl extends ProvaBuiltinImpl {

	public ProvaUnescapeImpl(KB kb) {
		super(kb,"unescape");
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
		PObj res = data[1];
		if( res instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) res;
			res = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(res instanceof Variable) && !(res instanceof Constant) )
			return false;
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof Constant) )
			return false;
		String in = ((Constant) lt).getObject().toString();
		
		if( res instanceof Variable )
			((Variable) res).setAssigned(ProvaConstantImpl.create(unescape(in)));
		else if( res instanceof Constant ) {
			return ((Constant) res).getObject().toString().equals(unescape(in));
		}
		return true;
	}

	@Override
	public int getArity() {
		return 2;
	}

	/**
	 * Translate escaped Java-standard string to a string with special characters
	 * embedded (for example, "\n" -> '\n').
	 * Unrecognised escapes are left as they are.
	 * Escape character is '\'.
	 *
	 * @param in escaped string
	 * @return unescaped string
	 */
	private String unescape(String in) {
		final String metachars = "\"btrn\'\\";
		final String chars = "\"\b\t\r\n\'\\";
		final char escape = '\\';
		StringBuffer out = new StringBuffer();

		int p = 0;
		int i;
		int len = in.length();
		while ((i = in.indexOf(escape, p)) != -1) {
			out.append(in.substring(p, i));
			if (i + 1 == len) {
				break;
			}
			char meta = in.charAt(i + 1);
			int k = metachars.indexOf(meta);
			if (k == -1) {
				out.append(escape);
				out.append(meta);
			} else {
				out.append(chars.charAt(k));
			}
			p = i + 2;
		}
		if (p < len) {
			out.append(in.substring(p));
		}
		return out.toString();
	}

}
