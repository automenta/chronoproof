package ws.prova.reference2.messaging;

import ws.prova.agent2.Reagent;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;

public class ProvaGenerateRuleImpl implements ProvaDelayedCommand {

	private final KB kb;
	
	private final Literal headControl;

	private final Literal[] provaLiterals;
	
	public ProvaGenerateRuleImpl(KB kb, Literal headControl,
			Literal[] provaLiterals) {
		this.kb = kb;
		this.headControl = headControl;
		this.provaLiterals = provaLiterals;
	}

	@Override
	public synchronized void process(Reagent prova) {
		kb.newRule(headControl, provaLiterals);
	}

}
