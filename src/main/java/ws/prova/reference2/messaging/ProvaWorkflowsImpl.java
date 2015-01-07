package ws.prova.reference2.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.messaging.ProvaWorkflows;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaMapImpl;
import ws.prova.kernel2.Rule;
import ws.prova.reference2.ProvaUnificationImpl;

public class ProvaWorkflowsImpl implements ProvaWorkflows {

	private final static Logger log = Logger.getLogger("prova");

	private final KB kb;

	private final ConcurrentMap<String,List<List<PObj>>> join_record = new ConcurrentHashMap<String, List<List<PObj>>>();

	private final ConcurrentMap<String,Object[]> predicate_join_record = new ConcurrentHashMap<String,Object[]>();

	private final ConcurrentMap<String,ReentrantLock> predicate_join_locks = new ConcurrentHashMap<String,ReentrantLock>();

	public ProvaWorkflowsImpl(KB kb) {
		this.kb = kb;
	}

	@Override
	public boolean init_join(Literal literal,
			List<Literal> newLiterals, Rule query) {
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PList termsCopy = (PList) terms.cloneWithVariables(variables);
		PObj[] data = termsCopy.getFixed();
		if( !(data[0] instanceof Constant) || !(data[1] instanceof Constant) )
			return false;
		// Key is XID+JoinID
		String key = ((Constant) data[0]).getObject().toString() + ((Constant) data[1]).getObject().toString(); 
		if( join_record.containsKey(key) )
			return false;
		PObj[] expectedList = null;
		if( data[2].getClass()==ProvaConstantImpl.class && ((Constant) data[2]).getObject() instanceof List ) {
			final List list = (List) ((Constant) data[2]).getObject();
			final List<PObj> wrappedList = new ArrayList<PObj>();
			for( Object o : list ) {
				wrappedList.add( ProvaMapImpl.wrap(o));
			}
			expectedList = (PObj[]) wrappedList.toArray(new PObj[wrappedList.size()]);
		} else {
			if( !(data[2] instanceof PList) )
				return false;
			expectedList = ((PList) data[2]).getFixed();
		}
		List<PObj> waiting = new ArrayList<PObj>();
                waiting.addAll(Arrays.asList(expectedList));
		List<PObj> complete = new ArrayList<PObj>();
		List<List<PObj>> record = new ArrayList<List<PObj>>();
		record.add(waiting);
		record.add(complete);
		join_record.put(key,record);
		return true;
	}

	@Override
	public boolean join_test(Literal literal,
			List<Literal> newLiterals, Rule query) {
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PList termsCopy = (PList) terms.cloneWithVariables(variables);
		PObj[] data = termsCopy.getFixed();
		if( !(data[1] instanceof Constant) || !(data[2] instanceof Constant) 
				|| !(data[4] instanceof Variable))
			return false;
		// Key is XID+JoinID
		String key = ((Constant) data[1]).getObject().toString() + ((Constant) data[2]).getObject().toString(); 
		List<List<PObj>> waitingAndComplete = join_record.get(key);
		if( waitingAndComplete==null )
			return false;
		Literal goalLit = kb.newLiteral("pred1",ProvaListImpl.create(new PObj[] {data[3]}));
		Rule goal = kb.newGoal(new Literal[] {goalLit});
		for (ListIterator<PObj> iter = waitingAndComplete.get(0).listIterator(); iter.hasNext();) {
			PObj t = iter.next().cloneWithVariables(variables);
			Literal lit = kb.newLiteral("pred1",ProvaListImpl.create(new PObj[] {t}));
			Rule rule = Rule.createVirtualRule(1, lit, null);
			ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
			boolean result = unification.unify();
			if( !result )
				continue;
			iter.remove();
			waitingAndComplete.get(1).add(data[3]);
			if (waitingAndComplete.get(0).isEmpty()) {
				join_record.remove(key);
				((Variable) data[4]).setAssigned(ProvaConstantImpl.create(waitingAndComplete.get(1)));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean init_predicate_join(Literal literal,
			List<Literal> newLiterals, Rule query) {
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PList termsCopy = (PList) terms.cloneWithVariables(variables);
		PObj[] data = termsCopy.getFixed();
		if( !(data[0] instanceof Constant) || !(data[1] instanceof Constant) 
				|| !(data[3] instanceof Constant) || !(data[2] instanceof PList) )
			return false;
		// Key is XID+JoinID
		String key = ((Constant) data[0]).getObject().toString() + ((Constant) data[1]).getObject().toString(); 
		if( predicate_join_record.containsKey(key) )
			return false;
		PObj[] expectedList = ((PList) data[2]).getFixed();
		List<PObj> waiting = new ArrayList<PObj>();
		for( PObj expected : expectedList ) {
			if( !(expected instanceof PList) )
				return false;
			waiting.add(expected);
		}
		List<PObj> complete = new ArrayList<PObj>();
		predicate_join_record.put(key,new Object[] {((Constant) data[3]).getObject(),waiting,complete});
		return true;
	}

	@Override
	public boolean stop_predicate_join(Literal literal,
			List<Literal> newLiterals, Rule query) {
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PList termsCopy = (PList) terms.cloneWithVariables(variables);
		PObj[] data = termsCopy.getFixed();
		if( !(data[0] instanceof Constant) || !(data[1] instanceof Constant) )
			return false;

		// Key is XID+JoinID
		String key = ((Constant) data[0]).getObject().toString() + ((Constant) data[1]).getObject().toString();
		predicate_join_record.remove(key);
		ReentrantLock lock = (ReentrantLock) predicate_join_locks.get(key);
		try {
			lock.unlock();
			predicate_join_locks.remove(key);
		} catch (Exception ignored) {
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean predicate_join_test(Literal literal,
			List<Literal> newLiterals, Rule query) {
		boolean rc = false;
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PList termsCopy = (PList) terms.cloneWithVariables(variables);
		PObj[] data = termsCopy.getFixed();
		if( !(data[0] instanceof Constant) || !(data[1] instanceof Constant) 
				|| !(data[2] instanceof PList) || !(data[3] instanceof PList))
			return false;
		// Key is XID+JoinID
		String key = ((Constant) data[0]).getObject().toString() + ((Constant) data[1]).getObject().toString(); 
		ReentrantLock lock = null;
		synchronized (predicate_join_locks ) {
			lock = (ReentrantLock) predicate_join_locks.get(key);
			if (lock == null) {
				lock = new ReentrantLock();
				predicate_join_locks.put(key, lock);
			}
			lock.lock();
		}
		try {
			Object[] value = (Object[]) predicate_join_record.get(key);
			String joinPredicate = (String) value[0];
			List<PList> complete = (List<PList>) value[2];
			if (value[1] instanceof Long) {
				// TODO: timeout processing
			} else {
				Literal goalLit = kb.newLiteral("pred1",(PList) data[2]);
				Rule goal = kb.newGoal(new Literal[] {goalLit});
				List<PList> waiting = (List<PList>) value[1];
				for (ListIterator<PList> iter = waiting.listIterator(); iter.hasNext();) {
					PList t = (PList) iter.next().cloneWithVariables(variables);
					Literal lit = kb.newLiteral("pred1",t);
					Rule rule = Rule.createVirtualRule(1, lit, null);
					ProvaUnificationImpl unification = new ProvaUnificationImpl(goal, rule);
					boolean result = unification.unify();
					if( !result )
						continue;
					iter.remove();
					complete.add((PList) data[2]);
					((Variable) data[4]).setAssigned(ProvaConstantImpl.create(joinPredicate));
					((Variable) data[5]).setAssigned(ProvaConstantImpl.create(waiting));
					((Variable) data[6]).setAssigned(ProvaConstantImpl.create(complete.size()));
					((Variable) data[7]).setAssigned(ProvaConstantImpl.create(complete));
					rc = true;
					break;
				}
			}
		} finally {
			if (!rc && lock != null)
				lock.unlock();
		}
		return rc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean predicate_join_exit(Literal literal,
			List<Literal> newLiterals, Rule query) {
		List<Variable> variables = query.getVariables();
		PList terms = literal.getTerms();
		PList termsCopy = (PList) terms.cloneWithVariables(variables);
		PObj[] data = termsCopy.getFixed();
		if( !(data[0] instanceof Constant) || !(data[1] instanceof Constant) )
			return false;
		// Key is XID+JoinID
		String key = ((Constant) data[0]).getObject().toString() + ((Constant) data[1]).getObject().toString(); 
		try {
			if( !(data[2] instanceof Constant) ) {
				
			} else {
				Object state = ((Constant) data[2]).getObject();
				if (state.equals("reset")) {
					Object[] value = (Object[]) predicate_join_record.get(key);
					if( value==null )
						// This may happen if stop has been executed from another thread
						return false;
					if( value[2] instanceof Integer ) {
						// TODO: deal with reset for pattern joins
					}
					List<PList> waiting = (List<PList>) value[1];
					List<PList> complete = (List<PList>) value[2];
					waiting.addAll(complete);
					complete.clear();
				} else if(state.equals("stop")) {
					log.debug("Cleaning up a pattern join instance "+key);
					predicate_join_record.remove(key);
					// TODO: Remove the matching pattern_join_instance (available only for pattern joins)
				}
			}
		} finally {
			ReentrantLock lock = predicate_join_locks.get(key);
			lock.unlock();
		}
		return false;
	}

}
