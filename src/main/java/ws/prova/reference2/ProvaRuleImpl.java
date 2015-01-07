package ws.prova.reference2;

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
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaRuleImpl implements Rule {

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

    private ProvaRuleImpl() {
    }

    public ProvaRuleImpl(long ruleId, Literal head, Literal[] body) {
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
            getSourceCode();
        }
    }

    public ProvaRuleImpl(long ruleId, Literal head,
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
        ProvaRuleImpl newRule = new ProvaRuleImpl();
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
    public ProvaRuleImpl(Literal[] body) {
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

    private ProvaRuleImpl(ProvaRuleImpl o) {
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

    private ProvaRuleImpl(ProvaRuleImpl o, boolean cloneVariables) {
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

    public ProvaRuleImpl(long ruleId, Literal head, Literal[] body,
            List<Variable> variables) {
        this.ruleId = ruleId;
        int size = variables.size();
        for (int i = 0; i < size; i++) {
            this.variables.add(variables.get(i).clone(ruleId));
        }
        this.head = head;
        this.body = body;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    @Override
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

    @Override
    public Literal getHead() {
        return head;
    }

    public void setBody(Literal[] body) {
        this.body = body;
    }

    @Override
    public Literal[] getBody() {
        return body;
    }

    /**
     * Get body with a prefix containing guard literals if sourceLiteral is
     * ProvaGuardedLiteralImpl
     */
    @Override
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

    @Override
    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public long getRuleId() {
        return ruleId;
    }

    @Override
    public long getAbsRuleId() {
        return ruleId >= 0 ? ruleId : -ruleId;
    }

    @Override
    public Rule cloneRule() {
        ProvaRuleImpl newRule = new ProvaRuleImpl(this);
        return newRule;
    }

    @Override
    public Rule cloneRule(boolean cloneVariables) {
        if (this.variables.isEmpty()) {
            return this;
        }
        ProvaRuleImpl newRule = new ProvaRuleImpl(this, cloneVariables);
        return newRule;
    }

    @Override
    public List<Variable> cloneVariables() {
        int size = variables.size();
        List<Variable> newVariables = new ArrayList<Variable>(size);
        for (int i = 0; i < size; i++) {
            newVariables.add(variables.get(i).clone());
        }
        return newVariables;
    }

    @Override
    /**
     * Advance the goal to the next query
     *
     * @return True if the next query is fail
     */
    public boolean advance() {
        offset++;
        return offset != body.length && body[offset].getTerms() == null;
    }

    @Override
    public Literal getTop() {
        return offset < body.length ? body[offset] : null;
    }

    @Override
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

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void addBodyLiteral(Literal literal) {
        Literal[] newBody = new Literal[body.length + 1];
        System.arraycopy(body, 0, newBody, 0, body.length);
        newBody[body.length] = literal;
        body = newBody;
        collectVariables(offset);
    }

    @Override
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

    @Override
    public Object getFirstArg() {
        return firstArg;
    }

    @Override
    public void removeAt(int index) {
        if (offset + index >= body.length) {
            return;
        }
        Literal[] newBody = new Literal[body.length - 1 - offset];
        System.arraycopy(body, offset + index + 1, newBody, index, body.length - 2 - offset);
        newBody[0] = body[0];
        body = newBody;
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }

    @Override
    public void setRemoved() {
        this.removed = true;
    }

    @Override
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

    @Override
    public String getSourceCode() {
        if (log.isDebugEnabled() && this.sourceCode == null) {
            if (head != null && head.getPredicate().getSymbol().length() == 0) {
                return toString();
            }
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
        return this.sourceCode;
    }

    @Override
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

    @Override
    public void setMetadata(String property, List<Object> value) {
        if (metadata == null) {
            metadata = new HashMap<String, List<Object>>();
        }
        metadata.put(property, value);
    }

    @Override
    public void setSrc(List<Object> value) {
        if (metadata == null) {
            metadata = new HashMap<String, List<Object>>();
        }
        metadata.put("src", value);
        final String src = (String) value.get(0);
        final Predicate predicate = head.getPredicate();
        predicate.getClauses().addRuleToSrc(this, src);
        predicate.kb().addClauseSetToSrc(predicate.getClauses(), src);
    }

    @Override
    public List<Object> getMetadata(String property) {
        return metadata == null ? null : metadata.get(property);
    }

    @Override
    public void addMetadata(Map<String, List<Object>> m) {
        if (m == null) {
            return;
        }
        if (metadata == null) {
            this.metadata = new HashMap<String, List<Object>>();
        }
        this.metadata.putAll(m);
    }

    @Override
    public Map<String, List<Object>> getMetadata() {
        if (metadata != null) {
            return metadata;
        }
        return Collections.emptyMap();
    }

    @Override
    public void setLine(int line) {
        if (metadata == null) {
            metadata = new HashMap<String, List<Object>>();
        }
        metadata.put("line", Collections.<Object>singletonList(Integer.toString(line)));
        this.line = line;
    }

    @Override
    public int getLine() {
        return this.line;
    }

    @Override
    public void setReagent(ProvaReagentImpl r) {
        this.reagent = r;
    }

    @Override
    public void run() {
        start = System.currentTimeMillis();
        try {
            Derivation result = reagent.submitSyncInternal(this);
            onFinished(result);
        } catch (RuntimeException e) {
            onError(e);
        }
    }

    @Override
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
