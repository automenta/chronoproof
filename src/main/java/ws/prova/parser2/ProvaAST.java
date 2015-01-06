package ws.prova.parser2;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

public class ProvaAST extends CommonTree {

	private int line = 0;
 	
    private int column = 0;

    public ProvaAST(Token tok) {
        super(tok);
        if( tok!=null ) {
        	line=tok.getLine();
        	column=tok.getCharPositionInLine();
        }
    }
    
        @Override
    public int getLine() {
    	return line;
    }
    
    public int getColumn() {
    	return column;
    }

	public void setLine(int line) {
		this.line = line;
	}

	public void setColumn(int column) {
		this.column = column;
	}

}
