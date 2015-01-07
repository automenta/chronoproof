package ws.prova.reference2.builtins;

import java.io.PrintWriter;
import java.util.List;
import ws.prova.agent2.Reagent;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Variable;

public class ProvaPrintImpl extends ProvaBuiltinImpl {

    public ProvaPrintImpl(KB kb, String symbol) {
        super(kb, symbol);
    }
    
    public ProvaPrintImpl(KB kb) {
        super(kb, "print");
    }

    protected boolean newline() { return false; }
    
    @Override
    public boolean process(Reagent prova, Derivation node,
            Goal goal, List<Literal> newLiterals, Rule query) {
        Literal literal = goal.getGoal();
        List<Variable> variables = query.getVariables();
        PList terms = (PList) literal.getTerms().cloneWithVariables(variables);
        PObj[] data = terms.getFixed();
        if (data.length > 2) {
            return false;
        }
        PrintWriter writer = kb.getPrinter();
        String separator = "";
        if (data.length == 2) {
            separator = data[1].toString(variables);
        }
        if (data[0] instanceof PList) {
            PObj[] objs = ((PList) data[0]).getFixed();
            synchronized (this) {
                for (int i = 0; i < objs.length; i++) {
                    if (i != 0) {
                        writer.print(separator);
                    }
                    writer.print(objs[i].toString(variables));
                    if (newline())
                        writer.println();
                }
            }
        }
        return true;
    }

}
