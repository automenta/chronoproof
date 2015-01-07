package ws.prova.kernel2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import ws.prova.agent2.ProvaReagentImpl;

public interface Rule extends Runnable {

    public void collectVariables();

    public List<Variable> getVariables();

    public List<Variable> cloneVariables();

    public Literal getHead();

    public Literal[] getBody();

    public long getRuleId();

    public Rule cloneRule();

    public boolean advance();

    public Literal getTop();

    public void setVariables(List<Variable> variables);

    public void substituteVariables(VariableIndex[] varsMap);

    public void setRuleId(long ruleId);

    public int getOffset();

    public void addBodyLiteral(Literal literal);

    public void replaceTopBodyLiteral(List<Literal> newLiterals);

    public Rule cloneRule(boolean cloneVariables);

    public void collectVariables(int offset);

    public Object getFirstArg();

    public void removeAt(int index);

    public void setRemoved();

    public boolean isRemoved();

    public boolean isCut();

    public String getSourceCode();

    public void computeSourceCode();

    public void setMetadata(String property, List<Object> value);

    public List<Object> getMetadata(String property);

    public void addMetadata(Map<String, List<Object>> m);

    public Map<String, List<Object>> getMetadata();

    public void setLine(int line);

    public int getLine();

    public Literal[] getGuardedBody(Literal sourceLiteral);

    public void setSrc(List<Object> value);

    public long getAbsRuleId();

    public void onRejected(RejectedExecutionException r);

    public void setReagent(ProvaReagentImpl aThis);

}
