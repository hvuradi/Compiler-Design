package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}
    FieldVisitor fv;
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int p_count = 0;
	int dec_count = 1;
	ArrayList<Dec> d_list = new ArrayList<Dec>();

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		for(Dec d : d_list){
			mv.visitLocalVariable(d.getIdent().getText(), classDesc, null, d.getsLabel(), d.geteLabel(), d.getSlotnumber());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTname());
		assignStatement.getVar().visit(this, arg);

		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

	 Token op = binaryChain.getArrow();
	 Chain chain = binaryChain.getE0();
	 Token t1 = chain.getFirstToken();
	 ChainElem ce = binaryChain.getE1();
	 Token t2 = ce.getFirstToken();

     if(chain.getTname() == TypeName.URL){
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className,chain.getFirstToken().getText(), chain.getTname().getJVMTypeDesc());
		mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);

	 }else if(chain.getTname() == TypeName.FILE){
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className,chain.getFirstToken().getText(), chain.getTname().getJVMTypeDesc());
	    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
		}else if(chain.getTname() == TypeName.NONE){
			mv.visitInsn(POP);
		}
	 else if(chain instanceof IdentChain) {
		 chain.visit(this, true);
	 }else chain.visit(this, null);


	if(t2.isKind(OP_BLUR) || t2.isKind(OP_CONVOLVE) || t2.isKind(OP_GRAY)){
		ce.visit(this, op);
	}
	else if(ce instanceof IdentChain) {
	    ce.visit(this, false);


	}else{
		ce.visit(this, null);
	}
		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		e0.visit(this, null);
		e1.visit(this, null);
		Token op = binaryExpression.getOp();
		if(e0.getTname().getJVMTypeDesc().equals("I") && e1.getTname().getJVMTypeDesc().equals("I")){
		if(op.isKind(PLUS)){
			mv.visitInsn(IADD);
		}else if(op.isKind(MINUS)){
			mv.visitInsn(ISUB);
		}else if(op.isKind(TIMES)){
			mv.visitInsn(IMUL);
		}else if(op.isKind(DIV)){
			mv.visitInsn(IDIV);
		}else if(op.isKind(MOD)){
			mv.visitInsn(IREM);
		}else if(op.isKind(GT) ){
			Label l1 = new Label();
	        mv.visitJumpInsn(IF_ICMPLE, l1);
	        mv.visitInsn(ICONST_1);
	        Label l2 = new Label();
	        mv.visitJumpInsn(GOTO, l2);
	        mv.visitLabel(l1);
	        mv.visitInsn(ICONST_0);
	        mv.visitLabel(l2);
		}else if(op.isKind(GE)){
			Label l1 = new Label();
	        mv.visitJumpInsn(IF_ICMPLT, l1);
	        mv.visitInsn(ICONST_1);
	        Label l2 = new Label();
	        mv.visitJumpInsn(GOTO, l2);
	        mv.visitLabel(l1);
	        mv.visitInsn(ICONST_0);
	        mv.visitLabel(l2);
		}else if(op.isKind(LT)){
			Label l1 = new Label();
	        mv.visitJumpInsn(IF_ICMPGE, l1);
	        mv.visitInsn(ICONST_1);
	        Label l2 = new Label();
	        mv.visitJumpInsn(GOTO, l2);
	        mv.visitLabel(l1);
	        mv.visitInsn(ICONST_0);
	        mv.visitLabel(l2);
		}else if(op.isKind(LE)){
			Label l1 = new Label();
	        mv.visitJumpInsn(IF_ICMPGT, l1);
	        mv.visitInsn(ICONST_1);
	        Label l2 = new Label();
	        mv.visitJumpInsn(GOTO, l2);
	        mv.visitLabel(l1);
	        mv.visitInsn(ICONST_0);
	        mv.visitLabel(l2);
		}else if(op.isKind(EQUAL)){
			Label l1 = new Label();
	        mv.visitJumpInsn(IF_ICMPNE, l1);
	        mv.visitInsn(ICONST_1);
	        Label l2 = new Label();
	        mv.visitJumpInsn(GOTO, l2);
	        mv.visitLabel(l1);
	        mv.visitInsn(ICONST_0);
	        mv.visitLabel(l2);
		}else if(op.isKind(NOTEQUAL)){
			Label l1 = new Label();
	        mv.visitJumpInsn(IF_ICMPEQ, l1);
	        mv.visitInsn(ICONST_1);
	        Label l2 = new Label();
	        mv.visitJumpInsn(GOTO, l2);
	        mv.visitLabel(l1);
	        mv.visitInsn(ICONST_0);
	        mv.visitLabel(l2);
		}
	  }else if(e0.getTname().getJVMTypeDesc().equals("Z") && e1.getTname().getJVMTypeDesc().equals("Z")){
		    if(op.isKind(GT) ){
			Label l1 = new Label();
		        mv.visitJumpInsn(IF_ICMPLE, l1);
		        mv.visitInsn(ICONST_1);
		        Label l2 = new Label();
		        mv.visitJumpInsn(GOTO, l2);
		        mv.visitLabel(l1);
		        mv.visitInsn(ICONST_0);
		        mv.visitLabel(l2);
			}else if(op.isKind(GE)){
				Label l1 = new Label();
		        mv.visitJumpInsn(IF_ICMPLT, l1);
		        mv.visitInsn(ICONST_1);
		        Label l2 = new Label();
		        mv.visitJumpInsn(GOTO, l2);
		        mv.visitLabel(l1);
		        mv.visitInsn(ICONST_0);
		        mv.visitLabel(l2);
			}else if(op.isKind(LT)){
				Label l1 = new Label();
		        mv.visitJumpInsn(IF_ICMPGE, l1);
		        mv.visitInsn(ICONST_1);
		        Label l2 = new Label();
		        mv.visitJumpInsn(GOTO, l2);
		        mv.visitLabel(l1);
		        mv.visitInsn(ICONST_0);
		        mv.visitLabel(l2);
			}else if(op.isKind(LE)){
				Label l1 = new Label();
		        mv.visitJumpInsn(IF_ICMPGT, l1);
		        mv.visitInsn(ICONST_1);
		        Label l2 = new Label();
		        mv.visitJumpInsn(GOTO, l2);
		        mv.visitLabel(l1);
		        mv.visitInsn(ICONST_0);
		        mv.visitLabel(l2);
			}else if(op.isKind(EQUAL)){
				Label l1 = new Label();
			    mv.visitJumpInsn(IF_ICMPNE, l1);
			    mv.visitInsn(ICONST_1);
			    Label l2 = new Label();
			    mv.visitJumpInsn(GOTO, l2);
			    mv.visitLabel(l1);
			    mv.visitInsn(ICONST_0);
			    mv.visitLabel(l2);
			}else if(op.isKind(NOTEQUAL)){
				Label l1 = new Label();
			    mv.visitJumpInsn(IF_ICMPEQ, l1);
			    mv.visitInsn(ICONST_1);
			    Label l2 = new Label();
			    mv.visitJumpInsn(GOTO, l2);
			    mv.visitLabel(l1);
			    mv.visitInsn(ICONST_0);
			    mv.visitLabel(l2);
			}else if(op.isKind(AND)){
				mv.visitInsn(IAND);
			}else if(op.isKind(OR)){
				mv.visitInsn(IOR);
			}
	  	}else if(e0.getTname() == IMAGE && e1.getTname() == IMAGE){
	  		if(op.isKind(PLUS)){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}else if(op.isKind(MINUS)){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}else if(op.isKind(NOTEQUAL)){
				Label l1 = new Label();
			    mv.visitJumpInsn(IF_ACMPEQ, l1);
			    mv.visitInsn(ICONST_1);
			    Label l2 = new Label();
			    mv.visitJumpInsn(GOTO, l2);
			    mv.visitLabel(l1);
			    mv.visitInsn(ICONST_0);
			    mv.visitLabel(l2);
			}else if(op.isKind(EQUAL)){
				Label l1 = new Label();
			    mv.visitJumpInsn(IF_ACMPNE, l1);
			    mv.visitInsn(ICONST_1);
			    Label l2 = new Label();
			    mv.visitJumpInsn(GOTO, l2);
			    mv.visitLabel(l1);
			    mv.visitInsn(ICONST_0);
			    mv.visitLabel(l2);
			}
	  	}else if(e0.getTname() == IMAGE && e1.getTname() == TypeName.INTEGER){
             if(op.isKind(TIMES)){
            	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
             }else if(op.isKind(DIV)){
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
             }else if(op.isKind(MOD)){
            	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
             }
	  	}else if(e0.getTname() == TypeName.INTEGER && e1.getTname() == IMAGE){
	  		 if(op.isKind(TIMES)){
	  			    mv.visitInsn(SWAP);
	            	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
	           }
	  	}
		return binaryExpression;
	}


	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label block_label = new Label();
		Label block_end = new Label();
		mv.visitLabel(block_label);
		List<Dec> l1 = block.getDecs();
		List<Statement> l2 = block.getStatements();
		for(Dec d: l1){

			d.setsLabel(block_label);
			d.visit(this, null);
			d_list.add(d);
			d.seteLabel(block_end);
		}
		for(Statement s : l2){
			s.visit(this, null);
			if(s instanceof BinaryChain)
				mv.visitInsn(POP);
		}


		mv.visitLabel(block_end);
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if(booleanLitExpression.getValue() == true){
			mv.visitInsn(ICONST_1);
		}else mv.visitInsn(ICONST_0);
		return booleanLitExpression;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {

	Token t = constantExpression.getFirstToken();
	if(t.isKind(KW_SCREENWIDTH)){
		mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", "()I", false);
	} else if(t.isKind(KW_SCREENHEIGHT)){
		mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", "()I", false);
	}
		return constantExpression;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
        declaration.setSlotnumber(dec_count);

        if(declaration.getTname() == FRAME || declaration.getTname() == IMAGE){
        	mv.visitInsn(ACONST_NULL);
        	mv.visitVarInsn(ASTORE, declaration.getSlotnumber());
        }
        dec_count++;
		return declaration;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
     Token op = (Token)arg;
	 Tuple tu = filterOpChain.getArg();
	 Token t = filterOpChain.getFirstToken();
	 if(op.isKind(ARROW)){
		 mv.visitInsn(ACONST_NULL);
	 }else if(op.isKind(BARARROW)){
		 mv.visitInsn(DUP);
	 }

	 if(t.isKind(OP_BLUR)){
		 mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
	 }else if(t.isKind(OP_CONVOLVE)){
		 mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
	 }else if(t.isKind(OP_GRAY)){
		 mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
	 }

		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
	 Tuple tu = frameOpChain.getArg();
	 tu.visit(this, null);
	 Token t = frameOpChain.getFirstToken();

	 if(t.isKind(KW_SHOW)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
	 }else if(t.isKind(KW_HIDE)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
	 }else if(t.isKind(KW_MOVE)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
	 }else if(t.isKind(KW_XLOC)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
	 }else if(t.isKind(KW_YLOC)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
	 }
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
	 Dec d = identChain.getD();
	 String var = d.getIdent().getText();
	 Boolean left = (Boolean)arg;
	 if(left == true){
		 if(d instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, var, d.getTname().getJVMTypeDesc());
		 }else{
			 if(identChain.getTname() == TypeName.IMAGE || identChain.getTname() == TypeName.FRAME){

  				mv.visitVarInsn(ALOAD, d.getSlotnumber());
  			  }
		 else{
  				mv.visitVarInsn(ILOAD, d.getSlotnumber());
		     }
	    }

	 }else {
		 if(d instanceof ParamDec){
		   if(d.getTname() == TypeName.INTEGER ){

			 mv.visitVarInsn(ALOAD, 0);
			 mv.visitInsn(SWAP);
			 mv.visitFieldInsn(PUTFIELD, className, var, d.getTname().getJVMTypeDesc());
			 mv.visitVarInsn(ALOAD, 0);
			 mv.visitFieldInsn(GETFIELD, className, var, d.getTname().getJVMTypeDesc());
			}
		    else if(identChain.getTname() == (TypeName.FILE)){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, var, d.getTname().getJVMTypeDesc());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
				mv.visitInsn(POP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, var, d.getTname().getJVMTypeDesc());
			}
	    }else {

	    	if(d.getTname() == TypeName.IMAGE){
	    	// mv.visitInsn(DUP);
	    	 mv.visitVarInsn(ASTORE, d.getSlotnumber());
	    	 mv.visitVarInsn(ALOAD, d.getSlotnumber());
	    }else if(d.getTname() == TypeName.FRAME){
	    	 mv.visitVarInsn(ALOAD, d.getSlotnumber());
	    	 mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
	    	 mv.visitVarInsn(ASTORE, d.getSlotnumber());
	    	 mv.visitVarInsn(ALOAD, d.getSlotnumber());
	    }else{
			mv.visitVarInsn(ISTORE, d.getSlotnumber());
			mv.visitVarInsn(ILOAD, d.getSlotnumber());
		}
	   }
	 }
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
        Dec d = identExpression.getDec();
        String var = identExpression.getFirstToken().getText();
        if(d instanceof ParamDec){
		mv.visitVarInsn(ALOAD,0);
		mv.visitFieldInsn(GETFIELD, className, var, d.getTname().getJVMTypeDesc());
        }else {
        	if (d.getTname().isType(TypeName.INTEGER) || d.getTname().isType(TypeName.BOOLEAN)) {
                	mv.visitVarInsn(ILOAD, d.getSlotnumber());
        	} else {
        			mv.visitVarInsn(ALOAD, d.getSlotnumber());
        	}
        }
		return identExpression;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		Dec d = identX.getDec();
		String var = identX.getFirstToken().getText();

		if(d instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, var, d.getTname().getJVMTypeDesc());
		}else{
			if( d.getTname() == TypeName.IMAGE){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, d.getSlotnumber());
			}else if (d.getTname() == TypeName.INTEGER || d.getTname() == TypeName.BOOLEAN){
				mv.visitVarInsn(ISTORE, d.getSlotnumber());
			} else {
				mv.visitVarInsn(ASTORE, d.getSlotnumber());
			}

		}
		return identX;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
        Expression e0 = ifStatement.getE();
        Block b = ifStatement.getB();
        Label l1 = new Label();
        e0.visit(this, null);
        mv.visitJumpInsn(IFEQ, l1);
        b.visit(this, null);
        mv.visitLabel(l1);

        return ifStatement;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
	Tuple tu = imageOpChain.getArg();
	tu.visit(this, null);
	Token t = imageOpChain.getFirstToken();
	if(t.isKind(KW_SCALE)){
		 mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
	}else if(t.isKind(OP_WIDTH)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
	}else if(t.isKind(OP_HEIGHT)){
		 mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig, false);
	}
		return imageOpChain;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		int val = intLitExpression.value;
		mv.visitLdcInsn(new Integer(val));
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		paramDec.setSlotnumber(-1);
		if(paramDec.getTname().getJVMTypeDesc().equals("I")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, new Integer(0));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(p_count);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}else if(paramDec.getTname().getJVMTypeDesc().equals("Z")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, new Boolean(false));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(p_count);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}else if(paramDec.getTname().getJVMTypeDesc().equals("Ljava/io/File;")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/io/File;", null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(p_count);
			mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKESPECIAL,  "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}else if(paramDec.getTname().getJVMTypeDesc().equals("Ljava/net/URL;")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/net/URL;", null, null);
			mv.visitVarInsn(ALOAD, 0);
			//mv.visitTypeInsn(NEW, "java/net/URL");
			//mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(p_count);
			//mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className,  paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
        fv.visitEnd();
        p_count++;

		return paramDec;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
	 sleepStatement.getE().visit(this, arg);
	 mv.visitInsn(I2L);
	 mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
	  List<Expression> e_list = tuple.getExprList();
      for(Expression e0 : e_list){
    	  e0.visit(this, null);
      }
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Expression e0 = whileStatement.getE();
		Block b = whileStatement.getB();
		Label l1 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		Label l2 = new Label();
		mv.visitLabel(l2);
		b.visit(this, null);
	//	mv.visitIincInsn(2, 1);
		mv.visitLabel(l1);
		e0.visit(this, null);
		mv.visitJumpInsn(IFNE, l2);
		return null;
	}

}
