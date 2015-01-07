package ws.prova.reference2.builtins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class ProvaCopyStreamImpl extends ProvaBuiltinImpl {

	final private static int sChunk = 8192;

	public ProvaCopyStreamImpl(KB kb) {
		super(kb, "copy_stream");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		PObj in = data[0];
		if (in instanceof VariableIndex) {
			VariableIndex varPtr = (VariableIndex) in;
			in = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		PObj out = data[1];
		if (out instanceof VariableIndex) {
			VariableIndex varPtr = (VariableIndex) out;
			out = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		InputStream is = (InputStream) ((Constant) in).getObject();
		OutputStream os = (OutputStream) ((Constant) out).getObject();
		runWithExceptions(is, os);
		return true;
	}

	private void process(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[sChunk];
		int length;
		while ((length = is.read(buffer, 0, sChunk)) != -1)
			os.write(buffer, 0, length);
	}

	private void runWithExceptions(InputStream is, OutputStream os) {
		IOException processException = null;
		try {
			process(is, os);
		} catch (IOException e) {
			processException = e;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					if (processException != null) {
						throw new RuntimeException(processException);
					} else {
						throw new RuntimeException(e);
					}
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					if (processException != null) {
						throw new RuntimeException(processException);
					} else {
						throw new RuntimeException(e);
					}
				}
			}
			if (processException != null) {
				throw new RuntimeException(processException);
			}
		}
	}

}
