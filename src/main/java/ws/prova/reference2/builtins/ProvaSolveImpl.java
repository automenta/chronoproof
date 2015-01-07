package ws.prova.reference2.builtins;

import java.io.PrintWriter;
import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.exchange.ProvaSolution;
import ws.prova.exchange.impl.ProvaSolutionImpl;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Results;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaSolveImpl extends ProvaBuiltinImpl {

	public ProvaSolveImpl(KB kb) {
		super(kb,"solve");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		Results resultSet = null;
		ProvaSolution solution = null;
		int offset = 0;
		if( terms.getFixed().length!=0 && terms.getFixed()[0] instanceof Constant ) {
			offset = 1;
			resultSet = (Results) ((Constant) terms.getFixed()[0]).getObject();
			solution = new ProvaSolutionImpl();
		}
		StringBuilder sb = new StringBuilder();
		// Iterate over variable (name,value) pairs
		for( int i=offset; i<terms.getFixed().length; i++ ) {
			PList nv = (PList) terms.getFixed()[i];
			if( i!=offset )
				sb.append(", ");
			final String name = nv.getFixed()[0].toString().replaceAll("\'", "");
			sb.append(name);
			sb.append('=');
			Object value = nv.getFixed()[1];
			if( value instanceof VariableIndex ) {
				VariableIndex varPtr = (VariableIndex) value;
				value = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			}
			sb.append(value);
			if( solution!=null ) {
				solution.add(name,value);
			}
		}
		// This goes to whatever output is chosen
		PrintWriter writer = kb.getPrinter();
		if( sb.length()==0 )
			writer.println("yes");
		else
			writer.println(sb);
		if( resultSet!=null )
			resultSet.add(solution);
		return false;
	}

}
