package hardcoded.compiler;

import java.io.File;
import java.util.*;

import hardcoded.compiler.Expression.*;
import hardcoded.compiler.Statement.*;
import hardcoded.lexer.Tokenizer;
import hardcoded.lexer.TokenizerFactory;
import hardcoded.lexer.TokenizerOld;
import hardcoded.utils.FileUtils;
import hardcoded.utils.StringUtils;
import hardcoded.visualization.HC2Visualization;

public class HCompiler2 {
	private static final Set<String> PRIMITIVES;
	private static final Set<String> MODIFIERS;
	private static final Set<String> KEYWORDS;
	
	static {
		Set<String> primitives = new HashSet<String>();
		primitives.addAll(Arrays.asList("void", "byte", "char", "bool", "int", "short", "long", "float", "double"));
		PRIMITIVES = Collections.unmodifiableSet(primitives);
		
		Set<String> modifiers = new HashSet<String>();
		modifiers.addAll(Arrays.asList("export"));
		MODIFIERS = Collections.unmodifiableSet(modifiers);
		

		Set<String> keywords = new HashSet<String>();
		keywords.addAll(Arrays.asList("if", "for", "while", "asm", "return", "break", "continue", "as", "true", "false", "newtype"));
		KEYWORDS = Collections.unmodifiableSet(keywords);
	}
	
	private static final boolean isModifier(Sym symbol) {
		return MODIFIERS.contains(symbol.value());
	}
	
	private static final boolean isPrimitive(Sym symbol) {
		return PRIMITIVES.contains(symbol.value());
	}
	
	private static final boolean isKeyword(Sym symbol) {
		return KEYWORDS.contains(symbol.value());
	}
	
	public static final void main(String[] args) {
		//int val = 204 ^ 229 * 119 ^ 75 ^ (5) - 163 + 69 + 10 * 30 * 191 | 145 * 139 + 183 * 165 * 248 * 6 & 183 * 243 ^ 11;
		//System.out.println("val -> " + val);
		
		new HCompiler2();
	}
	
	
	private void addPrimitive(String name, int size, boolean floating) {
		TYPES.put(name, new PrimitiveType(name, size, floating));
	}
	
	private void addPrimitive(String name, int size) {
		TYPES.put(name, new PrimitiveType(name, size));
	}
	
	private Map<String, Type> TYPES;
	private Map<String, Expression> GLOBAL;
	private Map<String, Function> FUNCTIONS;
	public HCompiler2() {
		FUNCTIONS = new HashMap<>();
		GLOBAL = new HashMap<>();
		TYPES = new HashMap<>();
		
		addPrimitive("void", 0);
		addPrimitive("long", 8);
		addPrimitive("int", 4);
		addPrimitive("short", 2);
		addPrimitive("byte", 1);
		
		addPrimitive("char", 1);
		addPrimitive("bool", 1);
		
		addPrimitive("double", 8, true);
		addPrimitive("float", 4, true);
		
		try {
			lexer = TokenizerFactory.loadFromFile(new File("res/project/lexer.lex"));
			
			build();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private File projectPath = new File("res/project/src/");
	public void build() throws Exception {
		importFile("main.hc");
	}
	
	private Set<String> importedFiles = new HashSet<>();
	private void importFile(String name) throws Exception {
		if(importedFiles.contains(name)) return; // Ignore
		importedFiles.add(name);
		
		Sym symbol = new Sym(TokenizerOld.generateTokenChain(lexer, FileUtils.readFileBytes(new File(projectPath, name))));
		System.out.println("Tokens: '" + symbol.token().toString(" ", Integer.MAX_VALUE) + "'");
		
		Program program = null;
		try {
			program = parseProgram(symbol);
		} catch(Exception e) {
			e.printStackTrace();
			throwError(symbol, "");
		}
		
		System.out.println();
		System.out.println("Program: '" + program + "'");
		for(Function func : program.functions) {
			System.out.println("  Function: '" + func + "'");
		}
		
		HC2Visualization hc2 = new HC2Visualization();
		hc2.show(program);
	}
	
	private Function curFunction;
	private Tokenizer lexer;
	private void parseCompiler(Sym symbol) {
		if(symbol.valueEquals("type")) {
			symbol.next();
			
			String name = symbol.value();
			if(TYPES.containsKey(name)) throwError(symbol, "Invalid type name. That type name is already defined '" + name + "'");
			if(!isValidName(symbol)) throwError(symbol, "Invalid type name. The value '" + name + "' is not a valid type name.");
			
			// Define a new type
			Type type = getTypeFromSymbol(symbol.next());
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid type syntax. Expected a semicolon but got '" + symbol + "'");
			symbol.next();
			
			if(TYPES.containsKey(name)) throwError(symbol, "Type is already defined '" + name + "'");
			
			TYPES.put(name, new Type(name, type.size(), type.isFloating()));
			System.out.println("#TYPE [" + name + "] as [" + type + "]");
		} else if(symbol.valueEquals("import")) {
			if(!symbol.next().groupEquals("STRING")) throwError(symbol, "Invalid import syntax. Expected a string but got '" + symbol + "'.");
			String path = symbol.value().substring(1, symbol.value().length() - 1);
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid import syntax. Expected a semicolon but got '" + symbol + "'");
			symbol.next();
			
			System.out.println("#IMPORT [" + path + "]");
			try {
				importFile(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(symbol.valueEquals("set")) {
			if(!isValidName(symbol.next())) throwError(symbol, "Invalid type name. The value '" + symbol + "' is not a valid type name.");
			String name = symbol.value();
			Expression expr = parseExpression(symbol.next());
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid set syntax. Expected a semicolon but got '" + symbol + "'");
			symbol.next();
			System.out.println("#SET [" + name + "] as [" + expr + "]");
			
			// TODO: Check that the expression is a compiler value that does not use variables.
			GLOBAL.put(name, expr);
		} else if(symbol.valueEquals("unset")) {
			String name = symbol.next().value();
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Did you forget a semicolon here?");
			symbol.next();
			
			System.out.println("#UNSET [" + name + "]");
			if(TYPES.containsKey(name)) {
				if(TYPES.get(name) instanceof PrimitiveType) throwError(symbol, "Invalid unset syntax. You cannot unset the primitive type '" + name + "'");
				TYPES.remove(name);
			}
			else if(GLOBAL.containsKey(name)) GLOBAL.remove(name);
			else; // Trying to remove something that does not exist...
		}
	}
	
	private Program parseProgram(Sym symbol) {
		Program program = new Program();
		
		Function func;
		
		while(true) {
			if(symbol.valueEquals("#")) {
				parseCompiler(symbol.next());
			} else {
				func = parseFunction(symbol);
				if(func == null) break;
				
				System.out.println("  Function: '" + func + "'");
				if(!func.isPlaceholder()) {
					program.functions.add(func);
				}
			}
		}
		
		return program;
	}
	
	private boolean isValidName(Sym symbol) {
		if(!symbol.groupEquals("IDENTIFIER")) return false;
		if(TYPES.containsKey(symbol.value())) return false;
		if(GLOBAL.containsKey(symbol.value())) return false; // Should only be for globals... 
		if(isModifier(symbol)) return false;
		if(isKeyword(symbol)) return false;
		if(isPrimitive(symbol)) return false;
		return true;
	}
	
	private Function parseFunction(Sym symbol) {
		if(symbol.remaining() < 1) return null;
		
		Function func = new Function();
		if(isModifier(symbol)) func.modifier = createNewModifier(symbol);
		if(!isType(symbol)) throwError(symbol, "Invalid function return type '" + symbol.value() + "'");
		
		func.returnType = getTypeFromSymbol(symbol);
		
		if(!isValidName(symbol)) throwError(symbol, "Invalid function name '" + symbol.value() + "'");
		
		boolean needsBody = false;
		if(FUNCTIONS.containsKey(symbol.value())) {
			Function def = FUNCTIONS.get(symbol.value());
			
			// Check that the arguments are different.. (Later)
			if(!def.isPlaceholder()) throwError(symbol, "Redefinition of a function named '" + symbol + "'");
			
			// Modifiers
			if(!def.returnType.equals(func.returnType)) throwError(symbol, "The return type of the defined function is of the wrong type. Expected '" + def.returnType + "'");
			if(def.modifier != func.modifier) throwError(symbol, "Function modifiers are different '" + func.modifier + "', Expected '" + def.modifier + "'");
			// def must be a placeholder and this function here cannot be 
			// Arguments must be same
			func = def;
			func.arguments.clear();
			
			needsBody = true;
		} else {
			func.name = symbol.value();
			FUNCTIONS.put(func.name, func);
		}
		curFunction = func;
		
		symbol.next();
		
		if(!symbol.valueEquals("(")) throwError(symbol, "Invalid function declaration. Did you forget a open bracket here ? '" + symbol + "'");
		else symbol.next();
		
		while(!symbol.valueEquals(")")) {
			Variable argument = createNewArgument2(symbol);
			func.arguments.add(argument);
			if(!symbol.valueEquals(",")) {
				if(symbol.valueEquals(")")) break;
				throwError(symbol, "Invalid function argument separator '" + symbol.value() + "' did you forget a comma ? ','");
			} else symbol.next();
		}
		
		if(!symbol.valueEquals(")")) {
			throwError(symbol, "Invalid closing of the funtion arguments. Expected a closing bracket ')'.");
		} else symbol.next();
		
		if(symbol.valueEquals(";")) {
			if(needsBody) throwError(symbol, "Invalid function declaration. Expected a body.");
			symbol.next();
			return func;
		} else if(!symbol.valueEquals("{")) {
			throwError(symbol, "Invalid function body. Expected a open bracket '{'.");
		}
		
		curFunction.inc_scope();
		func.body = getStatements(symbol);
		curFunction.dec_scope();
		return func;
	}
	
	private Statement createIfStatement(Sym symbol) {
		symbol.next();
		if(!symbol.valueEquals("(")) throwError(symbol, "Invalid if statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		symbol.next();
		IfStatement stat = new IfStatement();
		stat.condition = parseExpression(symbol);
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid if statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		stat.body = parseStatement(symbol.next());
		if(symbol.valueEquals("else")) stat.elseBody = parseStatement(symbol.next());
		
		if(stat.body instanceof EmptyStatement) {
			if(stat.elseBody == null) return stat.body;
			else if(stat.elseBody instanceof EmptyStatement) return stat.body;
			else {
				// Change the statement if (x) into 'if(!(x))'
			}
		} else {
			if(stat.elseBody instanceof EmptyStatement) stat.elseBody = null;
		}
		
		if(stat.condition.isPure()) {
			if(stat.condition instanceof NumberExpr) {
				NumberExpr expr = (NumberExpr)stat.condition;
				
				if(expr.value.doubleValue() != 0) return stat.body;
				if(stat.elseBody != null) return stat.elseBody;
				return new EmptyStatement();
			}
		}
		
		return stat;
	}
	
	private Statement createWhileStatement(Sym symbol) {
		symbol.next();
		if(!symbol.valueEquals("(")) throwError(symbol, "Invalid while statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		symbol.next();
		WhileStatement stat = new WhileStatement();
		stat.condition = parseExpression(symbol);
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid while statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		stat.body = parseStatement(symbol.next());
		
		if(stat.condition.isPure()) {
			if(stat.condition instanceof NumberExpr) {
				NumberExpr expr = (NumberExpr)stat.condition;
				
				if(expr.value.doubleValue() != 0) return stat;
				return new EmptyStatement();
			}
		}
		
		return stat;
	}
	
	private Statement createForStatement(Sym symbol) {
		symbol.next();
		if(!symbol.valueEquals("(")) throwError(symbol, "Invalid for statement. Expected open bracket '(' but got '" + symbol.value() + "'");
		symbol.next();
		ForStatement stat = new ForStatement(); {
			if(!symbol.valueEquals(";")) { stat.variables = getVariableDefinition(symbol); symbol.prev(); }
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid for statement (variables). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.valueEquals(";")) stat.condition = parseExpression(symbol);
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid for statement (condition). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.valueEquals(")")) stat.action = parseExpression(symbol);
			if(!symbol.valueEquals(")")) throwError(symbol, "Invalid for statement (action). Expected closing bracket ')' but got '" + symbol.value() + "'");
			symbol.next();
		}
		stat.body = getStatements(symbol);
		return stat;
	}
	
	private Statement parseStatement(Sym symbol) {
		symbol.mark();
		
		if(isType(symbol)) {
			Statement stat = getVariableDefinition(symbol);
			
			if(stat instanceof MultiVariableStatement) {
				MultiVariableStatement mvs = (MultiVariableStatement)stat;
				
				for(Variable var : mvs.define) {
					curFunction.getScope().add(var);
				}
				
//				StatementList list = new StatementList();
//				for(int i = 0; i < mvs.define.size(); i++) {
//					Variable var = mvs.define.get(i);
//					
//					if(var.initialized) {
//						TestExpr var_expr = new TestExpr(ExprType.mov, new StackExpr(var, curFunction.getVariableStackIndex(var)), var.value);
//						list.list.add(new ExprStatement(var_expr));
//						mvs.define.remove(i);
//						i--;
//					}
//				}
//				
//				if(!mvs.define.isEmpty()) {
//					list.list.add(mvs);
//				}
//				
//				if(list.list.size() == 1) return list.list.get(0);
//				symbol.resetMarked();
//				return list;
			}
			
			symbol.resetMarked();
			return stat;
		}
		
		if(symbol.valueEquals("if")) {
			Statement stat = createIfStatement(symbol);
			symbol.resetMarked();
			return stat;
		} else if(symbol.valueEquals("while")) {
			Statement stat = createWhileStatement(symbol);
			symbol.resetMarked();
			return stat;
		} else if(symbol.valueEquals("for")) {
			Statement stat = createForStatement(symbol);
			symbol.resetMarked();
			return stat;
		} else if(symbol.valueEquals("break")) {
			symbol.next();
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid break statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new BreakStatement();
		} else if(symbol.valueEquals("continue")) {
			symbol.next();
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid continue statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new ContinueStatement();
		} else if(symbol.valueEquals("return")) {
			symbol.next();
			TestExpr rexpr = new TestExpr(ExprType.ret);
			ExprStatement st_expr = new ExprStatement(rexpr);
			
			if(!symbol.valueEquals(";")) rexpr.add(parseExpression(symbol));
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid return statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return st_expr;
		} else if(symbol.valueEquals("{")) {
			return getStatements(symbol);
		} else {
			Expression expr = parseExpression(symbol);
			if(symbol.valueEquals(";")) {
				symbol.nextClear();
				return new ExprStatement(expr);
			}
			throwError(symbol, "Invalid expression statement. Expected semicolon but got '" + symbol.value() + "'");
		}
		
		return null;
	}
	
	private MultiVariableStatement getVariableDefinition(Sym symbol) {
		symbol.mark();
		MultiVariableStatement define = new MultiVariableStatement();
		define.type = getTypeFromSymbol(symbol);
		
		do {
			Variable stat = define.create();
			if(!isValidName(symbol)) throwError(symbol, "Invalid local variable name '" + symbol.value() + "'");
			if(curFunction.hasIdentifier(symbol.value())) throwError(symbol, "Redeclaration of a local variable '" + symbol.value() + "'");
			stat.name = symbol.value();
			symbol.next();
			
			if(symbol.valueEquals("[")) {
				Expression expr = parseExpression(symbol.next());
				if(!(expr instanceof NumberExpr)) {
					throwError(symbol, "Invalid array variable definition. Expected a integer expression but got '" + expr + "'");
				} else {
					NumberExpr number = (NumberExpr)expr;
					if(!number.isInteger()) throwError(symbol, "Invalid array variable definition. Expected a integer expression. '" + expr + "'");
					stat.arraySize = number.value.intValue();
				}
				stat.isArray = true;
				
				if(!symbol.valueEquals("]")) throwError(symbol, "Invalid array variable definition. Expected array closure ']' but got '" + symbol.value() + "'");
				symbol.next();
				if(!symbol.valueEquals(";")) throwError(symbol, "Invalid array variable definition. Expected a semicolon ';' but got '" + symbol.value() + "'");
				symbol.nextClear();
				return define;
			}
			
			if(symbol.valueEquals(";")) {
				stat.initialized = false;
				symbol.nextClear();
				return define;
			} else if(symbol.valueEquals("=")) {
				symbol.next();
				stat.value = parseExpression(symbol, true);
				
				if(symbol.valueEquals(";")) {
					stat.initialized = true;
					symbol.nextClear();
					return define;
				} else if(!symbol.valueEquals(",")) {
					throwError(symbol, "Invalid variable definition. Expected a comma or semicolon but got '" + symbol.value() + "'");
				}
				
				symbol.next();
				continue;
			}
		} while(true);
	}
	
	private Statement getStatements(Sym symbol) {
		symbol.mark();
		if(symbol.valueEquals(";")) {
			symbol.nextClear();
			return new EmptyStatement();
		}
		
		if(!symbol.valueEquals("{")) {
			Statement stat = parseStatement(symbol);
			symbol.resetMarked();
			return stat;
		}
		
		symbol.next();
		Statements stat = new Statements();
		
		for(;;) {
			if(symbol.valueEquals(";")) { symbol.next(); continue; }
			if(symbol.valueEquals("}")) break;
			
			Statement s = parseStatement(symbol);
			if(s instanceof EmptyStatement) continue;
			
			stat.list.add(s);
		}
		
		
		if(!symbol.valueEquals("}")) {
			throwError(symbol, "Invalid statement closing bracket. Expected '}' but got '" + symbol.value() + "'");
		}
		
		symbol.nextClear();
		
		if(stat.list.isEmpty()) return new EmptyStatement();
		return stat;
	}
	
	private Expression parseExpression(Sym symbol) {
		return parseExpression(symbol, false);
	}
	
	private Expression parseExpression(Sym symbol, boolean skipComma) {
		symbol.mark();
		
		Expression expr = new Object() {
			private String[] _s(String... array) { return array; }
			private ExprType[] _e(ExprType... array) { return array; }
			Expression e_test(Sym symbol, String[] values, ExprType[] exprs, java.util.function.Function<Sym, Expression> func) { return e_test(symbol, values, exprs, func, func); }
			Expression e_test(Sym symbol, String[] values, ExprType[] exprs, java.util.function.Function<Sym, Expression> entry, java.util.function.Function<Sym, Expression> func) {
				Expression expr = entry.apply(symbol);
				for(;;) {
					boolean found = false;
					for(int i = 0; i < values.length; i++) {
						String value = values[i];
						ExprType type = exprs[i];
						
						if(symbol.valueEquals(value)) {
							found = true;
							expr = new TestExpr(type, expr, func.apply(symbol.next()));
							break;
						}
					}
					
					if(!found) {
						return expr;
					}
				}
			}
			
			Expression parse(Sym symbol) { // expr: _exp15
				Expression expr = skipComma ? e14(symbol):e15(symbol);
				// String a = expr.toString();
				// expr = Expression.optimize(expr);
				// String b = expr.toString();
				// System.out.println("Parsed: '" + a + "' -> '" + b + "'");
				return expr;
			}
			
			// _exp15: _exp14 | _exp15 ',' _exp14
			Expression e15(Sym symbol) {
				Expression e14 = e14(symbol);
				if(symbol.valueEquals(",")) {
					symbol.next();
					return new BiExpr("COMMA OP", e14, e14(symbol));
				}
				return e14;
			}
			
			/*
			_exp14: _exp13
				  | _exp13 '=' _exp14
				  | _exp13 '+=' _exp14
				  | _exp13 '-=' _exp14
				  | _exp13 '*=' _exp14
				  | _exp13 '/=' _exp14
				  | _exp13 '%=' _exp14
				  | _exp13 '&=' _exp14
				  | _exp13 '|=' _exp14
				  | _exp13 '^=' _exp14
				  | _exp13 '>>=' _exp14
				  | _exp13 '<<=' _exp14
			 */
			Expression e14(Sym symbol) { // Left associative
				Expression e13 = e13(symbol);
				
				
				boolean validAssign = false;
				if(e13 instanceof IdentifierExpr) {
					validAssign = true;
				}
				
				if(e13 instanceof StackExpr) {
					validAssign = true;
				}
				
				if(e13 instanceof TestExpr) {
					TestExpr expr = (TestExpr)e13;
					validAssign = validAssign || (expr.type == ExprType.incptr || expr.type == ExprType.decptr);
				}
				
				// Valid objects are ptr objects or identifiers
				// TODO: Check that the left side can be defined.
				// If the next expression modifies the assigner then
				//   a temporary variable should get created that holds
				//   the value and adds it in the end.
				
				switch(symbol.value()) {
					case "=": {
						if(!validAssign) throwError(symbol, "Invalid placement of a assign expression.");
						return new TestExpr(ExprType.mov, e13, e14(symbol.next()));
					}
					case "+=": {
						if(!validAssign) throwError(symbol, "Invalid placement of a assign expression.");
						
						return new TestExpr(ExprType.mov, e13, new TestExpr(ExprType.add, e13, e14(symbol.next())));
					}
					case "-=":
					case "<<=":
					case ">>=":
					case "*=":
					case "/=": case "&=": case "^=": case "|=":
						if(!validAssign) throwError(symbol, "Invalid placement of a assign expression.");
						return new BiExpr(symbol.value(), e13, e14(symbol.next()));
					//default: return e13;
				}
				
				return e13;
			}
			
			// _exp13: _exp12 | _exp13 '?' _exp13 ':' _exp12
			Expression e13(Sym symbol) {
				Expression e13 = e12(symbol);
				
				if(symbol.valueEquals("?")) {
					symbol.next();
					
					Expression a = e12(symbol);
					if(!symbol.valueEquals(":")) throwError(symbol, "Invalid ternary operation a ? b : c. Missing the colon. '" + symbol + "'");
					else symbol.next();
					
					return new TeExpr(e13, "?", a, ":", e12(symbol));
				}
				
				return e13;
			}
			
			Expression e12(Sym symbol) { return e_test(symbol, _s("||"), _e(ExprType.cor), this::e11, this::e12); }		// _exp12: _exp11 | _exp12 '||' _exp11
			Expression e11(Sym symbol) { return e_test(symbol, _s("&&"), _e(ExprType.cand), this::e10, this::e11); }	// _exp11: _exp10 | _exp11 '&&' _exp10
			Expression e10(Sym symbol) { return e_test(symbol, _s("|"), _e(ExprType.or), this::e9); }	// _exp10: _exp9 | _exp10 '|' _exp9
			Expression e9(Sym symbol) { return e_test(symbol, _s("^"), _e(ExprType.xor), this::e8); }	// _exp9: _exp8 | _exp9 '^' _exp8
			Expression e8(Sym symbol) { return e_test(symbol, _s("&"), _e(ExprType.and), this::e7); }	// _exp8: _exp7 | _exp8 '&' _exp7
			
			// _exp7: _exp6 | _exp7 '==' _exp6 | _exp7 '!=' _exp6
			Expression e7(Sym symbol) {
				return e_test(symbol,
					_s("==", "!="),
					_e(ExprType.eq, ExprType.neq),
					this::e6,
					this::e7
				);
			}
			
			// _exp6: _exp5 | _exp6 '<' _exp5 | _exp6 '<=' _exp5 | _exp6 '>' _exp5 | _exp6 '>=' _exp5
			Expression e6(Sym symbol) {
				return e_test(symbol,
					_s("<", "<=", ">", ">="),
					_e(ExprType.lt, ExprType.lte, ExprType.gt, ExprType.gte),
					this::e5
				);
			}
			
			Expression e5(Sym symbol) { // _exp5: _exp4 | _exp5 '>>' _exp4 | _exp5 '<<' _exp4
				return e_test(symbol,
					_s("<<", ">>"),
					_e(ExprType.shl, ExprType.shr),
					this::e4
				);
			}
			
			Expression e4(Sym symbol) { // _exp4: _exp3 | _exp4 '+' _exp3 | _exp4 '-' _exp3
				return e_test(symbol,
					_s("+", "-"),
					_e(ExprType.add, ExprType.sub),
					this::e3
				);
			}
			
			Expression e3(Sym symbol) { // _exp3: _exp2 | _exp3 '*' _exp2 | _exp3 '/' _exp2 | _exp3 '%' _exp2
				return e_test(symbol,
					_s("*", "/", "%"),
					_e(ExprType.mul, ExprType.div, ExprType.mod),
					this::e2
				);
			}
			
			/*
			   _exp2: _exp1
					| '~' _exp1
					| '&' _exp1
					| '*' _exp1
					| '!' _exp1
					| '-' _exp1
					| '+' _exp1
					| '++' _exp1
					| '--' _exp1
					| _exp1 '++'
					| _exp1 '--'
					| '(' type ')' _exp2
			*/
			Expression e2(Sym symbol) {
				String value = symbol.value();
				
				switch(value) {
					case "&": return new TestExpr(ExprType.incptr, e1(symbol.next()));
					case "*": return new TestExpr(ExprType.decptr, e1(symbol.next()));
					
					case "+": return e1(symbol.next()); // Un needed
					
					case "!": return new TestExpr(ExprType.not, e1(symbol.next()));
					case "~": return new TestExpr(ExprType.nor, e1(symbol.next()));
					case "-": return new TestExpr(ExprType.neg, e1(symbol.next()));
					
					// ++ -- only on identifiers or pointers. Must be objects and not numbers!
					case "--": return new TestExpr(ExprType.sub, e1(symbol.next()), new NumberExpr(1));
					case "++": return new TestExpr(ExprType.add, e1(symbol.next()), new NumberExpr(1));
					
					case "(": {
						if(isType(symbol.next())) {
							Type type = getTypeFromSymbol(symbol);
							if(!symbol.valueEquals(")")) throwError(symbol, "Invalid cast expression. Expected closing bracket ')' but got '" + symbol + "'");
							return new CastExpr(type, e2(symbol.next()));
						} else symbol.prev();
					}
					default: {
						Expression e1 = e1(symbol);
						String v2 = symbol.value();
						
						switch(symbol.value()) {
							case "++": case "--": {
								symbol.next();
								return new UnExpr(e1, v2, true);
							}
						}
						
						for(;;) {
							switch(symbol.value()) {
								case "[": {
									Expression expr = e15(symbol.next());
									if(!symbol.valueEquals("]")) throwError(symbol, "Invalid array close character. Expected '[' but got '" + symbol.value() + "'");
									symbol.next();
									// e1 = new BiExpr(e1, "ARRAY", expr);
									e1 = new TestExpr(ExprType.decptr, new TestExpr(ExprType.add, e1, expr));
									continue;
								}
								
								// TODO: Later
								case ".": e1 = new BiExpr("MEMBER", e1, e1(symbol.next())); continue;
								case "->": e1 = new BiExpr("PMEMBER", e1, e1(symbol.next())); continue;
								case "::": e1 = new BiExpr("NMEMBER", e1, e1(symbol.next())); continue;
								case "(": {
									symbol.next();
									
									// System.out.println(e1.getClass() + ", " + e1);
									
									FunctionExpr func = (FunctionExpr)e1;
									
									TestExpr expr = new TestExpr(ExprType.call);
									expr.list.add(e1);
									// CallExpr expr = new CallExpr();
									// expr.pointer = e1;
									
									int length = func.func.arguments.size();
									for(int i = 0; i < length; i++) {
										Variable argument = func.func.arguments.get(i);
										
										Expression arg = e14(symbol);
										if(arg == null) throwError(symbol, "Invalid call argument value.");
										System.out.println(argument + " /// " + arg + " [" + arg.type() + "]");
										// expr.args.add(arg);
										expr.list.add(arg);
										
										if(symbol.valueEquals(",")) {
											if(i == length - 1) throwError(symbol, "Too many arguments calling function '" + func.func.name + "' Expected " + length + " argument" + (length == 1 ? "":"s") + ".");
											symbol.next();
											continue;
										}
										
										if(i != length - 1) throwError(symbol, "Not enough arguments to call function '" + func.func.name + "' expected " + length + " argument" + (length == 1 ? "":"s")+ " but got " + (i + 1) + "");
										
										if(!symbol.valueEquals(")")) throwError(symbol, "Invalid inline calling argument. Expected closing bracket ')' but got '" + symbol + "'");
										symbol.next();
										break;
										// TODO: Check that this function exists.
									}
									
									e1 = expr;
									continue;
								}
							}
							break;
						}
						
						return e1;
					}
				}
			}
			
			private int parseInteger(String value) {
				if(value.startsWith("0x")) return Integer.parseInt(value.substring(2), 16);
				else return Integer.parseInt(value);
			}
			
			private long parseLong(String value) {
				if(value.startsWith("0x")) return Long.parseLong(value.substring(2, value.length() - 1), 16);
				else return Long.parseLong(value);
			}
			
			/*
			_exp1: _exp0
				| _exp1 '[' expr ']'
			    | _exp1 '(' expr [',' expr] ')'
			_exp0: INTEGERLITERAL
				| STRINGLITERAL
				| IDENTIFIER
				| '(' expr ')'
			*/
			Expression e1(Sym symbol) {
				if(symbol.groupEquals("INT")) {
					String value = symbol.value(); symbol.next();
					return new NumberExpr(parseInteger(value));
				}
				
				if(symbol.groupEquals("LONG")) {
					String value = symbol.value(); symbol.next();
					return new NumberExpr(parseLong(value));
				}
				
				if(symbol.groupEquals("DOUBLE")) {
					String value = symbol.value(); symbol.next();
					return new NumberExpr(Double.parseDouble(value));
				}
				
				if(symbol.groupEquals("FLOAT")) {
					String value = symbol.value(); symbol.next();
					return new NumberExpr(Float.parseFloat(value));
				}
				
				if(symbol.groupEquals("STRING")) { // TODO: Unicode
					String value = symbol.value(); symbol.next();
					return new StringExpr(value);
				}
				
				if(symbol.groupEquals("CHAR")) { // TODO: Unicode
					String value = StringUtils.unescapeString(symbol.value().substring(1, symbol.value().length() - 1));
					if(value.length() != 1) throwError(symbol, "Invalid char expression. Expected only one character.");
					symbol.next();
					
					char c = value.charAt(0);
					return new NumberExpr((byte)(c & 0xff));
				}
				
				if(symbol.groupEquals("IDENTIFIER")) {
					String value = symbol.value();
					if(!curFunction.hasIdentifier(value)) {
						if(GLOBAL.containsKey(value)) {
							symbol.next();
							return GLOBAL.get(value);
						}
						if(FUNCTIONS.containsKey(value)) {
							symbol.next();
							return new FunctionExpr(FUNCTIONS.get(value));
						}
						throwError(symbol, "Undeclared variable name. Could not find the variable '" + value + "'");
					} else {
						// Check that value
						
//						if(true) {
//							// System.out.println("Identifier value = '" + value + "'");
//							Variable var = curFunction.getVariableFromName(value);
//							int stackIndex = curFunction.getVariableStackIndex(var);
//							symbol.next();
//							return new StackExpr(var, stackIndex);
//						}
					}
					
					symbol.next();
					return new IdentifierExpr(value);
				}
				
				if(symbol.valueEquals("(")) {
					Expression expr = e15(symbol.next());
					if(!symbol.valueEquals(")")) throwError(symbol, "Invalid bracket close on expression. Expected close bracket ')' but got '" + symbol.value() + "'");
					symbol.next();
					return expr;
				}
				
				//if(symbol.valueEquals("true")) { symbol.next(); return new ValExpr(1); }
				//if(symbol.valueEquals("false")) { symbol.next(); return new ValExpr(0); }
				
				return null;
			}
		}.parse(symbol);
		
		symbol.resetMarked();
		return expr;
	}
	
	public Argument createNewArgument(Sym symbol) {
		Argument argument = new Argument();
		argument.type = getTypeFromSymbol(symbol);
		if(!symbol.groupEquals("IDENTIFIER")) throwError(symbol, "Invalid function argument name '" + symbol.value() + "'");
		argument.name = symbol.value();
		symbol.next();
		return argument;
	}
	
	public Variable createNewArgument2(Sym symbol) {
		Variable variable = new Variable();
		variable.type = getTypeFromSymbol(symbol);
		if(!symbol.groupEquals("IDENTIFIER")) throwError(symbol, "Invalid function argument name '" + symbol.value() + "'");
		if(curFunction.hasIdentifier(symbol.value())) throwError(symbol, "Redefinition of a variable named '" + symbol.value() + "'");
		variable.name = symbol.value();
		symbol.next();
		return variable;
	}
	
	private boolean isType(Sym symbol) {
		return TYPES.containsKey(symbol.value());
	}
	
	public Type getTypeFromSymbol(Sym symbol) {
		if(!isType(symbol)) throwError(symbol, "Invalid type '" + symbol.toString() + "'");
		Type type = TYPES.get(symbol.value());
		symbol.next();
		
		if(symbol.valueEquals("*")) {
			int size = 0;
			
			while(symbol.valueEquals("*")) {
				size++;
				symbol.next();
			}
			
			return new PointerType(type, size);
		}
		
		return type;
	}
	
	public Modifier createNewModifier(Sym symbol) {
		Modifier modifier = new Modifier();
		modifier.name = symbol.toString();
		symbol.next();
		return modifier;
	}
	
	private void throwError(Sym symbol, String message) {
		int line = symbol.line();
		int column = symbol.column();
		
		if(!symbol.marked().isEmpty()) symbol.reset();
		throw new RuntimeException("(line:" + line + " column:" + column + ") " + message);
	}
}
