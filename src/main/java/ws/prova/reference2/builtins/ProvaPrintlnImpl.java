package ws.prova.reference2.builtins;

import ws.prova.kernel2.KB;

public class ProvaPrintlnImpl extends ProvaPrintImpl {

    public ProvaPrintlnImpl(KB kb) {
        super(kb, "println");
    }

    @Override
    protected boolean newline() {
        return true;
    }

}
