package hardcoded.compiler.parsetree;

import static hardcoded.compiler.constants.ExprType.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hardcoded.compiler.*;
import hardcoded.compiler.constants.*;
import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.context.Lang;
import hardcoded.compiler.errors.*;
import hardcoded.compiler.expression.*;
import hardcoded.compiler.impl.IBlock;
import hardcoded.compiler.impl.IProgram;
import hardcoded.compiler.statement.*;
import hardcoded.compiler.types.HighType;
import hardcoded.compiler.types.PrimitiveType;
import hardcoded.lexer.LexerFactory;
import hardcoded.lexer.LexerTokenizer;
import hardcoded.lexer.Token;
import hardcoded.utils.FileUtils;
import hardcoded.utils.StringUtils;

/**
 * 
 * @author HardCoded
 * @since v0.0
 */
public class ParseTreeGenerator {
	private static final LexerTokenizer LEXER;
	
	// FIXME: Add global variables
	// FIXME: Add non const arrays
	// FIXME: Defined types should be a part of program and not this generator.
	// FIXME: GLOBALS should be put inside Program and not the generator.
	
	static {
		LexerTokenizer lexer = null;
		
		try {
			lexer = LexerFactory.load(ParseTreeGenerator.class.getResourceAsStream("/lexer/lexer.lex"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		LEXER = lexer.getImmutableTokenizer();
	}
	
	
	private Map<String, Expression> GLOBAL = new LinkedHashMap<>();
	private Map<String, HighType> defined_types = new HashMap<>();
	
	// TODO: This build config is only used for searching for imported files!
	//       Could we maybe break this into another file or remove it?
	private BuildConfiguration config;
	private Function currentFunction;
	private Program currentProgram;
	private File sourceFile;
	private Lang reader;
	
	private boolean ran = false;
	public Program init(BuildConfiguration config, File mainFile) {
		if(ran) throw new CompilerException("ParseTreeGenerator is not reusable");
		ran = true;
		
		for(HighType t : Primitives.getAllTypes()) {
			defined_types.put(t.name(), t);
		}
		
		this.config = config;
		this.currentProgram = new Program();
		
		try {
			importFile(mainFile);
		} catch(Throwable t) {
			// This is a real error and was not thrown by the compiler
			syntaxError(CompilerError.MESSAGE, t.getClass() + ": " + t.getMessage());
			t.printStackTrace();
		}
		
		return currentProgram;
	}
	
	private void importFile(String path) {
		List<File> files = config.lookupFile(path);
		if(files.isEmpty()) {
			addSyntaxError(CompilerError.MESSAGE, -2, 1, "The file '" + path + "' does not exist");
			return;
		}
		
		if(files.size() > 1) {
			addSyntaxError(CompilerError.MESSAGE, -2, 1, "The file '" + path + "' has multiple definitions\n" + files);
			return;
		}

		// TODO: Disallow cannonical paths.
		/* Check the canonical path to disallow using relative paths.
		 * 
		 * The paths   [ "../src/file.hc" ] AND [ "file.hc" ]
		 * could point towards the same file but only when calculating
		 * the canonical file path we could see that they are the same.
		 */
		File sourceFile = files.get(0);
		
		if(!sourceFile.exists()) {
			addSyntaxError(CompilerError.MESSAGE, -2, 1, "The file '" + path + "' does not exist");
			return;
		}
		
		if(currentProgram.hasImportedFile(sourceFile)) {
			addSyntaxWarning(CompilerError.MESSAGE, -2, 1, "The file '" + path + "' has already been imported");
			return;
		}
		
		importFile(sourceFile);
	}
	
	// TODO: Supply more information about the token.
	private void importFile(File newSourceFile, byte... optionalBytes) {
		byte[] bytes = new byte[0];
		
		if(optionalBytes.length > 1) {
			bytes = optionalBytes;
		} else {
			try {
				bytes = FileUtils.readFileBytes(newSourceFile);
			} catch(IOException e) {
				syntaxError(CompilerError.INTERNAL_ERROR, e.getMessage());
				return;
			}
		}
		
		File lastSourceFile = sourceFile;
		sourceFile = newSourceFile;
		currentProgram.importedFiles.add(sourceFile.getAbsolutePath());
		
		Lang last = this.reader;
		this.reader = Lang.wrap(LEXER.parse(bytes));
		
		if(reader == null) {
			syntaxError(CompilerError.INTERNAL_ERROR, "this.reader was null");
			return;
		}
		
		try {
			// Read all blocks inside the file
			while(nextBlock() != null);
		} catch(CompilerException e) {
			// Used to break free if fatal or other errors occured
		}
		
		this.reader = last;
		this.sourceFile = lastSourceFile;
	}
	
	private IBlock nextBlock() {
		if(reader.valueEqualsAdvance("@")) {
			parseCompiler();
			
			return IBlock.EMPTY;
		} else {
			return makeFunction();
		}
	}
	
	private void parseCompiler() {
		String value = reader.value();
		
		if(value.equals("type")) {
			reader.next();
			
			String name = reader.value();
			if(defined_types.containsKey(name)) syntaxError(CompilerError.INVALID_TYPE_PROCESSOR_REDECLARATION, name);
			if(!isValidName(reader)) syntaxError(CompilerError.INVALID_TYPE_PROCESSOR_NAME, name);
			
			reader.next();
			HighType type = getTypeFromSymbol();
			if(!reader.valueEqualsAdvance(";")) syntaxError(CompilerError.INVALID_TYPE_PROCESSOR_EXPECTED_SEMICOLON, reader);
			defined_types.put(name, new HighType(name, type.type()));
			
		} else if(value.equals("import")) {
			reader.next();
			
			if(!reader.groupEquals("STRING")) syntaxError(CompilerError.INVALID_IMPORT_EXPECTED_STRING, reader.value());
			String pathname = reader.value();
			pathname = pathname.substring(1, pathname.length() - 1);
			
			if(!reader.next().valueEquals(";")) syntaxError(CompilerError.INVALID_IMPORT_EXPECTED_SEMICOLON, reader.value());
			reader.next();
			importFile(pathname);
		} else if(value.equals("set")) {
			if(!isValidName(reader.next())) syntaxError(CompilerError.INVALID_SET_PROCESSOR_NAME, reader);
			String name = reader.value(); // This should be marked...
			reader.next();
			Expression expr = nextExpression();
			if(!reader.valueEqualsAdvance(";")) syntaxError(CompilerError.INVALID_SET_PROCESSOR_EXPECTED_SEMICOLON, reader);
			
			// TODO: Check that the expression is a compiler value that does not use variables.
			GLOBAL.put(name, expr);
			
		} else if(value.equals("unset")) {
			String name = reader.next().value();
			if(!reader.next().valueEqualsAdvance(";")) syntaxError(CompilerError.INVALID_UNSET_PROCESSOR_EXPECTED_SEMICOLON, reader);
			
			if(defined_types.containsKey(name)) {
				if(defined_types.get(name) instanceof PrimitiveType) syntaxError(CompilerError.INVALID_UNSET_PROCESSOR_NO_PRIMITIVES, name);
				defined_types.remove(name);
			} else if(GLOBAL.containsKey(name)) {
				GLOBAL.remove(name);
			} else {
				// Trying to remove something that does not exist...
			}
			
		}
	}
	
	private Function makeFunction() {
		if(reader.remaining() < 1) return null;
		
		Function func = new Function();
		while(Modifiers.contains(reader.value())) {
			func.addModifier(nextFuncModifier());
		}
		
		if(!isType(reader)) addSyntaxError(CompilerError.INVALID_FUNCTION_TYPE, 0, 1, reader.value());
		func.returnType = getTypeFromSymbol();
		
		if(!isValidName(reader)) addSyntaxError(CompilerError.INVALID_FUNCTION_NAME, 0, 1, reader.value());
		boolean needsBody = false;
		
		if(currentProgram.hasFunction(reader.value())) {
			Function impl = currentProgram.getFunctionByName(reader.value());
			
			if(!impl.isPlaceholder()) {
				addSyntaxError(CompilerError.INVALID_FUNCTION_REDECLARATION, 0, 1, reader.value());
			} else {
				// Modifiers
				if(!impl.returnType.equals(func.returnType)) {
					int depth = func.returnType.type().depth();
					
					addSyntaxError(
						CompilerError.INVALID_FUNCTION_DECLARATION_WRONG_RETURN_TYPE,
						-depth, depth + 1, reader.value()
					);
				}
				
				if(!impl.getModifiers().containsAll(func.getModifiers())) {
					syntaxError(CompilerError.INVALID_FUNCTION_DECLARATION_WRONG_MODIFIERS, func.getModifiers(), impl.getModifiers());
				}
				
				func = impl;
				func.sourceLineIndex = reader.line();
				func.sourceFileOffset = reader.fileOffset();
				func.declaredFile = sourceFile;
				
				// TODO: We should not clear the arguments here
				func.arguments.clear();
				
				// TODO: Make this more general. What about function overloading? What should we do here?
				needsBody = true;
			}
		} else {
			func.sourceFileOffset = reader.fileOffset();
			func.sourceLineIndex = reader.line();
			func.declaredFile = sourceFile;
			func.name = reader.value();
			currentProgram.addFunction(func);
		}
		
		currentFunction = func;
		if(!reader.next().valueEquals("(")) {
			addSyntaxError(CompilerError.INVALID_FUNCTION_NAME, -1, 2, reader.peakString(-1, 2));
		}
		
		reader.next();
		while(!reader.valueEquals(")")) {
			Variable arg = nextFuncArgument();
			
			// Compare hightypes.
			func.addArgument(arg);
			
			if(!reader.valueEquals(",")) {
				if(reader.valueEquals(")")) break;
				syntaxError(CompilerError.MISSING_FUNCTION_PARAMETER_SEPARATOR, reader);
				break;
			}
			
			reader.next();
		}
		
		if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.INVALID_FUNCTION_DECLARATION_EXPECTED_CLOSING_PARENTHESIS, reader);
		if(reader.valueEquals(";")) {
			if(needsBody) syntaxError(CompilerError.INVALID_FUNCTION_DECLARATION_EXPECTED_A_FUNCTION_BODY);
			reader.next();
			
			return func;
		} else if(!reader.valueEquals("{")) {
			syntaxError(CompilerError.INVALID_FUNCTION_DECLARATION_EXPECTED_OPEN_CURLYBRACKET);
		}
		
		currentFunction.inc_scope();
		func.body = getStatements();
		currentFunction.dec_scope();
		
		for(String key : currentFunction.requiredLabels.keySet()) {
			if(!currentFunction.hasLabel(key)) {
				Token token = currentFunction.requiredLabels.get(key);
				int offset = reader.readerIndex() - reader.indexOf(token);
				addSyntaxError(CompilerError.INVALID_GOTO_LABEL_NOT_FOUND, -offset, 1, token);
			}
		}
		
		return func;
	}
	
	private Statement nextStatement() {
		reader.mark();
		
		if(isType(reader)) {
			Statement stat = getVariableDefinition();
			
			if(stat instanceof StatementList) {
				StatementList list = (StatementList)stat;
				for(int i = 0; i < list.size(); i++) {
					Variable var = (Variable)list.get(i);
					Identifier ident = currentFunction.add(var);
					
					if(!var.isInitialized()) {
						list.remove(i--);
					} else {
						list.set(i, new ExprStat(new OpExpr(ExprType.set, new AtomExpr(ident), var.value())));
					}
				}
			}
			
			reader.resetMarked();
			return stat;
		}
		
		if(reader.valueEquals("if")) {
			return makeIfStatement();
		} else if(reader.valueEquals("while")) {
			return makeWhileStatement();
		} else if(reader.valueEquals("for")) {
			return makeForStatement();
		}
		
		if(reader.valueEqualsAdvance("break")) {
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_BREAK_STATEMENT_EXPECTED_SEMICOLON, reader);
			reader.nextClear();
			return new ExprStat(new OpExpr(leave));
		} else if(reader.valueEqualsAdvance("continue")) {
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_CONTINUE_STATEMENT_EXPECTED_SEMICOLON, reader);
			reader.nextClear();
			return new ExprStat(new OpExpr(loop));
		} else if(reader.valueEqualsAdvance("return")) {
			OpExpr expr = new OpExpr(ret);
			if(!reader.valueEquals(";")) expr.add(nextExpression());
			else expr.add(new AtomExpr(0)); // TODO: Make sure that the return value is not null!!!
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_RETURN_STATEMENT_EXPECTED_SEMICOLON, reader);
			reader.nextClear();
			return new ExprStat(expr);
		} else if(reader.valueEqualsAdvance("goto")) {
			Expression expr;
			if(!isValidName(reader)) {
				syntaxError(CompilerError.INVALID_GOTO_LABEL_NAME, reader);
				expr = Expression.EMPTY;
			} else {
				OpExpr op = new OpExpr(jump);
				op.add(new AtomExpr(reader.value()));
				currentFunction.requiredLabels.put(reader.value(), reader.token());
				expr = op;
			}
			
			if(!reader.next().valueEquals(";")) syntaxError(CompilerError.INVALID_GOTO_STATEMENT_EXPECTED_SEMICOLON, reader);
			reader.nextClear();
			return new ExprStat(expr);
		} else if(reader.peak(1).valueEquals(":")) {
			Expression expr;
			if(!isValidName(reader)) {
				syntaxError(CompilerError.INVALID_LABEL_NAME, reader);
				expr = Expression.EMPTY;
			} else {
				String name = reader.value();
				if(currentFunction.hasLabel(name)) {
					syntaxError(CompilerError.INVALID_LABEL_REDECLARATION, reader);
					expr = Expression.EMPTY;
				} else {
					OpExpr op = new OpExpr(label);
					op.add(new AtomExpr(name));
					currentFunction.labels.add(name);
					expr = op;
				}
			}
			
			reader.next().nextClear();
			return new ExprStat(expr);
		}
		
		
		if(reader.valueEquals("{")) {
			return getStatements();
		} else {
			Expression expr = nextExpression();
			
			if(!hasModifications(expr)) {
				expr = Expression.EMPTY;
				syntaxError(CompilerError.INVALID_EXPRESSION_MESSAGE, "The expression was empty or has weird syntax", reader);
			}
			
			if(reader.valueEquals(";")) {
				reader.nextClear();
				
				return new ExprStat(expr);
			}
			
			syntaxError(CompilerError.INVALID_EXPR_STATEMENT_EXPECTED_SEMICOLON, reader);
			reader.nextClear();
		}
		
		return Statement.newEmpty();
	}
	
	// Check if a expression does any modification
	private boolean hasModifications(Expression expr) {
		switch(expr.type()) {
			case call:
			case jump:
			case label:
			case loop:
			case leave:
			case set: return true;
			
			default: {
				// Check if any element does any thing..
				if(expr.hasElements()) {
					for(Expression e : expr.getElements()) {
						if(hasModifications(e)) return true;
					}
				}
				
				return false;
			}
		}
	}
	
	private Statement makeWhileStatement() {
		reader.next();
		if(!reader.valueEqualsAdvance("(")) syntaxError(CompilerError.INVALID_WHILE_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		Expression condition = nextExpression();
		
		if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.INVALID_WHILE_STATEMENT_UNCLOSED_PARENTHESES, reader);
		Statement body = nextStatement();
		reader.resetMarked();
		
		return new WhileStat(condition, body);
	}
	
	private Statement makeForStatement() {
		boolean declares_variables = false;
		
		reader.next();
		if(!reader.valueEqualsAdvance("(")) syntaxError(CompilerError.INVALID_FOR_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		ForStat stat = new ForStat(); {
			if(!reader.valueEquals(";")) {
				declares_variables = true;
				currentFunction.inc_scope();
				Statement vars = getVariableDefinition();
				stat.setVariables(vars);
				
				if(vars instanceof StatementList) {
					StatementList list = (StatementList)vars;
					for(int i = 0; i < list.size(); i++) {
						Variable var = (Variable)list.get(i);
						Identifier ident = currentFunction.add(var);
						
						if(!var.isInitialized()) {
							list.remove(i--);
						} else {
							list.set(i, new ExprStat(new OpExpr(ExprType.set, new AtomExpr(ident), var.value())));
						}
					}
				}
				
				reader.prev();
			}
			
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_XXX_EXPECTED_SEMICOLON, "for statement (variables)", reader);
			reader.next();
			if(!reader.valueEquals(";")) stat.setCondition(nextExpression());
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_XXX_EXPECTED_SEMICOLON, "for statement (condition)", reader);
			reader.next();
			if(!reader.valueEquals(")")) stat.setAction(nextExpression());
			if(!reader.valueEquals(")")) syntaxError(CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		}
		
		reader.next();
		stat.setBody(getStatements());
		
		if(declares_variables) {
			currentFunction.dec_scope();
		}
		
		reader.resetMarked();
		return stat;
	}
	
	private Statement makeIfStatement() {
		reader.next();
		if(!reader.valueEqualsAdvance("(")) syntaxError(CompilerError.INVALID_IF_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		Expression condition = nextExpression();
		
		if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		Statement body = nextStatement();
		Statement elseBody;
		
		if(reader.valueEqualsAdvance("else")) {
			elseBody = nextStatement();
		} else {
			elseBody = Statement.newEmpty();
		}
		
		reader.resetMarked();
		return new IfStat(condition, body, elseBody);
	}
	
	private Statement getVariableDefinition() {
		reader.mark();
		HighType type = getTypeFromSymbol();
		
		if(type.type().equals(Atom.unf)) {
			syntaxError(CompilerError.INVALID_VARIABLE_TYPE, type);
		}
		
		List<Variable> list = new ArrayList<Variable>();
		while(true) {
			Variable var = new Variable(type);
			list.add(var);
			
			if(!isValidName(reader)) syntaxError(CompilerError.INVALID_VARIABLE_NAME, reader);
			if(currentFunction.hasIdentifier(reader.value())) syntaxError(CompilerError.REDECLARATION_OF_LOCAL_VARIABLE, reader);
			var.name = reader.value();
			reader.next();
			
			if(reader.valueEqualsAdvance("[")) {
				Expression expr = nextExpression();
				var.isArray = true;
				
				if(!(expr instanceof AtomExpr)) {
					syntaxError(CompilerError.INVALID_ARRAY_VARIABLE_DECLARATION_EXPECTED_INTEGER, expr);
				} else {
					AtomExpr number = (AtomExpr)expr;
					if(!number.isNumber()) {
						syntaxError(CompilerError.INVALID_ARRAY_VARIABLE_DECLARATION_EXPECTED_INTEGER, expr);
					}
					
					// TODO: What should we do if the array has a negative length?
					var.list.add(Expression.EMPTY);
					var.arraySize = (int)number.number();
					var.type = new HighType(type.name(), type.type().nextHigherPointer());
				}
				
				if(!reader.valueEqualsAdvance("]")) syntaxError(CompilerError.UNCLOSED_ARRAY_DEFINITION, reader);
				if(!reader.valueEquals(";")) syntaxError(CompilerError.UNCLOSED_VARIABLE_DECLARATION);
				reader.nextClear();
				break;
			}
			
			if(reader.valueEquals(";")) {
				reader.nextClear();
				break;
			} else if(reader.valueEqualsAdvance("=")) {
				Expression expr = nextExpression(true);
				LowType e_size = expr.size();
				
				if(!type.isPointer()) {
					if(e_size.isPointer()) {
						syntaxError(CompilerError.INVALID_VARIABLE_ASSIGNMENT, "Pointer sizes are different (Type:" + type.depth() + " != Got:" + e_size.depth() + ")");
					} else {
						if(type.size() != e_size.size()) {
							OpExpr next = new OpExpr(ExprType.cast, expr);
							next.override_size = type.type();
							expr = next;
							// syntaxError(CompilerError.INVALID_VARIABLE_ASSIGNMENT, "Expression sizes are different (Type:" + type.size() + " != Got:" + e_size.size() + ")");
						}
					}
				}
				
				
				var.setValue(expr);
				
				if(reader.valueEquals(";")) {
					reader.nextClear();
					break;
				}
			}
			
			if(!reader.valueEqualsAdvance(",")) {
				syntaxError(CompilerError.INVALID_VARIABLE_DECLARATION_MISSING_COLON_OR_SEMICOLON, reader);
				break;
			}
		}
		
		return new StatementList(list);
	}
	
	private Statement getStatements() {
		reader.mark();
		if(reader.valueEquals(";")) {
			reader.nextClear();
			return Statement.newEmpty();
		}
		
		if(!reader.valueEqualsAdvance("{")) {
			Statement stat = nextStatement();
			reader.resetMarked();
			return stat;
		}
		
		
		NestedStat stat = new NestedStat();
		for(;;) {
			if(reader.valueEqualsAdvance(";")) continue;
			if(reader.valueEquals("}")) break;
			
			Statement s = nextStatement();
			if(s == null || s.isEmptyStat()) continue;
			
			if(s.hasElements() && s.size() == 0) continue;
			if(s instanceof StatementList) {
				stat.getElements().addAll(((StatementList)s).getElements());
				continue;
			}
			
			stat.add(s);
		}
		
		if(!reader.valueEquals("}")) syntaxError(CompilerError.UNCLOSED_CURLY_BRACKETS_STATEMENT, reader);
		reader.nextClear();
		
		if(stat.size() == 0) {
			return Statement.newEmpty();
		}
		
		return stat;
	}
	
	private Expression nextExpression() {
		return nextExpression(false);
	}
	
	private Expression nextExpression(boolean skipComma) {
		reader.mark();
		
		try {
			/*
			 * This is a recursive descent parser.
			 */
			Expression expr = new Object() {
				private ExprType[] _e(ExprType... array) { return array; }
				private String[] _s(String... array) { return array; }
				
				Expression e_read(String[] values, ExprType[] exprs, java.util.function.Supplier<Expression> func) { return e_read(values, exprs, func, func); }
				Expression e_read(String[] values, ExprType[] exprs, java.util.function.Supplier<Expression> entry, java.util.function.Supplier<Expression> func) {
					for(Expression expr = entry.get();;) {
						boolean found = false;
						for(int i = 0; i < values.length; i++) {
							if(reader.valueEqualsAdvance(values[i])) {
								found = true;
								expr = new OpExpr(exprs[i], expr, func.get());
								break;
							}
						}
						
						if(!found) return expr;
					}
				}
				
				Expression e_read_combine(String value, ExprType type, java.util.function.Supplier<Expression> func) { return e_read_combine(value, type, func, func); }
				Expression e_read_combine(String value, ExprType type, java.util.function.Supplier<Expression> entry, java.util.function.Supplier<Expression> func) {
					boolean hasFirst = false;
					for(Expression expr = entry.get();;) {
						if(reader.valueEqualsAdvance(value)) {
							if(!hasFirst) {
								expr = new OpExpr(type, expr, func.get());
								hasFirst = true;
							} else {
								OpExpr o = (OpExpr)expr;
								o.add(func.get());
							}
						} else return expr;
					}
				}
				
				Expression parse() { return skipComma ? e14():e15(); }
				Expression e15() { return e_read_combine(",", comma, this::e14); }
				Expression e14() { // Left associative
					Expression lhs = e13();
					
					// Check if the value we are looking at is a assignment operator.
					if(!Operators.isAssignmentOperator(reader.value())) return lhs;
					
					// Check if the left hand side can be modified
					if(!acceptModification(lhs)) syntaxError(CompilerError.INVALID_MODIFICATION);
					
					ExprType type = null;
					switch(reader.value()) {
						case "=": type = set; break;
						case "-=": type = sub; break;
						case "+=": type = add; break;
						case "%=": type = mod; break;
						case "<<=": type = shl; break;
						case ">>=": type = shr; break;
						case "*=": type = mul; break;
						case "/=": type = div; break;
						case "&=": type = and; break;
						case "^=": type = xor; break;
						case "|=": type = or; break;
					}
					
					// FIXME: This should scale with the size of the baseSize of a lowType !!!
					//        Only allowed for [ += ] and [ -= ]
					
					reader.next();
					
					// The value that should be assigned.
					Expression assigner = lhs;
					
					// Right hand side of the expression.
					Expression rhs = e14();
					
					/*
					 * If the left hand side is a comma expression then
					 * this will be the expression containing the assigner
					 * variable.
					 */
					Expression comma_parent = lhs;
					boolean comma_assigned = false;
					
					{
						/* If the left hand side of the expression is a comma	[ ( ... , x) ]
						 * then the assignment operation should be placed only
						 * on the last element of that comma expression.
						 * 
						 * An example would be that the expression 	[ ( ... , x) += 5 ]
						 * should become 							[ ( ... , x += 5) ]
						 */
						if(lhs.type() == comma) {
							while(assigner.type() == comma) {
								comma_parent = assigner;
								assigner = assigner.last();
							}
							
							comma_assigned = true;
						}
						
						/* Check if the right hand side of the expression is not pure.
						 * A pure expression is a expression only made up of purely
						 * numbers and mathimatical operations on those numbers.
						 */
						if(!rhs.isPure()) {
							AtomExpr temp = new AtomExpr(currentFunction.temp(rhs.size()));
							rhs = new OpExpr(comma,
								new OpExpr(set, temp, rhs),
								temp
							);
						}
						
						if(type != set) rhs = new OpExpr(type, assigner, rhs);
						
						if(comma_assigned) {
							((OpExpr)comma_parent).set(comma_parent.length() - 1, new OpExpr(set, assigner, rhs));
							return lhs;
						}
						
						return new OpExpr(set, assigner, rhs);
					}
				}
				
				Expression e13() {
					Expression expr = e12();
					if(reader.valueEqualsAdvance("?")) {
						Expression b = e12();
						if(!reader.valueEqualsAdvance(":")) syntaxError(CompilerError.INVALID_TERNARY_OPERATOR_MISSING_COLON, reader);
						Expression c = e12();
						
						AtomExpr temp = new AtomExpr(currentFunction.temp(LowType.largest(b.size(), c.size())));
						return new OpExpr(
							comma,
							new OpExpr(cor,
								new OpExpr(cand,
									expr,
									new OpExpr(comma, new OpExpr(set, temp, b), new AtomExpr(1))
								),
								new OpExpr(comma, new OpExpr(set, temp, c))
							),
							temp
						);
					}
					
					return expr;
				}
				
				Expression e12() { return e_read(_s("||"), _e(cor), this::e11, this::e12); }
				Expression e11() { return e_read(_s("&&"), _e(cand), this::e10, this::e11); }
				Expression e10() { return e_read(_s("|"), _e(or), this::e9); }
				Expression e9() { return e_read(_s("^"), _e(xor), this::e8); }
				Expression e8() { return e_read(_s("&"), _e(and), this::e7); }
				Expression e7() { return e_read(_s("==", "!="), _e(eq, neq), this::e6, this::e7); }
				Expression e6() { return e_read(_s("<", "<=", ">", ">="), _e(lt, lte, gt, gte), this::e5); }
				Expression e5() { return e_read(_s("<<", ">>"), _e(shl, shr), this::e4); }
				Expression e4() { return e_read(_s("+", "-"), _e(add, sub), this::e3); }
				Expression e3() { return e_read(_s("*", "/", "%"), _e(mul, div, mod), this::e2_2); }
				
				Expression e2_2() {
					switch(reader.value()) {
						case "++": case "--": {
							ExprType dir = reader.value().equals("++") ? add:sub;
							
							reader.next();
							Expression rhs = e2_3();
							if(!acceptModification(rhs)) syntaxError(CompilerError.INVALID_MODIFICATION);
							
							Expression result = rhs;
							
							// FIXME: This should scale with the size of the baseSize of a lowType !!!
							
							/* If the right hand side is a comma expression then the
							 * modification should effect only the last element.
							 * 
							 * The expression		[ ++( ... , x ) ]
							 * will be evaluated	[ ( ... , ++x ) ]
							 */
							if(result.type() == comma) rhs = rhs.last();
							
							if(rhs.isPure()) {
								rhs = new OpExpr(set, rhs, new OpExpr(dir, rhs, new AtomExpr(1)));
							} else {
								AtomExpr ident = new AtomExpr(currentFunction.temp(rhs.size()));
								
								// TODO: Tell why a modification to a value that is not pure must be memory modification.
								// TODO: Check that the value is a valid memory modification.
								rhs = new OpExpr(comma,
									new OpExpr(set, ident, new OpExpr(addptr, rhs)),
									new OpExpr(set,
										new OpExpr(decptr, ident),
										new OpExpr(dir, new OpExpr(decptr, ident), new AtomExpr(1))
									)
								);
							}
							
							if(result.type() == comma) {
								result.set(result.length() - 1, rhs);
								return result;
							}
							
							return rhs;
						}
					}
					
					// Left hand side
					Expression lhs = e2_3();
					
					switch(reader.value()) {
						case "++": case "--": {
							if(!acceptModification(lhs)) syntaxError(CompilerError.INVALID_MODIFICATION);
							ExprType dir = reader.value().equals("++") ? add:sub;
							
							reader.next();
							
							// TODO: Allow for comma expressions!
							// TODO: Use the same solution as the assignment operators.
							// FIXME: This should scale with the size of the baseSize of a lowType !!!
							
							AtomExpr ident2 = new AtomExpr(currentFunction.temp(lhs.size().nextHigherPointer()));
							if(!lhs.isPure()) {
								AtomExpr ident1 = new AtomExpr(currentFunction.temp(lhs.size()));
								
								// ( ... ) ++
								
								// For this to be not pure it must be a pointer.
								
								return new OpExpr(comma,
									// Calculate inside of lhs...
									new OpExpr(set, ident2, new OpExpr(addptr, lhs)),
									new OpExpr(set, ident1, new OpExpr(decptr, ident2)),
									new OpExpr(set, new OpExpr(decptr, ident2), new OpExpr(dir, new OpExpr(decptr, ident2), new AtomExpr(1))),
									ident1
								);
							}
							
							return new OpExpr(comma,
								new OpExpr(set, ident2, lhs),
								new OpExpr(set, lhs, new OpExpr(dir, lhs, new AtomExpr(1))),
								ident2
							);
						}
					}
					
					return lhs;
				}
				
				Expression e2_3() {
					Expression lhs = e2();
					
					for(;;) {
						switch(reader.value()) {
							case "[": {
								reader.next();
								Expression expr = e15();
								
								if(!reader.valueEqualsAdvance("]")) syntaxError(CompilerError.UNCLOSED_ARRAY_EXPRESSION, reader);
								
								Expression result = lhs;
								if(result.type() == comma) lhs = lhs.last();
								
								LowType type = lhs.size();
								if(!type.isPointer() && lhs.type() != ExprType.decptr) {
									syntaxError(CompilerError.INVALID_ARRAY_NOT_A_POINTER, reader);
								}
								
								if(lhs instanceof AtomExpr) {
									AtomExpr atom = (AtomExpr)lhs;
									
									if(atom.isNumber()) {
										syntaxError(CompilerError.INVALID_ARRAY_NOT_A_POINTER, reader);
									} else if(atom.isIdentifier()) {
										Identifier ident = atom.identifier();
										
										switch(ident.id_type()) {
											case clazz:
											case funct: syntaxError(CompilerError.INVALID_ARRAY_NOT_A_POINTER, reader);
											default:
										}
									}
								}
								
								expr = new OpExpr(mul, expr, new AtomExpr(type.baseSize()));
								lhs = new OpExpr(decptr, new OpExpr(add, lhs, expr));
								
								if(result.type() == comma) {
									result.set(result.length() - 1, lhs);
									lhs = result;
								}
								
								continue;
							}
							
							// NOTE: Implement class blocks later
							// case ".": e1 = new BiExpr("MEMBER", e1, e1(symbol.next())); continue;
							// case "->": e1 = new BiExpr("PMEMBER", e1, e1(symbol.next())); continue;
							// case "::": e1 = new BiExpr("NMEMBER", e1, e1(symbol.next())); continue;
							
							case "(": {
								if(!(lhs instanceof AtomExpr)) {
									// NOTE: What if this was a function pointer?
									syntaxError(CompilerError.INVALID_FUNCTION_CALL_EXPRESSION, lhs);
									break;
								}
								
								OpExpr o = new OpExpr(call, lhs);
								reader.next();
								
								// Calling a function
								Function func = currentProgram.getFunctionByName(
									((AtomExpr)lhs).identifier().name()
								);
								
								boolean closed = false;
								
								int length = func.arguments.size();
								for(int i = 0; i < length; i++) {
									Expression arg = e14();
									if(arg == null) syntaxError(CompilerError.INVALID_FUNCTION_CALL_PARAMETER);
									o.add(arg);
									
									if(reader.valueEqualsAdvance(",")) {
										if(i == length - 1) syntaxError(CompilerError.TOO_MANY_FUNCTION_CALL_ARGUMENTS, func.name, length + (length == 1 ? " argument":"arguments"));
										continue;
									}
									
									if(i != length - 1) syntaxError(CompilerError.NOT_ENOUGH_FUNCTION_CALL_ARGUMENTS, func.name, length + (length == 1 ? " argument":"arguments"), i + 1);
									
									if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_CALL_PARENTHESES, reader);
									closed = true;
									break;
								}
								
								if(!closed) {
									if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_CALL_PARENTHESES, reader);
								}
								
								lhs = o;
								continue;
							}
						}
						break;
					}
					
					return lhs;
				}
				
				Expression e2() {
					String value = reader.value();
					
					switch(value) {
						// TODO: Error for globals
						case "&": { reader.next(); return new OpExpr(addptr, e1()); }
						case "*": {
							reader.next();
							Expression rhs = e1();
							if(!rhs.size().isPointer()) {
								syntaxError(CompilerError.INVALID_DEREFERENCE_EXPRESSION);
							}
							
							if(rhs instanceof AtomExpr) {
								AtomExpr atom = (AtomExpr)rhs;
								
								if(atom.isNumber()) {
									if(!atom.size().isPointer()) {
										syntaxError(CompilerError.INVALID_DEREFERENCE_EXPRESSION);
									}
								} else if(atom.isIdentifier()) {
									Identifier ident = atom.identifier();
									
									switch(ident.id_type()) {
										case clazz:
										case funct: syntaxError(CompilerError.INVALID_DEREFERENCE_EXPRESSION);
										default:
									}
								}
							}
							
							return new OpExpr(decptr, rhs);
						}
						case "+": { reader.next(); return e1(); }
						
						case "!": { reader.next(); return new OpExpr(not, e1()); }
						case "~": { reader.next(); return new OpExpr(nor, e1()); }
						case "-": { reader.next(); return new OpExpr(neg, e1()); }
						
						case "(": {
							if(isType(reader.next())) {
								HighType type = getTypeFromSymbol();
								if(type instanceof PrimitiveType) {
									if(type.type() == null) syntaxError(CompilerError.INVALID_CAST_TYPE, type.type());
								}
								
								// TODO: Change the type of the value to a 'i64' if the value is a class object.
								if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_CAST_PARENTHESES, reader);
								Expression rhs = e2();
								
								if(rhs instanceof AtomExpr) {
									AtomExpr a = (AtomExpr)rhs;
									
									if(a.isNumber()) {
										return a.convert(type.type());
									}
								}
								
								OpExpr expr = new OpExpr(cast, rhs);
								expr.override_size = type.type();
								// System.out.println("Cast of type: '" + type + "' / " + rhs);
								return expr;
							} else reader.prev();
						}
					}
					
					return e1();
				}
				
				Expression e1() {
					if(reader.groupEquals("DOUBLE") || reader.groupEquals("FLOAT")) syntaxError(CompilerError.FLOATING_TYPES_NOT_IMPLEMENTED);
					if(reader.groupEquals("LONG")) { String value = reader.valueAdvance(); return new AtomExpr(parseLong(value)); }
					if(reader.groupEquals("INT")) { String value = reader.valueAdvance(); return new AtomExpr(parseInteger(value)); }
					if(reader.groupEquals("STRING")) { String value = reader.valueAdvance(); return new AtomExpr(StringUtils.unescapeString(value.substring(1, value.length() - 1))); } // TODO: Unicode ?
					if(reader.groupEquals("BOOL")) { String value = reader.valueAdvance(); return new AtomExpr(Boolean.parseBoolean(value)); }
					
					if(reader.groupEquals("CHAR")) { // TODO: Unicode ?
						String value = StringUtils.unescapeString(reader.value().substring(1, reader.value().length() - 1));
						if(value.length() != 1) syntaxError(CompilerError.INVALID_CHAR_LITERAL_SIZE);
						reader.next();
						
						return new AtomExpr((byte)(value.charAt(0) & 0xff));
					}
					
					if(reader.groupEquals("IDENTIFIER")) {
						String value = reader.value();
						if(currentFunction == null || !currentFunction.hasIdentifier(value)) {
							if(GLOBAL.containsKey(value)) {
								reader.next();
								return GLOBAL.get(value);
							}
							
							if(currentProgram.hasFunction(value)) {
								reader.next();
								return new AtomExpr(currentProgram.getFunction(value));
							}
							
							syntaxError(CompilerError.UNDECLARED_VARIABLE_OR_FUNCTION, value);
						} else {
							reader.next();
							return new AtomExpr(currentFunction.getIdentifier(value));
						}
						
						reader.next();
						// FIXME: null is not allowed. Return a error expression
						return Expression.EMPTY;
					}
					
					if(reader.valueEqualsAdvance("(")) {
						Expression expr = e15();
						if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_EXPRESSION_PARENTHESES, reader);
						return expr;
					}
					
					syntaxError(CompilerError.INVALID_EXPRESSION, reader);
					return Expression.EMPTY;
				}
				
				private long parseLong(String value) {
					if(value.startsWith("0x")) return Long.parseLong(value.substring(2, value.length() - 1), 16);
					else return Long.parseLong(value.substring(0, value.length() - 1));
				}
				
				private int parseInteger(String value) {
					if(value.startsWith("0x")) return Integer.parseInt(value.substring(2), 16);
					else return Integer.parseInt(value);
				}
			}.parse();
			
			// We clone the expression becuse we want to make all branches
			// unique meaning that they wont share any references.
			reader.resetMarked();
			
			if(expr == null) return Expression.EMPTY;
			
			return expr.clone();
		} catch(StackOverflowError e) {
			e.printStackTrace();
			fatalSyntaxError(CompilerError.EXPRESSION_NESTED_TOO_DEEP);
		}
		
		// If the expression was nested too deep we still want to return someting.
		return Expression.EMPTY;
	}
	
	private Variable nextFuncArgument() {
		Variable variable = new Variable(getTypeFromSymbol());
		if(!reader.groupEquals("IDENTIFIER")) syntaxError(CompilerError.INVALID_FUNCTION_PARAMETER_NAME, reader);
		if(currentFunction.hasIdentifier(reader.value())) syntaxError(CompilerError.REDECLARATION_OF_FUNCTION_PARAMETER, reader);
		variable.name = reader.valueAdvance();
		return variable;
	}
	
	private Modifier nextFuncModifier() {
		String value = reader.valueAdvance();
		return Modifiers.get(value);
	}
	
	
	private boolean acceptModification(Expression expr) {
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			if(!e.isIdentifier()) return false;
			Identifier ident = e.identifier();
			
			switch(ident.id_type()) {
				case clazz:
				case funct: return false;
				default:
			}
			
			return !ident.isGenerated();
		}
		
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			if(e.type() == comma) {
				return acceptModification(e.last());
			}
			
			return e.type() == decptr
				|| e.type() == addptr;
		}
		
		return false;
	}
	
	private boolean isType(Lang reader) {
		String value = reader.value();
		if(reader.valueEquals("unsigned")
		|| reader.valueEquals("signed")) {
			value = reader.peak(1).value;
		}
		
		return defined_types.containsKey(value);
	}
	
	private HighType getTypeFromSymbol() {
		if(!isType(reader)) {
			syntaxError(CompilerError.INVALID_TYPE, reader);
			return HighType.INVALID;
		}
		
		HighType type = defined_types.get(reader.valueAdvance());
		
		if(reader.valueEquals("*")) {
			LowType low = type.type();
			
			int size = 0;
			while(reader.valueEqualsAdvance("*")) {
				size++;
			}
			
			type = new HighType(type.name(), LowType.create(low.type(), size));
		}

		return type;
	}
	
	private boolean isValidName(Lang reader) {
		if(!reader.groupEquals("IDENTIFIER")) return false;
		if(defined_types.containsKey(reader.value())) return false;
		if(GLOBAL.containsKey(reader.value())) return false; // Should only be for globals... 
		if(Modifiers.contains(reader.value())) return false;
		if(Keywords.contains(reader.value())) return false;
		return true;
	}
	
	private void addSyntaxError(CompilerError error, int offset, int count, Object... args) {
		currentProgram.hasErrors = true;
		currentProgram.syntaxMarkers.add(new CompilerMarker(
			sourceFile,
			reader,
			offset,
			count,
			SyntaxMarker.ERROR,
			String.format(error.getMessage(), args),
			_caller(),
			error
		));
	}
	
	private void addSyntaxWarning(CompilerError error, int offset, int count, Object... args) {
		currentProgram.syntaxMarkers.add(new CompilerMarker(
			sourceFile,
			reader,
			offset,
			count,
			SyntaxMarker.WARNING,
			String.format(error.getMessage(), args),
			_caller(),
			error
		));
	}
	
	private void syntaxError(CompilerError error, Object... args) {
		String compilerMessage = _caller();
		String message = String.format(error.getMessage(), args);
		
		currentProgram.hasErrors = true;
		currentProgram.syntaxMarkers.add(new CompilerMarker(
			sourceFile,
			reader,
			SyntaxMarker.ERROR,
			compilerMessage,
			message,
			error
		));
	}
	
	private void fatalSyntaxError(CompilerError error, Object... args) {
		String compilerMessage = _caller();
		String message = String.format(error.getMessage(), args);
		
		currentProgram.hasErrors = true;
		currentProgram.syntaxMarkers.add(new CompilerMarker(
			sourceFile,
			reader,
			SyntaxMarker.ERROR,
			compilerMessage,
			message,
			error
		));
		
		// Break the execution
		throw new CompilerException(message.toString());
	}
	
	@Deprecated(forRemoval = true)
	void syntaxError(String format, Object... args) {
		String compilerMessage = _caller();
		String message = String.format(format, args);
		
		currentProgram.hasErrors = true;
		currentProgram.syntaxMarkers.add(new CompilerMarker(
			sourceFile,
			reader,
			SyntaxMarker.ERROR,
			compilerMessage,
			message,
			CompilerError.NONE
		));
	}
	
	private String _caller() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[3];
		return "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
	}
	
	
	/**
	 * Create a Program from a single byte array. Any attempts to import other files will fail.
	 * @param bytes
	 * @return
	 */
	public static IProgram loadParseTreeFromBytes(byte[] bytes) {
		ParseTreeGenerator generator = new ParseTreeGenerator();
		Program program = new Program();
		
		try {
			for(HighType t : Primitives.getAllTypes()) {
				generator.defined_types.put(t.name(), t);
			}
			
			generator.currentProgram = program;
			generator.importFile(new File("null"), bytes);
		} catch(Throwable t) {
			t.printStackTrace();
			program.hasErrors = true;
		}
		
		return program;
	}
}