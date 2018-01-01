package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Statement extends ASTNode {
    TypeName tname;
	public Statement(Token firstToken) {
		super(firstToken);
	}

	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

	public TypeName getTname() {
		return tname;
	}

	public void setTname(TypeName tname) {
		this.tname = tname;
	}

}
