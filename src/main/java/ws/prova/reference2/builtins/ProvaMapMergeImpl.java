package ws.prova.reference2.builtins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import ws.prova.kernel2.Rule;

public class ProvaMapMergeImpl extends ProvaBuiltinImpl {

	public ProvaMapMergeImpl(KB kb) {
		super(kb, "map_merge");
	}

	/**
	 * Merge two lists and produce a third. Fail if there are conflicting values
	 * for the same key in both input lists.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Reagent prova, Derivation node,
			Goal goal, List<Literal> newLiterals, Rule query) {
		Literal literal = goal.getGoal();
		List<Variable> variables = query.getVariables();
		PList terms = (PList) literal.getTerms();
		PObj[] data = terms.getFixed();
		if (data.length != 2)
			return false;
		PObj a1 = data[0];
		if (a1 instanceof VariableIndex) {
			VariableIndex varPtr = (VariableIndex) a1;
			a1 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		PList args = (PList) a1;
		PObj[] fixed = args.getFixed();
		PObj[] m = new PObj[3];
		if (fixed.length == 3) {
			// Signature map_merge MapIn KeysList ValuesList : MapOut
			for (int k = 0; k < 3; k++) {
				m[k] = fixed[k];
				if (m[k] instanceof VariableIndex) {
					VariableIndex varPtr = (VariableIndex) m[k];
					m[k] = variables.get(varPtr.getIndex())
							.getRecursivelyAssigned();
				}
			}
			if (!(m[0] instanceof ProvaMapImpl))
				return false;
			if (!(m[1] instanceof PList))
				return false;
			if (!(m[2] instanceof PList))
				return false;
			Map<String, PObj> in = (Map<String, PObj>) ((ProvaMapImpl) m[0]).getObject();
			final PList valuesList = (PList) m[2];
			final Set<String> inKeys = in.keySet(); 
			final PList keysList = (PList) m[1];
			final Set<String> keys = new HashSet<String>();
			keys.addAll(inKeys);
			final Map<String, PObj> supplied = new HashMap<String, PObj>();
			for( int i=0; i<keysList.getFixed().length; i++ ) {
				supplied.put(keysList.getFixed()[i].toString(), valuesList.getFixed()[i]);
			}
			keys.retainAll(supplied.keySet());
			if( keys.isEmpty() ) {
				final Map<String, PObj> md = new HashMap<String, PObj>();
				md.putAll(in);
				for (int n = 0; n < keysList.getFixed().length; n++) {
					PObj okey = keysList.getFixed()[n];
					if (!(okey instanceof Constant))
						return false;
					PObj ovalue = valuesList.getFixed()[n];
					md.put(okey.toString(), ovalue);
				}
				PObj a2 = data[1];
				if (a2 instanceof VariableIndex) {
					VariableIndex varPtr = (VariableIndex) a2;
					a2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
				}
				if (a2 instanceof Variable) {
					((Variable) a2).setAssigned(ProvaMapImpl.wrap(md));
					return true;
				}
				if (!(a2 instanceof ProvaMapImpl))
					return false;
				final Map<String, PObj> mc = (Map<String, PObj>) ((ProvaMapImpl) a2)
						.getObject();
				if (!mc.keySet().equals(md.keySet()))
					return false;
				// Set up proper unification for values
				final PObj[] vals1 = new PObj[mc.size()];
				final PObj[] vals2 = new PObj[mc.size()];
				int i = 0;
				for (String key : mc.keySet()) {
					vals1[i] = mc.get(key);
					vals2[i++] = md.get(key);
				}
				final Predicate pred = new ProvaPredicateImpl("", 1, kb);
				final Literal lit = new ProvaLiteralImpl(pred,
						ProvaListImpl.create(vals1));
				final Rule clause = Rule.createVirtualRule(1, lit,
						null);
				pred.addClause(clause);
				final Literal newLiteral = new ProvaLiteralImpl(pred,
						ProvaListImpl.create(vals2));
				newLiterals.add(newLiteral);
				return true;
			}
			// There are common keys in the set 'keys':
			// ensure they match in both sets
			final PObj[] vals1 = new PObj[keys.size()];
			final PObj[] vals2 = new PObj[keys.size()];
			final PObj[] keysArray = new PObj[keys.size()];
			int i = 0;
			for (String key : keys) {
				keysArray[i] = ProvaConstantImpl.wrap(key);
				PObj oa = in.get(key);
				vals1[i] = oa;
				PObj ob = supplied.get(key);
				vals2[i] = ob;
				i++;
			}
			final Predicate pred = new ProvaPredicateImpl("", 1, kb);
			final Literal lit = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(vals1));
			final Rule clause = Rule.createVirtualRule(1, lit, null);
			pred.addClause(clause);
			final PList vals2list = ProvaListImpl.create(vals2);
			Literal newLiteral = new ProvaLiteralImpl(pred, vals2list);
			newLiterals.add(newLiteral);

			// First part of the result
			final Map<String, PObj> md = new HashMap<String, PObj>();
			for (String key : in.keySet()) {
				if (!keys.contains(key))
					md.put(key, in.get(key));
			}
			for (String key : supplied.keySet()) {
				if (!keys.contains(key))
					md.put(key, supplied.get(key));
			}
			PList newArray = ProvaListImpl.create(new PObj[] {
					ProvaMapImpl.wrap(md), ProvaListImpl.create(keysArray),
					vals2list });
			PList newArgs = ProvaListImpl.create(new PObj[] { newArray,
					data[1] });
			newLiteral = kb.newLiteral("map_merge", newArgs);
			newLiterals.add(newLiteral);
			return true;
		}
		if (fixed.length != 2)
			return false;
		PObj m1 = fixed[0];
		if (m1 instanceof VariableIndex) {
			VariableIndex varPtr = (VariableIndex) m1;
			m1 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if (!(m1 instanceof ProvaMapImpl))
			return false;
		PObj m2 = fixed[1];
		if (m2 instanceof VariableIndex) {
			VariableIndex varPtr = (VariableIndex) m2;
			m2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		if (!(m2 instanceof ProvaMapImpl))
			return false;
		PObj a2 = data[1];
		if (a2 instanceof VariableIndex) {
			VariableIndex varPtr = (VariableIndex) a2;
			a2 = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
		}
		final Map<String, PObj> ma = (Map<String, PObj>) ((ProvaMapImpl) m1)
				.getObject();
		final Map<String, PObj> mb = (Map<String, PObj>) ((ProvaMapImpl) m2)
				.getObject();
		final Set<String> keys = new HashSet<String>();
		keys.addAll(ma.keySet());
		final Set<String> keysb = mb.keySet();
		keys.retainAll(keysb);
		if (keys.isEmpty()) {
			// There are no common keys between the two input maps
			final Map<String, PObj> md = new HashMap<String, PObj>();
			md.putAll(ma);
			md.putAll(mb);
			if (a2 instanceof Variable) {
				((Variable) a2).setAssigned(ProvaMapImpl.wrap(md));
				return true;
			}
			if (!(a2 instanceof ProvaMapImpl))
				return false;
			final Map<String, PObj> mc = (Map<String, PObj>) ((ProvaMapImpl) a2)
					.getObject();
			if (!mc.keySet().equals(md.keySet()))
				return false;
			// Set up proper unification for values
			final PObj[] vals1 = new PObj[mc.size()];
			final PObj[] vals2 = new PObj[mc.size()];
			int i = 0;
			for (String key : mc.keySet()) {
				vals1[i] = mc.get(key);
				vals2[i++] = md.get(key);
			}
			final Predicate pred = new ProvaPredicateImpl("", 1, kb);
			final Literal lit = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(vals1));
			final Rule clause = Rule.createVirtualRule(1, lit,
					null);
			pred.addClause(clause);
			final Literal newLiteral = new ProvaLiteralImpl(pred,
					ProvaListImpl.create(vals2));
			newLiterals.add(newLiteral);
			return true;
		}

		// There are common keys in the set 'keys':
		// ensure they match in both sets
		final PObj[] vals1 = new PObj[keys.size()];
		final PObj[] vals2 = new PObj[keys.size()];
		final PObj[] keysArray = new PObj[keys.size()];
		int i = 0;
		for (String key : keys) {
			keysArray[i] = ProvaConstantImpl.wrap(key);
			PObj oa = ma.get(key);
			vals1[i] = oa;
			PObj ob = mb.get(key);
			vals2[i++] = ob;
		}
		final Predicate pred = new ProvaPredicateImpl("", 1, kb);
		final Literal lit = new ProvaLiteralImpl(pred,
				ProvaListImpl.create(vals1));
		final Rule clause = Rule.createVirtualRule(1, lit, null);
		pred.addClause(clause);
		final PList vals2list = ProvaListImpl.create(vals2);
		Literal newLiteral = new ProvaLiteralImpl(pred, vals2list);
		newLiterals.add(newLiteral);

		// First part of the result
		final Map<String, PObj> md = new HashMap<String, PObj>();
		for (String key : ma.keySet()) {
			if (!keys.contains(key))
				md.put(key, ma.get(key));
		}
		for (String key : mb.keySet()) {
			if (!keys.contains(key))
				md.put(key, mb.get(key));
		}
		PList newArray = ProvaListImpl.create(new PObj[] {
				ProvaMapImpl.wrap(md), ProvaListImpl.create(keysArray),
				vals2list });
		PList newArgs = ProvaListImpl.create(new PObj[] { newArray,
				data[1] });
		newLiteral = kb.newLiteral("map_merge", newArgs);
		newLiterals.add(newLiteral);
		return true;
	}

}
