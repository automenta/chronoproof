package ws.prova.reference2.builtins;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.beanutils.ConstructorUtils;
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
import ws.prova.reference2.ProvaGlobalConstantImpl;

public class ProvaConstructorImpl extends ProvaBuiltinImpl {

	public ProvaConstructorImpl(KB kb) {
		super(kb, "construct");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
		PObj[] data = terms.getFixed();
		if( data.length!=3 )
			return false;
		if( !(data[0] instanceof Constant) || !(data[2] instanceof PList) ) {
			return false;
		}
		PObj lt = data[1];
//		if( lt instanceof ProvaVariablePtr ) {
//			ProvaVariablePtr varPtr = (ProvaVariablePtr) lt;
//			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
//		}
		if( !(lt instanceof Variable) && !(lt instanceof Constant) ) {
			return false;
		}
		Constant classRef = (Constant) data[0];
		if( !(classRef.getObject() instanceof Class<?>) )
			return false;
		Class<?> targetClass = (Class<?>) classRef.getObject();
		PList argsList = (PList) data[2];
		List<Object> args = new ArrayList<Object>();
		// TODO: deal with the list tail
		for( PObj argObject : argsList.getFixed() ) {
			if( argObject instanceof VariableIndex ) {
				VariableIndex varPtr = (VariableIndex) argObject;
				argObject = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			}
			if( argObject instanceof Constant ) {
				args.add(((Constant) argObject).getObject());
			} else if( argObject instanceof PList ) {
				// Prova lists are converted to Java lists
				PList list = (PList) argObject;
				PObj[] os = list.getFixed();
				Object[] objs = new Object[os.length];
				for( int i=0; i<os.length; i++ ) {
					if( os[i] instanceof Constant )
						objs[i] = ((Constant) os[i]).getObject();
					else
						objs[i] = os[i];
				}
				args.add(Arrays.asList(objs));
			} else {
				args.add(argObject);
			}
		}
		try {
			final Object result = ConstructorUtils.invokeConstructor(targetClass, args.toArray());
			if( lt instanceof Variable )
				((Variable) lt).setAssigned(ProvaConstantImpl.create(result));
			else if( lt instanceof ProvaGlobalConstantImpl )
				((Constant) lt).setObject(result);
			else
				return ((Constant) lt).getObject().equals(result);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
