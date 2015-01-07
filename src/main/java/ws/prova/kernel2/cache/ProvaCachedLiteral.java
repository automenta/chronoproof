package ws.prova.kernel2.cache;

import ws.prova.kernel2.Goal;
import ws.prova.kernel2.Literal;

public interface ProvaCachedLiteral extends Literal {

        @Override
	public void setGoal(Goal provaGoal);

        @Override
	public ProvaCacheState getCacheState();

        @Override
	public Answers getAnswers();

        @Override
	public Goal getGoal();

}
