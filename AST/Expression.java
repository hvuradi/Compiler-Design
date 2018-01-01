package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	TypeName tname;
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

	public TypeName getTname() {
		return tname;
	}

	public void setTname(TypeName tname) {
		this.tname = tname;
	}

}
