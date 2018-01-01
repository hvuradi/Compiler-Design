package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;


import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Chain chain = binaryChain.getE0();
		ChainElem elem = binaryChain.getE1();

		Token t = elem.getFirstToken();
		Token op = binaryChain.getArrow();
		chain.visit(this, null);
		elem.visit(this, null);
		if(op.isKind(ARROW)){
			if((chain.getTname() == URL || chain.getTname() == FILE) && elem.getTname() == IMAGE){
			 	binaryChain.setTname(IMAGE);
			}else if(chain.getTname() == FRAME){
				if((elem instanceof FrameOpChain) && (t.isKind(KW_XLOC) || t.isKind(KW_YLOC))){
					binaryChain.setTname(INTEGER);
				}else if((elem instanceof FrameOpChain) && (t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE))){
					binaryChain.setTname(FRAME);
				}else throw new TypeCheckException("Error");
			}else if(chain.getTname() == IMAGE){
				if((elem instanceof ImageOpChain) && (t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT))){
					binaryChain.setTname(INTEGER);
				}else if(elem.getTname() == FRAME){
					binaryChain.setTname(FRAME);
				}else if(elem.getTname() == FILE){
					binaryChain.setTname(NONE);
				}else if((elem instanceof FilterOpChain) && (t.isKind(OP_GRAY) || t.isKind(OP_BLUR) || t.isKind(OP_CONVOLVE))){
					binaryChain.setTname(IMAGE);
				}else if((elem instanceof ImageOpChain) && (t.isKind(KW_SCALE))){
					binaryChain.setTname(IMAGE);
				}else if(elem instanceof IdentChain){
					binaryChain.setTname(IMAGE);
				}else if((elem instanceof IdentChain) && elem.getTname() == IMAGE){
					binaryChain.setTname(IMAGE);
				}else throw new TypeCheckException("Error");
			}else if(chain.getTname() == INTEGER && elem instanceof IdentChain && elem.getTname() == INTEGER){
				binaryChain.setTname(INTEGER);
			}
			else throw new TypeCheckException("Error");
		}else if(op.isKind(BARARROW)){
				if((chain.getTname() == IMAGE) && (elem instanceof FilterOpChain) && (t.isKind(OP_GRAY) || t.isKind(OP_BLUR) || t.isKind(OP_CONVOLVE))){
					binaryChain.setTname(IMAGE);
			}else throw new TypeCheckException("Error");
		}else throw new TypeCheckException("Error");
		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		e0.visit(this, null);
		e1.visit(this, null);
		Token op = binaryExpression.getOp();
		if(op.isKind(MINUS) || op.isKind(PLUS)){
			if(e0.getTname() == INTEGER && e1.getTname() == INTEGER){
				binaryExpression.setTname(INTEGER);
			}else if(e0.getTname() == IMAGE && e1.getTname() == IMAGE){
				binaryExpression.setTname(IMAGE);
			}else throw new TypeCheckException("Error");
		}else if(op.isKind(TIMES) || op.isKind(MOD) || op.isKind(DIV)){
			if(e0.getTname() == INTEGER && e1.getTname() == INTEGER){
				binaryExpression.setTname(INTEGER);
			}else if(e0.getTname() == INTEGER && e1.getTname() == IMAGE){
				binaryExpression.setTname(IMAGE);
			}else if(e0.getTname() == IMAGE && e1.getTname() == INTEGER){
				binaryExpression.setTname(IMAGE);
			}else throw new TypeCheckException("Error");
		}

		else if(op.isKind(LE) || op.isKind(LT) || op.isKind(GE) || op.isKind(GT) || op.isKind(AND) || op.isKind(OR)){
			if(e0.getTname() == INTEGER && e1.getTname() == INTEGER){
				binaryExpression.setTname(BOOLEAN);
			}else if(e0.getTname() == BOOLEAN && e1.getTname() == BOOLEAN){
				binaryExpression.setTname(BOOLEAN);
			}else throw new TypeCheckException("Error");
		}else if(op.isKind(EQUAL) || op.isKind(NOTEQUAL)){
			if(e0.getTname() == e1.getTname()){
				binaryExpression.setTname(BOOLEAN);
			}else throw new TypeCheckException("Error");
		}else throw new TypeCheckException("Error");

		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		int i = 0;
		int j = 0;
		int k = 0;
		int m = 0;
		List<Dec> declist = block.getDecs();
		List<Statement> stmtlist = block.getStatements();
			i = declist.size();
			k = stmtlist.size();
			while(j < i  && m < k){
				Dec d = declist.get(j);
				Statement s = stmtlist.get(m);
				Token t = d.getFirstToken();
				Token t1 = s.getFirstToken();
				if(t.getLinePos().line < t1.getLinePos().line){
					d.visit(this, null);
					j++;
				}else if(t.getLinePos().line == t1.getLinePos().line){
					if(t.getLinePos().posInLine < t1.getLinePos().posInLine){
						d.visit(this, null);
						j++;
					}else {s.visit(this, null); m++;}
				}else {s.visit(this, null); m++;}

			}
			if(j == i){
				for(int l=m;l<k;l++){
					Statement s = stmtlist.get(l);
					s.visit(this, null);
				}
			} else if(m == k){
				for(int l=j;l<i;l++){
					Dec d = declist.get(l);
					d.visit(this, null);
				}
			}
		symtab.leaveScope();
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTname(BOOLEAN);
		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tu = filterOpChain.getArg();
		tu.visit(this, null);
		if(tu.getExprList().size() == 0){
			filterOpChain.setTname(IMAGE);
		}else throw new TypeCheckException("Error");
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = frameOpChain.getFirstToken();
		Tuple tu = frameOpChain.getArg();
		tu.visit(this, null);
		if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE)){
			if(tu.getExprList().size() == 0){
				frameOpChain.setTname(NONE);
			}else throw new TypeCheckException("Error");
		}else if(t.isKind(KW_XLOC) || t.isKind(KW_YLOC)){
			if(tu.getExprList().size() == 0){
				frameOpChain.setTname(INTEGER);
			}else throw new TypeCheckException("Error");
		}else if(t.isKind(KW_MOVE)){
			if(tu.getExprList().size() == 2){
				frameOpChain.setTname(NONE);
			}else throw new TypeCheckException("Error");
		}else throw new TypeCheckException("error");
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = identChain.getFirstToken();
		if(symtab.lookup(t.getText())!=null){
		TypeName tname = (symtab.lookup(t.getText()).getTname());
		identChain.setTname(tname);
		identChain.setD(symtab.lookup(t.getText()));}
		else{
			throw new TypeCheckException("error");
		}
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = identExpression.getFirstToken();
		Dec d = symtab.lookup(t.getText());
		if(d != null){
		TypeName tname = (d.getTname());
		identExpression.setTname(tname);
		identExpression.setDec(d);
		}
		else{
			throw new TypeCheckException("error");
		}

		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = ifStatement.getE();
		Block b = ifStatement.getB();
		e0.visit(this, null);
		b.visit(this, null);
		if(e0.getTname() != BOOLEAN){
			throw new TypeCheckException("Error");
		}
		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTname(INTEGER);
		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = sleepStatement.getE();
		e0.visit(this, null);
		if(e0.getTname() != INTEGER){
			throw new TypeCheckException("Error");
		}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = whileStatement.getE();
		Block b = whileStatement.getB();
		e0.visit(this, null);
		b.visit(this, null);
		if(e0.getTname() != BOOLEAN){
			throw new TypeCheckException("Error");
		}
		return whileStatement;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = declaration.getFirstToken();
		TypeName tname = Type.getTypeName(t);
		declaration.setTname(tname);
		boolean boo = symtab.insert(declaration.getIdent().getText(), declaration);
		if(boo == false){
			throw new TypeCheckException("Error");
		}
		return declaration;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<ParamDec> l1= program.getParams();
		Block b = program.getB();

		for(ParamDec pd: l1){
			pd.visit(this, null);

		}
		b.visit(this, null);
		return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		IdentLValue ilv = assignStatement.getVar();
		Expression e0 = assignStatement.getE();
		ilv.visit(this, null);
		e0.visit(this, null);
		if(ilv.getDec().getTname() != e0.getTname()){
			throw new TypeCheckException("Error");
		}
		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = identX.getFirstToken();
		Dec d = symtab.lookup(t.getText());
		if(d == null){
			throw new TypeCheckException("not declared");
		}
		identX.setDec(d);
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = paramDec.getFirstToken();
		TypeName tname = Type.getTypeName(t);
		paramDec.setTname(tname);
		boolean boo = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(boo == false){
			throw new TypeCheckException("Error");
		}
		return paramDec;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTname(INTEGER);
		return constantExpression;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = imageOpChain.getFirstToken();
		Tuple tu = imageOpChain.getArg();
		tu.visit(this, null);
		if(t.isKind(OP_WIDTH)|| t.isKind(OP_HEIGHT)){
			if(tu.getExprList().size() == 0){
				imageOpChain.setTname(INTEGER);
			}else{
				throw new TypeCheckException("error");
			}
		}else if(t.isKind(KW_SCALE)){
			if(tu.getExprList().size() == 1){
				imageOpChain.setTname(IMAGE);
			}else{
				throw new TypeCheckException("error");
			}
		}else{
			throw new TypeCheckException("error");
		}
		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> l = tuple.getExprList();
		for(Expression e0: l){
			e0.visit(this, null);
			if(e0.getTname() != INTEGER){
				throw new TypeCheckException("Error");
			}
		}
		return tuple;
	}


}
