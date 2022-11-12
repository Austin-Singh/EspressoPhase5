package ModifierChecker;

import AST.*;
import Utilities.*;
import NameChecker.*;
import TypeChecker.*;
import Utilities.Error;

public class ModifierChecker extends Visitor {

	private SymbolTable classTable;
	private ClassDecl currentClass;
	private ClassBodyDecl currentContext;
	private boolean leftHandSide = false;

	public ModifierChecker(SymbolTable classTable, boolean debug) {
		this.classTable = classTable;
		this.debug = debug;
	}


	/** Super */
	public Object visitSuper(Super su) {
		println(su.line + ": visiting a super");

		if (currentContext.isStatic())
			Error.error(su,
					"non-static variable super cannot be referenced from a static context");

		return null;
	}

	/** This */
	public Object visitThis(This th) {
		println(th.line + ": visiting a this");

		if (currentContext.isStatic())
			Error.error(th,	"non-static variable this cannot be referenced from a static context");

		return null;
	}

	/** NameExpr */
	public Object visitNameExpr(NameExpr ne) {
	    println(ne.line + ": Visiting a name expression '" + ne.name() + "'. (Nothing to do!)");
	    return null;
	}

	/** Assignment */
	public Object visitAssignment(Assignment as) {
	    println(as.line + ": Visiting an assignment (Operator: " + as.op()+ ")");

		boolean oldLeftHandSide = leftHandSide;

		leftHandSide = true;
		as.left().visit(this);

		// Added 06/28/2012 - no assigning to the 'length' field of an array type
		if (as.left() instanceof FieldRef) {
			FieldRef fr = (FieldRef)as.left();
			if (fr.target().type.isArrayType() && fr.fieldName().getname().equals("length"))
				Error.error(fr,"Cannot assign a value to final variable length.");
		}

		leftHandSide = oldLeftHandSide;
		as.right().visit(this);

		return null;
	}

	/** CInvocation -- (COMPLETE)*/
	public Object visitCInvocation(CInvocation ci) {
	    println(ci.line + ": Visiting an explicit constructor invocation (" + (ci.superConstructorCall() ? "super" : "this") + ").");

		// YOUR CODE HERE
	    if(ci.superConstructorCall() && ci.constructor.getModifiers().isPrivate()) {
	    	Error.error(ci, "Private constructor cannot be instantiated.");
	    }
	    
	    super.visitCInvocation(ci);
	    // - END -

		return null;
	}

	/** ClassDecl -- (COMPLETE)*/
	public Object visitClassDecl(ClassDecl cd) {
		println(cd.line + ": Visiting a class declaration for class '" + cd.name() + "'.");

		currentClass = cd;

		// If this class has not yet been declared public make it so.
		if (!cd.modifiers.isPublic())
			cd.modifiers.set(true, false, new Modifier(Modifier.Public));

		// If this is an interface declare it abstract!
		if (cd.isInterface() && !cd.modifiers.isAbstract())
			cd.modifiers.set(false, false, new Modifier(Modifier.Abstract));

		// If this class extends another class then make sure it wasn't declared
		// final.
		if (cd.superClass() != null)
			if (cd.superClass().myDecl.modifiers.isFinal())
				Error.error(cd, "Class '" + cd.name()
						+ "' cannot inherit from final class '"
						+ cd.superClass().typeName() + "'.");

		// YOUR CODE HERE
		super.visitClassDecl(cd);
		// - END -

		return null;
	}

	/** ConstructorDecl -- (COMPLETE)*/
	public Object visitConstructorDecl(ConstructorDecl cd) {
	    println(cd.line + ": visiting a constructor declaration for class '" + cd.name() + "'.");

		// YOUR CODE HERE
	    currentContext = cd;
	    super.visitConstructorDecl(cd);
	    currentContext = null;
	    // - END -

		return null;
	}

	/** New -- (COMPLETE)*/
	public Object visitNew(New ne) {
	    println(ne.line + ": visiting a new '" + ne.type().myDecl.name() + "'.");

		// YOUR CODE HERE
	    if(ne.type().myDecl.modifiers.isAbstract()) {
	    	Error.error(ne, "New cannot instantiate abstract class.");
	    }
	    
	    if(ne.getConstructorDecl().getModifiers().isPrivate()) {
	    	Error.error(ne, "New cannot instantiate private class.");
	    }
	    
	    super.visitNew(ne);
	    // - END -

		return null;
	}

	/** StaticInit -- (COMPLETE)*/
	public Object visitStaticInitDecl(StaticInitDecl si) {
		println(si.line + ": visiting a static initializer");

		// YOUR CODE HERE
		currentContext = si;
	    super.visitStaticInitDecl(si);
	    currentContext = null;
	    // - END -

		return null;
	}

	/** FieldDecl -- (COMPLETE)*/
	public Object visitFieldDecl(FieldDecl fd) {
	    println(fd.line + ": Visiting a field declaration for field '" +fd.var().name() + "'.");

		// If field is not private and hasn't been declared public make it so.
		if (!fd.modifiers.isPrivate() && !fd.modifiers.isPublic())
			fd.modifiers.set(false, false, new Modifier(Modifier.Public));

		// YOUR CODE HERE
		if(fd.modifiers.isFinal() && fd.var().init() == null) {
			Error.error(fd, "Final field declarations must be initialized.");
		}
		
		currentContext = fd;
		super.visitFieldDecl(fd);
		currentContext = null;
		// - END -

		return null;
	}

	/** FieldRef -- (YET TO COMPLETE)*/
	public Object visitFieldRef(FieldRef fr) {
	    println(fr.line + ": Visiting a field reference '" + fr.fieldName() + "'.");

		// YOUR CODE HERE

		// - END -

		return null;
	}

	/** MethodDecl -- (YET TO COMPLETE)*/
	public Object visitMethodDecl(MethodDecl md) {
	    println(md.line + ": Visiting a method declaration for method '" + md.name() + "'.");

		// YOUR CODE HERE
	    
	    
	    currentContext = md;
	    super.visitMethodDecl(md);
	    currentContext = null;
		// - END -

		return null;
	}

	/** Invocation -- (YET TO COMPLETE)*/
	public Object visitInvocation(Invocation in) {
	    println(in.line + ": Visiting an invocation of method '" + in.methodName() + "'.");

		// YOUR CODE HERE

		// - END -

		return null;
	}

	/** UnaryPostExpression -- (COMPLETE)*/
    public Object visitUnaryPostExpr(UnaryPostExpr up) {
		println(up.line + ": visiting a unary post expression with operator '" + up.op() + "'.");
		
		// YOUR CODE HERE
		if(up.expr() instanceof FieldRef) {
			if(((FieldRef)up.expr()).myDecl.modifiers.isPrivate()) {
				Error.error(up, "Cannot assign to private field.");
			}
		}
		
		if(((NameExpr)up.expr()).myDecl instanceof FieldDecl) {
			if(((FieldDecl)((NameExpr)up.expr()).myDecl).modifiers.isFinal()) {
				Error.error(up, "Cannot assign to final field.");
			}
		}
		// - END -

		return null;
	}
		
	/** UnaryPreExpr -- (COMPLETE)*/
	public Object visitUnaryPreExpr(UnaryPreExpr up) {
		println(up.line + ": visiting a unary pre expression with operator '" + up.op() + "'.");
		
		// YOUR CODE HERE
		if(up.op().getKind() == PreOp.PLUSPLUS || up.op().getKind() == PreOp.MINUSMINUS) {
			if(up.expr() instanceof FieldRef) {
				if(((FieldRef)up.expr()).myDecl.modifiers.isPrivate()) {
					Error.error(up, "Cannot assign to private field.");
				}
			}
			
			if(((NameExpr)up.expr()).myDecl instanceof FieldDecl) {
				if(((FieldDecl)((NameExpr)up.expr()).myDecl).modifiers.isFinal()) {
					Error.error(up, "Cannot assign to final field.");
				}
			}
		}
		// - END -
		
		return null;
    }
}
