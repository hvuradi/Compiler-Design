package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	TypeName tname;
	public Chain(Token firstToken) {
		super(firstToken);
	}
	public TypeName getTname() {
		return tname;
	}
	public void setTname(TypeName tname) {
		this.tname = tname;
	}

}
