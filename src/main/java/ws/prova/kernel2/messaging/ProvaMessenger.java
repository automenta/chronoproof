package ws.prova.kernel2.messaging;

import java.util.List;
import java.util.Map;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.reference2.eventing.ProvaGroup;
import ws.prova.service.ProvaMiniService;

public interface ProvaMessenger {

	public boolean sendMsg(Literal literal, List<Literal> newLiterals,
			Rule query);

	public boolean spawn(Literal literal, List<Literal> newLiterals,
			Rule query);

	public boolean rcvMsg(Goal goal, List<Literal> newLiterals,
			Rule query, boolean mult);

	public void sendReturnAsMsg(Constant cid, Object ret);

	public boolean prepareMsg(Literal literal, List<Literal> newLiterals,
			Rule query);

	public String generateCid();

	public void addMsg(PList terms);

	public boolean rcvMsgP(Goal goal,
			List<Literal> newLiterals, Rule query, boolean mult);

	public boolean removeTemporalRule(Predicate predicate,
			Predicate predicate2, long key, boolean recursive, PList reaction, Map<String, List<Object>> metadata);

	public void cleanupGroup(String xorGroup);

	public void addGroupResult(PList terms);

	void scheduleCleanup(PObj xid, ProvaGroup group, Predicate p1, Predicate p2, long ruleid,
			long delay, long period, Map<String, List<Object>> metadata);

	void scheduleCleanup(ProvaGroup dynamic, long delay);

	public void stop();

	public void setService(ProvaMiniService service);

	public void addMsg(String xid, String dest, String agent, Object payload);

}
