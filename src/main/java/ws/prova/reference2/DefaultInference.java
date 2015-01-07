package ws.prova.reference2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Operation;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Inference;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.builtins.ProvaFailImpl;
import ws.prova.reference2.messaging.ProvaDelayedCommand;
import ws.prova.reference2.messaging.ProvaMessengerImpl;

public class DefaultInference implements Inference {

    private final static Logger log = Logger.getLogger("prova");

    private KB kb;

    private final Deque<Derivation> tabledNodes;

    private Derivation node;

    private final ProvaDerivationStepCounter counter;

    private Reagent prova;

    public static ThreadLocal<List<ProvaDelayedCommand>> delayedCommands = new ThreadLocal<List<ProvaDelayedCommand>>();

    public DefaultInference(KB kb, Rule query) {
        this.kb = kb;

        tabledNodes = new ArrayDeque<Derivation>();
        counter = new ProvaDerivationStepCounter();
        node = new ProvaDerivationNodeImpl();
        node.setFailed(true);
        node.setId(counter.next());
        node.setCut(false);
        node.setQuery(query);
        node.setCurrentGoal(new ProvaGoalImpl(query));
    }

    public DefaultInference(DefaultKB kb, Rule rule, Reagent r) {
        this(kb, rule);
        setReagent(r);
    }

    @Override
    public Derivation run() {
        // This might be set if it is a nested resolution run, for example, from consult()
        List<ProvaDelayedCommand> delayed0 = delayedCommands.get();
        try {
            if (delayed0 == null) {               
                delayedCommands.set(new ArrayList());
            }
            return _run();
        } finally {
            if (delayedCommands != null) {
                List<ProvaDelayedCommand> delayed = delayedCommands.get();
                for (Iterator<ProvaDelayedCommand> iter = delayed.iterator(); iter.hasNext();) {
                    ProvaDelayedCommand message = iter.next();
                    iter.remove();
                    message.process(prova);
                }
                if (delayed0 == null) {
                    delayedCommands.remove();
                }
            }
            ProvaMessengerImpl.cleanupThreadlocals();
        }
    }

    public Derivation _run() {
        List<Literal> newLiterals = new ArrayList<Literal>();
        tabledNodes.push(node);
        
        Rule query;
        
        while (!tabledNodes.isEmpty()) {
            node = tabledNodes.pop();
            query = node.getQuery();
            
            /*if (log.isDebugEnabled()) {
                log.debug(query);
            }*/
            
            final Goal goal = node.getCurrentGoal();

            if (goal == null) {
                node.setFailed(true);
                return node; // fail
            }

            Literal goalLiteral = goal.getGoal();
            Predicate predicate = goalLiteral.getPredicate();
            if (predicate instanceof ProvaFailImpl) {
                if (node.getParent() == null) {
                    node.setFailed(true);
                    return node; // fail
                }
                node = node.getParent();
                tabledNodes.push(node);
                continue;
            }

            final String symbol = predicate.getSymbol();
            if (symbol.equals("metadata")) {
                goal.updateMetadataGoal();
                goalLiteral = goal.getGoal();
                predicate = goalLiteral.getPredicate();
            }

            if (predicate instanceof Operation) {
                Operation builtin = (Operation) predicate;
                newLiterals.clear();
                
                boolean result;
                //try {
                    result = builtin.process(prova, node, goal, newLiterals, query);
                //}
//                catch (Throwable t) {
//                    log.error(t);
//                    continue;
//                }
                
                if (!result) {
                    node = node.getParent();
                } else {
                    final int size = newLiterals.size();
                    if (size == 1) {
                        // New substitute goal has appeared
                        query.replaceTopBodyLiteral(newLiterals);
                        // Goal update is sufficient
                        goal.update();
                    } else if (size > 1) {
						// This is interpreted as more than one literal replacing the current (top) goal.
                        // Non-deterministic choice is dealt with inside the built-ins that can create
                        //    virtual predicate (see, for example, ProvaElementImpl).
                        query.replaceTopBodyLiteral(newLiterals);
                        // This requires a new goal
                        node.setCurrentGoal(new ProvaGoalImpl(query));
                    } else {
                        boolean fail = query.advance();
                        if (fail) {
                            if (node.getParent() == null) {
                                node.setFailed(true);
                                return node; // fail
                            }
                            node = node.getParent();
                            tabledNodes.push(node);
                            continue;
                        }
                        // Goal update is sufficient
                        goal.update();
                    }
                }
                if (node != null) {
                    tabledNodes.push(node);
                }
                continue;
            }

            if ("cut".equals(symbol)) {
                // Check for cut
                boolean isCut = checkCut(node, goal);
                if (isCut) {
                    tabledNodes.push(node);
                    continue;
                }
            }

            Derivation newNode = null;
            Unification unification = null;
            goal.updateGround();
            
            while ((unification = goal.nextUnification(kb)) != null) {
                boolean result = unification.unify();
                if (!result) {
                    continue;
                }
                
                /*
                if (log.isDebugEnabled()) {
                    log.debug(">>> [" + unification.getTarget().getMetadata() + ']' + unification.getTarget().getSourceCode());
                }
                */
                
                Rule newQuery = unification.generateQuery(symbol, kb, query, node);
                if (goal.isSingleClause()) {
                    node.setCurrentGoal(new ProvaGoalImpl(newQuery));
                    node.setQuery(newQuery);
                    break;
                }
                newNode = new ProvaDerivationNodeImpl();
                newNode.setQuery(newQuery);
                newNode.setParent(node);
                newNode.setId(counter.next());
                newNode.setCut(false);
                newNode.setCurrentGoal(new ProvaGoalImpl(newQuery));
                tabledNodes.push(newNode);
                break;
            }

            if (goal.isSingleClause()) {
                tabledNodes.push(node);
                continue;
            }

            if (newNode == null) {
                node = node.getParent();
                if (node != null) {
                    tabledNodes.push(node);
                }
            }
        }
        return null;
    }

    /**
     * Check whether a goal has a cut.
     *
     * @param node
     * @param query.get
     * @return 0, if no cut found; otherwise, the node-id of the CUT
     */
    private boolean checkCut(final Derivation node, final Goal goal) {
        // The assumption is that cuts are only occurring in instances of TmpClause
        // in the first proof step, goal is an instance of Fact (originating from the query),
        // but then the predicate cannot be a cut anyway
        boolean rc = false;
        Rule query = goal.getQuery();
        Literal top = query.getTop();
        String symbol = top.getPredicate().getSymbol();
        while ("cut".equals(symbol)) {
            rc = true;
            PObj ref = top.getTerms().getFixed()[0];
            if (ref instanceof VariableIndex) {
                log.error("checkCut: " + ref);
            }
            Derivation cutNode = (Derivation) ((Constant) ref).getObject();
            Goal cutGoal = cutNode.getCurrentGoal();
            cutGoal.setCut(true);
            cutNode.setCut(true);
            query.advance();
            top = query.getTop();
            goal.setGoal(top);
            // This is key: avoid backtracking all the way back to the cut node's parent
            node.setParent(cutNode.getParent());
            if (top == null) {
                break;
            }
            symbol = top.getPredicate().getSymbol();
        }
        if (rc) {
            node.setCurrentGoal(new ProvaGoalImpl(query));
        }
        return rc;
    }

    public void setKb(KB kb) {
        this.kb = kb;
    }

    public KB getKb() {
        return kb;
    }

    @Override
    public void setReagent(Reagent prova) {
        this.prova = prova;
    }

}
