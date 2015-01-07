package ws.prova.reference2.messaging;

import ws.prova.agent2.Reagent;
import ws.prova.esb2.ProvaAgent;
import ws.prova.kernel2.PList;
import ws.prova.reference2.ProvaConstantImpl;

public class ProvaESBMessageImpl implements ProvaDelayedCommand {

	private final String dest;
	
	private final PList terms;
	
	private final ProvaAgent esb;
	
	public ProvaESBMessageImpl(String dest, PList terms,
			ProvaAgent esb) {
		this.dest = dest;
		this.terms = terms;
		this.terms.getFixed()[2] = ProvaConstantImpl.create(esb.getAgentName());
		this.esb = esb;
	}

	@Override
	public void process(Reagent prova) {
		try {
			esb.send(dest, terms);
		} catch (Exception e) {
			// TODO Throw a ProvaException
			throw new RuntimeException(e);
		}
	}

}
