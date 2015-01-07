package ws.prova.reference2.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.cache.Answers;
import ws.prova.reference2.cache.ProvaCacheStateImpl.ProvaCacheAnswerKey;

public class ProvaLocalAnswersImpl implements Answers {

	private final Map<ProvaCacheAnswerKey,PList> answers;
	
	public ProvaLocalAnswersImpl() {
		this.answers = new HashMap<ProvaCacheAnswerKey,PList>();
	}

	@Override
	public boolean addSolution(ProvaCacheAnswerKey key, PList terms) {
		PList oldAnswer = answers.get(key);
		if( oldAnswer!=null )
			return false;
		answers.put(key, terms);
		return true;
	}

	@Override
	public Collection<PList> getSolutions() {
		return answers.values();
	}

}
