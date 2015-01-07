package ws.prova.reference2.builtins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
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

public class ProvaFopenImpl extends ProvaBuiltinImpl {

	public ProvaFopenImpl(KB kb) {
		super(kb,"fopen");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		String filename = ((Constant) lt).getObject().toString();
		BufferedReader in = null;
		File file = new File(filename);

		if (!file.exists() || !file.canRead()) {
			try {
				in = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream(filename) ) );
			} catch( Exception e ) {
				throw new RuntimeException(e);
			}
		} else {
			FileReader fr;
			try {
				fr = new FileReader(file);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			in = new BufferedReader(fr);
		}
		PObj res = data[1];
		if( res instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) res;
			res = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		((Variable) res).setAssigned(ProvaConstantImpl.create(in));
		return true;
	}

}
