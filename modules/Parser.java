package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {
		//TODO
		Token ft = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = term();
		while(t.isKind(LE) || t.isKind(LT) || t.isKind(GE) || t.isKind(GT) || t.isKind(EQUAL) || t.isKind(NOTEQUAL)){
			Token op = consume();
			e1 = term();
			e0 = new BinaryExpression(ft,e0,op,e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		//TODO
		Token ft = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = elem();
		while(t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)){
			Token op1 = consume();
			e1 = elem();
			e0 = new BinaryExpression(ft,e0,op1,e1);
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		//TODO
		Token ft = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = factor();
		while(t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)){
			Token op2 = consume();
			e1 = factor();
			e0 = new BinaryExpression(ft,e0,op2,e1);
		}
		return e0;
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Token ft = null;
		Expression e = null;
		switch (kind) {
		case IDENT: {
			ft = consume();
			e = new IdentExpression(ft);
		}
			break;
		case INT_LIT: {
			ft = consume();
			e = new IntLitExpression(ft);
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			ft = consume();
			e = new BooleanLitExpression(ft);
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			ft = consume();
			e = new ConstantExpression(ft);
			
		}
			break;
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		Token ft = t;
		Block b = null;
		if(t.isKind(LBRACE)){
		consume();	
		Statement s = null;
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
			while(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME) 
					|| t.isKind(IDENT) || t.isKind(OP_SLEEP) || t.isKind(KW_WHILE) || t.isKind(KW_IF)
					|| t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) 
					|| t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE) 
					|| t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC)) {
				if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){
					Dec d = dec();
					decs.add(d);
				}
				else {
						s = statement();
						statements.add(s);
				     }
			    }
			b = new Block(ft, decs, statements); 
			match(RBRACE);
		}else{
		throw new SyntaxException("illegal token" + t.kind);
		}
		return b;
	}

	Program program() throws SyntaxException {
		//TODO
		ArrayList<ParamDec> paramList = new ArrayList<ParamDec>();
		Program p = null;
		Block b = null;
		Token ft = t;
		ParamDec pd = null;
		if(t.isKind(IDENT)){
			consume();
			
			if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_URL) || t.isKind(KW_FILE)){
				pd = paramDec();
				paramList.add(pd);
				while(t.isKind(COMMA)){
					consume();
					pd = paramDec();
					paramList.add(pd);
				}
				b = block();
			
			}else{
				
					b = block();
				
			}
			p = new Program(ft, paramList, b);
		}else{
			throw new SyntaxException("illegal token" + t.kind);
			}
		return p;
	}

        ParamDec paramDec() throws SyntaxException {
		//TODO
		if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_URL) || t.isKind(KW_FILE)){
			Token ft = consume();	
			Token ident = match(IDENT);
			ParamDec d = new ParamDec(ft, ident);
			return d;
		}else throw new SyntaxException("illegal token" + t.kind);
	}

	Dec dec() throws SyntaxException {
		//TODO
		if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) 
				|| t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){
			Token ft = consume();	
			Token id = match(IDENT);
			Dec d = new Dec(ft, id);
			return d;
		}else{
		throw new SyntaxException("illegal token" + t.kind);
		}
	}

	Statement statement() throws SyntaxException {
		//TODO
		Token ft = t;
		Statement s =null;
		Expression e0 = null;
		Block b = null;
		Chain c = null;
		ChainElem ce = null;
		ChainElem ce1 = null;
		if(t.isKind(OP_SLEEP)){
			consume();
			e0 = expression();
			s = new SleepStatement(ft, e0);
			match(SEMI);
		}
		else if(t.isKind(KW_IF)){
			consume();
			match(LPAREN);
			e0 = expression();
			match(RPAREN);
			b = block();
			s = new IfStatement(ft, e0, b);
			}
		else if(t.isKind(KW_WHILE)){
			consume();
			match(LPAREN);
			e0 = expression();
			match(RPAREN);
			b = block();
			s = new WhileStatement(ft, e0, b);
			}
		
	    else{
			if(t.isKind(IDENT)){
				consume();
				IdentLValue var = new IdentLValue(ft);
				if(t.isKind(ASSIGN)){
					consume();
					e0 = expression();
					s = new AssignmentStatement(ft, var, e0);
					match(SEMI);
				}
				else{
					c = new IdentChain(ft);
					Token op = arrowOp();
					ce = chainElem();
					c = new BinaryChain(ft, c, op, ce);
					while(t.isKind(ARROW) || t.isKind(BARARROW)){
						Token arrow = t;
						consume();
						ce1 = chainElem();
						c = new BinaryChain(ft, c, arrow, ce1);
					}
					match(SEMI);
					s = c;
				}
				
			}else{
				if(t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || t.isKind(OP_WIDTH) 
						|| t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE) || t.isKind(KW_SHOW) || t.isKind(KW_HIDE) 
						|| t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC)){
					
					c = chainElem();
					Token op = arrowOp();
					ce = chainElem();
					c = new BinaryChain(ft, c, op, ce);
					while(t.isKind(ARROW) || t.isKind(BARARROW)){
						Token arrow = t;
						consume();
						ce = chainElem();
						c = new BinaryChain(ft, c, arrow, ce);
					}
				
					match(SEMI);
					s = c;
				}else{
					throw new SyntaxException("illegal token" + t.kind);
				}
			}
			
		}
		return s;
	}

	Chain chain() throws SyntaxException {
		//TODO
		Token ft = t;
		Chain c = null;
		ChainElem ce = null;
		c = chainElem();
		Token ao = arrowOp();
		ce = chainElem();
		c = new BinaryChain(ft, c, ao, ce);
		while(t.isKind(ARROW) || t.isKind(BARARROW)){
			Token arrow = t;
			consume();
			ce = chainElem();
			c = new BinaryChain(ft, c, arrow, ce);
		}
		return c;
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		 Token ft= t;		 
		 ChainElem ce = null;	 
		if(t.isKind(IDENT)){
			consume();
			ce = new IdentChain(ft);
			
		}else if(t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE)){
		    consume();
		    Tuple tu = arg();
			ce = new FilterOpChain(ft, tu );
			
		}
	    else if(t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)){
	    	consume();
	    	Tuple tu = arg();
			ce = new ImageOpChain(ft, tu );
			
		}
		else{ if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC)){
			consume();
			Tuple tu = arg();
			ce = new FrameOpChain(ft, tu );
			
			}else{
				throw new SyntaxException("illegal token" + t.kind);
			}
		}
		return ce;
	}

	AssignmentStatement assign() throws SyntaxException {
		//TODO
		Token ft = t;
		IdentLValue var = new IdentLValue(ft);
		AssignmentStatement as = null;
		Expression e0 = null;
		if(t.isKind(IDENT)){
			consume();
		
			if(t.isKind(ASSIGN)){
				consume();
				e0 = expression();
				as = new AssignmentStatement(ft, var, e0);
			}
		}else{
		throw new SyntaxException("illegal token" + t.kind);
		} 
		return as;
	}
	
	Token arrowOp() throws SyntaxException {
		//TODO
		Token ft = t;
		if(t.isKind(ARROW) || t.isKind(BARARROW)){
			 consume();
		}else{
		throw new SyntaxException("illegal token" + t.kind);
		}
	   return ft;	
	}
	
	Tuple arg() throws SyntaxException {
		//TODO
		Token ft = t;
		Tuple t0 = null; 
		Expression e0 = null;
		ArrayList<Expression> argList = new ArrayList<Expression>();
		Expression e1 = null;
		if(t.isKind(LPAREN)){

		    consume();
			e0 = expression();
			argList.add(e0);
			while(t.isKind(COMMA)){
				consume();
				e1 = expression();
				argList.add(e1);
			}

			match(RPAREN);
		}
		t0 = new Tuple(ft, argList);
		return t0;
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
		
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
