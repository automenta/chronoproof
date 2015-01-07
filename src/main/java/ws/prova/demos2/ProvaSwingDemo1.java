package ws.prova.demos2;

import ws.prova.api2.Communicator;

public class ProvaSwingDemo1 {

	static final String kAgent = "prova";

	static final String kPort = null;

	public ProvaSwingDemo1() {
		final String rulebase = "rules/reloaded/swing.prova";
		
		try {
			new Communicator(kAgent,kPort,rulebase,null);
		} catch (Exception e) {
			System.err.println(e.getCause().getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		new ProvaSwingDemo1();
	}
	
}
