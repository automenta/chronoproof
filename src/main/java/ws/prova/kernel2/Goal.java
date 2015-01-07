package ws.prova.kernel2;

import java.util.Iterator;
import java.util.List;

public interface Goal {

	public Rule next();

	public Literal getGoal();

	public Unification nextUnification(KB kb);

	public Rule getQuery();

	public void setCut(boolean cut);

	public void setGoal(Literal top);

	public boolean hasNext();

	public Iterator<Rule> getIterator();

	public void addAnswer(PList terms);

	public void addOuterAnswer(PList terms);

	public boolean isSingleClause();

	public void removeTarget();

	public void updateMetadataGoal();

	public Object lookupMetadata(String variable, List<Variable> variables);

	public Rule getLastMatch();
	
	public void setLastMatch(Rule lastMatch);

	boolean isCut();

	public void update();

	public void updateGround();

}
