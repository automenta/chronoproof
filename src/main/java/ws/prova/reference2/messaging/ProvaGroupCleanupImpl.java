package ws.prova.reference2.messaging;

import ws.prova.agent2.ProvaReagent;

public class ProvaGroupCleanupImpl implements ProvaDelayedCommand {

	private final String group;

	public ProvaGroupCleanupImpl(String group) {
		this.group = group;
	}

	@Override
	public void process(ProvaReagent prova) {
		prova.getMessenger().cleanupGroup(group);

	}

}
