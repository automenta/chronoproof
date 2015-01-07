package ws.prova.reference2.builtins;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.MethodUtils;
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
import ws.prova.reference2.ProvaMapImpl;

public class ProvaJavaPredicateImpl extends ProvaBuiltinImpl {

	public ProvaJavaPredicateImpl(KB kb) {
		super(kb, "pcalc");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=3 )
			return false;
		PObj target = data[0];
		if( data[0] instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) data[0];
			target = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(target instanceof Constant) || !(data[1] instanceof Constant) || !(data[2] instanceof PList) ) {
			return false;
		}
		Object methodObject = ((Constant) data[1]).getObject();
		if( !(methodObject instanceof String) )
			return false;
		String method = (String) methodObject;
		PList argsList = (PList) data[2];
		List<Object> args = new ArrayList<Object>();
		// TODO: deal with the list tail
		for( PObj argObject : argsList.getFixed() ) {
			if( argObject instanceof VariableIndex ) {
				VariableIndex varPtr = (VariableIndex) argObject;
				argObject = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			}
			if( argObject instanceof ProvaMapImpl ) {
				final ProvaMapImpl map = (ProvaMapImpl) ((ProvaMapImpl) argObject).cloneWithVariables(variables);
				args.add( map.getObject() );
			} else if( argObject instanceof Constant ) {
				args.add(((Constant) argObject).getObject());
			} else {
				args.add(argObject);
			}
		}
		Object rc = null;
		try {
			Object targetObject = ((Constant) target).getObject();
			if( targetObject instanceof Class<?> )
				rc = MethodUtils.invokeStaticMethod((Class<?>) targetObject,method,args.toArray());
			else
				rc = MethodUtils.invokeMethod(((Constant) target).getObject(),method,args.toArray());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		return rc instanceof Boolean ? (Boolean) rc : true;
	}

}
