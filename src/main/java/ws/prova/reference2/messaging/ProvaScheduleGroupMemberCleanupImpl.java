package ws.prova.reference2.messaging;

import java.util.List;
import java.util.Map;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.reference2.eventing.ProvaGroup;

public class ProvaScheduleGroupMemberCleanupImpl implements ProvaDelayedCommand {

	private PObj xid;
	
	private ProvaGroup group;
	
	private Predicate p1;

	private Predicate p2;
	
	private long ruleid;
	
	private long delay;
	
	private Map<String, List<Object>> metadata;
	
	private long period;
	
	public ProvaScheduleGroupMemberCleanupImpl(
			final Predicate p1,
			final Predicate p2,
			final long ruleid,
			long delay,
			Map<String, List<Object>> metadata ) {
		this.xid = null;
		this.group = null;
		this.p1 = p1;
		this.p2 = p2;
		this.ruleid = ruleid;
		this.delay = delay;
		this.metadata = metadata;
		this.period = 0;
	}

	public ProvaScheduleGroupMemberCleanupImpl(
			PObj xid, ProvaGroup group, Predicate p1,
			Predicate p2, long ruleid, long delay, long period,
			Map<String, List<Object>> metadata) {
		this.xid = xid;
		this.group = group;
		this.p1 = p1;
		this.p2 = p2;
		this.ruleid = ruleid;
		this.delay = delay;
		this.metadata = metadata;
		this.period = period;
	}

	@Override
	public void process(Reagent prova) {
		prova.getMessenger().scheduleCleanup(xid,group,p1,p2,ruleid,delay,period,metadata);
	}

}
