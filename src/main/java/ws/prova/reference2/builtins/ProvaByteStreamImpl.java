package ws.prova.reference2.builtins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
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

public class ProvaByteStreamImpl extends ProvaBuiltinImpl {

	public ProvaByteStreamImpl(KB kb) {
		super(kb,"byte_stream");
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
		PObj n_out = data[1];
		if( n_out instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) n_out;
			n_out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
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
		if( !(args[0] instanceof Constant) || !(args[1] instanceof Constant) )
			return false;
		Object oin = ((Constant) args[0]).getObject();
		String enc = ((Constant) args[1]).toString();
		if( oin instanceof String ) {
			if( !(n_out instanceof Variable) )
				return false;
			String in = ((Constant) args[0]).toString();
			byte[] input;
			try {
				input = in.getBytes(enc);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			((Variable) n_out).setAssigned(ProvaConstantImpl.create(new ByteArrayInputStream(input)));
		} else if( oin instanceof ByteArrayOutputStream ) {
			try {
				((ByteArrayOutputStream) oin).toString(enc);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else
			return false;
		return true;
	}

}
