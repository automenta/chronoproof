package ws.prova.kernel2.cache;

import java.util.Collection;
import ws.prova.kernel2.PList;
import ws.prova.reference2.cache.ProvaCacheStateImpl.ProvaCacheAnswerKey;

public interface Answers {

	public boolean addSolution(ProvaCacheAnswerKey key, PList terms);

	public Collection<PList> getSolutions();

}
