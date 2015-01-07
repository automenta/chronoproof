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
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaMapImpl;
import ws.prova.reference2.ProvaPredicateImpl;
import ws.prova.reference2.ProvaRuleImpl;

public class ProvaJavaFunctionImpl extends ProvaBuiltinImpl {

	public ProvaJavaFunctionImpl(KB kb) {
		super(kb, "fcalc");
	}

	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PObj[] data = terms.getFixed();
		if( data.length!=5 )
				return false;
		if( !(data[1] instanceof Constant) || !(data[3] instanceof Constant) || !(data[4] instanceof PList)) {
			return false;
		}
		PObj lt = data[0];
		if( lt instanceof VariableIndex ) {
			VariableIndex varPtr = (VariableIndex) lt;
			lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if( !(lt instanceof Variable) && !(lt instanceof Constant)) {
			return false;
		}
		String type = (String) ((Constant) data[1]).getObject();
		PList argsList = (PList) data[4];
		// TODO: deal with the list tail
		String method = (String) ((Constant) data[3]).getObject();
		List<Object> args = new ArrayList<Object>();
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
		Object ret = null;
		if( type.equals("s") ) {
			// A static call
			Constant classRef = (Constant) data[2];
			if( !(classRef.getObject() instanceof Class<?>) )
				return false;
			Class<?> targetClass = (Class<?>) classRef.getObject();
			try {
				ret = MethodUtils.invokeStaticMethod(targetClass,method,args.toArray());
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
		} else {
			// An instance call
			PObj target = data[2];
			if( target instanceof VariableIndex ) {
				VariableIndex varPtr = (VariableIndex) target;
				target = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
			}
			if( target instanceof Constant ) {
				Object oTarget = ((Constant) target).getObject();
				try {
					ret = MethodUtils.invokeMethod(oTarget,method,args.toArray());
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getCause());
				}
			}
		}
		if( lt instanceof Variable ) {
			((Variable) lt).setAssigned(ret instanceof PObj ? (PObj) ret : ProvaConstantImpl.create(ret) );
		} else if( lt instanceof Constant ) {
			if( ret instanceof PObj ) {
				// Send this to the unification
				final Predicate pred = new ProvaPredicateImpl("", 1, kb);
				final Literal lit = new ProvaLiteralImpl(pred,
						ProvaListImpl.create(new PObj[] {(PObj) ret}));
				final Rule clause = ProvaRuleImpl.createVirtualRule(1, lit,
						null);
				pred.addClause(clause);
				final Literal newLiteral = new ProvaLiteralImpl(pred,
						ProvaListImpl.create(new PObj[] {lt}));
				newLiterals.add(newLiteral);
				return true;
			}
			return ((Constant) lt).getObject().equals(ret);
		} else if( lt instanceof PList ) {
			if( !(ret instanceof PList) )
				return false;
			// Send this to the unification
			final Predicate pred = new ProvaPredicateImpl("", 1, kb);
			final Literal lit = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(new PObj[] {(PObj) ret}));
			final Rule clause = ProvaRuleImpl.createVirtualRule(1, lit,
					null);
			pred.addClause(clause);
			final Literal newLiteral = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(new PObj[] {lt}));
			newLiterals.add(newLiteral);
			return true;
		} else
			return false;
		return true;
	}

}
