package ws.prova.reference2.eventing;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.reference2.messaging.RemoveList;
import ws.prova.reference2.messaging.where.WhereNode;

public interface ProvaGroup {
	
	public enum EventDetectionStatus {incomplete,complete,preserved,failed}

	public void addRemoveEntry(long ruleid, RemoveList rl);

	public void start(RemoveList rl, Map<Long, ProvaGroup> ruleid2Group);

	public RemoveList getResultRemoveEntry();

	public Map<Long, RemoveList> getRemoveMap();

	public String getDynamicGroup();

	public void addResult(PList result);

	public EventDetectionStatus eventDetected(KB kb, Reagent prova, long ruleid, PList reaction, Map<String, List<Object>> metadata, Map<Long, ProvaGroup> ruleid2Group);

	public void stop();

	public boolean cleanup(KB kb, Reagent prova, Map<Long, ProvaGroup> ruleid2Group, Map<String, ProvaGroup> dynamic2Group);

	public boolean isOperatorConfigured();

	public void setParent(ProvaGroup parent);

	public ProvaGroup getParent();

	public void addChild(ProvaGroup g);

	public List<ProvaGroup> getChildren();

	public void immediateCleanup(Map<Long, ProvaGroup> ruleid2Group, Map<String, ProvaGroup> dynamic2Group);

	public String getOperatorName();

	public void childFailed(ProvaGroup child, Map<Long, ProvaGroup> ruleid2Group, Map<String, ProvaGroup> dynamic2Group);

	boolean isGroupFailed();

	public void setTimeout(long delay);

	public void putId2ruleid(String id, long ruleid);

	public void pause(long ruleidToPause);

	boolean isFailed();
	
	public String getStaticGroup();

	public void setTemplate(boolean template);

	public boolean isTemplate();

	public ProvaGroup clone();

	public void setDynamicGroup(String dynamicGroup);

	public void start(Map<Long,ProvaGroup> ruleid2Group );

	public boolean isPermanent();

	public void setTimerFuture(ScheduledFuture<?> future);

	public void addWhere(WhereNode where);

	public boolean isExtended();

	public void setExtended(boolean extended);

	public void addTimeoutEntry(RemoveList rl);

	public void cleanupTimeoutEntries();

	public void setCountMax(int countMax);

	public void setConcrete(ProvaGroup group);
	
}
