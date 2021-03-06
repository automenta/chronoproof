package test.ws.prova.test2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Test;
import ws.prova.api2.ProvaCommunicator;
import ws.prova.api2.Communicator;
import ws.prova.exchange.ProvaSolution;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PObj;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;

public class ProvaMetadataTest {

	static final String kAgent = "prova";

	static final String kPort = null;

	private ProvaCommunicator prova = null;
	
	@After
	public void shutdown() {
		if( prova!=null ) {
			prova.shutdown();
			prova = null;
		}
	}
	
	@Test
	public void hohpe_using_groups() {
		final String rulebase = "rules/reloaded/hohpe_using_groups.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger count = new AtomicInteger();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);

		try {
			synchronized(this) {
				wait(1000);
				org.junit.Assert.assertEquals(1,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void sensor_esper() {
		final String rulebase = "rules/reloaded/sensor_esper.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger count = new AtomicInteger();
		globals.put("$Count", count);
		AtomicInteger timeoutCount = new AtomicInteger();
		globals.put("$TimeoutCount", timeoutCount);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(2500);
				org.junit.Assert.assertEquals("Incorrect number of patterns",4,count.get());
				org.junit.Assert.assertEquals("Incorrect number of timeouts",6,timeoutCount.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void scala2() {
		final String rulebase = "rules/reloaded/scala2.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger successfulOrders = new AtomicInteger();
		globals.put("$SuccessfulOrders", successfulOrders);
		AtomicInteger successfulCancels = new AtomicInteger();
		globals.put("$SuccessfulCancels", successfulCancels);
		AtomicInteger failedCancels = new AtomicInteger();
		globals.put("$FailedCancels", failedCancels);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(5000);
				org.junit.Assert.assertEquals("Incorrect number of successful orders",4,successfulOrders.get());
				org.junit.Assert.assertEquals("Incorrect number of successful cancels",2,successfulCancels.get());
				org.junit.Assert.assertEquals("Incorrect number of failed cancels",4,failedCancels.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void scala2_simple() {
		final String rulebase = "rules/reloaded/scala2_simple.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger successfulOrders = new AtomicInteger();
		globals.put("$SuccessfulOrders", successfulOrders);
		AtomicInteger successfulCancels = new AtomicInteger();
		globals.put("$SuccessfulCancels", successfulCancels);
		AtomicInteger failedCancels = new AtomicInteger();
		globals.put("$FailedCancels", failedCancels);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0,0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(5000);
				org.junit.Assert.assertEquals("Incorrect number of successful orders",4,successfulOrders.get());
				org.junit.Assert.assertEquals("Incorrect number of successful cancels",2,successfulCancels.get());
				org.junit.Assert.assertEquals("Incorrect number of failed cancels",4,failedCancels.get());
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Uses the Prova 3.2.1 shortened versions of sendMsg, rcvMsg and rcvMult.
	 */
	@Test
	public void scala2_simple_short() {
		final String rulebase = "rules/reloaded/scala2_simple_short.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger successfulOrders = new AtomicInteger();
		globals.put("$SuccessfulOrders", successfulOrders);
		AtomicInteger successfulCancels = new AtomicInteger();
		globals.put("$SuccessfulCancels", successfulCancels);
		AtomicInteger failedCancels = new AtomicInteger();
		globals.put("$FailedCancels", failedCancels);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0,0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(5000);
				org.junit.Assert.assertEquals("Incorrect number of successful orders",4,successfulOrders.get());
				org.junit.Assert.assertEquals("Incorrect number of successful cancels",2,successfulCancels.get());
				org.junit.Assert.assertEquals("Incorrect number of failed cancels",4,failedCancels.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void scala1() {
		final String rulebase = "rules/reloaded/scala1.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger successfulOrders = new AtomicInteger();
		globals.put("$SuccessfulOrders", successfulOrders);
		AtomicInteger successfulCancels = new AtomicInteger();
		globals.put("$SuccessfulCancels", successfulCancels);
		AtomicInteger failedCancels = new AtomicInteger();
		globals.put("$FailedCancels", failedCancels);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(2000);
				org.junit.Assert.assertEquals("Incorrect number of successful orders",2,successfulOrders.get());
				org.junit.Assert.assertEquals("Incorrect number of successful cancels",1,successfulCancels.get());
				org.junit.Assert.assertEquals("Incorrect number of failed cancels",1,failedCancels.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void stable_limit() {
		final String rulebase = "rules/reloaded/stable_limit.prova";
		
		Map<String,Object> globals = new HashMap<String,Object>();
		AtomicInteger count = new AtomicInteger();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(3000);
				org.junit.Assert.assertEquals(2,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void where() {
		final String rulebase = "rules/reloaded/where.prova";
		
		AtomicInteger count = new AtomicInteger();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(4000);
				org.junit.Assert.assertEquals(5,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void and_multi_vars() {
		final String rulebase = "rules/reloaded/and_multi_vars.prova";
		
		AtomicInteger count = new AtomicInteger();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(3000);
				org.junit.Assert.assertEquals(4,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void or_common_interest() {
		final String rulebase = "rules/reloaded/or_common_interest.prova";
		
		AtomicInteger count = new AtomicInteger();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(3000);
				org.junit.Assert.assertEquals("Unexpected number of detections",3,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void cep005() {
		final String rulebase = "rules/reloaded/cep005.prova";
		
		CountDownLatch latch = new CountDownLatch(75);
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Latch", latch);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				boolean success = latch.await(60, TimeUnit.SECONDS);
				org.junit.Assert.assertTrue("Could not receive all messages",success);
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void and_vars() {
		final String rulebase = "rules/reloaded/and_vars.prova";
		
		AtomicInteger count = new AtomicInteger();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0,0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(3000);
				org.junit.Assert.assertEquals(2,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void retrospective_not_not() {
		final String rulebase = "rules/reloaded/retrospective_not_not.prova";
		
		AtomicInteger count = new AtomicInteger();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(1000);
				org.junit.Assert.assertEquals(1,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void retrospective_not() {
		final String rulebase = "rules/reloaded/retrospective_not.prova";
		
		AtomicInteger count = new AtomicInteger();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		final int numSolutions[] = {0,0};
		List<ProvaSolution[]> solutions = prova.getSolutions(true);

		org.junit.Assert.assertEquals(solutions.size(),numSolutions.length);
		for( int i=0; i<numSolutions.length; i++ )
			org.junit.Assert.assertEquals("Solution "+(i+1)+" incorrect",solutions.get(i).length,numSolutions[i]);

		try {
			synchronized(this) {
				wait(1000);
				org.junit.Assert.assertEquals(1,count.get());
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void histo_rate() {
		final String rulebase = "rules/reloaded/histo_rate.prova";
		
		AtomicLong count = new AtomicLong();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		
		// Send a hundred messages to the consulted Prova rulebase.
		try {
			Random r = new Random(17);
			// Accumulate histogram counts for each message
			for( int i=0; i<100; i++ ) {
				Map<String,Object> payload = new HashMap<String,Object>();
				payload.put("field", r.nextDouble());
				PList terms = ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("test"),
						ProvaConstantImpl.create("self"),
						ProvaConstantImpl.create(0),
						ProvaConstantImpl.create("inform"),
						ProvaListImpl.create(new PObj[] {
								ProvaConstantImpl.create(payload)
								})
				});
				prova.add(terms);
				Thread.sleep(4);
			}
			synchronized(this) {
				wait(1000);
				// All 100 events must be detected in groups
				org.junit.Assert.assertEquals(100L,count.get());
			}
		} catch (Exception e) {
			org.junit.Assert.fail("Unexpected exception: "+e.getLocalizedMessage());
		}
	}

	public class ResultCounter {
		private final long maxCount;
		private volatile long count = 0;
		public ResultCounter(long maxCount) {
			this.maxCount = maxCount;
		}
		public synchronized long incrementAndTestCompletion() {
			count++;
			return count==maxCount ? 0 : count;
		}
		public long get() {
			return count;
		}
	}
	
	@Test
	public void last_a_fby_b_big() {
		final String rulebase = "rules/reloaded/last_a_fby_b_big.prova";
		
		final long maxCount = 10000;
		
		ResultCounter count = new ResultCounter(maxCount);
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		CountDownLatch doneSignal = new CountDownLatch(1);
		globals.put("$Latch", doneSignal);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		
		long startTime = System.currentTimeMillis();
		
		// Send maxCount messages to the consulted Prova rulebase.
		try {
			for( int i=0; i<maxCount; i++ ) {
				int shift = i % 32;
				PList terms = ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create(""+shift),
						ProvaConstantImpl.create("async"),
						ProvaConstantImpl.create(0),
						ProvaConstantImpl.create("request"),
						ProvaListImpl.create(new PObj[] {
								ProvaConstantImpl.create("login"),
								ProvaConstantImpl.create("user"+shift),
								ProvaConstantImpl.create("10.10.10.10")
								})
				});
				prova.add(terms);
				PList terms2 = ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create(""+shift),
						ProvaConstantImpl.create("async"),
						ProvaConstantImpl.create(0),
						ProvaConstantImpl.create("request"),
						ProvaListImpl.create(new PObj[] {
								ProvaConstantImpl.create("login"),
								ProvaConstantImpl.create("user"+shift),
								ProvaConstantImpl.create("20.20.20.20")
								})
				});
				prova.add(terms2);
				PList terms3 = ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create(""+shift),
						ProvaConstantImpl.create("async"),
						ProvaConstantImpl.create(0),
						ProvaConstantImpl.create("request"),
						ProvaListImpl.create(new PObj[] {
								ProvaConstantImpl.create("logout"),
								ProvaConstantImpl.create("user"+shift),
								ProvaConstantImpl.create("10.10.10.10")
								})
				});
				prova.add(terms3);
				// Wait for 10 microseconds
				//Thread.sleep(0,10000);
                                Thread.sleep(0,10);
			}
			long diff0 = System.currentTimeMillis()-startTime;
			System.out.println("All messages sent in "+diff0+" ms");
			doneSignal.await(5, TimeUnit.SECONDS);
			long diff = System.currentTimeMillis()-startTime;
			System.out.println(count.get()+" patterns detected in "+diff+" ms");
			// All maxCount events must be detected
			org.junit.Assert.assertEquals(maxCount,count.get());
		} catch (Exception e) {
			org.junit.Assert.fail("Unexpected exception: "+e.getLocalizedMessage());
		}
	}


	@Test
	public void groupby_acc() {
		final String rulebase = "rules/reloaded/groupby_acc.prova";
		
		AtomicLong count = new AtomicLong();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		
		// Send a hundred messages to the consulted Prova rulebase.
		try {
			// Accumulate values sorted by i%5 every 25 milliseconds.
			for( int i=0; i<100; i++ ) {
				PList terms = ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("test"),
						ProvaConstantImpl.create("self"),
						ProvaConstantImpl.create(0),
						ProvaConstantImpl.create("inform"),
						ProvaListImpl.create(new PObj[] {
								ProvaConstantImpl.create("a"),
								ProvaConstantImpl.create(i),
								ProvaConstantImpl.create(5.0-i*0.2)
								})
				});
				prova.add(terms);
				Thread.sleep(4);
			}
			synchronized(this) {
				wait(2000);
				// All 100 events must be detected in groups
				org.junit.Assert.assertEquals(100L,count.get());
			}
		} catch (Exception e) {
			org.junit.Assert.fail("Unexpected exception: "+e.getLocalizedMessage());
		}
	}

	@Test
	public void groupby_rate() {
		final String rulebase = "rules/reloaded/groupby_rate.prova";
		
		AtomicLong count = new AtomicLong();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		
		// Send a hundred messages to the consulted Prova rulebase.
		try {
			// Accumulate groupby counts for each i%5 every 25 milliseconds.
			for( int i=0; i<100; i++ ) {
				PList terms = ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("test"),
						ProvaConstantImpl.create("self"),
						ProvaConstantImpl.create(0),
						ProvaConstantImpl.create("inform"),
						ProvaListImpl.create(new PObj[] {
								ProvaConstantImpl.create("a"),
								ProvaConstantImpl.create(i)
								})
				});
				prova.add(terms);
				Thread.sleep(4);
			}
			synchronized(this) {
				wait(1500);
				// All 100 events must be detected in groups
				org.junit.Assert.assertEquals(100L,count.get());
			}
		} catch (Exception e) {
			org.junit.Assert.fail("Unexpected exception: "+e.getLocalizedMessage());
		}
	}

	@Test
	public void groupby_size() {
		final String rulebase = "rules/reloaded/groupby_size.prova";
		
		AtomicLong count = new AtomicLong();
		Map<String,Object> globals = new HashMap<String,Object>();
		globals.put("$Count", count);
		prova = new Communicator(kAgent,kPort,rulebase,globals);
		
		// Send a hundred messages to the consulted Prova rulebase.
		// Accumulate groupby counts for each i%5 every 5 messages.
		for( int i=0; i<100; i++ ) {
			PList terms = ProvaListImpl.create(new PObj[] {
					ProvaConstantImpl.create("test"),
					ProvaConstantImpl.create("async"),
					ProvaConstantImpl.create(0),
					ProvaConstantImpl.create("inform"),
					ProvaListImpl.create(new PObj[] {
							ProvaConstantImpl.create("a"),
							ProvaConstantImpl.create(i)
							})
			});
			prova.add(terms);
		}
		try {
			synchronized(this) {
				wait(1000);
				// Expecting 100 results gathered by all groupby maps sent
				org.junit.Assert.assertEquals(100,count.get());
			}
		} catch (Exception e) {
		}
	}

}
