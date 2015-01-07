package ws.prova.agent2;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import javax.swing.SwingUtilities;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Logger;
import static ws.prova.agent2.ProvaThreadpoolEnum.IMMEDIATE;
import ws.prova.api2.ProvaCommunicator;
import ws.prova.esb2.ProvaAgent;
import ws.prova.exchange.ProvaSolution;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Inference;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.messaging.ProvaMessenger;
import ws.prova.kernel2.messaging.ProvaWorkflows;
import ws.prova.parser2.ProvaParsingException;
import ws.prova.reference2.DefaultKB;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.ProvaSwingAdaptor;
import ws.prova.reference2.messaging.ProvaMessengerImpl;
import ws.prova.reference2.messaging.ProvaWorkflowsImpl;
import ws.prova.service.ProvaMiniService;

@SuppressWarnings("unused")
public class ProvaReagentImpl implements Reagent {

    private final static Logger log = Logger.getLogger("prova");

    private static final ProvaSolution[] noSolutions = new ProvaSolution[0];

    static {
        MethodUtils.setCacheMethods(true);

    }

    private String agent;

    private String password;

    private String port;

    private KB kb;

    private String machine;

    // A queue for sequential execution of Prova goals and inbound messages
    private final OpPool sequential;

    private final OpPool[] partitionedPool;

    private final Map<Thread, Integer> threadToPartition;

    private ProvaMessenger messenger;

    private List<ProvaSolution[]> initializationSolutions;

    private ProvaWorkflows workflows;

    private ProvaSwingAdaptor swingAdaptor;

    private long latestTimestamp;

    private boolean allowedShutdown = true;

    final int threadsPerPool = 4;
    final int threadsPerPartition = 1;
    final int partitions = 4;
    final int ringBufferSize = 16384;
    private boolean yieldAfterNewTask = false;
    private final OpPool pool;

    public static class Op {

        public Runnable call;
        
    }
    public final static EventHandler<Op> opHandler = new EventHandler<Op>() {

        @Override
        public void onEvent(Op op, long l, boolean bln) throws Exception {
            try {
                op.call.run();
            }
            catch (Exception e) {
                log.error(e);
            }
        }
    };

    public static class OpPool {

        private final RingBuffer<Op> poolRing;
        private final ExecutorService pool;
        private final Disruptor<Op> poolDisruptor;

        public OpPool(int size, int threads, final String name) {
            this(size, threads, new ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    return new Thread(r, name);
                }                
            });
        }
        
        public OpPool(int size, int threads, ThreadFactory f) {
        
            if (f == null)
                this.pool = Executors.newFixedThreadPool(threads);
            else
                this.pool = Executors.newFixedThreadPool(threads, f);

            poolDisruptor = new Disruptor<Op>(new EventFactory<Op>() {
                @Override public Op newInstance() {
                    return new Op();
                }
            }, size, pool
            , ProducerType.MULTI, new SleepingWaitStrategy());
            /*, new SingleThreadedClaimStrategy(RING_SIZE),new SleepingWaitStrategy()*/

            final EventHandler<Op>[] handlers = new EventHandler[threads];
            for (int i = 0; i < threads; i++) {
                //handlers[i] = opHandler;
                handlers[i] = new EventHandler<Op>() {

                    @Override
                    public void onEvent(Op op, long l, boolean bln) throws Exception {
                        try {
                            op.call.run();
                        }
                        catch (Exception e) {
                            log.error(e);
                        }
                    }
                };
      
            }
            
            poolDisruptor.handleEventsWith(handlers);

            poolRing = poolDisruptor.start();
            

        }

        public void execute(Runnable c) {
            // Publishers claim events in sequence
            long sequence = poolRing.next();
            Op event = poolRing.get(sequence);

            event.call = c;

            // make the event available to EventProcessors
            poolRing.publish(sequence);
        }

        private boolean isShutdown() {
            return pool.isShutdown();
        }

        private void shutdown() {
            poolDisruptor.halt();
            poolDisruptor.shutdown();
            
            pool.shutdownNow();
            
        }

    }

    public ProvaReagentImpl(ProvaCommunicator communicator, ProvaMiniService service, String agent, String port, String[] prot, Object rules, ProvaAgent esb, Map<String, Object> globals) {
        this.agent = agent;
        this.port = port;
        // this.queue = queue;
        try {
            this.machine = InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (UnknownHostException ex) {
        }

        kb = new DefaultKB();
        kb.setGlobals(globals);

        this.sequential = new OpPool(ringBufferSize, 1, "sequence");
        this.pool = new OpPool(ringBufferSize, threadsPerPool, "pool");

        threadToPartition = new WeakHashMap<Thread, Integer>(partitions);
        partitionedPool = new OpPool[partitions];
        for (int i = 0; i < partitions; i++) {
            final int index = i;
            this.partitionedPool[i] = new OpPool(ringBufferSize, threadsPerPartition, new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "partition-" + index);
                    threadToPartition.put(t, index);
                    return t;
                }
                
            });
        }

        this.messenger = new ProvaMessengerImpl(this, kb, agent, password,
                machine, esb);
        this.messenger.setService(service);
        communicator.setMessenger(messenger);
        this.workflows = new ProvaWorkflowsImpl(kb);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        if (rules != null && !rules.equals("")) {
            // Import prova rules from .prova file or a BufferedReader
            try {
                // Wait indefinitely for results
                if (rules instanceof String) {
                    initializationSolutions = this.consultSync((String) rules,
                            (String) rules, new Object[]{}).get();
                } else {
                    initializationSolutions = this.consultSync(
                            (BufferedReader) rules, "-1", new Object[]{})
                            .get();
                }
            } catch (Exception e) {
                if (e.getCause() instanceof ProvaParsingException) {
                    throw new RuntimeException(e.getCause());
                } else if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            } finally {
                if (initializationSolutions == null) {
                    shutdown();
                }
            }
        }
    }

    @Override
    public List<ProvaSolution[]> getInitializationSolutions() {
        return initializationSolutions;
    }

    /**
     * Clean up by executing the shutdown predicate if it is in the rulebase.
     * TODO: It is now sent via the Executor queue so various problems may
     * arise.
     */
    private void cleanUp() {
        // BufferedReader in = new BufferedReader( new
        // StringReader(":-eval(shutdown).") );
        // consultSync(in,"shutdown",null);
    }

    public List<ProvaSolution[]> consultSyncInternal(BufferedReader in,
            String key, Object[] objects) {
        return kb.consultSyncInternal(this, in, key, objects);
    }

    public List<ProvaSolution[]> consultSyncInternal(String src, String key,
            Object[] objects) {
        return kb.consultSyncInternal(this, src, key, objects);
    }

    @Override
    public Future<List<ProvaSolution[]>> consultSync(final String src,
            final String key, final Object[] objects) {
        Callable<List<ProvaSolution[]>> task = new Callable<List<ProvaSolution[]>>() {
            @Override
            public List<ProvaSolution[]> call() {
                return ProvaReagentImpl.this.consultSyncInternal(src, key,
                        objects);
            }
        };

        FutureTask<List<ProvaSolution[]>> ftask = new FutureTask(task);
        sequential.execute(ftask);
        return ftask;
    }

    @Override
    public Future<List<ProvaSolution[]>> consultSync(final BufferedReader in,
            final String key, final Object[] objects) {
        Callable<List<ProvaSolution[]>> task = new Callable<List<ProvaSolution[]>>() {
            @Override
            public List<ProvaSolution[]> call() {
                return ProvaReagentImpl.this.consultSyncInternal(in, key,
                        objects);
            }
        };
        FutureTask<List<ProvaSolution[]>> ftask = new FutureTask(task);        
        sequential.execute(ftask);
        return ftask;
    }

    @Override
    public void consultAsync(final String src, final String key,
            final Object[] objects) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                final StringReader sr = new StringReader(src);
                final BufferedReader in = new BufferedReader(sr);
                ProvaReagentImpl.this.consultSyncInternal(in, key, objects);
            }
        };
        sequential.execute(task);
    }

    @Override
    public void consultAsync(final BufferedReader in, final String key,
            final Object[] objects) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                ProvaReagentImpl.this.consultSyncInternal(in, key, objects);
            }
        };
        sequential.execute(task);
    }

    /*
     * Callable<Object> job = new Callable<Object>() {
     * 
     * @Override public Object call() throws Exception { try {
     * ProvaReagentImpl.this.submitSyncInternal(goal); } catch( RuntimeException
     * e ) { System.out.println("Runtime Java exception: "+e.getCause()); }
     * return null; } };
     */
    @Override
    public void submitAsync(final long partition, final Rule goal,
            final ProvaThreadpoolEnum threadPool) {

        OpPool e = null;
        switch (threadPool) {
            case IMMEDIATE:
                goal.run();
                return;
            case MAIN:
                e = sequential;
                break;
            case TASK:
                e = pool;
                break;
            case CONVERSATION:
                e = partitionedPool[threadIndex(partition)];
                break;
            case SWING:
                //throw new RuntimeException("We will handle Swing differently, dont use this");
                // All Swing events are queued to the Swing events thread
                // (this is to be conforming to the Swing threads rules)
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        ProvaReagentImpl.this.submitSyncInternal(goal);
                    }
                };
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        task.run();
                    } else {
                        SwingUtilities.invokeAndWait(task);
                    }
                } catch (Exception ex) {
                    log.error(ex);
                }

                return;
        }

        this.latestTimestamp = System.currentTimeMillis();

        if (e == null || e.isShutdown()) {
            return;
        }

        goal.setReagent(this);
        try {            
            e.execute(goal);
        } catch (RejectedExecutionException r) {
            goal.onRejected(r);
        }

        yield();

    }

    /**
     * Map partition key to the conversation thread index
     *
     * @param partition partition key
     * @return thread index
     */
    public int threadIndex(final long partition) {
        return (int) (partition % (partitionedPool.length));
    }

    @Override
    public void executeTask(final long partition, final Runnable task,
            final ProvaThreadpoolEnum threadPool) {
        if (threadPool == IMMEDIATE) {
            task.run();
            return;
        }

        switch (threadPool) {
            case MAIN:
                sequential.execute(task);
                break;
            case CONVERSATION:
                partitionedPool[threadIndex(partition)].execute(task);
                break;
            case SWING:
                // All Swing events are queued to the Swing events thread
                // (this is to be conforming to the Swing threads rules)
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        task.run();
                    } else {
                        SwingUtilities.invokeAndWait(task);
                    }
                } catch (InvocationTargetException ex) {
                } catch (InterruptedException ex) {
                }
                break;
            case TASK:
                pool.execute(task);
                break;
        }

        yield();
    }

    @Override
    public void spawn(final PList terms) {
        final PObj[] data = terms.getFixed();
        final int length = data.length;
        if (length < 4) {
            return;
        }
        if (!(data[0] instanceof Constant)) {
            return;
        }
        if (!(data[2] instanceof Constant)) {
            return;
        }
        final String method = (String) ((Constant) data[2]).getObject();
        if (!(data[1] instanceof Constant)) {
            return;
        }
        final Object target = ((Constant) data[1]).getObject();
        Object[] args0 = null;
        final Object argsRaw = data[3];
        if (argsRaw instanceof PList) {
            final PList argsList = (PList) argsRaw;
            args0 = new Object[argsList.getFixed().length];
            for (int i = 0; i < args0.length; i++) {
                PObj po = argsList.getFixed()[i];
                if (!(po instanceof Constant)) {
                    return;
                }
                args0[i] = ((Constant) po).getObject();
            }
        } else if (argsRaw instanceof Constant) {
            args0 = new Object[1];
            args0[0] = ((Constant) argsRaw).getObject();
        }
        final Object[] args = args0;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                Object ret = null;
                try {
                    if (target instanceof Class<?>) {
                        Class<?> targetClass = (Class<?>) target;
                        ret = MethodUtils.invokeStaticMethod(targetClass, method,
                                args);
                    } else {
                        ret = MethodUtils.invokeMethod(target, method, args);
                    }
                } catch (Exception ex) {
                    log.error(ex);
                }
                messenger.sendReturnAsMsg((Constant) data[0], ret);
                return /*ret*/;
            }
        };
        pool.execute(task);
        yield();
        return;
    }

    protected void yield() {
        if (yieldAfterNewTask) {
            Thread.yield();
        }
    }

    public Derivation submitSyncInternal(Rule goal) {
        Inference engine = new DefaultInference(kb, goal);
        engine.setReagent(this);
        return engine.run();
    }

    @Override
    public void setPrintWriter(PrintWriter printWriter) {
        kb.setPrinter(printWriter);
    }

    @Override
    public ProvaMessenger getMessenger() {
        return this.messenger;
    }

    @Override
    public KB getKb() {
        return kb;
    }

    @Override
    public String getAgent() {
        return agent;
    }

    @Override
    public void shutdown() {
        messenger.stop();
        pool.shutdown();
        for (OpPool partitioned : partitionedPool) {
            partitioned.shutdown();
            partitioned = null;
        }
        sequential.shutdown();
    }

    @Override
    public ProvaWorkflows getWorkflows() {
        return this.workflows;
    }

    @Override
    public void unconsultSync(String src) {
        kb.unconsultSync(src);
    }

    @Override
    public ProvaSwingAdaptor getSwingAdaptor() {
        if (this.swingAdaptor == null) {
            swingAdaptor = new ProvaSwingAdaptor(this);
        }
        return this.swingAdaptor;
    }

    @Override
    public boolean canShutdown() {
        return allowedShutdown
                && System.currentTimeMillis() > this.latestTimestamp + 1000;
    }

    @Override
    public void setAllowedShutdown(boolean allowedShutdown) {
        this.allowedShutdown = allowedShutdown;

    }

    @Override
    public boolean isInPartitionThread(long partition) {
        return threadToPartition.get(Thread.currentThread()) == threadIndex(partition);
    }

    @Override
    public void setGlobalConstant(String name, Object value) {
        kb.setGlobalConstant(name, value);
    }

    /*
     private class ArrayBlockingQueueWithPut<E> extends ArrayBlockingQueue<E> {

     private static final long serialVersionUID = -3392821517081645923L;

     public ArrayBlockingQueueWithPut(int capacity) {
     super(capacity);
     }

     public boolean offer(E e) {
     try {
     put(e);
     //				this.offer(e, 86400, TimeUnit.SECONDS);
     } catch (InterruptedException e1) {
     log.info("Interrupted asynchronous thread");
     }
     return true;
     }
     }
     */
}
