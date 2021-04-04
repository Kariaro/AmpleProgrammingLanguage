package com.hardcoded.compiler.parsetree;

import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.expression.AtomExpr;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * Validates the syntax and throws exceptions when variables
 * and functions are redeclared within the same file.
 * 
 * <p>Types will not be checked.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleTreeValidator {
	private static final Log LOGGER = Log.getLogger(AmpleTreeValidator.class);
	
	public AmpleTreeValidator() {
		
	}
	
	public LinkerScope process(Options options, ProgramStat stat) {
		LOGGER.debug("Started validator");
		
		
		// Replace all duplicate expression and make sure everything is unique
		makeExpressionUnique(stat);
		
//		String str = TreeUtils.printTree(stat).replace("\t", "    ");
//		System.out.println("################################################");
//		System.out.println(str);
//		System.out.println("################################################");
		
		// Process the program
		return processProgram(stat);
	}
	
	// Replaces all expressions with a deep copy of themselves
	// This will make all expressions unique and no expression
	// will share references.
	void makeExpressionUnique(Statement stat) {
		if(stat instanceof ExprStat) {
			ExprStat s = (ExprStat)stat;
			
			List<Expression> list = s.getExpressions();
			for(int i = 0; i < list.size(); i++) {
				list.set(i, TreeUtils.deepCopy(list.get(0)));
			}
		}
		
		for(Statement s : stat.getStatements()) {
			makeExpressionUnique(s);
		}
	}
	
	LinkerScope processProgram(ProgramStat stat) {
		LinkerScope link = new LinkerScope();
		link.push(); // Create global scope
		
		for(Statement s : stat.getStatements()) {
			if(s instanceof FuncStat) {
				processFunction((FuncStat)s, link);
			}
			
			if(s instanceof ClassStat) {
				processClass((ClassStat)s, link);
			}
			
			if(s instanceof DefineStat) {
				processDefine((DefineStat)s, link);
			}
			
			if(s instanceof ImportStat) {
				processImport((ImportStat)s, link);
			}
		}
		
		return link;
	}
	
	private void processImport(ImportStat stat, LinkerScope link) {
		String value = stat.getPath().value;
		value = value.substring(1, value.length() - 1);
		
		// Warn for redefinitions maybe?
		link.addImportedFile(value);
	}
	
	private void processClass(ClassStat stat, LinkerScope link) {
		String name = stat.getName().value;
		if(link.hasGlobal(name, Reference.Type.CLASS)) {
			throw_exception(stat, "Redeclaration of global class '%s'", name);
		}
		
		link.addExported(name, Reference.Type.CLASS);
		stat.setReference(link.addGlobal(name, Reference.Type.CLASS));
		
		for(Statement s : stat.getStatements()) {
			if(s instanceof FuncStat) {
				processFunction((FuncStat)s, link);
			}
			
			if(s instanceof ClassStat) {
				processClass((ClassStat)s, link);
			}
			
			if(s instanceof DefineStat) {
				processDefine((DefineStat)s, link);
			}
		}
	}

	private void processDefine(DefineStat stat, LinkerScope link) {
		String name = stat.getName().value;
		if(link.hasGlobal(name, Reference.Type.VAR)) {
			throw_exception(stat, "Redeclaration of global variable '%s'", name);
		}
		
		link.addExported(name, Reference.Type.VAR);
		stat.setReference(link.addGlobal(name, Reference.Type.VAR));
	}
	
	void processFunction(FuncStat stat, LinkerScope link) {
		link.push();
		link.addExported(stat.getName().value, Reference.Type.FUN);
		stat.setReference(link.addGlobal(stat.getName().value, Reference.Type.FUN));
		
		for(DefineStat s : stat.getArguments()) {
			String name = s.getName().value;
			
			if(link.hasLocal(name)) {
				throw_exception(s, "Duplicate function parameter name '%s'", s.getName());
			}
			
			s.setReference(link.addLocal(name));
		}
		
		for(Statement s : stat.getStatements()) {
			processFunctionTree(s, link);
		}
		
		link.pop();
	}
	
	// How to allow propagation of global variables but keep internal
	void processFunctionTree(Statement stat, LinkerScope link) {
		if(stat instanceof DefineStat) {
			DefineStat s = (DefineStat)stat;
			String name = s.getName().value;
			
			if(link.hasLocal(name)) {
				throw_exception(stat, "Redefined variable '%s'", name);
			}
			
			s.setReference(link.addLocal(name));
		}
		
		if(stat instanceof LabelStat
		|| stat instanceof GotoStat) {
			IRefContainer s = (IRefContainer)stat;
			String name = s.getRefToken().value;
			
			Reference ref = null;
			if(link.hasLocals(name, Reference.Type.LABEL)) {
				ref = link.getLocals(name, Reference.Type.LABEL);
			} else {
				ref = link.addLocals(name, Reference.Type.LABEL);
			}
			
			s.setReference(ref);
		}
		
		boolean new_scope = false;
		if(stat instanceof IfStat
		|| stat instanceof IfElseStat
		|| stat instanceof ForStat
		|| stat instanceof WhileStat
		|| stat instanceof DoWhileStat
		|| stat instanceof ScopeStat) {
			link.push();
			new_scope = true;
		}
		
		if(stat instanceof ExprStat) {
			ExprStat s = (ExprStat)stat;
			
			for(Expression e : s.getExpressions()) {
				processFunctionTree(e, link);
			}
		}
		
		for(Statement s : stat.getStatements()) {
			processFunctionTree(s, link);
		}
		
		if(new_scope) {
			link.pop();
		}
	}
	
	void processFunctionTree(Expression expr, LinkerScope link) {
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			if(e.isReference()) {
				Reference ref = e.getReference();
				String name = ref.getName();
				
				if(link.hasLocal(name)) {
					ref = link.getLocal(name);
					e.setReference(ref);
				} else if(ref.isTemporary()) {
					ref = link.addLocal(name);
					e.setReference(ref);
				} else if(link.hasGlobal(name, ref.getType())) {
					ref = link.getGlobal(name, ref.getType());
					e.setReference(ref);
				} else {
					if(link.hasImported(name, ref.getType())) {
						ref = link.getImported(name, ref.getType());
					} else {
						ref = link.addImported(name, ref.getType());
					}
					
					e.setReference(ref);
				}
			}
		}
		
		for(Expression e : expr.getExpressions()) {
			processFunctionTree(e, link);
		}
	}
	
	<T> T throw_exception(Statement stat, String format, Object... args) {
		String extra = String.format("(line: %d, column: %d) ", stat.getLineIndex(), stat.getColumnIndex());
		throw new ParseTreeException(extra + format, args);
	}
}
