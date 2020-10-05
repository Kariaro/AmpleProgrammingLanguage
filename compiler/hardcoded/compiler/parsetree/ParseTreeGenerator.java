package hardcoded.compiler.parsetree;

import static hardcoded.compiler.constants.ExprType.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hardcoded.compiler.Block;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Identifier;
import hardcoded.compiler.Program;
import hardcoded.compiler.constants.*;
import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.context.Lang;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.expression.OpExpr;
import hardcoded.compiler.statement.*;
import hardcoded.compiler.types.PointerType;
import hardcoded.compiler.types.PrimitiveType;
import hardcoded.compiler.types.Type;
import hardcoded.errors.CompilerError;
import hardcoded.lexer.Tokenizer;
import hardcoded.lexer.TokenizerFactory;
import hardcoded.lexer.TokenizerOld;
import hardcoded.utils.FileUtils;
import hardcoded.utils.StringUtils;

// NOTE: Try unrolling parsetree recursion.
public class ParseTreeGenerator {
	private static final Tokenizer LEXER;
	
	static {
		Tokenizer lexer = null;
		
		try {
			lexer = TokenizerFactory.loadFromFile(new File("res/project/lexer.lex"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		LEXER = lexer.getImmutableTokenizer();
	}
	
	
	private Map<String, Function> FUNCTIONS = new LinkedHashMap<>();
	private Map<String, Expression> GLOBAL = new LinkedHashMap<>();
	private Map<String, Type> defined_types = new HashMap<>();
	private Set<String> importedFiles = new HashSet<>();
	
	private Function current_function;
	private Program current_program;
	private boolean hasErrors;
	
	private File source_path;
	private Lang reader;
	
	public Program init(File source_path, String filename) {
		reset();
		
		this.current_program = new Program();
		this.source_path = source_path;
		importFile(filename);
		
		return this.current_program;
	}
	
	public boolean hasErrors() {
		return hasErrors;
	}
	
	public void reset() {
		hasErrors = false;
		current_program = null;
		source_path = null;
		reader = null;
		
		importedFiles.clear();
		defined_types.clear();
		GLOBAL.clear();
		FUNCTIONS.clear();
		
		for(Type t : Primitives.getAllTypes()) {
			defined_types.put(t.name(), t);
		}
	}
	
	private void importFile(String filename) {
		if(importedFiles.contains(filename)) return;
		importedFiles.add(filename);
		
		Lang last = this.reader;
		Lang next = null;
		
		try {
			next = new Lang(TokenizerOld.generateTokenChain(LEXER, FileUtils.readFileBytes(new File(source_path, filename))));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.reader = next;
		
		System.out.printf("Tokens: '%s'\n", next.token().toString(" ", Integer.MAX_VALUE));
		
		try {
			nextProgram();
		} catch(Exception e) {
			e.printStackTrace();
			syntaxError("");
		}
		
		this.reader = last;
	}
	
	public Program nextProgram() {
		do {
			Block block = nextBlock();
			if(block == null) break;
			
			if(!current_program.contains(block)) {
				current_program.add(block);
			}
		} while(true);
		
		return current_program;
	}
	
	public Block nextBlock() {
		if(reader.valueEqualsAdvance("@")) {
			parseCompiler();
			return Block.EMPTY;
		} else {
			return makeFunction();
		}
	}
	
	private void parseCompiler() {
		switch(reader.value()) {
			case "type": {
				reader.next();
				
				String name = reader.value();
				if(defined_types.containsKey(name)) syntaxError("Invalid type name. That type name is already defined '%s'", name);
				if(!isValidName(reader)) syntaxError("Invalid type name. The value '%s' is not a valid type name.", name);
				
				reader.next();
				Type type = getTypeFromSymbol();
				if(!reader.valueEqualsAdvance(";")) syntaxError("Invalid type syntax. Expected a semicolon but got '%s'", reader);
				
				if(defined_types.containsKey(name)) syntaxError("Type is already defined '%s'", name);
				defined_types.put(name, new Type(name, type.atomType(), type.size(), type.isSigned()));
				
				// System.out.println("#TYPE [" + name + "] as [" + type + "]");
				break;
			}
			case "import": {
				if(!reader.next().groupEquals("STRING")) syntaxError("Invalid import syntax. Expected a string but got '%s'", reader);
				String filename = reader.value().substring(1, reader.value().length() - 1);
				if(!reader.next().valueEqualsAdvance(";")) syntaxError("Invalid import syntax. Expected a semicolon but got '%s'", reader);
				
				// System.out.println("#IMPORT [" + filename + "]");
				importFile(filename);
				break;
			}
			case "set": {
				if(!isValidName(reader.next())) syntaxError("Invalid type name. The value '%s' is not a valid type name.", reader);
				String name = reader.value();
				reader.next();
				Expression expr = nextExpression();
				if(!reader.valueEqualsAdvance(";")) syntaxError("Invalid set syntax. Expected a semicolon but got '%s'", reader);
				// System.out.println("#SET [" + name + "] as [" + expr + "]");
				
				// TODO: Check that the expression is a compiler value that does not use variables.
				GLOBAL.put(name, expr);
				break;
			}
			case "unset": {
				String name = reader.next().value();
				if(!reader.next().valueEqualsAdvance(";")) syntaxError("Did you forget a semicolon here?");
				
				// System.out.println("#UNSET [" + name + "]");
				if(defined_types.containsKey(name)) {
					if(defined_types.get(name) instanceof PrimitiveType) syntaxError("Invalid unset syntax. You cannot unset the primitive type '%s'", name);
					defined_types.remove(name);
				} else if(GLOBAL.containsKey(name)) {
					GLOBAL.remove(name);
				} else {
					// Trying to remove something that does not exist...
				}
				
				break;
			}
		}
	}
	
	private Function makeFunction() {
		if(reader.remaining() < 1) return null;
		
		Function func = new Function();
		if(Modifiers.contains(reader.value())) func.modifier = nextFuncModifier();
		if(!isType(reader)) syntaxError(CompilerError.INVALID_TYPE, reader);
		func.returnType = getTypeFromSymbol();
		
		if(!isValidName(reader)) syntaxError(CompilerError.INVALID_FUNCTION_NAME, reader);
		
		boolean needsBody = false;
		if(FUNCTIONS.containsKey(reader.value())) {
			Function def = FUNCTIONS.get(reader.value());
			
			// Check that the arguments are different.. (Later)
			if(!def.isPlaceholder()) syntaxError("Redefinition of a function named '%s'", reader);
			
			// Modifiers
			if(!def.returnType.equals(func.returnType)) syntaxError("The return type of the defined function is of the wrong type. Expected '%s'", def.returnType);
			if(def.modifier != func.modifier) syntaxError("Function modifiers are different '%s', expected '%s'", func.modifier, def.modifier);
			// def must be a placeholder and this function here cannot be 
			// TODO: Arguments must be same
			
			func = def;
			func.arguments.clear();
			
			needsBody = true;
		} else {
			func.name = reader.value();
			FUNCTIONS.put(func.name, func);
		}
		
		current_function = func;
		
		if(!reader.next().valueEqualsAdvance("(")) syntaxError(CompilerError.INVALID_XXX_DEFINITION_EXPECTED_OPEN_PARENTHESIS, "function", reader);
		
		while(!reader.valueEquals(")")) {
			Variable arg = nextFuncArgument();
			func.arguments.add(Identifier.createParamIdent(arg.name, func.arguments.size(), arg.type.atomType()));
			
			if(!reader.valueEquals(",")) {
				if(reader.valueEquals(")")) break;
				syntaxError(CompilerError.MISSING_FUNCTION_PARAMETER_SEPARATOR, reader);
			} else reader.next();
		}
		
		if(!current_program.hasFunction(func.name)) {
			current_program.add(func);
		}
		
		if(!reader.valueEqualsAdvance(")")) syntaxError("Invalid closing of the funtion arguments. Expected a closing bracket ')'.");
		if(reader.valueEqualsAdvance(";")) {
			if(needsBody) syntaxError("Invalid function declaration. Expected a body.");
			return func;
		} else if(!reader.valueEquals("{")) {
			syntaxError("Invalid function body. Expected a open bracket '{'.");
		}
		
		current_function.inc_scope();
		func.body = getStatements();
		current_function.dec_scope();
		return func;
	}
	
	private Statement nextStatement() {
		reader.mark();
		
		if(isType(reader)) {
			Statement stat = getVariableDefinition();
			
			if(stat instanceof StatementList) {
				StatementList list = (StatementList)stat;
				for(int i = 0; i < list.list.size(); i++) {
					Variable var = (Variable)list.list.get(i);
					Identifier ident = current_function.add(var);
					
					if(!var.isInitialized()) {
						list.list.remove(i--);
					} else {
						list.list.set(i, new ExprStat(new OpExpr(ExprType.set, new AtomExpr(ident), var.value())));
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
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_SEMICOLON, "break", reader);
			reader.nextClear();
			return new ExprStat(new OpExpr(leave));
		} else if(reader.valueEqualsAdvance("continue")) {
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_SEMICOLON, "continue", reader);
			reader.nextClear();
			return new ExprStat(new OpExpr(loop));
		} else if(reader.valueEqualsAdvance("return")) {
			OpExpr expr = new OpExpr(ret);
			if(!reader.valueEquals(";")) expr.add(nextExpression());
			if(!reader.valueEquals(";")) syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_SEMICOLON, "return", reader);
			reader.nextClear();
			return new ExprStat(expr);
		}
		
		
		if(reader.valueEquals("{")) {
			return getStatements();
		} else {
			Expression expr = nextExpression();
			if(reader.valueEquals(";")) {
				reader.nextClear();
				return new ExprStat(expr);
			}
			
			syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_SEMICOLON, "expr", reader);
			reader.nextClear();
		}
		
		return null;
	}
	
	private Statement makeWhileStatement() {
		reader.next();
		if(!reader.valueEqualsAdvance("(")) syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_OPEN_PARENTHESIS, "while", reader);
		Expression condition = nextExpression();
		
		if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		Statement body = nextStatement();
		
		reader.resetMarked();
		return new WhileStat(condition, body);
	}
	
	private Statement makeForStatement() {
		reader.next();
		if(!reader.valueEquals("(")) syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_OPEN_PARENTHESIS, "for", reader);
		ForStat stat = new ForStat(); {
			reader.next();
			if(!reader.valueEquals(";")) { stat.setVariables(getVariableDefinition()); reader.prev(); }
			if(!reader.valueEquals(";")) syntaxError("Invalid for statement (variables). Expected semicolon but got '%s'", reader);
			reader.next();
			if(!reader.valueEquals(";")) stat.setCondition(nextExpression());
			if(!reader.valueEquals(";")) syntaxError("Invalid for statement (condition). Expected semicolon but got '%s'", reader);
			reader.next();
			if(!reader.valueEquals(")")) stat.setAction(nextExpression());
			if(!reader.valueEquals(")")) syntaxError(CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		}
		reader.next();
		stat.setBody(getStatements());
		reader.resetMarked();
		return stat;
	}
	
	private Statement makeIfStatement() {
		reader.next();
		if(!reader.valueEqualsAdvance("(")) syntaxError(CompilerError.INVALID_XXX_STATEMENT_EXPECTED_OPEN_PARENTHESIS, "if", reader);
		Expression condition = nextExpression();
		
		if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_STATEMENT_PARENTHESES, reader);
		Statement body = nextStatement();
		Statement elseBody = null;
		
		if(reader.valueEqualsAdvance("else")) {
			elseBody = nextStatement();
		}
		
		reader.resetMarked();
		return new IfStat(condition, body, elseBody);
	}
	
	private Statement getVariableDefinition() {
		reader.mark();
		Type type = getTypeFromSymbol();
		
		List<Variable> list = new ArrayList<Variable>();
		do {
			Variable var = new Variable(type);
			list.add(var);
			
			if(!isValidName(reader)) syntaxError(CompilerError.INVALID_VARIABLE_NAME, reader);
			if(current_function.hasIdentifier(reader.value())) syntaxError("Redeclaration of a local variable '%s'", reader);
			var.name = reader.value();
			reader.next();
			
			if(reader.valueEqualsAdvance("[")) {
				Expression expr = nextExpression();
				if(!(expr instanceof AtomExpr)) {
					syntaxError("Invalid array variable definition. Expected a integer expression but got '%s'", expr);
				} else {
					AtomExpr number = (AtomExpr)expr;
					if(!number.isNumber()) {
						syntaxError("Invalid array variable definition. Expected a integer expression. But got '%s'", expr);
					}
					
					// TODO: What should we do if the array has a negative length?
					
					var.list.add(Expression.EMPTY);
					var.arraySize = (int)number.i_value;
				}
				var.isArray = true;
				
				{
					int pointerLength = 1;
					if(var.type instanceof PointerType) {
						pointerLength = ((PointerType)var.type).pointerLength + 1;
					}
					
					var.type = new PointerType(type, pointerLength);
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
				var.setValue(nextExpression(true));
				
				if(reader.valueEquals(";")) {
					reader.nextClear();
					break;
				}
			}
			
			if(!reader.valueEqualsAdvance(",")) {
				syntaxError("Invalid variable definition. Expected a comma or semicolon but got '%s'", reader);
			}
		} while(true);

		// System.out.println(reader + " -> ?" + list);
		return new StatementList(list);
	}
	
	private Statement getStatements() {
		reader.mark();
		if(reader.valueEquals(";")) {
			reader.nextClear();
			return Statement.EMPTY;
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
			if(s == Statement.EMPTY || s == null) continue;
			
			if(s.hasStatements() && s.getStatements().size() == 0) continue;
			if(s instanceof StatementList) {
				stat.list.addAll(((StatementList)s).list);
				continue;
			}
			
			stat.list.add(s);
		}
		
		if(!reader.valueEquals("}")) syntaxError(CompilerError.UNCLOSED_CURLY_BRACKETS_STATEMENT, reader);
		reader.nextClear();
		
		if(stat.list.isEmpty()) return Statement.EMPTY;
		return stat;
	}
	
	private Expression nextExpression() {
		return nextExpression(false);
	}
	
	private Expression nextExpression(boolean skipComma) {
		reader.mark();
		
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
			
			Expression parse() {
				Expression expr = skipComma ? e14():e15();
				// System.out.println("Expr -> " + expr);
				
				return expr;
			}
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
						AtomExpr temp = new AtomExpr(current_function.temp(rhs.calculateSize()));
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
					if(!reader.valueEqualsAdvance(":")) syntaxError("Invalid ternary operation a ? b : c. Missing the colon. '%s'", reader);
					Expression c = e12();
					
					
					AtomExpr temp = new AtomExpr(current_function.temp(AtomType.largest(b.calculateSize(), c.calculateSize())));
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
							AtomExpr ident = new AtomExpr(current_function.temp(rhs.calculateSize()));
							
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
						
						AtomExpr ident2 = new AtomExpr(current_function.temp(lhs.calculateSize()));
						// System.out.println("Comma expr -> " + ident2 + ", [" + lhs + "]");
						if(!lhs.isPure()) {
							AtomExpr ident1 = new AtomExpr(current_function.temp(lhs.calculateSize()));
							
							// ( ... ) ++
							
							// For this to be not pure it must be a pointer.
							
							return new OpExpr(comma,
								// Calculate inside of lhs...
								new OpExpr(set, ident2, new OpExpr(addptr, lhs)),
								new OpExpr(set, ident1, new OpExpr(decptr, ident2)),
								new OpExpr(set, new OpExpr(decptr, ident2), new OpExpr(dir, new OpExpr(decptr, ident2), new AtomExpr(1))),
								new OpExpr(set, lhs, new OpExpr(dir, lhs, new AtomExpr(1))),
								ident1
							);
							
//							return new OpExpr(comma,
//								new OpExpr(set, ident2, new OpExpr(addptr, lhs)),
//								new OpExpr(set, ident1, new OpExpr(decptr, ident2)),
//								new OpExpr(set, new OpExpr(decptr, ident2), new OpExpr(dir, new OpExpr(decptr, ident2), new AtomExpr(1))),
//								new OpExpr(set, lhs, new OpExpr(dir, lhs, new AtomExpr(1))),
//								ident1
//							);
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
				Expression e1 = e2();
				
				for(;;) {
					switch(reader.value()) {
						case "[": {
							reader.next();
							Expression expr = e15();
							
							if(!reader.valueEqualsAdvance("]")) syntaxError(CompilerError.UNCLOSED_ARRAY_EXPRESSION, reader);
							
							Expression result = e1;
							if(result.type() == comma) e1 = e1.last();
							
							AtomType type = e1.calculateSize();
							expr = new OpExpr(mul, expr, new AtomExpr(type.size()));
							e1 = new OpExpr(decptr, new OpExpr(add, e1, expr));
							
							if(result.type() == comma) {
								result.set(result.length() - 1, e1);
								e1 = result;
							}
							
							continue;
						}
						
						// NOTE: Implement class structures later
						// case ".": e1 = new BiExpr("MEMBER", e1, e1(symbol.next())); continue;
						// case "->": e1 = new BiExpr("PMEMBER", e1, e1(symbol.next())); continue;
						// case "::": e1 = new BiExpr("NMEMBER", e1, e1(symbol.next())); continue;
						
						case "(": {
							if(!(e1 instanceof AtomExpr)) {
								// Function pointer ?
								syntaxError("Invalid call expression! %s", e1);
								break;
							}
							OpExpr o = new OpExpr(call);
							o.list.add(e1);
							reader.next();
							
							AtomExpr ae = (AtomExpr)e1;
							Function func = FUNCTIONS.get(ae.d_value.name());
							
							boolean closed = false;
							
							int length = func.arguments.size();
							for(int i = 0; i < length; i++) {
								Expression arg = e14();
								if(arg == null) syntaxError("Invalid call argument value.");
								o.list.add(arg);
								
								if(reader.valueEqualsAdvance(",")) {
									if(i == length - 1) syntaxError("Too many arguments calling function '%s' expected %s", func.name, length + (length == 1 ? " argument":"arguments"));
									continue;
								}
								
								if(i != length - 1) syntaxError("Not enough arguments to call function '%s' expected %s but got %d", func.name, length + (length == 1 ? " argument":"arguments"), i + 1);
								
								if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_CALL_PARENTHESES, reader);
								closed = true;
								break;
							}
							
							if(!closed) {
								if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_CALL_PARENTHESES, reader);
							}
							
							e1 = o;
							continue;
						}
					}
					break;
				}
				
				return e1;
			}
			
			Expression e2() {
				String value = reader.value();
				
				switch(value) {
					case "&": { reader.next(); return new OpExpr(addptr, e1()); }
					case "*": { reader.next(); return new OpExpr(decptr, e1()); }
					case "+": { reader.next(); return e1(); }
					
					case "!": { reader.next(); return new OpExpr(not, e1()); }
					case "~": { reader.next(); return new OpExpr(nor, e1()); }
					case "-": { reader.next(); return new OpExpr(neg, e1()); }
					
					case "(": {
						if(isType(reader.next())) {
							Type type = getTypeFromSymbol();
							if(type instanceof PrimitiveType) {
								if(type.atomType() == null) syntaxError(CompilerError.INVALID_CAST_TYPE, type.atomType());
							}
							
							// TODO: Change the type of the value to a 'i64' if the value is a class object.
							if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_CAST_PARENTHESES, reader);
							Expression rhs = e2();
							
							if(rhs instanceof AtomExpr) {
								AtomExpr a = (AtomExpr)rhs;
								
								if(a.isNumber()) {
									return a.convert(type.atomType());
								}
							}
							
							OpExpr expr = new OpExpr(cast, rhs);
							expr.override_size = type.atomType();
							return expr;
						} else reader.prev();
					}
				}
				
				return e1();
			}
			
			Expression e1() {
				if(reader.groupEquals("DOUBLE") || reader.groupEquals("FLOAT")) syntaxError("Float data types are not implemented in this language.");
				if(reader.groupEquals("LONG")) { String value = reader.value(); reader.next(); return new AtomExpr(parseLong(value)); }
				if(reader.groupEquals("INT")) { String value = reader.value(); reader.next(); return new AtomExpr(parseInteger(value)); }
				if(reader.groupEquals("STRING")) { String value = reader.value(); reader.next(); return new AtomExpr(value.substring(1, value.length() - 1)); } // TODO: Unicode ?
				
				if(reader.groupEquals("CHAR")) { // TODO: Unicode ?
					String value = StringUtils.unescapeString(reader.value().substring(1, reader.value().length() - 1));
					if(value.length() != 1) syntaxError(CompilerError.INVALID_CHAR_LITERAL_SIZE);
					reader.next();
					
					return new AtomExpr((byte)(value.charAt(0) & 0xff));
				}
				
				if(reader.groupEquals("IDENTIFIER")) {
					String value = reader.value();
					if(!current_function.hasIdentifier(value)) {
						if(GLOBAL.containsKey(value)) {
							reader.next();
							return GLOBAL.get(value);
						}
						if(FUNCTIONS.containsKey(value)) {
							reader.next();
							return new AtomExpr(current_program.getFunction(value));
						}
						
						syntaxError("Undeclared variable name. Could not find the variable '%s'", value);
					} else {
						reader.next();
						return new AtomExpr(current_function.getIdentifier(value));
					}
					
					reader.next();
					return null;
				}
				
				if(reader.valueEqualsAdvance("(")) {
					Expression expr = e15();
					if(!reader.valueEqualsAdvance(")")) syntaxError(CompilerError.UNCLOSED_EXPRESSION_PARENTHESES, reader);
					return expr;
				}
				
				return null;
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
		return expr.clone();
	}
	
	public Variable nextFuncArgument() {
		Variable variable = new Variable(getTypeFromSymbol());
		if(!reader.groupEquals("IDENTIFIER")) syntaxError("Invalid function argument name '%s'", reader);
		if(current_function.hasIdentifier(reader.value())) syntaxError("Redefinition of a variable named '%s'", reader);
		variable.name = reader.value();
		reader.next();
		return variable;
	}
	
	public Modifier nextFuncModifier() {
		String value = reader.toString();
		reader.next();
		return Modifiers.get(value);
	}
	
	
	private boolean acceptModification(Expression expr) {
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			if(!e.isIdentifier()) return false;
			Identifier ident = e.d_value;
			
			return !ident.temp();
		}
		
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			if(e.type == comma) {
				return acceptModification(e.last());
			}
			
			return e.type == decptr
				|| e.type == addptr;
		}
		
		return false;
	}
	
	private boolean isType(Lang reader) {
		return defined_types.containsKey(reader.value());
	}
	
	public Type getTypeFromSymbol() {
		if(!isType(reader)) syntaxError(CompilerError.INVALID_TYPE, reader);
		Type type = defined_types.get(reader.value());
		reader.next();
		
		if(reader.valueEquals("*")) {
			int size = 0;
			
			while(reader.valueEqualsAdvance("*")) {
				size++;
			}
			
			type = new PointerType(type, size);
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
	
	
	
	public void syntaxError(CompilerError error, Object... args) {
		StringBuilder message = new StringBuilder();
		
		message.append(_caller()).append("(line:").append(reader.line()).append(", col:").append(reader.column()).append(") ")
			.append(String.format(error.getMessage(), args));
		
		System.err.println(message);
		hasErrors = true;
	}
	
	public void syntaxError(String format, Object... args) {
		StringBuilder message = new StringBuilder();
		
		message.append(_caller()).append("(line:").append(reader.line()).append(", col:").append(reader.column()).append(") ")
			.append(String.format(format, args));
		
		System.err.println(message);
		hasErrors = true;
	}
	
	private String _caller() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[3];
		return "(" + element.getFileName() + ":" + element.getLineNumber() + ") ";
	}
}
