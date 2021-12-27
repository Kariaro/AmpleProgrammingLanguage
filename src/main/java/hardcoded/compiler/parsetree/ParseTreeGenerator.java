package hardcoded.compiler.parsetree;

import static hardcoded.compiler.constants.ExprType.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hardcoded.compiler.constants.*;
import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.context.LangContext;
import hardcoded.compiler.errors.*;
import hardcoded.compiler.expression.*;
import hardcoded.compiler.impl.*;
import hardcoded.compiler.statement.*;
import hardcoded.compiler.types.HighType;
import hardcoded.compiler.types.PrimitiveType;
import hardcoded.configuration.CompilerConfiguration;
import hardcoded.lexer.LexerTokenizer;
import hardcoded.lexer.Token;
import hardcoded.lexer.TypeGroups;
import hardcoded.utils.*;
import hardcoded.lexer.Token.Type;

public class ParseTreeGenerator {
	// FIXME: Add global variables
	// FIXME: Add non const arrays
	// TODO: The source file should be stored inside the reader
	
	private CompilerConfiguration config;
	private Function currentFunction;
	private Program currentProgram;
	
	private File sourceFile;
	private LangContext reader;
	
	private boolean ran = false;
	public Program init(CompilerConfiguration config, File mainFile) {
		if(ran) throw new CompilerException("ParseTreeGenerator is not reusable");
		ran = true;
		
		this.config = config;
		this.currentProgram = new Program();
		for(HighType t : Primitives.getAllTypes()) {
			this.currentProgram.addDefinedType(t.name(), t);
		}
		
		try {
			importFile(mainFile);
		} catch(Throwable t) {
			// This is a real error and was not thrown by the compiler
			syntaxError(CompilerError.MESSAGE, "%s: %s".formatted(t.getCause(), t.getMessage()));
			t.printStackTrace();
		}
		
		return currentProgram;
	}
	
	private void importFile(String path) {
		List<File> files = config.lookupFile(path);
		if(files.isEmpty()) {
			addSyntaxError(CompilerError.MESSAGE, -2, 1, "The file '%s' does not exist".formatted(path));
			return;
		}
		
		if(files.size() > 1) {
			addSyntaxError(CompilerError.MESSAGE, -2, 1, "The file '%s' has multiple definitions\n%s".formatted(path, files));
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
			addSyntaxError(CompilerError.MESSAGE, -2, 1, "The file '%s' does not exist".formatted(path));
			return;
		}
		
		if(currentProgram.hasImportedFile(sourceFile)) {
			addSyntaxWarning(CompilerError.MESSAGE, -2, 1, "The file '%s' has already been imported".formatted(path));
			return;
		}
		
		importFile(sourceFile);
	}
	
	// Try to add more information about each source file
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
		
		// Keep a reference to the last source file.
		File lastSourceFile = sourceFile;
		LangContext lastReader = this.reader;
		
		sourceFile = newSourceFile;
		currentProgram.addImportedFile(newSourceFile);
		reader = LangContext.wrap(LexerTokenizer.parse(newSourceFile, bytes));
		
		try {
			// Read all blocks inside the file
			while(nextBlock() != null);
		} catch(CompilerException e) {
			// Used to break free if fatal or other errors occured
		}
		
		this.reader = lastReader;
		this.sourceFile = lastSourceFile;
	}
	
	private IBlock nextBlock() {
		if(reader.value().startsWith("@")) {
			parseCompiler();
			
			return IBlock.EMPTY;
		} else {
			if(reader.type() == Type.EOF) return null;
			return makeFunction();
		}
	}
	
	private void parseCompiler() {
		if(reader.type() == Type.IMPORT) {
			reader.advance();

			tryMatchOrSyntaxError(Type.STRING, CompilerError.INVALID_IMPORT_EXPECTED_STRING, reader.value());
			
			String pathname = reader.value();
			pathname = pathname.substring(1, pathname.length() - 1);

			reader.advance();
			tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_IMPORT_EXPECTED_SEMICOLON, reader);
			reader.advance();
			importFile(pathname);
		}
		// TODO: Before creating defines we need to complete compiler annotations and make the type
		//       checking methods much better at storing the internal state.
		
		/*
		else if(reader.type() == Type.DEFINE_TYPE) {
			reader.advance();
			
			String name = reader.value();
			if(defined_types.containsKey(name)) syntaxError(CompilerError.INVALID_TYPE_PROCESSOR_REDECLARATION, name);
			if(!isValidName(reader)) syntaxError(CompilerError.INVALID_TYPE_PROCESSOR_NAME, name);
			
			reader.advance();
			HighType type = getTypeFromSymbol();
			
			tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_TYPE_PROCESSOR_EXPECTED_SEMICOLON, reader);
			reader.advance();
			
			defined_types.put(name, new HighType(name, type.type()));
		} else if(reader.type() == Type.DEFINE) {
			reader.advance();
			if(!isValidName(reader)) syntaxError(CompilerError.INVALID_SET_PROCESSOR_NAME, reader);
			String name = reader.value();
			reader.advance();
			Expression expr = nextExpression();
			
			tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_SET_PROCESSOR_EXPECTED_SEMICOLON, reader);
			reader.advance();
			
			GLOBAL.put(name, expr);
			
		} else if(reader.type() == Type.UNDEFINE) {
			reader.advance();
			String name = reader.value();
			reader.advance();

			tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_UNSET_PROCESSOR_EXPECTED_SEMICOLON, reader);
			reader.advance();
			
			if(defined_types.containsKey(name)) {
				if(defined_types.get(name) instanceof PrimitiveType) syntaxError(CompilerError.INVALID_UNSET_PROCESSOR_NO_PRIMITIVES, name);
				defined_types.remove(name);
			} else if(GLOBAL.containsKey(name)) {
				GLOBAL.remove(name);
			} else {
				// Trying to remove something that does not exist...
			}
			
		}
		*/
	}
	
	private Function makeFunction() {
		Position startPosition = reader.position();
		
		List<Modifier> modifiers = new ArrayList<>();
		Function func;
		
		while(TypeGroups.isFunctionModifier(reader.type())) {
			modifiers.add(nextFuncModifier());
		}
		
		if(!isType(reader)) addSyntaxError(CompilerError.INVALID_FUNCTION_TYPE, 0, 1, reader.value());
		HighType returnType = getTypeFromSymbol();
		
		if(!isValidName(reader)) addSyntaxError(CompilerError.INVALID_FUNCTION_NAME, 0, 1, reader.value());
		String functionName = reader.value();
		
		boolean needsBody = false;
		
		if(currentProgram.hasFunction(functionName)) {
			func = currentProgram.getFunctionByName(functionName);
			
			if(!func.isPlaceholder()) {
				addSyntaxError(CompilerError.INVALID_FUNCTION_REDECLARATION, 0, 1, functionName);
			} else {
				if(!func.getReturnType().equals(returnType)) {
					int depth = func.getReturnType().depth();
					
					addSyntaxError(
						CompilerError.INVALID_FUNCTION_DECLARATION_WRONG_RETURN_TYPE,
						-depth, depth + 1, reader.value()
					);
				}
				
				if(!func.getModifiers().containsAll(modifiers)) {
					syntaxError(CompilerError.INVALID_FUNCTION_DECLARATION_WRONG_MODIFIERS, modifiers, func.getModifiers());
				}
				
				func.getArguments().clear();
				needsBody = true;
			}
		} else {
			func = new Function(functionName, returnType, modifiers);
			currentProgram.addFunction(func);
		}
		
		currentFunction = func;
		reader.advance();
		
		tryMatchOrSyntaxError(Type.LEFT_PARENTHESIS, -1, 2, CompilerError.INVALID_FUNCTION_NAME, reader.peakString(-1, 2));
		reader.advance();
		
		while(reader.type() != Type.RIGHT_PARENTHESIS) {
			VariableStat arg = nextFuncArgument();
			
			// Compare hightypes.
			func.addArgument(arg);
			
			if(reader.type() != Type.COMMA) {
				tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.MISSING_FUNCTION_PARAMETER_SEPARATOR, reader);
				break;
			}
			
			reader.advance();
		}
		
		tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.INVALID_FUNCTION_DECLARATION_EXPECTED_CLOSING_PARENTHESIS, reader);
		reader.advance();
		
		if(reader.type() == Type.SEMICOLON) {
			if(needsBody) syntaxError(CompilerError.INVALID_FUNCTION_DECLARATION_EXPECTED_A_FUNCTION_BODY);
			reader.advance();
			
			func.syntaxPosition = ISyntaxPosition.of(startPosition, reader.lastPositionEnd());
			return func;
		}

		tryMatchOrSyntaxError(Type.LEFT_CURLY_BRACKET, CompilerError.INVALID_FUNCTION_DECLARATION_EXPECTED_OPEN_CURLYBRACKET, reader);
		
		currentFunction.pushScope();
		func.body = getStatements();
		currentFunction.popScope();
		
		for(Map.Entry<String, Token> entry : currentFunction.getRequiredLabels().entrySet()) {
			if(!currentFunction.hasLabel(entry.getKey())) {
				Token token = entry.getValue();
				int offset = reader.readerIndex() - reader.indexOf(token);
				addSyntaxError(CompilerError.INVALID_GOTO_LABEL_NOT_FOUND, -offset, 1, token);
			}
		}
		
		return func;
	}
	
	private Statement nextStatement() {
		if(reader.type() == Type.EOF) return Statement.newEmpty();
		
		if(isType(reader)) {
			Statement stat = getVariableDefinition();
			
			if(stat instanceof StatementList list) {
				for(int i = 0; i < list.size(); i++) {
					VariableStat var = (VariableStat)list.get(i);
					Identifier ident = currentFunction.add(var);
					
					if(!var.isInitialized()) {
						list.remove(i--);
					} else {
						list.set(i, new ExprStat(new OpExpr(ExprType.set, new AtomExpr(ident), var.getValue())));
					}
				}
			}
			
			return stat;
		}
		
		switch(reader.type()) {
			case IF -> { return makeIfStatement(); }
			case FOR -> { return makeForStatement(); }
			case DO -> { return makeDoWhileStatement(); }
			case WHILE -> { return makeWhileStatement(); }
			case LEFT_CURLY_BRACKET -> { return getStatements(); }
		}
		
		switch(reader.type()) {
			case BREAK -> {
				reader.advance();
				tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_BREAK_STATEMENT_EXPECTED_SEMICOLON, reader);
				reader.advance();
				return new ExprStat(new OpExpr(leave));
			}
			case CONTINUE -> {
				reader.advance();
				tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_CONTINUE_STATEMENT_EXPECTED_SEMICOLON, reader);
				reader.advance();
				return new ExprStat(new OpExpr(loop));
			}
			case RETURN -> {
				reader.advance();
				OpExpr expr = new OpExpr(ret);
				if(reader.type() != Type.SEMICOLON) {
					expr.add(nextExpression());
				} else {
					expr.add(new AtomExpr(0));
				}
				
				tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_RETURN_STATEMENT_EXPECTED_SEMICOLON, reader);
				reader.advance();
				return new ExprStat(expr);
			}
			case GOTO -> {
				reader.advance();
				Expression expr;
				if(!isValidName(reader)) {
					syntaxError(CompilerError.INVALID_GOTO_LABEL_NAME, reader);
					expr = Expression.EMPTY;
				} else {
					OpExpr op = new OpExpr(jump);
					op.add(new AtomExpr(reader.value()));
					currentFunction.addRequiredLabel(reader.value(), reader.token());
					expr = op;
				}
				
				reader.advance();

				tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_GOTO_STATEMENT_EXPECTED_SEMICOLON, reader);
				reader.advance();
				return new ExprStat(expr);
			}
			case IDENTIFIER -> {
				reader.advance();
				if(reader.type() == Type.COLON) {
					Expression expr;
					reader.recede();
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
							currentFunction.addLabel(name);
							expr = op;
						}
					}
					
					reader.advance();
					reader.advance();
					return new ExprStat(expr);
				} else {
					reader.recede();
				}
			}
		}
		
		Expression expr = nextExpression();
		
		if(!hasModifications(expr)) {
			expr = Expression.EMPTY;
			syntaxError(CompilerError.INVALID_EXPRESSION_MESSAGE, "The expression was empty or has weird syntax", reader);
		}
		
		if(reader.type() == Type.SEMICOLON) {
			reader.advance();
			return new ExprStat(expr);
		}
		
		syntaxError(CompilerError.INVALID_EXPR_STATEMENT_EXPECTED_SEMICOLON, reader);
		reader.advance();
		
		return Statement.newEmpty();
	}
	
	private Statement makeWhileStatement() {
		Position startPosition = reader.position();
		reader.advance();

		tryMatchOrSyntaxError(Type.LEFT_PARENTHESIS, CompilerError.INVALID_WHILE_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		reader.advance();
		
		Expression condition = nextExpression();
		
		tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.INVALID_WHILE_STATEMENT_UNCLOSED_PARENTHESES, reader);
		reader.advance();
		
		Statement body = getStatements();
		return new WhileStat(condition, body, ISyntaxPosition.of(startPosition, reader.lastPositionEnd()));
	}
	
	private Statement makeDoWhileStatement() {
		Position startPosition = reader.position();
		
		reader.advance();
		Statement body = nextStatement();
		
		tryMatchOrSyntaxError(Type.WHILE, CompilerError.INVALID_SYNTAX, "do while loop is missing while keyword");
		reader.advance();
		
		tryMatchOrSyntaxError(Type.LEFT_PARENTHESIS, CompilerError.INVALID_WHILE_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		reader.advance();
		
		Expression condition = nextExpression();
		
		tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.INVALID_WHILE_STATEMENT_UNCLOSED_PARENTHESES, reader);
		reader.advance();
		
		tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_SYNTAX, "do while loop is missing closing semicolon");
		reader.advance();
		
		return new DoWhileStat(condition, body, ISyntaxPosition.of(startPosition, reader.lastPositionEnd()));
	}
	
	private Statement makeForStatement() {
		Position startPosition = reader.position();
		reader.advance();
		tryMatchOrSyntaxError(Type.LEFT_PARENTHESIS, CompilerError.INVALID_FOR_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		reader.advance();
		
		Statement variableDecl;
		Expression condition = Expression.EMPTY;
		Expression action = Expression.EMPTY;
		Statement body;
		
		// Push a new scope
		currentFunction.pushScope();
		
		if(reader.type() != Type.SEMICOLON) {
			variableDecl = getVariableDefinition();
			
			if(variableDecl instanceof StatementList list) {
				for(int i = 0; i < list.size(); i++) {
					VariableStat var = (VariableStat)list.get(i);
					Identifier ident = currentFunction.add(var);
					
					if(!var.isInitialized()) {
						list.remove(i--);
					} else {
						list.set(i, new ExprStat(new OpExpr(ExprType.set, new AtomExpr(ident), var.getValue())));
					}
				}
			}
			
			reader.recede();
		} else {
			variableDecl = Statement.newEmpty();
		}

		tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_XXX_EXPECTED_SEMICOLON, "for statement (variables)", reader);
		reader.advance();
		if(reader.type() != Type.SEMICOLON) condition = nextExpression();
		
		tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.INVALID_XXX_EXPECTED_SEMICOLON, "for statement (condition)", reader);
		reader.advance();
		if(reader.type() != Type.SEMICOLON) action = nextExpression();
		
		tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		reader.advance();
		body = getStatements();
		
		// Pop Scope
		currentFunction.popScope();
		
		return new ForStat(variableDecl, condition, action, body, ISyntaxPosition.of(startPosition, reader.lastPositionEnd()));
	}
	
	private Statement makeIfStatement() {
		Position startPosition = reader.position();
		
		reader.advance();
		tryMatchOrSyntaxError(Type.LEFT_PARENTHESIS, CompilerError.INVALID_IF_STATEMENT_EXPECTED_OPEN_PARENTHESIS, reader);
		reader.advance();
		
		Expression condition = nextExpression();

		tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		reader.advance();
		
		Statement body = nextStatement();
		Statement elseBody;
		
		if(reader.type() == Type.ELSE) {
			reader.advance();
			elseBody = nextStatement();
		} else {
			elseBody = Statement.newEmpty();
		}
		
		return new IfStat(condition, body, elseBody, ISyntaxPosition.of(startPosition, reader.lastPositionEnd()));
	}
	
	private Statement getVariableDefinition() {
		HighType type = getTypeFromSymbol();
		
		if(type.type().equals(Atom.unf)) {
			syntaxError(CompilerError.INVALID_VARIABLE_TYPE, type);
		}
		
		List<VariableStat> list = new ArrayList<VariableStat>();
		while(true) {
			if(!isValidName(reader)) syntaxError(CompilerError.INVALID_VARIABLE_NAME, reader);
			if(currentFunction.hasIdentifier(reader.value())) syntaxError(CompilerError.REDECLARATION_OF_LOCAL_VARIABLE, reader);
			String var_name = reader.value();
			reader.advance();
			
			VariableStat var = new VariableStat(type, var_name);
			list.add(var);
			
			if(reader.type() == Type.LEFT_SQUARE_BRACKET) {
				reader.advance();
				Expression expr = nextExpression();
				var.isArray = true;
				
				if(expr instanceof AtomExpr number) {
					if(!number.isNumber()) {
						syntaxError(CompilerError.INVALID_ARRAY_VARIABLE_DECLARATION_EXPECTED_INTEGER, expr);
					}
					
					var.list.add(Expression.EMPTY);
					var.arraySize = (int)number.number();
					var.setType(new HighType(type.name(), type.type().nextHigherPointer()));
				} else {
					syntaxError(CompilerError.INVALID_ARRAY_VARIABLE_DECLARATION_EXPECTED_INTEGER, expr);
				}
				
				tryMatchOrSyntaxError(Type.RIGHT_SQUARE_BRACKET, CompilerError.UNCLOSED_ARRAY_DEFINITION, reader);
				reader.advance();
				
				tryMatchOrSyntaxError(Type.SEMICOLON, CompilerError.UNCLOSED_VARIABLE_DECLARATION);
				reader.advance();
				break;
			}
			
			if(reader.type() == Type.SEMICOLON) {
				reader.advance();
				break;
			} else if(reader.type() == Type.ASSIGN) {
				reader.advance();
				Expression expr = nextExpression(true);
				LowType e_size = expr.size();
				
				if(!type.isPointer()) {
					if(e_size.isPointer()) {
						syntaxError(CompilerError.INVALID_VARIABLE_ASSIGNMENT, "Pointer sizes are different (Type:%s != Got: %s)".formatted(type.depth(), e_size.depth()));
					} else {
						if(type.size() != e_size.size()) {
							OpExpr next = new OpExpr(ExprType.cast, expr);
							next.override_size = type.type();
							expr = next;
						}
					}
				}
				
				var.setValue(expr);
				
				if(reader.type() == Type.SEMICOLON) {
					reader.advance();
					break;
				}
			}
			
			if(reader.type() != Type.COMMA) {
				syntaxError(CompilerError.INVALID_VARIABLE_DECLARATION_MISSING_COLON_OR_SEMICOLON, reader);
				break;
			}
			
			reader.advance();
		}
		
		return new StatementList(list);
	}
	
	private Statement getStatements() {
		if(reader.type() == Type.SEMICOLON) {
			reader.advance();
			return Statement.newEmpty();
		}
		
		if(reader.type() != Type.LEFT_CURLY_BRACKET) {
			return nextStatement();
		} else {
			reader.advance();
		}
		
		NestedStat stat = new NestedStat();
		while(true) {
			if(reader.type() == Type.EOF
			|| reader.type() == Type.RIGHT_CURLY_BRACKET) break;
			
			if(reader.type() == Type.SEMICOLON) {
				reader.advance();
				continue;
			}
			
			// This should never return null.
			Statement s = nextStatement();
			if(s.isEmptyStat() || (s.hasElements() && s.size() == 0)) continue;
			
			if(s instanceof StatementList statList) {
				stat.getElements().addAll(statList.getElements());
				continue;
			}
			
			stat.add(s);
		}
		
		tryMatchOrSyntaxError(Type.RIGHT_CURLY_BRACKET, CompilerError.UNCLOSED_CURLY_BRACKETS_STATEMENT, reader);
		reader.advance();
		
		if(stat.size() == 0) {
			return Statement.newEmpty();
		}
		
		return stat;
	}
	
	// Check if a expression does any modification
	private boolean hasModifications(Expression expr) {
		switch(expr.type()) {
			case call, jump, label, loop, leave, set -> {
				return true;
			}
			
			default -> {
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
	
	private Expression nextExpression() {
		return nextExpression(false);
	}
	
	private Expression nextExpression(boolean skipComma) {
		try {
			/*
			 * This is a recursive descent parser.
			 */
			Expression expr = new Object() {
				private ExprType[] _e(ExprType... array) { return array; }
				private Type[] _s(Type... array) { return array; }
				
				Expression e_read(Type[] values, ExprType[] exprs, java.util.function.Supplier<Expression> func) { return e_read(values, exprs, func, func); }
				Expression e_read(Type[] values, ExprType[] exprs, java.util.function.Supplier<Expression> entry, java.util.function.Supplier<Expression> func) {
					for(Expression expr = entry.get();;) {
						boolean found = false;
						for(int i = 0; i < values.length; i++) {
							if(reader.type() == values[i]) {
								reader.advance();
								found = true;
								expr = new OpExpr(exprs[i], expr, func.get());
								break;
							}
						}
						
						if(!found) return expr;
					}
				}
				
				Expression e_read_combine(Type value, ExprType type, java.util.function.Supplier<Expression> func) { return e_read_combine(value, type, func, func); }
				Expression e_read_combine(Type value, ExprType type, java.util.function.Supplier<Expression> entry, java.util.function.Supplier<Expression> func) {
					boolean hasFirst = false;
					for(Expression expr = entry.get();;) {
						if(reader.type() == value) {
							reader.advance();
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
				Expression e15() { return e_read_combine(Type.COMMA, comma, this::e14); }
				Expression e14() { // Left associative
					Expression left = e13();
					
					// Check if the value we are looking at is a assignment operator.
					if(!TypeGroups.isAssignmentOperator(reader.type())) {
						return left;
					}
					
					// Check if the left hand side can be modified
					if(!acceptModification(left)) {
						syntaxError(CompilerError.INVALID_MODIFICATION);
					}
					
					ExprType type = switch(reader.type()) {
						case ASSIGN: yield set;
						case ADD_ASSIGN: yield add;
						case SUB_ASSIGN: yield sub;
						case MUL_ASSIGN: yield mul;
						case DIV_ASSIGN: yield div;
						case MOD_ASSIGN: yield mod;
						case AND_ASSIGN: yield and;
						case XOR_ASSIGN: yield xor;
						case OR_ASSIGN: yield or;
						case SHIFT_LEFT_ASSIGN: yield shl;
						case SHIFT_RIGHT_ASSIGN: yield shr;
						default: yield null;
					};
					
					// FIXME: This should scale with the size of the baseSize of a lowType !!!
					//        Only allowed for [ += ] and [ -= ]
					reader.advance();
					
					// The value that should be assigned.
					Expression assigner = left;
					
					// Right hand side of the expression.
					Expression right = e14();
					
					/*
					 * If the left hand side is a comma expression then
					 * this will be the expression containing the assigner
					 * variable.
					 */
					Expression comma_parent = left;
					boolean comma_assigned = false;
					
					{
						/* If the left hand side of the expression is a comma	[ ( ... , x) ]
						 * then the assignment operation should be placed only
						 * on the last element of that comma expression.
						 * 
						 * An example would be that the expression 	[ ( ... , x) += 5 ]
						 * should become 							[ ( ... , x += 5) ]
						 */
						if(left.type() == comma) {
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
						if(!right.isPure()) {
							AtomExpr temp = new AtomExpr(currentFunction.getTempLocal(right.size()));
							right = new OpExpr(comma,
								new OpExpr(set, temp, right),
								temp
							);
						}
						
						if(type != set) right = new OpExpr(type, assigner, right);
						
						if(comma_assigned) {
							((OpExpr)comma_parent).set(comma_parent.length() - 1, new OpExpr(set, assigner, right));
							return left;
						}
						
						return new OpExpr(set, assigner, right);
					}
				}
				
				Expression e13() {
					Expression expr = e12();
					if(reader.type() == Type.QUESTION_MARK) {
						reader.advance();
						Expression b = e12();
						
						tryMatchOrSyntaxError(Type.COLON, CompilerError.INVALID_TERNARY_OPERATOR_MISSING_COLON, reader);
						reader.advance();
						Expression c = e12();
						
						AtomExpr temp = new AtomExpr(currentFunction.getTempLocal(LowType.largest(b.size(), c.size())));
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
				
				Expression e12() { return e_read(_s(Type.COR), _e(cor), this::e11, this::e12); }
				Expression e11() { return e_read(_s(Type.CAND), _e(cand), this::e10, this::e11); }
				Expression e10() { return e_read(_s(Type.OR), _e(or), this::e9); }
				Expression e9() { return e_read(_s(Type.XOR), _e(xor), this::e8); }
				Expression e8() { return e_read(_s(Type.AND), _e(and), this::e7); }
				Expression e7() { return e_read(_s(Type.EQUALS, Type.NOT_EQUALS), _e(eq, neq), this::e6, this::e7); }
				Expression e6() { return e_read(_s(Type.LESS_THAN, Type.LESS_THAN_EQUAL, Type.MORE_THAN, Type.MORE_THAN_EQUAL), _e(lt, lte, gt, gte), this::e5); }
				Expression e5() { return e_read(_s(Type.SHIFT_LEFT, Type.SHIFT_RIGHT), _e(shl, shr), this::e4); }
				Expression e4() { return e_read(_s(Type.PLUS, Type.MINUS), _e(add, sub), this::e3); }
				Expression e3() { return e_read(_s(Type.MUL, Type.DIV, Type.MOD), _e(mul, div, mod), this::e2_2); }
				
				Expression e2_2() {
					switch(reader.type()) {
						case INC, DEC -> {
							ExprType dir = reader.type() == Type.INC ? add:sub;
							
							reader.advance();
							Expression right = e2_3();
							if(!acceptModification(right)) {
								syntaxError(CompilerError.INVALID_MODIFICATION);
							}
							
							Expression result = right;
							
							// FIXME: This should scale with the size of the baseSize of a lowType !!!
							
							/* If the right hand side is a comma expression then the
							 * modification should effect only the last element.
							 * 
							 * The expression		[ ++( ... , x ) ]
							 * will be evaluated	[ ( ... , ++x ) ]
							 */
							if(result.type() == comma) {
								right = right.last();
							}
							
							if(right.isPure()) {
								right = new OpExpr(set, right, new OpExpr(dir, right, new AtomExpr(1)));
							} else {
								AtomExpr ident = new AtomExpr(currentFunction.getTempLocal(right.size()));
								
								// TODO: Tell why a modification to a value that is not pure must be memory modification.
								// TODO: Check that the value is a valid memory modification.
								right = new OpExpr(comma,
									new OpExpr(set, ident, new OpExpr(incptr, right)),
									new OpExpr(set,
										new OpExpr(decptr, ident),
										new OpExpr(dir, new OpExpr(decptr, ident), new AtomExpr(1))
									)
								);
							}
							
							if(result.type() == comma) {
								result.set(result.length() - 1, right);
								return result;
							}
							
							return right;
						}
					}
					
					// Left hand side
					Expression left = e2_3();

					switch(reader.type()) {
						case INC, DEC -> {
							if(!acceptModification(left)) syntaxError(CompilerError.INVALID_MODIFICATION);
							ExprType dir = reader.type() == Type.INC ? add:sub;
							
							reader.advance();
							
							// TODO: Allow for comma expressions!
							// TODO: Use the same solution as the assignment operators.
							// FIXME: This should scale with the size of the baseSize of a lowType !!!
							
							AtomExpr ident2 = new AtomExpr(currentFunction.getTempLocal(left.size().nextHigherPointer()));
							if(!left.isPure()) {
								AtomExpr ident1 = new AtomExpr(currentFunction.getTempLocal(left.size()));
								
								// ( ... ) ++
								
								// For this to be not pure it must be a pointer.
								
								return new OpExpr(comma,
									// Calculate inside of lhs...
									new OpExpr(set, ident2, new OpExpr(incptr, left)),
									new OpExpr(set, ident1, new OpExpr(decptr, ident2)),
									new OpExpr(set, new OpExpr(decptr, ident2), new OpExpr(dir, new OpExpr(decptr, ident2), new AtomExpr(1))),
									ident1
								);
							}
							
							return new OpExpr(comma,
								new OpExpr(set, ident2, left),
								new OpExpr(set, left, new OpExpr(dir, left, new AtomExpr(1))),
								ident2
							);
						}
					}
					
					return left;
				}
				
				Expression e2_3() {
					Expression left = e2();
					
					while(true) {
						switch(reader.type()) {
							case LEFT_SQUARE_BRACKET -> {
								reader.advance();
								Expression expr = e15();
								
								tryMatchOrSyntaxError(Type.RIGHT_SQUARE_BRACKET, CompilerError.UNCLOSED_ARRAY_EXPRESSION, reader);
								reader.advance();
								
								Expression result = left;
								if(result.type() == comma) left = left.last();
								
								LowType type = left.size();
								if(!type.isPointer() && left.type() != ExprType.decptr) {
									syntaxError(CompilerError.INVALID_ARRAY_NOT_A_POINTER, reader);
								}
								
								if(left instanceof AtomExpr atom) {
									if(atom.isNumber()) {
										syntaxError(CompilerError.INVALID_ARRAY_NOT_A_POINTER, reader);
									} else if(atom.isIdentifier()) {
										Identifier ident = atom.identifier();
										
										switch(ident.getIdType()) {
											case clazz, funct -> {
												syntaxError(CompilerError.INVALID_ARRAY_NOT_A_POINTER, reader);
											}
										}
									}
								}
								
								left = new OpExpr(decptr, new OpExpr(add,
									left,
									new OpExpr(mul, expr, new AtomExpr(type.baseSize()))	
								));
								
								if(result.type() == comma) {
									result.set(result.length() - 1, left);
									left = result;
								}
								
								continue;
							}
							
							case LEFT_PARENTHESIS -> {
								if(!(left instanceof AtomExpr)) {
									// NOTE: What if this was a function pointer?
									syntaxError(CompilerError.INVALID_FUNCTION_CALL_EXPRESSION, left);
									break;
								}
								
								OpExpr expr = new OpExpr(call, left);
								reader.advance();
								
								// Calling a function
								Function func = currentProgram.getFunctionByName(
									((AtomExpr)left).identifier().getName()
								);
								
								boolean closed = false;
								
								int length = func.getArguments().size();
								for(int i = 0; i < length; i++) {
									Expression arg = e14();
									if(arg == null) syntaxError(CompilerError.INVALID_FUNCTION_CALL_PARAMETER);
									expr.add(arg);
									
									if(reader.type() == Type.COMMA) {
										reader.advance();
										if(i == length - 1) syntaxError(CompilerError.TOO_MANY_FUNCTION_CALL_ARGUMENTS, func.getName(), length + (length == 1 ? " argument":"arguments"));
										continue;
									}
									
									if(i != length - 1) syntaxError(CompilerError.NOT_ENOUGH_FUNCTION_CALL_ARGUMENTS, func.getName(), length + (length == 1 ? " argument":"arguments"), i + 1);
									
									tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.UNCLOSED_CALL_PARENTHESES, reader);
									reader.advance();
									
									closed = true;
									break;
								}
								
								if(!closed) {
									tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.UNCLOSED_CALL_PARENTHESES, reader);
									reader.advance();
								}
								
								left = expr;
								continue;
							}
						}
						break;
					}
					
					return left;
				}
				
				Expression e2() {
					switch(reader.type()) {
						// Reference
						case AND -> { reader.advance(); return new OpExpr(incptr, e1()); }
						// Dereference
						case MUL -> {
							reader.advance();
							Expression rhs = e1();
							if(!rhs.size().isPointer()) {
								syntaxError(CompilerError.INVALID_DEREFERENCE_EXPRESSION);
							}
							
							if(rhs instanceof AtomExpr atom) {
								if(atom.isNumber()) {
									if(!atom.size().isPointer()) {
										syntaxError(CompilerError.INVALID_DEREFERENCE_EXPRESSION);
									}
								} else if(atom.isIdentifier()) {
									Identifier ident = atom.identifier();
									
									switch(ident.getIdType()) {
										case clazz, funct -> {
											syntaxError(CompilerError.INVALID_DEREFERENCE_EXPRESSION);
										}
									}
								}
							}
							
							return new OpExpr(decptr, rhs);
						}
						case PLUS -> { reader.advance(); return e1(); }
						case NOT -> { reader.advance(); return new OpExpr(not, e1()); }
						case NOR -> { reader.advance(); return new OpExpr(nor, e1()); }
						case MINUS -> { reader.advance(); return new OpExpr(neg, e1()); }
						case LEFT_PARENTHESIS -> {
							reader.advance();
							if(isType(reader)) {
								HighType type = getTypeFromSymbol();
								if(type instanceof PrimitiveType && type.type() == null) {
									syntaxError(CompilerError.INVALID_CAST_TYPE, type.type());
								}
								
								tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.UNCLOSED_CAST_PARENTHESES, reader);
								reader.advance();
								Expression rhs = e2();
								
								if(rhs instanceof AtomExpr atom && atom.isNumber()) {
									return atom.convert(type.type());
								}
								
								OpExpr expr = new OpExpr(cast, rhs);
								expr.override_size = type.type();
								return expr;
							} else {
								reader.recede();
							}
						}
					}
					
					return e1();
				}
				
				Expression e1() {
					switch(reader.type()) {
						case DOUBLE -> {
							syntaxError(CompilerError.FLOATING_TYPES_NOT_IMPLEMENTED);
						}
						case LONG -> {
							String value = reader.value();
							reader.advance();
							return new AtomExpr(StringUtils.parseLong(value));
						}
						case INT -> {
							String value = reader.value();
							reader.advance();
							return new AtomExpr(StringUtils.parseInteger(value));
						}
						case STRING -> {
							String value = reader.value();
							reader.advance();
							return new AtomExpr(StringUtils.unescapeString(value.substring(1, value.length() - 1)));
						}
						case BOOLEAN -> {
							String value = reader.value();
							reader.advance();
							return new AtomExpr(Boolean.parseBoolean(value));
						}
						case CHARACTER -> {
							String value = StringUtils.unescapeString(reader.value().substring(1, reader.value().length() - 1));
							if(value.length() != 1) syntaxError(CompilerError.INVALID_CHAR_LITERAL_SIZE);
							reader.advance();
							return new AtomExpr(value.charAt(0));
						}
						case IDENTIFIER -> {
							String value = reader.value();
							if(currentFunction == null || !currentFunction.hasIdentifier(value)) {
								if(currentProgram.hasDefinedGlobal(value)) {
									reader.advance();
									return currentProgram.getDefinedGlobal(value);
								}
								
								if(currentProgram.hasFunction(value)) {
									reader.advance();
									return new AtomExpr(currentProgram.getFunction(value));
								}
								
								syntaxError(CompilerError.UNDECLARED_VARIABLE_OR_FUNCTION, value);
							} else {
								reader.advance();
								return new AtomExpr(currentFunction.getIdentifier(value));
							}
							
							reader.advance();
							return Expression.EMPTY;
						}
						case LEFT_PARENTHESIS -> {
							reader.advance();
							Expression expr = e15();
							tryMatchOrSyntaxError(Type.RIGHT_PARENTHESIS, CompilerError.UNCLOSED_EXPRESSION_PARENTHESES, reader);
							reader.advance();
							return expr;
						}
					}
					
					syntaxError(CompilerError.INVALID_EXPRESSION, reader);
					return Expression.EMPTY;
				}
			}.parse();
			
			// We clone the expression becuse we want to make all branches
			// unique meaning that they wont share any references.
			if(expr == null) return Expression.EMPTY;
			
			return expr.clone();
		} catch(StackOverflowError e) {
			e.printStackTrace();
			fatalSyntaxError(CompilerError.EXPRESSION_NESTED_TOO_DEEP);
		}
		
		// If the expression was nested too deep we still want to return someting.
		return Expression.EMPTY;
	}
	
	private VariableStat nextFuncArgument() {
		HighType highType = getTypeFromSymbol();
		tryMatchOrSyntaxError(Type.IDENTIFIER, CompilerError.INVALID_FUNCTION_PARAMETER_NAME, reader);
		if(currentFunction.hasIdentifier(reader.value())) syntaxError(CompilerError.REDECLARATION_OF_FUNCTION_PARAMETER, reader);
		String varName = reader.value();
		reader.advance();
		
		return new VariableStat(highType, varName);
	}
	
	private Modifier nextFuncModifier() {
		String value = reader.value();
		reader.advance();
		return Modifiers.get(value);
	}
	
	private boolean acceptModification(Expression expr) {
		if(expr instanceof AtomExpr atom) {
			if(!atom.isIdentifier()) return false;
			Identifier ident = atom.identifier();
			
			switch(ident.getIdType()) {
				case clazz, funct -> {
					return false;
				}
			}
			
			return !ident.isGenerated();
		}
		
		if(expr instanceof OpExpr e) {
			if(e.type() == comma) {
				return acceptModification(e.last());
			}
			
			return e.type() == decptr
				|| e.type() == incptr;
		}
		
		return false;
	}
	
	private HighType getTypeFromSymbol() {
		if(!isType(reader)) {
			syntaxError(CompilerError.INVALID_TYPE, reader);
			return HighType.INVALID;
		}
		
		HighType type = currentProgram.getDefinedType(reader.value());
		reader.advance();
		
		if(reader.type() == Type.MUL) {
			LowType low = type.type();
			
			int size = 0;
			while(reader.type() == Type.MUL) {
				reader.advance();
				size++;
			}
			
			type = new HighType(type.name(), LowType.create(low.type(), size));
		}

		return type;
	}
	
	private boolean isType(LangContext reader) {
		return currentProgram.hasDefinedType(reader.value());
	}
	
	private boolean isValidName(LangContext reader) {
		return reader.type() == Type.IDENTIFIER
			&& !currentProgram.hasDefinedType(reader.value())
			&& !currentProgram.hasDefinedGlobal(reader.value());
	}
	
	private void addSyntaxError(CompilerError error, int offset, int count, Object... args) {
		currentProgram.addSyntaxMarker(new CompilerMarker(
			sourceFile,
			reader,
			offset,
			count,
			ISyntaxMarker.ERROR,
			_caller(),
			error.getMessage().formatted(args),
			error
		));
	}
	
	private void addSyntaxWarning(CompilerError error, int offset, int count, Object... args) {
		currentProgram.addSyntaxMarker(new CompilerMarker(
			sourceFile,
			reader,
			offset,
			count,
			ISyntaxMarker.WARNING,
			_caller(),
			error.getMessage().formatted(args),
			error
		));
	}
	
	private void syntaxError(CompilerError error, Object... args) {
		String compilerMessage = _caller();
		String message = String.format(error.getMessage(), args);
		
		currentProgram.addSyntaxMarker(new CompilerMarker(
			sourceFile,
			reader,
			ISyntaxMarker.ERROR,
			compilerMessage,
			message,
			error
		));
	}
	
	private void tryMatchOrSyntaxError(Type type, CompilerError error, Object... args) {
		if(reader.type() == type) return;
		
		String compilerMessage = _caller();
		String message = String.format(error.getMessage(), args);
		
		currentProgram.addSyntaxMarker(new CompilerMarker(
			sourceFile,
			reader,
			ISyntaxMarker.ERROR,
			compilerMessage,
			message,
			error
		));
	}
	
	private void tryMatchOrSyntaxError(Type type, int offset, int count, CompilerError error, Object... args) {
		if(reader.type() == type) return;
		
		String compilerMessage = _caller();
		String message = String.format(error.getMessage(), args);
		currentProgram.addSyntaxMarker(new CompilerMarker(
			sourceFile,
			reader,
			offset,
			count,
			ISyntaxMarker.ERROR,
			compilerMessage,
			message,
			error
		));
	}
	
	private void fatalSyntaxError(CompilerError error, Object... args) {
		String compilerMessage = _caller();
		String message = String.format(error.getMessage(), args);
		currentProgram.addSyntaxMarker(new CompilerMarker(
			sourceFile,
			reader,
			ISyntaxMarker.ERROR,
			compilerMessage,
			message,
			error
		));
		
		// Break the execution
		throw new CompilerException(message.toString());
	}
	
	private String _caller() {
		if(DebugUtils.isDeveloper()) {
			StackTraceElement element = Thread.currentThread().getStackTrace()[3];
			return "(%s:%d) ".formatted(element.getFileName(), element.getLineNumber());
		}
		
		return "";
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
				generator.currentProgram.addDefinedType(t.name(), t);
			}
			
			generator.currentProgram = program;
			generator.importFile(new File("null"), bytes);
		} catch(Throwable t) {
			program.addSyntaxMarker(new CompilerMarker(t));
			t.printStackTrace();
		}
		
		return program;
	}
}