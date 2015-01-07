package ws.prova.reference2.builtins;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.exchange.ProvaSolution;
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

public class ProvaConsultImpl extends ProvaBuiltinImpl {

	public ProvaConsultImpl(KB kb) {
		super(kb, "consult");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=1 )
				return false;
		PObj source = data[0];
		if( data[0] instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) data[0];
			source = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(source instanceof Constant) ) {
			return false;
		}
		Object rules = ((Constant) source).getObject();
		@SuppressWarnings("unused")
		List<ProvaSolution[]> resultSets = null;
		try {
			if( rules instanceof String ) {
				resultSets = kb.consultSyncInternal(prova,(String) rules,(String) rules,null);
			} else if( rules instanceof BufferedReader ) {
				resultSets = kb.consultSyncInternal(prova, (BufferedReader) rules,"-1",null);
			} else if( rules instanceof StringBuffer ) {
				StringReader sr = new StringReader(((StringBuffer) rules).toString());
				BufferedReader in = new BufferedReader(sr);
				resultSets = kb.consultSyncInternal(prova, in,"-1",null);
			}
			return true;
		} catch( RuntimeException e ) {
			throw e;
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}

}
