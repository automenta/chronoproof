package ws.prova.kernel2;

import java.lang.Runnable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import org.apache.log4j.Logger;
import ws.prova.agent2.ProvaReagentImpl;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.ProvaGuardedLiteralImpl;

public class Rule implements Runnable {

    private final static Logger log = Logger.getLogger("prova");

    private long ruleId;

    private List<Variable> variables = new ArrayList<Variable>();

    private Literal head;

    private Literal[] body;

    private int offset = 0;

    private Object firstArg;

    private boolean removed = false;

    private int cut = 0;

    private String sourceCode;

    private Map<String, List<Object>> metadata;

    private int line;

    transient private ProvaReagentImpl reagent = null;
    private long start;

    private Rule() {
    }

    public Rule(long ruleId, Literal head, Literal[] body) {
        this.ruleId = ruleId;
        setHead(head);
        this.body = body;
        if (ruleId != 0) {
            collectVariables();
        }
        for (Variable variable : variables) {
            variable.setRuleId(ruleId);
        }
        if (this.head != null) {
            this.head.addClause(this);
        }
        if (log.isDebugEnabled()) {
            logSourceCode();
        }
    }

    public Rule(long ruleId, Literal head,
            Literal[] body, boolean addInFront) {
        this.ruleId = ruleId;
        setHead(head);
        this.body = body;
        if (ruleId != 0) {
            collectVariables();
        }
        for (Variable variable : variables) {
            variable.setRuleId(ruleId);
        }
        if (this.head != null) {
            this.head.addClauseA(this);
        }
    }

    public static Rule createVirtualRule(long ruleId, Literal head, Literal[] body) {
        Rule newRule = new Rule();
        newRule.ruleId = ruleId;
        newRule.setHead(head);
        newRule.body = body;
        if (ruleId != 0) {
            newRule.collectVariables();
        }
        for (Variable variable : newRule.variables) {
            variable.setRuleId(ruleId);
        }
        return newRule;
    }

    /*
     * Generate rule for a goal
     */
    public Rule(Literal[] body) {
        this.ruleId = 0;
//		this.head = null;
        this.body = body;
        collectVariables();
        for (Variable variable : variables) {
            variable.setRuleId(ruleId);
        }
        if (this.head != null) {
            this.head.addClause(this);
        }
    }

    private Rule(Rule o) {
        this.ruleId = o.ruleId;
        this.variables = o.cloneVariables();
        setHead(o.head);
        if (o.offset != 0 && o.body != null && o.body.length != 0) {
            this.body = new Literal[o.body.length - o.offset];
            System.arraycopy(o.body, o.offset, this.body, 0, o.body.length - o.offset);
            return;
        }
        this.body = o.body;
    }

    private Rule(Rule o, boolean cloneVariables) {
        this.ruleId = o.ruleId;
        this.variables = cloneVariables ? o.cloneVariables() : o.getVariables();
        this.head = o.head;
        if (o.offset != 0 && o.body != null && o.body.length != 0) {
            this.body = new Literal[o.body.length - o.offset];
            System.arraycopy(o.body, o.offset, this.body, 0, o.body.length - o.offset);
            return;
        }
        this.body = o.body;
        this.sourceCode = o.sourceCode;
        this.line = o.line;
        this.metadata = o.metadata;
    }

    public Rule(long ruleId, Literal head, Literal[] body,
            List<Variable> variables) {
        this.ruleId = ruleId;
        int size = variables.size();
        for (int i = 0; i < size; i++) {
            this.variables.add(variables.get(i).clone(ruleId));
        }
        this.head = head;
        this.body = body;
    }

    
    public void collectVariables() {
        if (head != null) {
            head.collectVariables(ruleId, variables);
        }
        if (body != null) {
            for (Literal literal : body) {
                literal.collectVariables(ruleId, variables);
            }
        }
    }

    
    public void collectVariables(int offset) {
        if (head != null) {
            head.collectVariables(ruleId, variables);
        }
        if (body != null) {
            for (int i = offset; i < body.length; i++) {
                body[i].collectVariables(ruleId, variables);
            }
        }
    }

    
    public void substituteVariables(VariableIndex[] varsMap) {
        if (head != null) {
            head.substituteVariables(varsMap);
        }
        if (body != null) {
            for (Literal literal : body) {
                literal.substituteVariables(varsMap);
            }
        }
    }

    
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    
    public List<Variable> getVariables() {
        return variables;
    }

    public void setHead(Literal head) {
        if (head == null) {
            return;
        }
        this.head = head;
        final PObj[] fixed = head.getTerms().getFixed();
        if (fixed != null && fixed.length != 0) {
            if (fixed[0] instanceof Constant) {
                this.firstArg = ((Constant) fixed[0]).getObject();
            } else {
                this.firstArg = "@";
            }
        }
    }

    
    public Literal getHead() {
        return head;
    }

    public void setBody(Literal[] body) {
        this.body = body;
    }

    
    public Literal[] getBody() {
        return body;
    }

    /**
     * Get body with a prefix containing guard literals if sourceLiteral is
     * ProvaGuardedLiteralImpl
     */
    
    public Literal[] getGuardedBody(Literal sourceLiteral) {
        if (sourceLiteral instanceof ProvaGuardedLiteralImpl) {
            List<Literal> guard = ((ProvaGuardedLiteralImpl) sourceLiteral).getGuard();
            Literal[] guardedBody = new Literal[guard.size() + body.length];
            int index = 0;
            for (Literal g : guard) {
                guardedBody[index++] = g;
            }
            System.arraycopy(body, 0, guardedBody, guard.size(), body.length);
            return guardedBody;
        }
        return body;
    }

    
    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    
    public long getRuleId() {
        return ruleId;
    }

    
    public long getAbsRuleId() {
        return ruleId >= 0 ? ruleId : -ruleId;
    }

    
    public Rule cloneRule() {
        Rule newRule = new Rule(this);
        return newRule;
    }

    
    public Rule cloneRule(boolean cloneVariables) {
        if (this.variables.isEmpty()) {
            return this;
        }
        Rule newRule = new Rule(this, cloneVariables);
        return newRule;
    }

    
    public List<Variable> cloneVariables() {
        int size = variables.size();
        List<Variable> newVariables = new ArrayList<Variable>(size);
        for (int i = 0; i < size; i++) {
            newVariables.add(variables.get(i).clone());
        }
        return newVariables;
    }

    
    /**
     * Advance the goal to the next query
     *
     * @return True if the next query is fail
     */
    public boolean advance() {
        offset++;
        return offset != body.length && body[offset].getTerms() == null;
    }

    
    public Literal getTop() {
        return offset < body.length ? body[offset] : null;
    }

    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (head != null) {
            Map<String, List<Object>> metadata = head.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                sb.append(metadata);
                sb.append(" ");
            }
            sb.append(head.cloneWithVariables(ruleId, variables));
        }
        if (body != null) {
            sb.append(":-");
            for (int i = offset; i < body.length; i++) {
                if (i != 0) {
                    sb.append(',');
                } else {
                    Map<String, List<Object>> metadata = body[i].getMetadata();
                    if (metadata != null && !metadata.isEmpty()) {
                        sb.append(metadata);
                        sb.append(" ");
                    }
                }
                sb.append(body[i].cloneWithVariables(ruleId, variables));
            }
        }
        return sb.toString();
    }

    
    public int getOffset() {
        return offset;
    }

    
    public void addBodyLiteral(Literal literal) {
        Literal[] newBody = new Literal[body.length + 1];
        System.arraycopy(body, 0, newBody, 0, body.length);
        newBody[body.length] = literal;
        body = newBody;
        collectVariables(offset);
    }

    
    public void replaceTopBodyLiteral(List<Literal> literals) {
        if (literals.size() == 1) {
            body[offset] = literals.get(0);
            return;
        }
        int size = literals.size() - 1;
        Literal[] newBody = new Literal[body.length + size - offset];
        newBody[0] = literals.get(0);
        System.arraycopy(body, offset + 1, newBody, 1 + size, body.length - offset - 1);
        for (int i = 1; i <= size; i++) {
            newBody[i] = literals.get(i);
        }
        body = newBody;
        offset = 0;
        // We must not collect variables here as substitutions of constants are made after the unification stage
//		collectVariables(offset);
    }

    
    public Object getFirstArg() {
        return firstArg;
    }

    
    public void removeAt(int index) {
        if (offset + index >= body.length) {
            return;
        }
        Literal[] newBody = new Literal[body.length - 1 - offset];
        System.arraycopy(body, offset + index + 1, newBody, index, body.length - 2 - offset);
        newBody[0] = body[0];
        body = newBody;
    }

    
    public boolean isRemoved() {
        return this.removed;
    }

    
    public void setRemoved() {
        this.removed = true;
    }

    
    public boolean isCut() {
        if (cut == 0) {
            cut = -1;
            if (body != null && body.length != 0) {
                String symbol = body[offset].getPredicate().getSymbol();
                if (symbol.equals("cut")) {
                    cut = 1;
                }
            }
        }
        return cut > 0;
    }

    public void logSourceCode() {
        if (this.sourceCode == null || (head != null && head.getPredicate().getSymbol().length() == 0))
            return;
            
        if (log.isDebugEnabled() && this.sourceCode == null) {
            
            StringBuilder sb = new StringBuilder();
            if (head != null) {
                sb.append(head.getSourceCode());
            }
            sb.append(":-");
            if (body != null) {
                for (int i = offset; i < body.length; i++) {
                    if (i != offset) {
                        sb.append(',');
                    }
                    sb.append(body[i].getSourceCode());
                }
            }
            sb.append('.');
            this.sourceCode = sb.toString();
        }        
    }
    public String getSourceCode() {
        return this.sourceCode;
    }

    
    public void computeSourceCode() {
        if (log.isDebugEnabled()) {
            if (head != null) {
                head.setSourceCode(head.cloneWithVariables(variables).toString());
            }
            if (body == null) {
                return;
            }
            for (int i = offset; i < body.length; i++) {
                body[i].setSourceCode(body[i].cloneWithVariables(variables).toString());
            }
        }
    }

    
    public void setMetadata(String property, List<Object> value) {
        if (metadata == null) {
            metadata = new HashMap<String, List<Object>>();
        }
        metadata.put(property, value);
    }

    
    public void setSrc(List<Object> value) {        
        setMetadata("src", value);
        final String src = (String) value.get(0);
        final Predicate predicate = head.getPredicate();
        predicate.getClauses().addRuleToSrc(this, src);
        predicate.kb().addClauseSetToSrc(predicate.getClauses(), src);
    }

    
    public List<Object> getMetadata(String property) {
        return metadata == null ? null : metadata.get(property);
    }

    
    public void addMetadata(Map<String, List<Object>> m) {
        if (m == null) {
            return;
        }
        if (metadata == null) {
            this.metadata = new HashMap<String, List<Object>>();
        }
        this.metadata.putAll(m);
    }

    
    public Map<String, List<Object>> getMetadata() {
        if (metadata != null) {
            return metadata;
        }
        return Collections.emptyMap();
    }

    
    public void setLine(int line) {
        if (metadata == null) {
            metadata = new HashMap<String, List<Object>>();
        }
        metadata.put("line", Collections.<Object>singletonList(Integer.toString(line)));
        this.line = line;
    }

    
    public int getLine() {
        return this.line;
    }

    
    public void setReagent(ProvaReagentImpl r) {
        this.reagent = r;
    }

    
    public void run() {
        start = System.currentTimeMillis();
        try {
            Derivation result = reagent.submitSyncInternal(this);
            onFinished(result);
        } catch (RuntimeException e) {
            onError(e);
        }
    }

    
    public void onRejected(RejectedExecutionException r) {                
        log.error(new Object[] { this, "Rejected", r });
    }
    
    private void onFinished(Derivation result) {
        if (log.isDebugEnabled())
            log.debug(new Object[] { this, "Finished", result, System.currentTimeMillis() - start });
    }

    private void onError(RuntimeException e) {                        
        log.error(new Object[] { this, "Error", e, System.currentTimeMillis() - start });
        if (log.isDebugEnabled())
            e.printStackTrace();
    }


}
