package ws.prova.reference2.eventing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import org.apache.log4j.Logger;
import ws.prova.agent2.Reagent;
import ws.prova.agent2.ProvaThreadpoolEnum;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.messaging.ProvaDelayedCommand;
import ws.prova.reference2.messaging.ProvaMessengerImpl;
import ws.prova.reference2.messaging.RemoveList;
import ws.prova.reference2.messaging.where.WhereNode;

public class ProvaBasicGroupImpl implements ProvaGroup {

	private final static Logger log = Logger.getLogger("prova.eventing");

	protected String dynamicGroup;
	
	protected List<Object> results;
	
	protected RemoveList resultRemoveEntry;
	
	protected List<RemoveList> timeoutRemoveEntries;
	
	protected Map<Long,RemoveList> removeMap;

	protected PList lastReaction;

	protected ProvaGroup parent;

	protected List<ProvaGroup> children;
	
	protected long timeToLive;
	
	protected boolean failed = false;

	protected Map<String,Long> id2ruleid;
	
	protected Set<Long> paused;

	private String staticGroup;
	
	protected boolean template;
	
	protected boolean permanent;
	
	protected String templateDynamicGroup;
	
	protected ScheduledFuture<?> future = null;
	
	protected long numEmitted = 0;
	
	protected List<WhereNode> where = null;

	private boolean extended = false;

	protected int countMax;
	
	private ProvaGroup concrete = null;

	public ProvaBasicGroupImpl(String dynamicGroup, String staticGroup) {
		this.dynamicGroup = dynamicGroup;
		this.staticGroup = staticGroup;
		this.timeToLive = 0;
		this.template = false;
		this.templateDynamicGroup = null;
		this.permanent = false;
		this.countMax = -1;
		removeMap = new HashMap<Long,RemoveList>();
		paused = new HashSet<Long>();
	}

	public ProvaBasicGroupImpl(ProvaBasicGroupImpl g) {
		this.dynamicGroup = g.dynamicGroup;
		this.templateDynamicGroup = g.templateDynamicGroup;
		this.staticGroup = g.staticGroup;
		this.removeMap = g.removeMap;
		this.resultRemoveEntry = g.resultRemoveEntry;
		this.timeoutRemoveEntries = g.timeoutRemoveEntries;
		this.timeToLive = g.timeToLive;
		this.children = g.children;
		this.id2ruleid = g.id2ruleid;
		this.paused = g.paused;
		this.template = g.template;
		this.permanent = g.permanent;
		this.countMax = g.countMax;
		this.future = g.future;
		if( children!=null ) {
			for( ProvaGroup c: children )
				c.setParent(this);
		}
		this.where = g.where;
	}

	@Override
	public ProvaGroup clone() {
		ProvaBasicGroupImpl g = new ProvaBasicGroupImpl(this);
		g.adjustClone(this);
		return g;
	}
	
	protected void adjustClone( ProvaBasicGroupImpl group ) {
		permanent = true;
		templateDynamicGroup = group.dynamicGroup;
		removeMap = new HashMap<Long,RemoveList>();
		removeMap.putAll(group.removeMap);
		paused = new HashSet<Long>();
	}
	
	@Override
	public String getStaticGroup() {
		return this.staticGroup;
	}
	
	@Override
	public String getOperatorName() {
		return "undefined";
	}

	@Override
	public void addRemoveEntry(long ruleid, RemoveList rl) {
		removeMap.put(ruleid,rl);
		
	}

	@Override
	public void start(Map<Long,ProvaGroup> ruleid2Group ) {
		for( Entry<Long,RemoveList> r : removeMap.entrySet() )
			ruleid2Group.put(r.getKey(), this);
	}

	@Override
	public void start(RemoveList rl, Map<Long,ProvaGroup> ruleid2Group ) {
		this.resultRemoveEntry = rl;
		for( Entry<Long,RemoveList> r : removeMap.entrySet() )
			ruleid2Group.put(r.getKey(), this);
	}

	@Override
	public void addTimeoutEntry( RemoveList rl ) {
		if( this.timeoutRemoveEntries==null )
			this.timeoutRemoveEntries = new ArrayList<RemoveList>();
		this.timeoutRemoveEntries.add(rl);
	}

	@Override
	public void cleanupTimeoutEntries() {
		if( timeoutRemoveEntries==null )
			return;
		for( RemoveList rl : timeoutRemoveEntries ) {
			long k = rl.getRuleid();
			rl.getP1().getClauses().removeTemporalClause(k);
			rl.getP2().getClauses().removeTemporalClause(k);
		}
	}
	
	@Override
	public String getDynamicGroup() {
		return this.dynamicGroup;
	}
	
	@Override
	public RemoveList getResultRemoveEntry() {
		return this.resultRemoveEntry;
	}
	
	@Override
	public Map<Long,RemoveList> getRemoveMap() {
		return this.removeMap;
	}

	@Override
	public void addResult(PList result) {
		this.results.add(result);
	}

	@Override
	public EventDetectionStatus eventDetected(KB kb, Reagent prova,
			long key, PList reaction, Map<String, List<Object>> metadata, Map<Long, ProvaGroup> ruleid2Group) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop() {
		for( Entry<Long, RemoveList> e : removeMap.entrySet() ) {
			if( log.isDebugEnabled() )
				log.debug(e);
			RemoveList r = e.getValue();
			long k = r.getRuleid();
			r.getP1().getClauses().removeTemporalClause(k);
			r.getP2().getClauses().removeTemporalClause(k);
		}
		if( children!=null )
			for( ProvaGroup c : children )
				c.stop();
		// Remove any pending timers
		if( future!=null )
			future.cancel(false);
	}

	@Override
	public boolean cleanup(KB kb, Reagent prova, Map<Long, ProvaGroup> ruleid2Group, Map<String, ProvaGroup> dynamic2Group) {
		if( failed ) {
			immediateCleanup(ruleid2Group, dynamic2Group);
//			System.out.println("Removed failed group: "+dynamicGroup);
			return true;
		}
		List<ProvaDelayedCommand> delayed = DefaultInference.delayedCommands.get();
		// delayed is non-null when this is run from the end-of-goal cleanup task
		if( delayed!=null && !removeMap.isEmpty() )
			return false;
		if( delayed==null ) {
			// Timeout cleanup: add all @not reactions as results
			results.clear();
			for( Iterator<Entry<Long, RemoveList>> iter = removeMap.entrySet().iterator(); iter.hasNext(); ) {
				Entry<Long, RemoveList> e = iter.next();
				RemoveList rl = e.getValue();
				if( rl.isNot() ) {
					PList reaction = rl.getReaction();
					reaction.getFixed()[1] = ProvaConstantImpl.create("async");
					reaction.getFixed()[2] = ProvaConstantImpl.create(0);
					lastReaction = reaction;
//					lastReaction = ProvaListImpl.create(new ProvaObject[] {rl.getReaction().getFixed()[0],ProvaConstantImpl.create(0),reaction});
					PList reactionM = ProvaListImpl.create(new PObj[] {ProvaConstantImpl.create("not"),reaction.shallowCopy()});
					addResult(reactionM);
					iter.remove();
				}
			}
		}
		boolean resultsSent = sendGroupResults(results, kb, prova);
		for( Entry<Long, RemoveList> e : removeMap.entrySet() ) {
			long k = e.getKey();
			ruleid2Group.remove(k);
		}
		if( !resultsSent ) {
			long k = resultRemoveEntry.getRuleid();
			resultRemoveEntry.getP1().getClauses().removeTemporalClause(k);
			resultRemoveEntry.getP2().getClauses().removeTemporalClause(k);
			cleanupTimeoutEntries();
		}
		if( children!=null ) {
			for( ProvaGroup c : children ) {
				c.immediateCleanup(ruleid2Group,dynamic2Group);
			}
		}
		dynamic2Group.remove(dynamicGroup);
//		System.out.println("Removed group: "+dynamicGroup);
		if( lastReaction==null ) {
			if( log.isDebugEnabled() )
				log.debug("Group failed");
			if( parent!=null )
				parent.childFailed(this,ruleid2Group,dynamic2Group);
		}
		return true;
	}

	@Override
	public void immediateCleanup(Map<Long, ProvaGroup> ruleid2Group, Map<String, ProvaGroup> dynamic2Group) {
		for( Entry<Long, RemoveList> e : removeMap.entrySet() ) {
			long k = e.getKey();
			ruleid2Group.remove(k);
		}
		long k = resultRemoveEntry.getRuleid();
		resultRemoveEntry.getP1().getClauses().removeTemporalClause(k);
		resultRemoveEntry.getP2().getClauses().removeTemporalClause(k);
		cleanupTimeoutEntries();
		if( children!=null ) {
			for( ProvaGroup c : children )
				c.immediateCleanup(ruleid2Group,dynamic2Group);
		}
		dynamic2Group.remove(dynamicGroup);
		if( log.isDebugEnabled() )
			log.debug("Group removed: "+dynamicGroup);
		if( future!=null )
			future.cancel(false);
	}

	@Override
	public boolean isOperatorConfigured() {
		return false;
	}

	protected synchronized boolean sendGroupResults(List<Object> results, KB kb, Reagent prova) {
		PList content = null;
		if( isGroupFailed() ) {
			// TODO: Is it always timeout here?
			if( log.isDebugEnabled() )
				log.debug("Timeout group results: "+results);
			content = ProvaListImpl.create(new PObj[] {ProvaConstantImpl.create(results)});
			if( results.isEmpty() ) {
				// Timeout and no results
				if( timeoutRemoveEntries!=null && !timeoutRemoveEntries.isEmpty() ) {
					lastReaction = timeoutRemoveEntries.get(0).getReaction();
					lastReaction.getFixed()[1] = ProvaConstantImpl.create("async");
					lastReaction.getFixed()[2] = ProvaConstantImpl.create(0);
				} else
					return false;
			} else {
				final Object last = results.get(results.size()-1);
				if( last instanceof PList )
					lastReaction = ((PList) last).shallowCopy();
				else
					lastReaction = this.resultRemoveEntry.getReaction();
				lastReaction.getFixed()[3] = ProvaConstantImpl.create("timeout");
				if( numEmitted!=0 )
					results.clear();
			}
		} else {
			if( log.isDebugEnabled() )
				log.debug("Group results: "+results);
			content = ProvaListImpl.create(new PObj[] {ProvaConstantImpl.create(results)});
			if( lastReaction==null ) {
				if( log.isDebugEnabled() )
					log.debug("Empty results");
				lastReaction = resultRemoveEntry.getReaction();
				lastReaction.getFixed()[1] = ProvaConstantImpl.create("async");
				lastReaction.getFixed()[2] = ProvaConstantImpl.create(0);
			} else {
				if( lastReaction.getFixed().length==0 )
					// A timeout expired so that strict @count is successful
					lastReaction = (PList) results.get(results.size()-1);
				else if( lastReaction.getFixed().length==2 ) {
					final PObj[] newFixed = new PObj[5];
					newFixed[0] = lastReaction.getFixed()[0];
					newFixed[1] = ProvaConstantImpl.create("async");
					newFixed[2] = ProvaConstantImpl.create(0);
					newFixed[4] = lastReaction.getFixed()[1];
					lastReaction = ProvaListImpl.create(newFixed, lastReaction.getTail());
				}
				PObj o = lastReaction.getFixed()[2];
				if( o instanceof PList )
					lastReaction = (PList) o;
			}
			lastReaction.getFixed()[3] = ProvaConstantImpl.create(getOperatorName());
		}
		lastReaction.getFixed()[4] = content;
		final PObj cidOriginal = lastReaction.getFixed()[0];
		final String cid = cidOriginal instanceof Constant ? ((Constant) cidOriginal).getObject().toString() : "0";
		final PObj cidObject = ProvaConstantImpl.create(cid);
		lastReaction.getFixed()[0] = cidObject;
		Literal lit = kb.newHeadLiteral("rcvMsg",lastReaction);
		Map<String, List<Object>> meta = new HashMap<String, List<Object>>(1);
		if( this.templateDynamicGroup!=null )
			meta.put("group", Arrays.asList(new Object[] {templateDynamicGroup}));
		else
			meta.put("group", Arrays.asList(new Object[] {dynamicGroup}));
		lit.addMetadata(meta);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")}).cloneRule();
		if( log.isDebugEnabled() )
			log.debug("Sent group results: "+goal);
		if( cid.equals("0") )
			prova.submitAsync(0,goal,ProvaThreadpoolEnum.MAIN);
		else
			prova.submitAsync(ProvaMessengerImpl.partitionKey(cid),goal,ProvaThreadpoolEnum.CONVERSATION);
		numEmitted++;
		return true;
	}

	@Override
	public boolean isGroupFailed() {
		boolean result = results.isEmpty() || lastReaction==null;
		return result;
	}

	@Override
	public void setParent(ProvaGroup parent) {
		this.parent = parent;
	}

	@Override
	public ProvaGroup getParent() {
		return this.parent;
	}

	@Override
	public void addChild(ProvaGroup g) {
		if( children==null )
			children = new ArrayList<ProvaGroup>();
		children.add(g);
	}

	@Override
	public List<ProvaGroup> getChildren() {
		return this.children;
	}

	@Override
	public void childFailed(ProvaGroup child, Map<Long, ProvaGroup> ruleid2Group, Map<String, ProvaGroup> dynamic2Group) {
		throw new RuntimeException("Unsupported method");
	}

	@Override
	public void setTimeout(long delay) {
		this.timeToLive = delay;
	}
	
	@Override
	public boolean isFailed() {
		return failed;
	}

	@Override
	public void putId2ruleid(String id, long ruleid) {
		if( id2ruleid==null )
			id2ruleid = new HashMap<String,Long>();
		id2ruleid.put(id,ruleid);
	}

	@Override
	public void pause(long ruleidToPause) {
		paused.add(ruleidToPause);
	}

	protected void resume(long ruleidToPause) {
		paused.remove(ruleidToPause);
	}

	@Override
	public void setTemplate(boolean template) {
		this.template = template;
	}

	@Override
	public boolean isTemplate() {
		return template;
	}

	@Override
	public void setDynamicGroup(String dynamicGroup) {
		this.dynamicGroup = dynamicGroup;
	}

	@Override
	public boolean isPermanent() {
		return permanent;
	}

	@Override
	public void setTimerFuture(ScheduledFuture<?> future) {
		if( this.concrete!=null )
			this.concrete.setTimerFuture(future);
		else
			this.future = future;		
	}

	@Override
	public void addWhere(WhereNode newWhere) {
		if( where==null )
			where = new ArrayList<WhereNode>();
		where.add(newWhere);
		
	}

	@Override
	public boolean isExtended() {
		return this.extended;
	}

	@Override
	public void setExtended(boolean extended) {
		this.extended = extended;
	}

	@Override
	public void setCountMax(int countMax) {
		this.countMax  = countMax;
	}

	@Override
	public void setConcrete(ProvaGroup group) {
		this.concrete = group;
	}

}
