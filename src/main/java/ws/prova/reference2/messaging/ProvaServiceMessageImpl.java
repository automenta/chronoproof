package ws.prova.reference2.messaging;

import java.util.Map;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.PList;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaMapImpl;
import ws.prova.service.ProvaMiniService;

public class ProvaServiceMessageImpl implements ProvaDelayedCommand {

	private final String dest;
	
	private final ProvaMiniService service;

	private final String xid;

	private Object payload;

	private final String agent;

	private final String verb;
	
	public ProvaServiceMessageImpl(String dest, PList terms,
			String agent, ProvaMiniService service2) {
		this.xid = terms.getFixed()[0].toString();
		this.dest = dest;
		this.agent = agent;
		this.verb = terms.getFixed()[3].toString();
		this.service = service2;
		this.payload = terms.getFixed()[4];
		if( this.payload instanceof ProvaMapImpl )
			this.payload = ((ProvaMapImpl) payload).unwrap();
		else if( this.payload.getClass()==ProvaConstantImpl.class && ((Constant) this.payload).getObject() instanceof Map) 
			this.payload = ((Constant) this.payload).getObject();
	}

	@Override
	public void process(Reagent prova) {
		try {
			service.send(xid, dest, agent, verb, payload);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
