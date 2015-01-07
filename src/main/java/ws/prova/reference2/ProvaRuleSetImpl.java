package ws.prova.reference2;

import com.sun.xml.internal.xsom.impl.scd.Iterators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.RuleSet;
import ws.prova.kernel2.Unification;

public class ProvaRuleSetImpl implements RuleSet, Iterable<Rule> {

    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger(ProvaRuleSetImpl.class);

    private final String symbol;

    private final int arity;

    private List<Rule> clauses;

    private ConcurrentMap<Object, List<Rule>> firstArgMap;

    private ConcurrentMap<String, List<Rule>> srcMap;

    private ConcurrentMap<Long, Rule> temporalRuleMap;

    public ProvaRuleSetImpl(String symbol) {
        this.symbol = symbol;
        this.arity = -1;
    }

    public ProvaRuleSetImpl(String symbol, int arity) {
        this.symbol = symbol;
        this.arity = arity;
    }

    public void setClauses(List<Rule> clauses) {
        if (numClauses()!=0)
            this.getClauses().clear();
        this.getClauses().addAll(clauses);
    }

    @Override
    public int numClauses() {
        if (clauses == null) return 0;
        return clauses.size();
    }
    
    @Override
    public List<Rule> getClauses() {
        if (clauses == null) {
             clauses = new ArrayList<Rule>();
        }
        return clauses;
    }

    @Override
    /**
     * Now implements pre-filtering by spotting mismatched constants in the
     * source and target arguments
     */
    public synchronized List<Rule> getClauses(Object key, PObj[] source) {
        List<Rule> bound = getFirstArgMap().get(key);
        List<Rule> free = getFirstArgMap().get("@");
        if (bound == null) {
            return free;
        } else if (free == null) {
            return bound;
        }
        // Merge the two
        final int boundSize = bound.size();
        final int freeSize = free.size();
        List<Rule> merged = new ArrayList<Rule>(boundSize + freeSize);
        int i1 = 0;
        int i2 = 0;
        while (i1 < boundSize && i2 < freeSize) {
            Rule next;
            if (bound.get(i1).getAbsRuleId() > free.get(i2).getAbsRuleId()) {
                next = free.get(i2++);
            } else {
                next = bound.get(i1++);
            }
            PObj[] target = next.getHead().getTerms().getFixed();
            if (preFilter(source, target)) {
                merged.add(next);
            }
        }
        while (i1 < boundSize) {
            Rule next = bound.get(i1++);
            PObj[] target = next.getHead().getTerms().getFixed();
            if (preFilter(source, target)) {
                merged.add(next);
            }
        }
        while (i2 < freeSize) {
            Rule next = free.get(i2++);
            PObj[] target = next.getHead().getTerms().getFixed();
            if (preFilter(source, target)) {
                merged.add(next);
            }
        }
        return merged;
    }

    /**
     * Reject obvious mismatches between constants
     *
     * @param source goal arguments
     * @param target rule head arguments
     * @return true if no constants are mismatched
     */
    private boolean preFilter(PObj[] source, PObj[] target) {
        for (int i = 1; i < source.length; i++) {
            if (source[i] instanceof Constant && target[i] instanceof Constant
                    && !((Constant) source[i]).matched((Constant) target[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void removeClauses(Object key) {
        if (key instanceof Long && ((Long) key) < 0) {
            Rule rule = temporalRuleMap.remove(-((Long) key));
            rule.setRemoved();
            getClauses().remove(rule);
            return;
        }
        List<Rule> bound = getFirstArgMap().get(key);
        for (Rule rule : bound) {
            rule.setRemoved();
            getClauses().remove(rule);
        }
    }

    /**
     * This is only used for removing temporal rules in inline reactions,
     * including rcvMsg and @temporal_rule_control. TODO: Optimise rule storage
     * for multi-key access
     */
    @Override
    public synchronized void removeTemporalClause(long key) {
        Rule rule = temporalRuleMap.remove(-key);
        if (rule == null) {
            return;
        }
        rule.setRemoved();
        if (!"@".equals(rule.getFirstArg())) {
            List<Rule> children = getFirstArgMap().get(rule.getFirstArg());
            children.remove(rule);
        }
        getClauses().remove(rule);
    }

    /**
     * Remove only the clause that is subsumed by the query.
     */
    @Override
    public boolean removeClausesByMatch(KB kb, PObj[] data) {
        Literal goalLit = kb.newLiteral(data);
        Rule query = kb.newGoal(new Literal[]{goalLit});
        Goal goal = new ProvaGoalImpl(query);
        Unification unification = null;
        while ((unification = goal.nextUnification(kb)) != null) {
            boolean result = unification.unify();
            if (!result || !unification.targetUnchanged()) {
                continue;
            }
            goal.removeTarget();
            return true;
        }
        return false;
    }

    /**
     * Remove only the clauses that are subsumed by the query.
     */
    @Override
    public boolean removeAllClausesByMatch(KB kb,
            PObj[] data) {
        Literal goalLit = kb.newLiteral(data);
        Rule query = kb.newGoal(new Literal[]{goalLit});
        Goal goal = new ProvaGoalImpl(query);
        Unification unification = null;
        while ((unification = goal.nextUnification(kb)) != null) {
            boolean result = unification.unify();
            if (result && unification.targetUnchanged()) {
                goal.removeTarget();
            }
        }
        return true;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public int getArity() {
        return arity;
    }

    @Override
    public synchronized void add(Rule clause) {
        final Object firstArg = clause.getFirstArg();
        if (firstArg != null) {
            List<Rule> rules = getFirstArgMap().get(firstArg);
            if (rules == null) {                
                rules = new ArrayList();
                getFirstArgMap().put(firstArg, rules);
            }
            rules.add(clause);
        }
        if (clause.getRuleId() < 0) {
            // It is a temporal rule, so store it in the map
            getTemporalRuleMap().put(clause.getRuleId(), clause);
        }
        // TODO: understand the implications of this
        
        if ((numClauses() > 0) && (getClauses().contains(clause)))
            return;
        
        getClauses().add(clause);        
    }

    @Override
    public synchronized void addA(Rule clause) {
        final Object firstArg = clause.getFirstArg();
        if (firstArg != null) {
            List<Rule> rules = getFirstArgMap().get(firstArg);
            if (rules == null) {
                rules = new ArrayList<Rule>();
                getFirstArgMap().put(firstArg, rules);
            }
            rules.add(0, clause);
        }
        // TODO: understand the implications of this
        if (!clauses.contains(clause)) {
            getClauses().add(0, clause);
        }
    }

    @Override
    public Object size() {
        return numClauses();
    }

    @Override
    public synchronized void addAll(RuleSet ruleSet) {
        for (Rule clause : ruleSet.getClauses()) {
            add(clause);
        }
    }

    @Override
    public synchronized void addRuleToSrc(ProvaRuleImpl rule, String src) {
        List<Rule> rules = getSrcMap().get(src);
        if (rules == null) {
            rules = new ArrayList<Rule>();
            getSrcMap().put(src, rules);
        }
        rules.add(rule);
    }

    @Override
    public synchronized void removeClausesBySrc(String src) {
        List<Rule> rules = getSrcMap().remove(src);
        if (rules == null) {
            return;
        }
        for (Rule rule : rules) {
            rule.setRemoved();
            getClauses().remove(rule);
        }
    }

    @Override
    public Unification nextMatch(KB kb, Goal goal) {
        Unification unification = null;
        while ((unification = goal.nextUnification(kb)) != null) {
            boolean result = unification.unify();
            if (result) {
                return unification;
            }
        }
        return null;
    }

    /**
     * @return the firstArgMap
     */
    public ConcurrentMap<Object, List<Rule>> getFirstArgMap() {
        if (firstArgMap == null)
            firstArgMap = new ConcurrentHashMap<>();
        return firstArgMap;
    }

    /**
     * @return the srcMap
     */
    public ConcurrentMap<String, List<Rule>> getSrcMap() {
        if (srcMap == null) {
            srcMap = new ConcurrentHashMap<>();
        }
        return srcMap;
    }

    public ConcurrentMap<Long, Rule> getTemporalRuleMap() {
        if (this.temporalRuleMap == null) {
            this.temporalRuleMap = new ConcurrentHashMap<Long, Rule>();
        }
        return temporalRuleMap;
    }

    @Override
    public Iterator<Rule> iterator() {
        if (numClauses() == 0)  
            return Iterators.empty();
        return getClauses().iterator();
    }

    
    
}
