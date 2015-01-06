package ws.prova.reference2.messaging;

import ws.prova.agent2.ProvaReagent;
import ws.prova.agent2.ProvaThreadpoolEnum;
import ws.prova.kernel2.ProvaRule;

public class ProvaMessageImpl implements ProvaDelayedCommand {

	private final long id;
	
	private final ProvaRule goal;
	
	private final ProvaThreadpoolEnum pool;
	
	public ProvaMessageImpl(long id, ProvaRule goal,
			ProvaThreadpoolEnum pool) {
		this.id = id;
		this.goal = goal;
		this.pool = pool;
	}

	@Override
	public void process(ProvaReagent prova) {
		prova.submitAsync(id,goal,pool);
//		System.out.println("sent:"+goal);
	}

}
