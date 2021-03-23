package com.hardcoded.compiler.parsetree;

import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
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
	
	public void process(Options options, ProgramStat stat) {
		LOGGER.debug("Started validator");
		
		
		// Replace all duplicate expression and make sure everything is unique
		makeExpressionUnique(stat);
		
//		String str = TreeUtils.printTree(stat).replace("\t", "    ");
//		System.out.println("################################################");
//		System.out.println(str);
//		System.out.println("################################################");
		
		// Process the program
		processProgram(stat);
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
	
	void processProgram(ProgramStat stat) {
		// Functions are defined inside ClassStat and ProgramStat
		// Global variables are defined inside the ProgramStat
		// Imports are defined inside the ProgramStat
		
		LinkerScope link = new LinkerScope();
		link.push(); // Create global scope
		
		for(Statement s : stat.getStatements()) {
			// System.out.printf("(line: %d, column: %d) %s\n", s.getLineIndex(), s.getColumnIndex(), s);
			if(s instanceof FuncStat) {
				processFunction((FuncStat)s, link);
			}
			
			if(s instanceof DefineStat) {
				processDefine((DefineStat)s, link);
			}
		}
		
		/*
		String str = TreeUtils.printTree(stat).replace("\t", "    ");
		System.out.println("################################################");
		System.out.println(str);
		System.out.println("################################################");
		
		Map<Integer, String> map = link.map();
		System.out.println("Mappings:");
		for(Integer key : map.keySet()) {
			System.out.printf("  %02x: %s\n", key, map.get(key));
		}
		System.out.println("---------------------");
		

		System.out.println("Imported:");
		for(Reference ref : link.getImport()) {
			System.out.printf("  %4d: (%s) %s\n", ref.getUniqueIndex(), ref.getType() == Type.VAR ? "variable":"function", ref.getName());
		}
		System.out.println("---------------------");
		*/
		// throw_exception(stat, "Not implemented yet!");
	}
	
	private void processDefine(DefineStat stat, LinkerScope link) {
		String name = stat.getName().value;
		if(link.hasGlobal(name)) {
			throw_exception(stat, "Redeclaration of global variable '%s'", name);
		}
		
		link.addGlobal(name);
	}

	// If a variable is undefined make sure it is flagged as a imported variable
	void processFunction(FuncStat stat, LinkerScope link) {
		link.push();
		LOGGER.debug(stat);
		
		for(DefineStat s : stat.getArguments()) {
			String name = s.getName().value;
			
			if(link.hasLocal(name)) {
				throw_exception(s, "Duplicate function parameter name '%s'", s.getName());
			}
			
			link.addLocal(name);
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
			
			link.addLocal(name);
		}
		
		boolean new_scope = false;
		if(stat instanceof IfStat
		|| stat instanceof IfElseStat
		|| stat instanceof ForStat
		|| stat instanceof WhileStat
		|| stat instanceof DoWhileStat
		|| stat instanceof ListStat) {
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
					e.set(ref);
				} else if(ref.isTemporary()) {
					ref = link.addLocal(name);
					e.set(ref);
				} else if(link.hasGlobal(name)) {
					ref = link.getGlobal(name);
					e.set(ref);
				} else {
					// Token t = e.getToken();
					// System.out.printf("Could not find reference '%s'. This must be a global %s (line: %s, column: %s)\n", name, ref.getType(), t.line, t.column);
					
					if(link.hasImported(name, ref.getType())) {
						ref = link.getImported(name, ref.getType());
					} else {
						ref = link.addImported(name, ref.getType());
					}
					
					e.set(ref);
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
