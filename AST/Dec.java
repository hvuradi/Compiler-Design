package cop5556sp17.AST;

import org.objectweb.asm.Label;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public class Dec extends ASTNode {

	final Token ident;
	TypeName tname;
	int slotnumber;
	Label sLabel;
	Label eLabel;
	public Dec(Token firstToken, Token ident) {
		super(firstToken);

		this.ident = ident;
	}

	public Token getType() {
		return firstToken;
	}

	public Token getIdent() {
		return ident;
	}

	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}

	public TypeName getTname() {
		return tname;
	}

	public void setTname(TypeName tname) {
		this.tname = tname;
	}

	public int getSlotnumber() {
		return slotnumber;
	}

	public void setSlotnumber(int slotnumber) {
		this.slotnumber = slotnumber;
	}

	public Label getsLabel() {
		return sLabel;
	}

	public void setsLabel(Label sLabel) {
		this.sLabel = sLabel;
	}

	public Label geteLabel() {
		return eLabel;
	}

	public void seteLabel(Label eLabel) {
		this.eLabel = eLabel;
	}

}
