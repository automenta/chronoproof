package ws.prova.kernel2.messaging;

import java.util.List;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.Rule;

public interface ProvaWorkflows {

	boolean init_join(Literal literal, List<Literal> newLiterals,
			Rule query);

	boolean join_test(Literal literal, List<Literal> newLiterals,
			Rule query);

	boolean init_predicate_join(Literal literal,
			List<Literal> newLiterals, Rule query);

	boolean predicate_join_test(Literal literal,
			List<Literal> newLiterals, Rule query);

	boolean predicate_join_exit(Literal literal,
			List<Literal> newLiterals, Rule query);

	public boolean stop_predicate_join(Literal literal,
			List<Literal> newLiterals, Rule query);

}
