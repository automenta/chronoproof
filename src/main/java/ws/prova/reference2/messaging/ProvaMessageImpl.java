package ws.prova.reference2.messaging;

import ws.prova.agent2.Reagent;
import ws.prova.agent2.ProvaThreadpoolEnum;
import ws.prova.kernel2.Rule;

public class ProvaMessageImpl implements ProvaDelayedCommand {

	private final long id;
	
	private final Rule goal;
	
	private final ProvaThreadpoolEnum pool;
	
	public ProvaMessageImpl(long id, Rule goal,
			ProvaThreadpoolEnum pool) {
		this.id = id;
		this.goal = goal;
		this.pool = pool;
	}

	@Override
	public void process(Reagent prova) {
		prova.submitAsync(id,goal,pool);
//		System.out.println("sent:"+goal);
	}

}
