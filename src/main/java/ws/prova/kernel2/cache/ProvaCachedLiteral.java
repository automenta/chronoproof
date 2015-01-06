package ws.prova.kernel2.cache;

import ws.prova.kernel2.ProvaGoal;
import ws.prova.kernel2.ProvaLiteral;

public interface ProvaCachedLiteral extends ProvaLiteral {

        @Override
	public void setGoal(ProvaGoal provaGoal);

        @Override
	public ProvaCacheState getCacheState();

        @Override
	public ProvaLocalAnswers getAnswers();

        @Override
	public ProvaGoal getGoal();

}
