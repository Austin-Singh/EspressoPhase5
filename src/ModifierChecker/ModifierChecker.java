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
			Error.error("non-static variable super cannot be referenced from a static context");

		return null;
	}

	/** This */
	public Object visitThis(This th) {
		println(th.line + ": visiting a this");

		if (currentContext.isStatic())
			Error.error("non-static variable this cannot be referenced from a static context");

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
				Error.error("Cannot assign a value to final variable length.");
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
	    	Error.error("Private constructor cannot be instantiated.");
	    }else{ 
			super.visitCInvocation(ci);
		}
	    // - END -

		return null;
	}

	/** ClassDecl -- (COMPLETE)*/
	public Object visitClassDecl(ClassDecl cd) {
		println(cd.line + ": Visiting a class declaration for class '" + cd.name() + "'.");

		currentClass = cd;

		// If this class has not yet been declared public make it so.
		if (!cd.modifiers.isPublic()){
			cd.modifiers.set(true, false, new Modifier(Modifier.Public));
		}

		// If this is an interface declare it abstract!
		if (cd.isInterface() && !cd.modifiers.isAbstract()){
			cd.modifiers.set(false, false, new Modifier(Modifier.Abstract));
		}

		// If this class extends another class then make sure it wasn't declared
		// final.
		if (cd.superClass() != null){
			if (cd.superClass().myDecl.modifiers.isFinal()){
				Error.error("Class '" + cd.name() + "' cannot inherit from final class '" + cd.superClass().typeName() + "'.");
			}
		}

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
	    	Error.error("New cannot instantiate abstract class.");
	    }
	    
	    if(ne.getConstructorDecl().getModifiers().isPrivate()) {
	    	if(!ne.type().myDecl.name().equals(currentClass.name())) {
	    		Error.error(ne.type().myDecl.name() + "( ) has private access in '" + ne.type().myDecl.name() + "'.");
	    	}
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
		if (!fd.modifiers.isPrivate() && !fd.modifiers.isPublic()){
			fd.modifiers.set(false, false, new Modifier(Modifier.Public));
		}

		// YOUR CODE HERE
		if(fd.modifiers.isFinal() && fd.var().init() == null) {
			Error.error("Final field declarations must be initialized.");
		}else{
			currentContext = fd;
			super.visitFieldDecl(fd);
			currentContext = null;
		}
		// - END -

		return null;
	}

	/** UnaryPostExpression -- (COMPLETE)*/
    public Object visitUnaryPostExpr(UnaryPostExpr up) {
		println(up.line + ": visiting a unary post expression with operator '" + up.op() + "'.");
		
		// YOUR CODE HERE
		if(up.expr() instanceof FieldRef) {

			if(((FieldRef)up.expr()).myDecl.modifiers.isPrivate()) {
				if(!((ClassType)((FieldRef)up.expr()).targetType).myDecl.name().equals(currentClass.name())) {
					Error.error("Cannot assign a value to private field '" + ((FieldRef)up.expr()).fieldName().getname() + "'.");
				}
			}

			if(((FieldRef)up.expr()).myDecl.modifiers.isFinal()) {
				Error.error("Cannot assign a value to final field '" + ((FieldRef)up.expr()).fieldName().getname() + "'.");
			}
			visitFieldRef(((FieldRef)up.expr()));
		}else if(up.expr() instanceof NameExpr) {
			if(((NameExpr)up.expr()).myDecl instanceof FieldDecl) {
				if(((FieldDecl)((NameExpr)up.expr()).myDecl).modifiers.isFinal()) {
					Error.error("Cannot assign a value to final field '" + ((NameExpr)up.expr()).name().getname() + "'.");
				}
			}
			visitNameExpr(((NameExpr)up.expr()));
		}

		// - END -
		return null;
	}
		
	/** UnaryPreExpr -- (COMPLETE)*/
	public Object visitUnaryPreExpr(UnaryPreExpr up) {
		println(up.line + ": visiting a unary pre expression with operator '" + up.op() + "'.");
		
		// YOUR CODE HERE
		
			if(up.expr() instanceof FieldRef) {
				if(((FieldRef)up.expr()).myDecl.modifiers.isPrivate()) {
					if(!((ClassType)((FieldRef)up.expr()).targetType).myDecl.name().equals(currentClass.name())) {
						Error.error("Cannot assign a value to private field '" + ((FieldRef)up.expr()).fieldName().getname() + "'.");
					}
				}
				if(((FieldRef)up.expr()).myDecl.modifiers.isFinal()) {
					Error.error("Cannot assign a value to final field '" + ((FieldRef)up.expr()).fieldName().getname() + "'.");
				}
				visitFieldRef(((FieldRef)up.expr()));
			}
			else if(up.expr() instanceof NameExpr) {
				if(((NameExpr)up.expr()).myDecl instanceof FieldDecl) {
					if(((FieldDecl)((NameExpr)up.expr()).myDecl).modifiers.isFinal()) {
						Error.error("Cannot assign a value to final field '" + ((NameExpr)up.expr()).name().getname() + "'.");
					}
				}
				visitNameExpr(((NameExpr)up.expr()));
			}
		

		// - END -
		
		return null;
    }

	/** FieldRef -- (COMPLETE)*/
	public Object visitFieldRef(FieldRef fr) {
	    println(fr.line + ": Visiting a field reference '" + fr.fieldName() + "'.");

		// YOUR CODE HERE
		ClassType classType = (ClassType)fr.targetType;
		FieldDecl fieldDecl = fr.myDecl;

		if (!(classType.typeName().equals(currentClass.className().getname()))){
			if(fieldDecl.getModifiers().isPrivate()){
				Error.error("field '" + fr.fieldName().getname() + "' was declared 'private' and cannot be accessed outside its class.");
			}
		}

		if (fr.target() instanceof NameExpr){
			if((((NameExpr)fr.target()).myDecl instanceof ClassDecl)){
				if(!(fieldDecl.getModifiers().isStatic())){
					Error.error("non-static field '" + fr.fieldName().getname() + "' cannot be referenced in a static context.");
				}
			}
		}
		
		if(leftHandSide){
			if(fieldDecl.getModifiers().isFinal()){
				Error.error("Cannot assign a value to final field '" + fr.fieldName().getname() + "'.");
			}  
		}
		// - END -

		return null;
	}

	/** MethodDecl -- (COMPLETE)*/
	public Object visitMethodDecl(MethodDecl md) {
	    println(md.line + ": Visiting a method declaration for method '" + md.name() + "'.");

		// YOUR CODE HERE
        currentContext = md;

        if (md.getModifiers().isAbstract() && md.block() != null){
            Error.error("Abstract method '" + md.getname() + "' cannot have a body.");
        }

        if (md.block() == null) {
            if (currentClass.isClass() && !currentClass.getModifiers().isAbstract()){
                Error.error("Method '" + md.getname() + "' does not have a body, or class should be declared abstract.");
            }else if (currentClass.isClass() && !md.getModifiers().isAbstract()){
                Error.error("Method '" + md.getname() + "' does not have a body, or should be declared abstract.");
            }else if (currentClass.isInterface() && md.getModifiers().isFinal()){
                Error.error("Method '" + md.getname() + "' cannot be declared final in an interface.");
            }else if (md.getModifiers().isFinal()){
                Error.error("Abstract method '" + md.getname() + "' cannot be declared final.");
            }
        }

        if (currentClass.superClass() != null) {
            MethodDecl methodDecl = (MethodDecl) new TypeChecker(classTable, true).findMethod(currentClass.superClass().myDecl.allMethods, md.getname(), md.params(),true);
            if (methodDecl != null) {
                if (md.paramSignature().equals(methodDecl.paramSignature())) {
                    if (methodDecl.getModifiers().isFinal()){
                        Error.error("Method '" + md.getname() + "' was implemented as final in super class, cannot be reimplemented.");
                    }else if (methodDecl.getModifiers().isStatic() && !md.getModifiers().isStatic()){
                        Error.error("Method '" + md.getname() + "' declared static in superclass, cannot be reimplemented non-static.");
                    }else if (!methodDecl.getModifiers().isStatic() && md.getModifiers().isStatic()){
                        Error.error("Method '" + md.getname() + "' declared non-static in superclass, cannot be reimplemented static.");
                    }
                }
            }
        }

        super.visitMethodDecl(md);
        currentContext = null;
		// - END -

		return null;
	}

	/** Invocation -- (COMPLETE)*/
	public Object visitInvocation(Invocation in) {
	    println(in.line + ": Visiting an invocation of method '" + in.methodName() + "'.");

		// YOUR CODE HERE
		String methodName = in.methodName().getname();

		if ((in.target() instanceof NameExpr)) {
			if((((NameExpr)in.target()).myDecl instanceof ClassDecl)){
				if(!in.targetMethod.getModifiers().isStatic()){
					Error.error("non-static method '" + in.methodName().getname() + "' cannot be referenced from a static context.");
				}
			}
		}

		if (in.target() == null && currentContext.isStatic()){
			if(!in.targetMethod.getModifiers().isStatic()){
				Error.error("non-static method '" + in.methodName().getname() + "' cannot be referenced from a static context.");
			}
		}

		if (in.targetMethod.getModifiers().isPrivate()){
			if(!currentClass.name().equals(((ClassType)in.targetType).myDecl.name())){
				Error.error("" + in.methodName().getname() + "(" + Type.parseSignature(in.targetMethod.paramSignature()) + " ) has private access in '" + ((ClassType)in.targetType).myDecl.name() + "'.");
			}
		}

		if (in.targetMethod.getModifiers().isPrivate()){
			if (!in.targetMethod.getMyClass().equals(currentClass)){
				Error.error("" + in.methodName().getname() + "(" + Type.parseSignature(in.targetMethod.paramSignature()) + " ) has private access in '" + in.targetMethod.getMyClass().className().getname() + "'.");
			}
		}

		super.visitInvocation(in);
		// - END -

		return null;
	}

}
