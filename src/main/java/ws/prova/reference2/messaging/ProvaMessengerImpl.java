package ws.prova.reference2.messaging;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import ws.prova.agent2.Reagent;
import ws.prova.agent2.ProvaThreadpoolEnum;
import ws.prova.esb2.ProvaAgent;
import ws.prova.eventing.ProvaEventsAccumulator;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.kernel2.messaging.ProvaMessenger;
import ws.prova.parser.WhereLexer;
import ws.prova.parser.WhereParser;
import ws.prova.reference2.ProvaConstantImpl;
import ws.prova.reference2.ProvaListImpl;
import ws.prova.reference2.ProvaLiteralImpl;
import ws.prova.reference2.ProvaMapImpl;
import ws.prova.reference2.DefaultInference;
import ws.prova.reference2.ProvaVariableImpl;
import ws.prova.reference2.eventing.ProvaAndGroupImpl;
import ws.prova.reference2.eventing.ProvaBasicGroupImpl;
import ws.prova.reference2.eventing.ProvaGroup;
import ws.prova.reference2.eventing.ProvaGroup.EventDetectionStatus;
import ws.prova.reference2.eventing.ProvaOrGroupImpl;
import ws.prova.reference2.messaging.where.WhereNode;
import ws.prova.reference2.messaging.where.WhereTreeVisitor;
import ws.prova.service.ProvaMiniService;
import ws.prova.util2.ProvaTimeUtils;

@SuppressWarnings("unused")
public class ProvaMessengerImpl implements ProvaMessenger {

    private static final Variable CTLPROTOCOL = ProvaVariableImpl
            .create("CtlProtocol");

    private static final Variable CTLFROM = ProvaVariableImpl
            .create("CtlFrom");

    private static final ProvaConstantImpl EOF = ProvaConstantImpl
            .create("eof");

    private final static Logger log = Logger.getLogger("prova");

    private final Reagent prova;

    private final KB kb;

    private final String agent;

    private final String password;

    private final String machine;

    private final ProvaAgent esb;

    private final AtomicLong unique_iid = new AtomicLong();

    private final AtomicLong reaction_iid = new AtomicLong();

    private final ConcurrentMap<Long, List<String>> ruleid2outbound = new ConcurrentHashMap<Long, List<String>>();

    private final ConcurrentMap<String, List<Long>> inbound2ruleids = new ConcurrentHashMap<String, List<Long>>();

    private final ConcurrentMap<String, String> dynamic2Static = new ConcurrentHashMap<String, String>();

    private final ConcurrentMap<String, ProvaGroup> dynamic2Group = new ConcurrentHashMap<String, ProvaGroup>();

    private final ConcurrentMap<Long, ProvaGroup> ruleid2Group = new ConcurrentHashMap<Long, ProvaGroup>();

    private final ConcurrentMap<Long, ProvaGroup> outcomeRuleid2Group = new ConcurrentHashMap<Long, ProvaGroup>();

    private final ScheduledThreadPoolExecutor timers;

    private final Predicate rcvMsg2;

    private ProvaMiniService service;

    private static final ThreadLocal<Map<String, String>> tlStatic2Dynamic = new ThreadLocal<Map<String, String>>();

    private static final ThreadLocal<Map<String, ProvaGroup>> tlDynamic = new ThreadLocal<Map<String, ProvaGroup>>();

    private class TimerThreadFactory implements ThreadFactory {

        private int count = 1;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"timer-" + (count++));
        }

    }

    public ProvaMessengerImpl(Reagent prova, KB kb,
            String agent, String password, String machine, ProvaAgent esb) {
        this.prova = prova;
        this.kb = kb;
        this.agent = agent;
        this.password = password;
        this.machine = machine;
        this.esb = esb;
        this.timers = new ScheduledThreadPoolExecutor(1, new TimerThreadFactory());
        this.rcvMsg2 = kb.getOrGeneratePredicate("rcvMsg", 2);
    }

    /**
     * Prepare a rcvMsg goal for sending on the main agent thread if the verb is
     * 'self' or a thread chosen according to the conversation-id cid (if the
     * verb is 'async' or other). The reactions corresponding to the same
     * conversation-id are thus always run by the same thread.
     *
     * The message is sent when the next rcvMsg literal is encountered or the
     * whole query is complete.
     */
    @Override
    public boolean prepareMsg(Literal literal,
            List<Literal> newLiterals, Rule query) {
        ProvaDelayedCommand message = null;
            List<Variable> variables = query.getVariables();
            PList terms = literal.getTerms();
            PObj[] data = terms.getFixed();
            PObj lt = data[0];
            if (lt instanceof VariableIndex) {
                VariableIndex varPtr = (VariableIndex) lt;
                lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
            }
            String cid;
            if (lt instanceof Constant) {
                // Follow up on an existing conversation
                cid = lt.toString();
            } else if (lt instanceof Variable) {
                // Generate a unique conversation-id
                cid = generateCid();
                ((Variable) lt).setAssigned(ProvaConstantImpl.create(cid));
            } else {
                return false;
            }
            if (!(data[1] instanceof Constant)) {
                return false;
            }
            String protocol = ((Constant) data[1]).getObject().toString();
            if (!(data[2] instanceof Constant)) {
                return false;
            }
            String dest = ((Constant) data[2]).getObject().toString();
            PList termsCopy = (PList) terms
                    .cloneWithVariables(variables);
            if (null != protocol) {
                switch (protocol) {
                    case "esb":
                        if (esb == null) {
                            return false;
                        }
                        message = new ProvaESBMessageImpl(dest, termsCopy, esb);
                        break;
                    case "osgi":
                        if (service == null) {
                            return false;
                        }
                        message = new ProvaServiceMessageImpl(dest, termsCopy, agent,
                                service);
                        break;
                    default:
                        Literal lit = kb.newHeadLiteral("rcvMsg", termsCopy);
                        Rule goal = kb.newGoal(new Literal[]{lit,
                            kb.newLiteral("fail")});
                        if ("async".equals(protocol)) {
                            message = new ProvaMessageImpl(partitionKey(cid), goal,
                                    ProvaThreadpoolEnum.CONVERSATION);
                        } else if ("task".equals(protocol)) {
                            message = new ProvaMessageImpl(0, goal,
                                    ProvaThreadpoolEnum.TASK);
                        } else if ("swing".equals(protocol)) {
                            message = new ProvaMessageImpl(0, goal,
                                    ProvaThreadpoolEnum.SWING);
                        } else if ("self".equals(protocol) || "0".equals(dest)) {
                            message = new ProvaMessageImpl(0, goal,
                                    ProvaThreadpoolEnum.MAIN);
                        }
                        break;
                }
            }
        if (message != null) {
            List<ProvaDelayedCommand> delayed = DefaultInference.delayedCommands
                    .get();
            delayed.add(message);
            return true;
        }
        return false;
    }

    public static long partitionKey(Object cid) {
        return cid.hashCode();
    }
    public static long partitionKey(String cid) {
        long key = Math.abs(cid.hashCode());
        return key;
    }

    /**
     * Submits asynchronously a rcvMsg goal scheduled on the main agent thread
     * if the verb is 'self' or a thread chosen according to the conversation-id
     * cid (if the verb is 'async' or other). The reactions corresponding to the
     * same conversation-id are thus always run by the same thread.
     */
    @Override
    public boolean sendMsg(Literal literal,
            List<Literal> newLiterals, Rule query) {
        try {
            if (literal.isGround()) {
                return sendMsgGround(literal, newLiterals, query);
            }
            List<Variable> variables = query.getVariables();
            PList terms = literal.getTerms();
            PObj[] data = terms.getFixed();
            PObj lt = data[0];
            if (lt instanceof VariableIndex) {
                VariableIndex varPtr = (VariableIndex) lt;
                lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
            }
            String cid;
            if (lt instanceof Constant) {
                // Follow up on an existing conversation
                cid = lt.toString();
            } else if (lt instanceof Variable) {
                // Generate a unique conversation-id
                cid = generateCid();
                ((Variable) lt).setAssigned(ProvaConstantImpl.create(cid));
            } else {
                return false;
            }
            if (data.length == 2) {
                final PList termsCopy = terms.copyWithVariables(variables);
                final Literal lit = new ProvaLiteralImpl(rcvMsg2,
                        termsCopy);
                final Rule goal = kb.newGoal(new Literal[]{
                    lit, kb.newLiteral("fail")});
                prova.submitAsync(partitionKey(cid), goal,
                        ProvaThreadpoolEnum.CONVERSATION);
                return true;
            }

            if (!(data[1] instanceof Constant)) {
                return false;
            }
            String protocol = (/*(ProvaConstant)*/data[1]).toString();
            PObj destObject = data[2];
            if (destObject instanceof VariableIndex) {
                VariableIndex varPtr = (VariableIndex) destObject;
                destObject = variables.get(varPtr.getIndex())
                        .getRecursivelyAssigned();
            }
            if (!(destObject instanceof Constant)) {
                return false;
            }
            String dest = (/*(ProvaConstant)*/destObject).toString();
            if (!(data[3] instanceof Constant)) {
                return false;
            }
            final PList termsCopy = terms.copyWithVariables(variables);
            if (null != protocol) {
                switch (protocol) {
                    case "esb": {
                        if (esb == null) {
                            return false;
                        }
                        ProvaDelayedCommand message = new ProvaESBMessageImpl(dest,
                                termsCopy, esb);
                        message.process(prova);
                        return true;
                    }
                    case "osgi": {
                        if (service == null) {
                            return false;
                        }
                        ProvaDelayedCommand message = new ProvaServiceMessageImpl(dest,
                                termsCopy, agent, service);
                        message.process(prova);
                        return true;
                    }
                }
            }
            String verb = (/*(ProvaConstant)*/data[3]).toString();
            Literal lit = null;
            // if( "eof".equals(verb) ) {
            lit = kb.newHeadLiteral("rcvMsg", termsCopy);
			// } else {
            // termsCopy = ProvaListImpl.create(new ProvaObject[]
            // {termsCopy.getFixed()[0],ProvaAnyImpl.create(),termsCopy});
            // lit = kb.generateHeadLiteral("@temporal_rule", termsCopy);
            // }
            final Rule goal = kb.newGoal(new Literal[]{lit,
                kb.newLiteral("fail")});
            if ("async".equals(protocol)) {
                prova.submitAsync(partitionKey(cid), goal,
                        ProvaThreadpoolEnum.CONVERSATION);
                return true;
            } else if ("task".equals(protocol)) {
                prova.submitAsync(0, goal, ProvaThreadpoolEnum.TASK);
                return true;
            } else if ("swing".equals(protocol)) {
                prova.submitAsync(0, goal, ProvaThreadpoolEnum.SWING);
                return true;
            } else if ("self".equals(protocol) || "0".equals(dest)) {
                prova.submitAsync(0, goal, ProvaThreadpoolEnum.MAIN);
                return true;
            } else {
                // TODO: Other protocols
            }
        } catch (Exception e) {
            // TODO: For now, just log this
            log.error("sendMessage: " + e);
        }
        return false;
    }

    private boolean sendMsgGround(Literal literal,
            List<Literal> newLiterals, Rule query) {
        PList terms = literal.getTerms();
        PObj[] data = terms.getFixed();
        if (data.length == 2) {
            // Assume it is a shortened version with conversation-id and payload
            // only sent over async protocol
            final Literal lit = new ProvaLiteralImpl(rcvMsg2, terms);
            final Rule goal = kb.newGoal(new Literal[]{lit,
                kb.newLiteral("fail")});
            prova.submitAsync(partitionKey(data[0]), goal,
                    ProvaThreadpoolEnum.CONVERSATION);
            return true;
        }
        String protocol = data[1].toString();
        String dest = data[2].toString();
        if (null != protocol) {
            switch (protocol) {
                case "esb": {
                    if (esb == null) {
                        return false;
                    }
                    ProvaDelayedCommand message = new ProvaESBMessageImpl(dest, terms,
                            esb);
                    message.process(prova);
                    return true;
                }
                case "osgi": {
                    if (service == null) {
                        return false;
                    }
                    ProvaDelayedCommand message = new ProvaServiceMessageImpl(dest,
                            terms, agent, service);
                    message.process(prova);
                    return true;
                }
            }
        }
        if (!(data[3] instanceof Constant)) {
            return false;
        }
        String verb = data[3].toString();
        Literal lit = null;
        PList termsCopy = terms;
        // if( "eof".equals(verb) ) {
        lit = kb.newHeadLiteral("rcvMsg", termsCopy);
		// } else {
        // termsCopy = ProvaListImpl.create(new ProvaObject[]
        // {data[0],ProvaAnyImpl.create(),termsCopy});
        // lit = kb.generateHeadLiteral("@temporal_rule", termsCopy);
        // }
        Rule goal = kb.newGoal(new Literal[]{lit,
            kb.newLiteral("fail")});
        if ("async".equals(protocol)) {
            String cid = data[0].toString();
            prova.submitAsync(partitionKey(cid), goal,
                    ProvaThreadpoolEnum.CONVERSATION);
            return true;
        } else if ("task".equals(protocol)) {
            prova.submitAsync(0, goal, ProvaThreadpoolEnum.TASK);
            return true;
        } else if ("swing".equals(protocol)) {
            prova.submitAsync(0, goal, ProvaThreadpoolEnum.SWING);
            return true;
        } else if ("self".equals(protocol) || "0".equals(dest)) {
            prova.submitAsync(0, goal, ProvaThreadpoolEnum.MAIN);
            return true;
        } else {
            // TODO: Other protocols
        }
        return false;
    }

    @Override
    public void sendReturnAsMsg(Constant cid, Object ret) {
        if (ret == null) {
            ret = 0;
        }
        PList terms = ProvaListImpl.create(new PObj[]{cid,
            ProvaConstantImpl.create("self"),
            ProvaConstantImpl.create("0"),
            ProvaConstantImpl.create("return"),
            ProvaConstantImpl.create(ret)});
        Literal lit = kb.newHeadLiteral("rcvMsg", terms);
        Rule goal = kb.newGoal(new Literal[]{lit,
            kb.newLiteral("fail")});
        prova.submitAsync(partitionKey(cid.getObject().toString()), goal,
                ProvaThreadpoolEnum.CONVERSATION);
    }

    @Override
    public boolean spawn(Literal literal, List<Literal> newLiterals,
            Rule query) {
        List<Variable> variables = query.getVariables();
        PList terms = (PList) literal.getTerms(); // .cloneWithVariables(variables);
        PObj[] data = terms.getFixed();
        PObj lt = data[0];
        if (lt instanceof VariableIndex) {
            VariableIndex varPtr = (VariableIndex) lt;
            lt = variables.get(varPtr.getIndex()).getRecursivelyAssigned();
        }
        if (lt instanceof Variable) {
            // Generate a unique conversation-id
            String cid = generateCid();
            ((Variable) lt).setAssigned(ProvaConstantImpl.create(cid));
        }
        prova.spawn((PList) terms.cloneWithVariables(variables));
        return true;
    }

    @Override
    public String generateCid() {
        return prova.getAgent() + ':' + unique_iid.incrementAndGet();
    }

    public static WhereNode parse(String expr) throws Exception {
        ByteArrayInputStream rawinput = new ByteArrayInputStream(
                expr.getBytes("UTF-8"));
        ANTLRInputStream input = new ANTLRInputStream(rawinput);
        WhereLexer lexer = new WhereLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        WhereParser parser = new WhereParser(tokens);

        CommonTree tree = (CommonTree) parser.expr().getTree();

        return WhereTreeVisitor.visit(tree);
    }

    @Override
    public boolean rcvMsg(Goal goal, List<Literal> newLiterals,
            Rule query, boolean mult) {
        final Literal literal = goal.getGoal();
        final Rule copy = query.cloneRule();
        final List<Variable> variables = copy.getVariables();
        final Literal literalClone = (Literal) literal
                .cloneWithVariables(variables);
        final PList terms = literalClone.getTerms();
        try {
            final PObj[] data = terms.getFixed();
            final PObj xid = data[0];
            if (!(xid instanceof Constant)
                    && !(xid instanceof Variable)) {
                return false;
            }
            if (data.length != 2 && !(data[2] instanceof Constant)
                    && !(data[2] instanceof Variable)) {
                return false;
            }
            final long ruleid = reaction_iid.incrementAndGet();
            final Constant tid = ProvaConstantImpl.create(ruleid);
            final boolean meta = literal.getMetadata() != null;
            if (!meta) {
                final List<Literal> body = new ArrayList<Literal>();
                final List<Literal> guard = literalClone.getGuard();
                if (guard != null) {
                    for (Literal g : guard) {
                        body.add(g);
                    }
                }
                final PObj poXID = data[0];
                PList headControlList = null;
                if (data.length == 2) {
                    final PObj ctlProtocol = CTLPROTOCOL;
                    headControlList = ProvaListImpl.create(new PObj[]{
                        tid, ctlProtocol, CTLFROM, EOF, terms});
					// headControlList = ProvaListImpl.create(new ProvaObject[]
                    // {
                    // tid, terms });
                } else {
                    final PObj poProtocol = data[1];
                    final PObj ctlProtocol = poProtocol instanceof Constant ? poProtocol
                            : CTLPROTOCOL;
                    headControlList = ProvaListImpl.create(new PObj[]{
                        tid, ctlProtocol, CTLFROM, EOF, terms});
                }
                // Add the reaction and termination rule head literals
                final Literal head = kb.newHeadLiteral("rcvMsg",
                        terms);
                final Literal headControl = kb.newLiteral("@temporal_rule_control", (PList) headControlList
                        .cloneWithVariables(variables));
                final PList removeList = ProvaListImpl
                        .create(new PObj[]{
                            ProvaConstantImpl.create(head.getPredicate()),
                            ProvaConstantImpl.create(headControl
                                    .getPredicate()), tid, head.getTerms()});
                final Literal removeLiteral = kb.newLiteral(
                        "@temporal_rule_remove", removeList);

                if (!mult) {
                    body.add(removeLiteral);
                }
                final Literal[] queryLiterals = copy.getBody();
                for (int i = 1; i < queryLiterals.length; i++) {
                    body.add((Literal) queryLiterals[i]
                            .cloneWithVariables(variables));
                }

                final Rule rule = kb.newRule(ruleid, head,
                        body.toArray(new Literal[]{}));
                kb.newRule(ruleid, headControl,
                        new Literal[]{removeLiteral});
                if (log.isDebugEnabled()) {
                    log.debug("Added temporal rule: " + rule);
                }
                return false;
            }
            List<Object> groups = literal.getMetadata("group");
            List<Object> groupsAnd = literal.getMetadata("and");
            List<Object> groupsOr = literal.getMetadata("or");
            List<Object> groupsNot = literal.getMetadata("not");
            List<Object> groupsStop = literal.getMetadata("stop");
            List<Object> groupsOptional = literal.getMetadata("optional");
            List<Object> groupsCount = literal.getMetadata("count");
            List<Object> groupsSize = literal.getMetadata("size");
            List<Object> groupsPause = literal.getMetadata("pause");
            List<Object> groupsResume = literal.getMetadata("resume");
            List<Object> groupsPaused = literal.getMetadata("paused");
            Object size = null;
            Object sizeReset = null;
            Object sizeObject = null;
            if (groupsSize != null && !groupsSize.isEmpty()) {
                size = groupsSize.get(0);
                size = goal.lookupMetadata(size.toString(), variables);
                if (groupsSize.size() > 1) {
                    sizeReset = groupsSize.get(1);
                    sizeReset = goal.lookupMetadata(sizeReset.toString(),
                            variables);
                    if (groupsSize.size() > 2) {
                        sizeObject = groupsSize.get(2);
                        sizeObject = goal.lookupMetadata(sizeObject.toString(),
                                variables);
                    }
                }
            }
            Integer countMin = null;
            Integer countMax = null;
            // The IGNORE mode is the default
            Integer countMode = 0;
            if (groupsCount != null && !groupsCount.isEmpty()) {
                String s = groupsCount.get(0).toString();
                countMin = Integer.parseInt((String) goal.lookupMetadata(s,
                        variables));
                if (groupsCount.size() > 1) {
                    s = groupsCount.get(1).toString();
                    countMax = Integer.parseInt((String) goal.lookupMetadata(s,
                            variables));
                    if (groupsCount.size() > 2) {
                        s = groupsCount.get(2).toString();
                        if (null != s) {
                            switch (s) {
                                case "record":
                                    countMode = 1;
                                    break;
                                case "strict":
                                    countMode = 2;
                                    break;
                            }
                        }
                    } else {
                        if (countMax == -1) {
                            countMax = countMin;
                            countMode = 1;
                        }

                    }
                } else {
                    if (countMin == -1) {
                        countMin = 0;
                        countMax = 0;
                        countMode = 1;
                    } else {
                        countMax = countMin;
                        countMode = 0;
                    }
                }
            }
            List<Object> groupsTimeout = literal.getMetadata("timeout");
            Object timeout = null;
            if (groupsTimeout != null && !groupsTimeout.isEmpty()) {
                timeout = groupsTimeout.get(0);
                timeout = goal.lookupMetadata(timeout.toString(), variables);
            }
            List<Object> groupsTimer = literal.getMetadata("timer");
            Object timer = null;
            Object timerReset = null;
            Object timerObject = null;
            if (groupsTimer != null && !groupsTimer.isEmpty()) {
                timer = groupsTimer.get(0);
                timer = goal.lookupMetadata(timer.toString(), variables);
                if (groupsTimer.size() > 1) {
                    timerReset = groupsTimer.get(1);
                    timerReset = goal.lookupMetadata(timerReset.toString(),
                            variables);
                } else {
                    timerReset = timer;
                }
                if (groupsTimer.size() > 2) {
                    timerObject = groupsTimer.get(2);
                    timerObject = goal.lookupMetadata(timerObject.toString(),
                            variables);
                }
            }
            List<Object> groupsVar = literal.getMetadata("vars");
            List<Object> vars = null;
            if (groupsVar != null && !groupsVar.isEmpty()) {
                vars = new ArrayList<Object>();
                for (int i = 0; i < groupsVar.size(); i++) {
                    Object var = groupsVar.get(i);
                    var = goal.lookupMetadata(var.toString(), variables);
                    vars.add(var);
                }
            }
            List<Object> groupsWhere = literal.getMetadata("where");
            WhereNode where = null;
            if (groupsWhere != null && !groupsWhere.isEmpty()) {
                where = parse(groupsWhere.get(0).toString());
            }
            final List<Literal> body = new ArrayList<Literal>();
            final List<Literal> guard = literalClone.getGuard();
            if (guard != null) {
                for (Literal g : guard) {
                    body.add(g);
                }
            }
            final PObj poXID = data[0];
            final PObj poProtocol = data[1];
            final PObj ctlProtocol = poProtocol instanceof Constant ? poProtocol
                    : CTLPROTOCOL;
            ProvaGroup dynamic = null;
            Rule temporalRule = null;
            PList headControlList = ProvaListImpl.create(new PObj[]{
                tid, ctlProtocol, CTLFROM, EOF, terms});
            // Add the reaction and termination rule head literals
            final Literal head = kb.newHeadLiteral("rcvMsg", terms);
            final Literal headControl = kb.newLiteral("@temporal_rule_control",
                    (PList) headControlList.cloneWithVariables(variables));
            final RemoveList rl = new RemoveList(head.getPredicate(),
                    headControl.getPredicate(), ruleid, (PList) head
                    .getTerms().cloneWithVariables(variables));
            dynamic = generateOrReuseDynamicGroup(goal, variables, ruleid, rl);
            PList removeList = ProvaListImpl.create(new PObj[]{
                ProvaConstantImpl.create(head.getPredicate()),
                ProvaConstantImpl.create(headControl.getPredicate()), tid,
                head.getTerms()});
            final Literal removeLiteral = kb.newLiteral(
                    "@temporal_rule_remove", removeList);
            if (groupsWhere != null) {
                dynamic.addWhere(where);
            }
            if (dynamic != null && dynamic.getParent() != null) {
				// Composite reaction is part of another @group: add an
                // @add_group_result relation
                PList addAndResultList = ProvaListImpl
                        .create(new PObj[]{
                            ProvaConstantImpl.create(dynamic.getParent()
                                    .getDynamicGroup()), head.getTerms()});
                removeLiteral.setMetadata("rule", Arrays
                        .asList(new Object[]{dynamic.getParent()
                            .getDynamicGroup()}));
                body.add(kb.newLiteral("@add_group_result",
                        addAndResultList));
                if (groupsNot != null) {
                    removeLiteral.setMetadata("not",
                            Arrays.asList(new Object[]{}));
                    rl.setNot(true);
                }
                if (groupsTimeout != null) {
                    removeLiteral.setMetadata("timeout", groupsTimeout);
                }
                if (groupsStop != null) {
                    removeLiteral.setMetadata("stop", groupsStop);
                    rl.setOptional(groupsStop.isEmpty());
                }
                if (groupsOptional != null) {
                    rl.setOptional(true);
                }
                if (groupsPause != null) {
                    removeLiteral.setMetadata("pause", groupsPause);
                }
                if (groupsResume != null) {
                    removeLiteral.setMetadata("resume", groupsResume);
                }
                if (groupsPaused != null) {
                    removeLiteral.setMetadata("paused", groupsPaused);
                    dynamic.getParent().pause(ruleid);
                }
            } else if (groups != null && !groups.isEmpty()) {
				// Reaction is a member of a @group: add an @add_group_result
                // relation
                PList addAndResultList = ProvaListImpl
                        .create(new PObj[]{
                            ProvaConstantImpl.create(dynamic
                                    .getDynamicGroup()), head.getTerms()});
                removeLiteral.setMetadata("rule", Arrays
                        .asList(new Object[]{dynamic.getDynamicGroup()}));
                body.add(kb.newLiteral("@add_group_result",
                        addAndResultList));
                if (groupsNot != null) {
                    removeLiteral.setMetadata("not",
                            Arrays.asList(new Object[]{}));
                    rl.setNot(true);
                }
                if (groupsTimeout != null) {
                    removeLiteral.setMetadata("timeout", groupsTimeout);
                }
                if (groupsStop != null) {
                    removeLiteral.setMetadata("stop", groupsStop);
                    rl.setOptional(groupsStop.isEmpty());
                }
                if (groupsOptional != null) {
                    rl.setOptional(true);
                }
                if (groupsPause != null) {
                    removeLiteral.setMetadata("pause", groupsPause);
                }
                if (groupsResume != null) {
                    removeLiteral.setMetadata("resume", groupsResume);
                }
                if (groupsPaused != null) {
                    removeLiteral.setMetadata("paused", groupsPaused);
                    dynamic.pause(ruleid);
                }
                if (mult) {
                    // if( xid instanceof ProvaVariable )
                    dynamic.setTemplate(true);
					// else
                    // removeLiteral.setMetadata("count", Arrays.asList(new
                    // Object[] {0}));
                    mult = false;
                }
            }
            if (!mult) {
                body.add(removeLiteral);
            }
            Literal[] queryLiterals = copy.getBody();
            for (int i = 1; i < queryLiterals.length; i++) {
                body.add((Literal) queryLiterals[i]
                        .cloneWithVariables(variables));
            }
            if (groupsSize != null) {
                if (sizeObject != null) {
                    removeLiteral.setMetadata(
                            "size",
                            Arrays.asList(new Object[]{size, sizeReset,
                                sizeObject}));
                } else if (sizeReset != null) {
                    removeLiteral.setMetadata("size",
                            Arrays.asList(new Object[]{size, sizeReset}));
                } else {
                    // Lone @size determines whether the event is required
                    removeLiteral.setMetadata("size",
                            Arrays.asList(new Object[]{size}));
                    rl.setOptional(Integer.parseInt((String) size) <= 0);
                }
            }
            if (groupsCount != null) {
                if (groupsAnd != null || groupsOr != null) {
                    // A count constraint on an exit reaction
                    dynamic.setCountMax(countMax);
                }
                removeLiteral.setMetadata(
                        "count",
                        Arrays.asList(new Object[]{countMin, countMax,
                            countMode}));
                rl.setOptional(countMin <= 0);
            }
            if (groupsTimer != null) {
                if (timerObject != null) {
                    removeLiteral.setMetadata(
                            "timer",
                            Arrays.asList(new Object[]{timer, timerReset,
                                timerObject}));
                } else if (timerReset != null) {
                    removeLiteral.setMetadata("timer",
                            Arrays.asList(new Object[]{timer, timerReset}));
                } else {
                    removeLiteral.setMetadata("timer",
                            Arrays.asList(new Object[]{timer}));
                }
            }
            if (groupsVar != null) {
                removeLiteral.setMetadata("vars", vars);
            }

			// if( poProtocol instanceof ProvaConstant && poXID instanceof
            // ProvaConstant && ((ProvaConstant)
            // poProtocol).getObject().equals("async") ) {
            // temporalRule = kb.generateLocalRule(prova,
            // partitionKey(poXID.toString()), head, body
            // .toArray(new ProvaLiteral[] {}));
            // }
            // synchronized (kb) {
            temporalRule = kb.newRule(ruleid, head,
                    body.toArray(new Literal[]{}));
            kb.newRule(ruleid, headControl,
                    new Literal[]{removeLiteral});
            if (log.isDebugEnabled()) {
                log.debug("Added temporal rule: "
                        + (dynamic == null ? "" : dynamic.getDynamicGroup())
                        + " " + head);
            }
            // }

            if (dynamic != null && dynamic.isOperatorConfigured()) {
                temporalRule.setMetadata("group", Arrays
                        .asList(new Object[]{dynamic.getDynamicGroup()}));
				// if (!xid.isGround())
                // Very important: remove literal is tagged with the group
                // for open reactions
                // (i.e., reactions with free conversation-id) so that
                // when it is evaluated, there is a way to find out whether
                // the group is a template one
                // and so the rules should not be removed.
                removeLiteral.setMetadata("group", Arrays
                        .asList(new Object[]{dynamic.getDynamicGroup()}));
                if (timeout != null) {
                    long delay = ProvaTimeUtils
                            .timeIntervalInMilliseconds(timeout);
                    List<ProvaDelayedCommand> delayed = DefaultInference.delayedCommands
                            .get();
                    if (groupsAnd != null || groupsOr != null) {
                        delayed.add(new ProvaScheduleGroupCleanupImpl(dynamic,
                                delay));
                    } else {
                        delayed.add(new ProvaScheduleGroupMemberCleanupImpl(
                                xid, dynamic, head.getPredicate(), headControl
                                .getPredicate(), ruleid, delay, 0,
                                removeLiteral.getMetadata()));
                    }
                }
            } else if (dynamic == null && timeout != null) {
                // Provide for an individual timeout here
                if (timeout != null) {
                    long delay = ProvaTimeUtils
                            .timeIntervalInMilliseconds(timeout);
                    scheduleCleanup(xid, dynamic, head.getPredicate(),
                            headControl.getPredicate(), ruleid, delay, 0,
                            removeLiteral.getMetadata());
                }
            } else if (dynamic != null && timeout != null) {
                // A group member timeout
                if (timeout != null) {
                    long delay = ProvaTimeUtils
                            .timeIntervalInMilliseconds(timeout);
                    List<ProvaDelayedCommand> delayed = DefaultInference.delayedCommands
                            .get();
                    delayed.add(new ProvaScheduleGroupMemberCleanupImpl(xid,
                            dynamic, head.getPredicate(), headControl
                            .getPredicate(), ruleid, delay, 0,
                            removeLiteral.getMetadata()));
                }
            } else if (dynamic != null && timer != null) {
                // A group member timer
                if (timer != null) {
                    long delay = ProvaTimeUtils
                            .timeIntervalInMilliseconds(timer);
                    long period = ProvaTimeUtils
                            .timeIntervalInMilliseconds(timerReset);
                    List<ProvaDelayedCommand> delayed = DefaultInference.delayedCommands
                            .get();
                    if (timerObject != null
                            && timerObject instanceof ProvaEventsAccumulator) {
                        ProvaEventsAccumulator acc = (ProvaEventsAccumulator) timerObject;
                        Date now = new Date();
                        if (acc.getDuration() != 0) {
							// State passed to the operator
                            // Expected end of current timer
                            Date endDate = DateUtils.addMilliseconds(
                                    acc.getStartTime(), acc.getDuration());
                            // Time remaining in the current window
                            long timeRemaining = endDate.getTime()
                                    - now.getTime();
                            if (timeRemaining > 0) {
                                delay = timeRemaining;
                            } else {
                                delay = 0;
                            }
                        } else {
                            // Set the accumulator's start time
                            acc.setStartTime(now);
                            acc.setDuration((int) period);
                        }
                    }
                    delayed.add(new ProvaScheduleGroupMemberCleanupImpl(xid,
                            dynamic, head.getPredicate(), headControl
                            .getPredicate(), ruleid, delay, period,
                            removeLiteral.getMetadata()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Possible formatting error in rcvMsg: "
                    + e.getMessage());
        }
        return false;
    }

    private ProvaGroup generateOrReuseDynamicGroup(Goal goal,
            List<Variable> variables, long ruleid, RemoveList rl) {
        Literal literal = goal.getGoal();
        ProvaGroup g = null;

        List<Object> groups = literal.getMetadata("and");
        String dynamicGroup = null;
        if (groups != null && !groups.isEmpty()) {
            dynamicGroup = generateOrReuseDynamicGroup(
                    groups.get(0).toString(), goal, variables);
            if (tlDynamic.get() == null) {
                tlDynamic.set(new HashMap<String, ProvaGroup>());
            }
            Map<String, ProvaGroup> d2g = tlDynamic.get();
            g = d2g.get(dynamicGroup);
            if (g == null) {
                g = new ProvaAndGroupImpl(dynamicGroup, groups.get(0)
                        .toString());
            } else if (g instanceof ProvaAndGroupImpl) {
                g.addTimeoutEntry(rl);
                return g;
            } else if (g instanceof ProvaBasicGroupImpl) {
                g = new ProvaAndGroupImpl(g);
            } else {
                throw new RuntimeException(
                        "Event group can only have one operator");
            }
            d2g.put(dynamicGroup, g);
            dynamic2Group.put(dynamicGroup, g);
			// if( g.isTemplate() )
            // ruleid2Group.put(ruleid, g);
            // outcomeRuleid2Group.put(ruleid, g);
            g.start(rl, ruleid2Group);
        }

        groups = literal.getMetadata("or");
        if (groups != null && !groups.isEmpty()) {
            if (dynamicGroup != null) // Both @and and @or present
            {
                throw new RuntimeException(
                        "Multiple operators on event groups are not allowed");
            }
            dynamicGroup = generateOrReuseDynamicGroup(
                    groups.get(0).toString(), goal, variables);
            if (tlDynamic.get() == null) {
                tlDynamic.set(new HashMap<String, ProvaGroup>());
            }
            Map<String, ProvaGroup> d2g = tlDynamic.get();
            g = d2g.get(dynamicGroup);
            if (g == null) {
                g = new ProvaOrGroupImpl(dynamicGroup, groups.get(0).toString());
            } else if (g instanceof ProvaOrGroupImpl) {
                g.addTimeoutEntry(rl);
                return g;
            } else if (g instanceof ProvaBasicGroupImpl) {
                g = new ProvaOrGroupImpl(g);
            } else {
                throw new RuntimeException(
                        "Event group can only have one operator");
            }
            d2g.put(dynamicGroup, g);
            dynamic2Group.put(dynamicGroup, g);
            outcomeRuleid2Group.put(ruleid, g);
            g.start(rl, ruleid2Group);
        }

		// A @group may include a previous result in a new group or be just a
        // member of a new group
        ProvaGroup gg = null;
        groups = literal.getMetadata("group");
        if (groups != null && !groups.isEmpty()) {
            dynamicGroup = generateOrReuseDynamicGroup(
                    groups.get(0).toString(), goal, variables);
            if (tlDynamic.get() == null) {
                tlDynamic.set(new HashMap<String, ProvaGroup>());
            }
            Map<String, ProvaGroup> d2g = tlDynamic.get();
            gg = d2g.get(dynamicGroup);
            if (gg == null) {
                gg = new ProvaBasicGroupImpl(dynamicGroup, groups.get(0)
                        .toString());
                d2g.put(dynamicGroup, gg);
            } else {
                gg.setExtended(true);
            }
            gg.addRemoveEntry(ruleid, rl);
            List<Object> groupsId = literal.getMetadata("id");
            if (groupsId != null && !groupsId.isEmpty()) {
                gg.putId2ruleid(groupsId.get(0).toString(), ruleid);
            }
            if (gg.isOperatorConfigured()) {
                gg.start(rl, ruleid2Group);
            }
            if (g == null) {
                return gg;
            }
            g.setParent(gg);
            gg.addChild(g);
        }
        return g;
    }

    private String generateOrReuseDynamicGroup(String group, Goal goal,
            List<Variable> variables) {
        String dynamicGroup;
        if (Character.isUpperCase(group.charAt(0))) {
            // Look up the group value in the bound metadata
            dynamicGroup = goal.lookupMetadata(group, variables).toString();
        } else {
            // Generate or reuse the actual dynamic group-id
            if (tlStatic2Dynamic.get() == null) {
                tlStatic2Dynamic.set(new HashMap<String, String>());
            }
            Map<String, String> s2d = tlStatic2Dynamic.get();
            dynamicGroup = s2d.get(group);
			// if( dynamicGroup!=null ) {
            // ProvaGroup oldGroup = dynamic2Group.get(dynamicGroup);
            // if( oldGroup!=null && oldGroup.getRemoveMap().isEmpty() )
            // // The old group instance is complete and is awaiting the results
            // publication
            // // so we need to create a new group instance
            // dynamicGroup = null;
            // }
            if (dynamicGroup == null) {
                dynamicGroup = generateCid();
                s2d.put(group, dynamicGroup);
                dynamic2Static.put(dynamicGroup, group);
            }
        }
        return dynamicGroup;
    }

    @Override
    public boolean rcvMsgP(Goal goal, List<Literal> newLiterals,
            Rule query, boolean mult) {
        Literal literal = goal.getGoal();
        Rule copy = query.cloneRule();
        List<Variable> variables = copy.getVariables();
        Literal literalClone = (Literal) literal
                .cloneWithVariables(variables);
        PList terms = literalClone.getTerms();
        try {
            PObj[] data = terms.getFixed();
            if (!(data[0] instanceof PList)
                    || !(data[1] instanceof PList)
                    || !(data[2] instanceof PList)) {
                return false;
            }
            PObj[] oInbound = ((PList) data[0]).getFixed();
            PObj[] oOutbound = ((PList) data[1]).getFixed();
            PObj[] reaction = ((PList) data[2]).getFixed();
            PObj[] reactionFixed = new PObj[reaction.length - 1];
            System.arraycopy(reaction, 1, reactionFixed, 0,
                    reactionFixed.length);
            PList reactionTerms = ProvaListImpl.create(reactionFixed);
            PObj xid = reactionFixed[0];
            if (!(xid instanceof Constant)
                    && !(xid instanceof Variable)) {
                return false;
            }
            if (!(reaction[2] instanceof Constant)
                    && !(reaction[2] instanceof Variable)) {
                return false;
            }
			// String source = (reaction[2] instanceof ProvaConstant) ?
            // ((ProvaConstant) reaction[2]).getObject().toString() : "";
            final long ruleid = reaction_iid.incrementAndGet();
            Constant tid = ProvaConstantImpl.create(ruleid);
            List<String> inbound = new ArrayList<String>();
            for (PObj o : oInbound) {
                String s = ((Constant) o).getObject().toString();
                inbound.add(s);
                List<Long> ruleids = inbound2ruleids.get(s);
                if (ruleids == null) {
                    ruleids = new ArrayList<Long>();
                    inbound2ruleids.put(s, ruleids);
                }
                ruleids.add(ruleid);
            }
            List<String> outbound = new ArrayList<String>();
            for (PObj o : oOutbound) {
                outbound.add(((Constant) o).getObject().toString());
            }
            // ruleid2inbound.put(ruleid, inbound);
            ruleid2outbound.put(ruleid, outbound);
            Literal head = null;
            Literal headControl = null;
            // synchronized (kb) {
            Variable ctlProtocol = CTLPROTOCOL;
            Variable ctlFrom = CTLFROM;

            PList headControlList = ProvaListImpl.create(new PObj[]{
                tid, ctlProtocol, ctlFrom, EOF, terms});
            head = kb.newHeadLiteral("rcvMsg", reactionTerms);
            headControl = kb.newLiteral("@temporal_rule_control",
                    (PList) headControlList.cloneWithVariables(variables));

            List<Literal> body = new ArrayList<Literal>();
            PList removeList = ProvaListImpl.create(new PObj[]{
                ProvaConstantImpl.create(head.getPredicate()),
                ProvaConstantImpl.create(headControl.getPredicate()), tid,
                head.getTerms()});
            List<Literal> guard = literalClone.getGuard();
            if (guard != null) {
                for (Literal g : guard) {
                    body.add(g);
                }
            }
            if (data.length > 3
                    && data[3] instanceof PList
                    && ((Constant) ((PList) data[3]).getFixed()[0])
                    .getObject().toString().equals("condition")) {
                String symbol = ((Constant) ((PList) ((PList) data[3])
                        .getFixed()[1]).getFixed()[0]).getObject().toString();
                body.add(kb.newLiteral(symbol,
                        ((PList) ((PList) data[3]).getFixed()[1])
                        .getFixed(), 1));
            }
            if (!mult) {
                body.add(kb
                        .newLiteral("@temporal_rule_remove", removeList));
            }
            Literal[] queryLiterals = copy.getBody();
            for (int i = 1; i < queryLiterals.length; i++) {
                body.add((Literal) queryLiterals[i]
                        .cloneWithVariables(variables));
            }
            Rule temporalRule = kb.newRule(ruleid, head,
                    body.toArray(new Literal[]{}));
            // Add end-of-reaction removal rule
            temporalRule = kb.newRule(ruleid, headControl,
                    new Literal[]{kb.newLiteral(
                                "@temporal_rule_remove", removeList)});
            if (log.isDebugEnabled()) {
                log.debug("Added temporal rule: " + head);
            }
            // }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void scheduleCleanup(final ProvaGroup dynamic, long delayMS) {

        dynamic.setTimeout(delayMS);

        final String dynamicGroup = dynamic.getDynamicGroup();
        TimerTask cleanup = new TimerTask() {

            @Override
            public void run() {
                // synchronized (kb) {
                removeGroup(dynamicGroup, true);
                // }
            }

        };
        ScheduledFuture<?> future = timers.schedule(cleanup, delayMS,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleCleanup(final PObj xid, final ProvaGroup group,
            final Predicate p1, final Predicate p2,
            final long ruleid, final long delay, final long period,
            final Map<String, List<Object>> metadata) {
        final String protocol = (xid == null || xid instanceof Variable) ? "self"
                : "async";
        final String cid = (xid == null || xid instanceof Variable) ? "basic"
                : ((Constant) xid).getObject().toString();
        TimerTask cleanup = new TimerTask() {

            @Override
            public void run() {
                if ("self".equals(protocol)) {
                    prova.executeTask(0, new Runnable() {
                        @Override
                        public void run() {
                            /* synchronized (kb)*/ {
                                removeTemporalRule(p1, p2, ruleid, true, null,
                                        metadata);
                            }
                        }

                    }, ProvaThreadpoolEnum.MAIN);
                } else {
                    prova.executeTask(partitionKey(cid), new Runnable() {
                        @Override
                        public void run() {
                            /*synchronized (kb)*/ {
                                removeTemporalRule(p1, p2, ruleid, true, null,
                                        metadata);
                            }
                        }

                    }, ProvaThreadpoolEnum.CONVERSATION);
                }
            }

        };
        ScheduledFuture<?> future;
        if (period == 0) {
            future = timers.schedule(cleanup, delay, TimeUnit.MILLISECONDS);
        } else {
            future = timers.scheduleAtFixedRate(cleanup, delay, period,
                    TimeUnit.MILLISECONDS);
        }
        if (group != null) {
            group.setTimerFuture(future);
        }
    }

    @Override
    public void addMsg(PList terms) {
        PObj[] data = terms.getFixed();
        PObj lt = data[0];

        String cid = "";
        if (lt instanceof Constant) // Follow up on an existing conversation
        {
            cid = (String) ((Constant) lt).getObject();
        }
        String prot = ((Constant) data[1]).getObject().toString();
        Literal lit = kb.newHeadLiteral("rcvMsg", terms);
        Rule goal = kb.newGoal(new Literal[]{lit,
            kb.newLiteral("fail")});
        // The CONVERSATION pool is the default (see PROVA-39)
        ProvaThreadpoolEnum dest = ProvaThreadpoolEnum.CONVERSATION;
        if (null != prot) {
            switch (prot) {
                case "self":
                    dest = ProvaThreadpoolEnum.MAIN;
                    break;
                case "task":
                    dest = ProvaThreadpoolEnum.TASK;
                    break;
            }
        }
        prova.submitAsync(partitionKey(cid), goal, dest);
    }

    @Override
    public void addMsg(String xid, String agent, String verb, Object payload) {
        PList terms = ProvaListImpl.create(new PObj[]{
            ProvaConstantImpl.create(xid),
            ProvaConstantImpl.create("osgi"),
            ProvaConstantImpl.create(agent),
            ProvaConstantImpl.create(verb),
            payload instanceof Map<?, ?> ? ProvaMapImpl
            .wrapValues((Map<?, ?>) payload) : ProvaConstantImpl
            .wrap(payload)});
        Literal lit = kb.newHeadLiteral("rcvMsg", terms);
        Rule goal = kb.newGoal(new Literal[]{lit,
            kb.newLiteral("fail")});
        // The CONVERSATION pool is the default (see PROVA-39)
        ProvaThreadpoolEnum dest = ProvaThreadpoolEnum.CONVERSATION;
        prova.submitAsync(partitionKey(xid), goal, dest);
    }

    @Override
    public synchronized boolean removeTemporalRule(Predicate predicate,
            Predicate predicate2, long key, boolean recursive,
            PList reaction, Map<String, List<Object>> metadata) {
        boolean rc = true;
        if (log.isDebugEnabled() && reaction != null) {
            log.debug("Removing " + reaction + " at " + key + " with "
                    + metadata);
        }
        if (reaction == null && log.isDebugEnabled()) {
            log.debug("Removing on timeout");
        }
        ProvaGroup group = ruleid2Group.get(key);

        List<Object> groups = null;
        if (metadata != null) {
            groups = metadata.get("group");
        }
        boolean avoidRemovingRule = false;
        
        if (group == null && metadata == null) {
            group = outcomeRuleid2Group.get(key);
        }
        if (group == null && metadata != null) {
            String dynamic = null;
            if (groups != null) {
                dynamic = groups.get(0).toString();
                group = dynamic2Group.get(dynamic);
            }
        }
        if (group != null) {
            if (group.isPermanent() || group.isTemplate()) {
                avoidRemovingRule = true;
            }
            EventDetectionStatus detectionStatus = group.eventDetected(kb,
                    prova, key, reaction, metadata, ruleid2Group);
            if (detectionStatus == EventDetectionStatus.failed) {
                return false;
            }
            if (detectionStatus == EventDetectionStatus.complete) {
                removeGroup(group.getDynamicGroup(), recursive);
            } else if (detectionStatus == EventDetectionStatus.preserved) {
                return rc;
            }
        } else if (metadata != null && metadata.containsKey("count")) {
			// // TODO: This code is never executed
            // log.error("Unexpected code branch");
            List<Object> countList = metadata.get("count");
            int count = (Integer) countList.get(0);
            if (count == 0) // @count(0) reactions never terminate, unless they have an @id
            // and are stopped by a control reaction
            {
                return rc;
            }
            countList.set(0, --count);
            if (count != 0) {
                return rc;
            }
        }

        // Do not remove anything if it is a rcvMult reaction
        if (avoidRemovingRule) {
            return rc;
        }

        predicate.getClauses().removeTemporalClause(key);
        predicate2.getClauses().removeTemporalClause(key);
        if (ruleid2outbound.get(key) == null) {
            return rc;
        }
        List<String> outbound = ruleid2outbound.get(key);
        for (String s : outbound) {
            // Ids of temporal rules to remove
            List<Long> inbound = inbound2ruleids.get(s);
            if (inbound == null) {
                continue;
            }
            if (recursive) {
                for (Iterator<Long> iter = inbound.iterator(); iter.hasNext();) {
                    long i = iter.next();
                    iter.remove();
                    removeTemporalRule(predicate, predicate2, i, false,
                            reaction, metadata);
                }
            }
            inbound2ruleids.remove(s);
        }
        ruleid2outbound.remove(key);
        outcomeRuleid2Group.remove(key);
        return rc;
    }

    /**
     * Remove a dynamic group
     *
     * @param dynamicGroup
     * @param recursive
     */
    private void removeGroup(String dynamicGroup, boolean recursive) {
        ProvaGroup group = dynamic2Group.get(dynamicGroup);
        if (group != null && !group.isExtended()) {
            group.stop();
        }

        List<ProvaDelayedCommand> delayed = DefaultInference.delayedCommands
                .get();
        if (delayed == null) {
			// Running from a timed task: remove the dynamic group for good
            // synchronized(group) {
            cleanupGroup(dynamicGroup);
			// }
            // new ProvaGroupCleanupImpl(dynamicGroup).process(prova);
            return;
        }

		// Running from a goal: restore the group name to dynamic group mapping
        // into ThreadLocal.
        // This is needed in case there is a follow-up reaction for the same
        // group name.
        // Should there be no follow-up in the remainder of the goal, the
        // dynamic group will go for good.
        delayed.add(new ProvaGroupCleanupImpl(dynamicGroup));
        if (tlStatic2Dynamic.get() == null) {
            tlStatic2Dynamic.set(new HashMap<String, String>());
        }
        Map<String, String> s2d = tlStatic2Dynamic.get();
        s2d.put(dynamic2Static.get(dynamicGroup), dynamicGroup);

        if (group != null) {
            if (tlDynamic.get() == null) {
                tlDynamic.set(new HashMap<String, ProvaGroup>());
            }
            Map<String, ProvaGroup> d2g = tlDynamic.get();
            d2g.put(dynamicGroup, group);
        }

    }

    @Override
    public void cleanupGroup(String dynamicGroup) {
        ProvaGroup group = dynamic2Group.get(dynamicGroup);
        if (group != null && group.isExtended()) {
			// Do not clean up if the reaction group was extended with a new
            // reaction
            // Clear the 'extended' flag
            group.setExtended(false);
            return;
        }
        dynamic2Static.remove(dynamicGroup);
        if (group != null) {
            /*synchronized (kb)*/ {
                group.cleanup(kb, prova, ruleid2Group, dynamic2Group);
            }
        }
    }

    @Override
    public void addGroupResult(PList terms) {
        PObj[] fixed = terms.getFixed();
        String dynamicGroup = (String) ((Constant) fixed[0]).getObject();
        ProvaGroup group = dynamic2Group.get(dynamicGroup);
        if (group != null) {
            if (group.isTemplate()) {
                dynamicGroup = this.generateCid();
                group = group.clone();
                group.setDynamicGroup(dynamicGroup);
                group.setTemplate(false);
                group.start(ruleid2Group);
                dynamic2Group.put(dynamicGroup, group);
                if (log.isDebugEnabled()) {
                    log.debug("Group " + dynamicGroup
                            + " is a template/concrete");
                }
            }
            group.addResult((PList) fixed[1]);
            // Generate or reuse the actual dynamic group-id
            if (tlStatic2Dynamic.get() == null) {
                tlStatic2Dynamic.set(new HashMap<String, String>());
            }
            Map<String, String> s2d = tlStatic2Dynamic.get();
            String dynamicGroupPrev = s2d.get(group.getStaticGroup());
            if (dynamicGroupPrev == null) {
                s2d.put(group.getStaticGroup(), dynamicGroup);
            }
            if (tlDynamic.get() == null) {
                tlDynamic.set(new HashMap<String, ProvaGroup>());
            }
            Map<String, ProvaGroup> d2g = tlDynamic.get();
            ProvaGroup gg = d2g.get(dynamicGroup);
            if (gg == null) {
                d2g.put(dynamicGroup, group);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Group " + dynamicGroup + " is missing");
        }

    }

    public static void cleanupThreadlocals() {
        tlStatic2Dynamic.remove();
        tlDynamic.remove();
    }

    @Override
    public void stop() {
        timers.shutdownNow();
    }

    @Override
    public void setService(ProvaMiniService service) {
        this.service = service;
    }

}
