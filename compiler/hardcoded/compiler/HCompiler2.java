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
import static hardcoded.compiler.Expression.ExprType.*;

public class HCompiler2 {
	private static final Set<String> MODIFIERS;
	private static final Set<String> KEYWORDS;
	
	static {
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
		if(currentProgram != null) throw new RuntimeException("Building twice is not allowed.........");
		currentProgram = new Program();
		
		importFile(currentProgram, "prog.hc");
		doConstantFolding(); // test_syntax
		
		HC2Visualization hc2 = new HC2Visualization();
		hc2.show(currentProgram);
		
		for(Function func : currentProgram.functions) {
			System.out.println("========================================================");
			String str = printPretty(func);
			System.out.println(str.replace("\t", "    "));
		}
		
		System.out.println("========================================================");
	}
	
	private Set<String> importedFiles = new HashSet<>();
	private Program currentProgram;
	
	private String printPretty(Function func) {
		StringBuilder sb = new StringBuilder();
		sb.append(func.returnType).append(" ").append(func.name).append("(");
		for(int i = 0; i < func.arguments.size(); i++) {
			String value = (func.arguments.get(i)).toString();
			sb.append(value.substring(0, value.length() - 1));
			if(i < func.arguments.size() - 1) sb.append(", ");
		}
		sb.append(")");
		if(func.isPlaceholder()) return sb.append(";").toString(); // Should be removed if it is a placeholder. NO CODE
		return sb.append(" {\n").append(printPretty(func.body)).append("}").toString();
	}
	
	private String printPretty(Expression stat) {
		StringBuilder sb = new StringBuilder();
		if(stat == null) return "?"; // Invalid
//		if(stat instanceof IfStatement) {
//			IfStatement is = (IfStatement)stat;
//			sb.append("if(").append(is.condition()).append(")");
//			if(is.body == null) return sb.append(";").toString();
//			if(is.body.elementsStatements().size() < 1) sb.append(" ").append(printPretty(is.body)).toString(); else sb.append(" {\n").append(printPretty(is.body)).append("}");
//			if(is.elseBody != null) {
//				if(is.elseBody.elementsStatements().size() < 1) return sb.append(" else ").append(printPretty(is.elseBody)).toString();
//				return sb.append("} else {\n").append(printPretty(is.body)).append("}").toString();
//			}
//			return sb.toString();
//		}
//		if(stat instanceof WhileStatement || stat.type() == loop) {
//			WhileStatement ws = (WhileStatement)stat; sb.append("while(").append(ws.condition()).append(")");
//			if(ws.body == null) return sb.append(";").toString();
//			if(ws.body.elementsStatements().size() < 1) return sb.append(" ").append(printPretty(ws.body)).toString();
//			return sb.append(" {\n").append(printPretty(ws.body)).append("}").toString();
//		}
//		if(stat instanceof ForStatement) {
//			ForStatement fs = (ForStatement)stat;
//			sb.append("for(").append(fs.variables == null ? "":fs.variables).append("; ").append(fs.condition() == null ? "":fs.condition()).append("; ").append(fs.action() == null ? "":fs.action()).append(")");
//			if(fs.body == null) return sb.append(";").toString(); if(fs.body.elementsStatements().size() < 1) return sb.append(" ").append(printPretty(fs.body)).toString();
//			return sb.append(" {\n").append(printPretty(fs.body)).append("}").toString();
//		}
//		List<Statement> list = stat.elementsStatements();
//		if(list.size() > 0) {
//			for(Statement s : list) {
//				String str = printPretty(s);
//				sb.append("\t").append(str.replace("\n", "\n\t")).append("\n");
//			}
//		} else return stat.toString();
		List<Expression> list = stat.elements();
		if(list.size() > 0) {
			for(Expression s : list) {
				String str = printPretty(s);
				sb.append("\t").append(str.replace("\n", "\n\t")).append("\n");
			}
		} else return stat.toString();
		return sb.toString();
	}
	
	private void constantFolding(List<Expression> parent, int index, Function func) {
		Expression expr = parent.get(index);
		// System.out.println("Folding: [" + func.name + "], [" + expr + "]");
		
		if(expr instanceof OperatorExpr) {
			OperatorExpr e = (OperatorExpr)expr;
			
			if(e.type == add || e.type == sub || e.type == cor || e.type == cand || e.type == comma) {
				for(int i = e.size() - 1; i >= 0; i--) {
					Expression ex = e.get(i);
					if(ex instanceof OperatorExpr) {
						OperatorExpr nx = (OperatorExpr)ex;
						
						if(nx.type == e.type) {
							e.list.remove(i);
							e.list.addAll(i, nx.list);
							i += nx.list.size();
							continue;
						}
					}
				}
			}
	
			switch(e.type) {
				case add: {
					// int value = (int)e.list.stream().filter(x -> x instanceof NumberExpr).map(x -> (NumberExpr)x).map(x -> x.value).mapToDouble(x -> x.doubleValue()).sum();
					// System.out.println("value: " + value);
					break;
				}
				case neg: {
//					if(e.isPure() && Expression.isNumber(e.first())) {
//						parent.set(index, ((NumberExpr)e.first()).neg());
//					}
					break;
				}
				default: {
					
				}
			}
		}
		
		// Good now we can change the object in question..
		// parent.set(index, null); // Testing
		
		// System.out.println("       : [" + Expression.optimize(expr) + "]");
	}
	
	private void doConstantFolding() {
		// System.out.println("Function had null list. doConstantFolding");
		
		for(Function func : currentProgram.functions) {
			// System.out.println("Function: " + func);
			List<List<Expression>> lists = func.body.stat_expressionsAll();
			
			for(int i = 0; i < lists.size(); i++) {
				List<Expression> list = lists.get(i);
				
				for(int j = 0; j < list.size(); j++) {
					constantFolding(list, j, func);
				}
			}
		}
	}
	
	private void importFile(Program program, String name) throws Exception {
		if(importedFiles.contains(name)) return; // Ignore
		importedFiles.add(name);
		
		Sym symbol = new Sym(TokenizerOld.generateTokenChain(lexer, FileUtils.readFileBytes(new File(projectPath, name))));
		System.out.println("Tokens: '" + symbol.token().toString(" ", Integer.MAX_VALUE) + "'");
		
		try {
			parseProgram(program, symbol);
		} catch(Exception e) {
			e.printStackTrace();
			throwError(symbol, "");
		}
		
//		System.out.println();
//		System.out.println("Program: '" + program + "'");
//		for(Function func : program.functions) {
//			System.out.println("  Function: '" + func + "'");
//		}
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
			// System.out.println("#TYPE [" + name + "] as [" + type + "]");
		} else if(symbol.valueEquals("import")) {
			if(!symbol.next().groupEquals("STRING")) throwError(symbol, "Invalid import syntax. Expected a string but got '" + symbol + "'.");
			String path = symbol.value().substring(1, symbol.value().length() - 1);
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid import syntax. Expected a semicolon but got '" + symbol + "'");
			symbol.next();
			
			// System.out.println("#IMPORT [" + path + "]");
			try {
				importFile(currentProgram, path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(symbol.valueEquals("set")) {
			if(!isValidName(symbol.next())) throwError(symbol, "Invalid type name. The value '" + symbol + "' is not a valid type name.");
			String name = symbol.value();
			Expression expr = parseExpression(symbol.next());
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid set syntax. Expected a semicolon but got '" + symbol + "'");
			symbol.next();
			// System.out.println("#SET [" + name + "] as [" + expr + "]");
			
			// TODO: Check that the expression is a compiler value that does not use variables.
			GLOBAL.put(name, expr);
		} else if(symbol.valueEquals("unset")) {
			String name = symbol.next().value();
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Did you forget a semicolon here?");
			symbol.next();
			
			// System.out.println("#UNSET [" + name + "]");
			if(TYPES.containsKey(name)) {
				if(TYPES.get(name) instanceof PrimitiveType) throwError(symbol, "Invalid unset syntax. You cannot unset the primitive type '" + name + "'");
				TYPES.remove(name);
			}
			else if(GLOBAL.containsKey(name)) GLOBAL.remove(name);
			else; // Trying to remove something that does not exist...
		}
	}
	
	private Program parseProgram(Program program, Sym symbol) {
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
		if(TYPES.containsKey(symbol.value())) return false; // Contains all primitives.
		if(GLOBAL.containsKey(symbol.value())) return false; // Should only be for globals... 
		if(isModifier(symbol)) return false;
		if(isKeyword(symbol)) return false;
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
			// TODO: Arguments must be same
			
			func = def;
			func.arguments.clear();
			
			needsBody = true;
		} else {
			func.name = symbol.value();
			FUNCTIONS.put(func.name, func);
		}
		curFunction = func;
		
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid function declaration. Did you forget a open bracket here ? '" + symbol + "'");
		symbol.next();
		
		while(!symbol.valueEquals(")")) {
			Variable argument = createNewArgument2(symbol);
			func.arguments.add(argument);
			if(!symbol.valueEquals(",")) {
				if(symbol.valueEquals(")")) break;
				throwError(symbol, "Invalid function argument separator '" + symbol.value() + "' did you forget a comma ? ','");
			} else symbol.next();
		}
		
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid closing of the funtion arguments. Expected a closing bracket ')'.");
		symbol.next();
		
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
	
	private Expression createIfStatement(Sym symbol) {
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid if statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		OperatorExpr expr = new OperatorExpr(cand);
		expr.add(parseExpression(symbol.next()));
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid if statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		expr.add(parseStatement(symbol.next()));
		if(symbol.valueEquals("else")) expr = new OperatorExpr(cor, expr, parseStatement(symbol.next()));
		return expr;
	}
	
	private Expression createWhileExpr(Sym symbol) {
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid while statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		OperatorExpr while_expr = new OperatorExpr(loop);
		while_expr.add(parseExpression(symbol.next()));
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid while statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		while_expr.add(parseStatement(symbol.next()));
		return while_expr;
	}
	
	private Expression createForStatement(Sym symbol) {
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid for statement. Expected open bracket '(' but got '" + symbol.value() + "'");
		ForStatement stat = new ForStatement(); {
			symbol.next();
			if(!symbol.valueEquals(";")) { stat.variables = getVariableDefinition(symbol); symbol.prev(); }
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid for statement (variables). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.valueEquals(";")) stat.setCondition(parseExpression(symbol));
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid for statement (condition). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.valueEquals(")")) stat.setAction(parseExpression(symbol));
			if(!symbol.valueEquals(")")) throwError(symbol, "Invalid for statement (action). Expected closing bracket ')' but got '" + symbol.value() + "'");
		}
		stat.body = getStatements(symbol.next());
		return stat;
	}
	
	private Expression parseStatement(Sym symbol) {
		symbol.mark();
		
		if(isType(symbol)) {
			Statement stat = getVariableDefinition(symbol);
			
			if(stat instanceof ExpandStatement) {
				ExpandStatement list = (ExpandStatement)stat;
				for(int i = 0; i < list.list.size(); i++) {
					Variable var = (Variable)list.list.get(i);
					curFunction.getScope().add(var);
				}
			}
			
			symbol.resetMarked();
			return stat;
		}
		
		if(symbol.valueEquals("if")) {
			Expression stat = createIfStatement(symbol);
			symbol.resetMarked();
			return stat;
		} else if(symbol.valueEquals("while")) {
			Expression stat = createWhileExpr(symbol);
			symbol.resetMarked();
			return stat;
		} else if(symbol.valueEquals("for")) {
			Expression stat = createForStatement(symbol);
			symbol.resetMarked();
			return stat;
		}
		
		
		if(symbol.valueEquals("break")) {
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid break statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new BreakStatement();
		} else if(symbol.valueEquals("continue")) {
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid continue statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new ContinueStatement();
		} else if(symbol.valueEquals("return")) {
			OperatorExpr rexpr = new OperatorExpr(ret);
			ExprStatement st_expr = new ExprStatement(rexpr);
			
			symbol.next();
			if(!symbol.valueEquals(";")) rexpr.add(parseExpression(symbol));
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid return statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return st_expr;
		}
		
		
		if(symbol.valueEquals("{")) {
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
	
	private Statement getVariableDefinition(Sym symbol) {
		symbol.mark();
		Type type = getTypeFromSymbol(symbol);
		
		List<Variable> list = new ArrayList<Variable>();
		do {
			Variable var = new Variable(type);
			list.add(var);
			
			if(!isValidName(symbol)) throwError(symbol, "Invalid local variable name '" + symbol.value() + "'");
			if(curFunction.hasIdentifier(symbol.value())) throwError(symbol, "Redeclaration of a local variable '" + symbol.value() + "'");
			var.name = symbol.value();
			symbol.next();
			
			if(symbol.valueEquals("[")) {
				Expression expr = parseExpression(symbol.next());
				if(!(expr instanceof NumberExpr)) {
					throwError(symbol, "Invalid array variable definition. Expected a integer expression but got '" + expr + "'");
				} else {
					NumberExpr number = (NumberExpr)expr;
					if(!number.isInteger()) throwError(symbol, "Invalid array variable definition. Expected a integer expression. '" + expr + "'");
					var.arraySize = number.value().intValue();
				}
				var.isArray = true;
				
				if(!symbol.valueEquals("]")) throwError(symbol, "Invalid array variable definition. Expected array closure ']' but got '" + symbol.value() + "'");
				symbol.next();
				if(!symbol.valueEquals(";")) throwError(symbol, "Invalid array variable definition. Expected a semicolon ';' but got '" + symbol.value() + "'");
				symbol.nextClear();
				break;
			}
			
			if(symbol.valueEquals(";")) {
				symbol.nextClear();
				break;
			} else if(symbol.valueEquals("=")) {
				var.setValue(parseExpression(symbol.next(), true));
				
				if(symbol.valueEquals(";")) {
					symbol.nextClear();
					break;
				} else if(!symbol.valueEquals(",")) {
					throwError(symbol, "Invalid variable definition. Expected a comma or semicolon but got '" + symbol.value() + "'");
				}
				
				symbol.next();
				continue;
			}
		} while(true);
		
		return new ExpandStatement(list);
	}
	
	private Expression getStatements(Sym symbol) {
		symbol.mark();
		if(symbol.valueEquals(";")) {
			symbol.nextClear();
			return new EmptyStatement();
		}
		
		if(!symbol.valueEquals("{")) {
			Expression stat = parseStatement(symbol);
			symbol.resetMarked();
			return stat;
		}
		
		Statements stat = new Statements();
		symbol.next();
		for(;;) {
			if(symbol.valueEquals(";")) { symbol.next(); continue; }
			if(symbol.valueEquals("}")) break;
			
			Expression s = parseStatement(symbol);
			if(s instanceof EmptyStatement) continue;
			if(s instanceof ExpandStatement) {
				ExpandStatement es = (ExpandStatement)s;
				if(!es.list.isEmpty()) {
					stat.list.addAll(es.list);
					continue;
				}
			}
			
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
							expr = new OperatorExpr(type, expr, func.apply(symbol.next()));
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
				// System.out.println("Parsed: '" + expr + "' is_pure=" + expr.isPure());
				return expr;
			}
			
			Expression e15(Sym symbol) { // _exp15: _exp14 | _exp15 ',' _exp14
				return e_test(symbol, _s(","), _e(comma), this::e14);
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
				
				// If the next expression modifies the assigner then
				//   a temporary variable should get created that holds
				//   the value and adds it in the end.
				
				ExprType type = null;
				switch(symbol.value()) {
					case "=": {
						if(!acceptModification(e13)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
						return new OperatorExpr(mov, e13, e14(symbol.next()));
					}
					case "+=": type = add; break;
					case "-=": type = sub; break;
					case "<<=": type = shl; break;
					case ">>=": type = shr; break;
					case "*=": type = mul; break;
					case "%=": type = mod; break;
					case "/=": type = div; break;
					case "&=": type = and; break;
					case "^=": type = xor; break;
					case "|=": type = or; break;
				}
				
				// TODO: Would be nice to not allow assigns inside expressions...
				// if(!validAssign) throwError(symbol, "Invalid placement of a assign expression.");
				// return new TestExpr(ExprType.mov, e13, e14(symbol.next()));
				if(type == null) return e13;
				switch(type) {
					case add: case sub:
					case shl: case shr:
					case mul: case div:
					case and: case xor:
					case mod: case or: {
						if(!acceptModification(e13)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
						Expression next = e14(symbol.next());
						if(!next.isPure()) {
							IdentifierExpr ident = curFunction.temp(new UndefinedType());
							return new OperatorExpr(comma,
								new OperatorExpr(mov, ident, new OperatorExpr(addptr, e13)),
								new OperatorExpr(mov,
									new OperatorExpr(decptr, ident),
									new OperatorExpr(type,
										new OperatorExpr(decptr, ident),
										next
									)
								)
							);
						}
						
						return new OperatorExpr(mov, e13, new OperatorExpr(type, e13, next));
					}
					default:
				}
				
				return e13;
			}
			
			Expression e13(Sym symbol) { // _exp13: _exp12 | _exp13 '?' _exp13 ':' _exp12
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
			
			Expression e12(Sym symbol) { return e_test(symbol, _s("||"), _e(cor), this::e11, this::e12); }
			Expression e11(Sym symbol) { return e_test(symbol, _s("&&"), _e(cand), this::e10, this::e11); }
			Expression e10(Sym symbol) { return e_test(symbol, _s("|"), _e(or), this::e9); }
			Expression e9(Sym symbol) { return e_test(symbol, _s("^"), _e(xor), this::e8); }
			Expression e8(Sym symbol) { return e_test(symbol, _s("&"), _e(and), this::e7); }
			Expression e7(Sym symbol) { return e_test(symbol, _s("==", "!="), _e(eq, neq), this::e6, this::e7); }
			Expression e6(Sym symbol) { return e_test(symbol, _s("<", "<=", ">", ">="), _e(lt, lte, gt, gte), this::e5 ); }
			Expression e5(Sym symbol) { return e_test(symbol, _s("<<", ">>"), _e(shl, shr), this::e4); }
			Expression e4(Sym symbol) { return e_test(symbol, _s("+", "-"), _e(add, sub), this::e3); }
			Expression e3(Sym symbol) { return e_test(symbol, _s("*", "/", "%"), _e(mul, div, mod), this::e2); }
			
			/* _exp2: _exp1
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
			   _exp1: _exp0
					| _exp1 '[' expr ']'
			    	| _exp1 '(' expr [',' expr] ')'
			*/
			Expression e2(Sym symbol) {
				String value = symbol.value();
				
				switch(value) {
					case "&": return new OperatorExpr(addptr, e2(symbol.next()));
					case "*": return new OperatorExpr(decptr, e2(symbol.next()));
					case "+": return e1(symbol.next());
					
					case "!": return new OperatorExpr(not, e1(symbol.next()));
					case "~": return new OperatorExpr(nor, e1(symbol.next()));
					case "-": return new OperatorExpr(neg, e1(symbol.next()));
					
					// FIXME: Add this later!!!!
					case "++": case "--": {
						ExprType type = value.equals("--") ? sub:add;
						Expression expr = e1(symbol.next());
						
						if(!acceptModification(expr)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
						
						if(!expr.isPure()) {
							IdentifierExpr ident = curFunction.temp(new UndefinedType());
							return new OperatorExpr(comma,
								new OperatorExpr(mov, ident, new OperatorExpr(addptr, expr)),
								new OperatorExpr(mov,
									new OperatorExpr(decptr, ident),
									new OperatorExpr(type,
										new OperatorExpr(decptr, ident),
										new NumberExpr(1)
									)
								)
							);
						}
						
						return new OperatorExpr(mov, expr, new OperatorExpr(type, expr, new NumberExpr(1)));
					}
					
					case "(": {
						if(isType(symbol.next())) {
							Type type = getTypeFromSymbol(symbol);
							if(!symbol.valueEquals(")")) throwError(symbol, "Invalid cast expression. Expected closing bracket ')' but got '" + symbol + "'");
							return new CastExpr(type, e2(symbol.next()));
						} else symbol.prev();
					}
					default: {
						Expression e1 = e1(symbol);
						
						// FIXME: Add this later!!!!
						switch(symbol.value()) {
							case "++": case "--": {
								if(!acceptModification(e1)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
								
								ExprType type = value.equals("--") ? sub:add;
								symbol.next();
								IdentifierExpr ident2 = curFunction.temp(new UndefinedType());
								if(!e1.isPure()) {
									IdentifierExpr ident1 = curFunction.temp(new UndefinedType());
									
									return new OperatorExpr(comma,
										new OperatorExpr(mov, ident2, new OperatorExpr(addptr, e1)),
										new OperatorExpr(mov, ident1, new OperatorExpr(decptr, ident2)),
										new OperatorExpr(mov, new OperatorExpr(decptr, ident2), new OperatorExpr(type, new OperatorExpr(decptr, ident2), new NumberExpr(1))),
										new OperatorExpr(mov,
											e1,
											new OperatorExpr(type, e1, new NumberExpr(1))
										),
										ident1
									);
								}
								
								return new OperatorExpr(comma,
									new OperatorExpr(mov, ident2, e1),
									new OperatorExpr(mov,
										e1,
										new OperatorExpr(type, e1, new NumberExpr(1))
									),
									ident2
								);
							}
						}
						
						for(;;) {
							switch(symbol.value()) {
								case "[": {
									Expression expr = e15(symbol.next());
									if(!symbol.valueEquals("]")) throwError(symbol, "Invalid array close character. Expected '[' but got '" + symbol.value() + "'");
									symbol.next();
									
									e1 = new OperatorExpr(decptr, new OperatorExpr(add, e1, expr));
									continue;
								}
								
								// TODO: Later
								// case ".": e1 = new BiExpr("MEMBER", e1, e1(symbol.next())); continue;
								// case "->": e1 = new BiExpr("PMEMBER", e1, e1(symbol.next())); continue;
								// case "::": e1 = new BiExpr("NMEMBER", e1, e1(symbol.next())); continue;
								
								case "(": {
									OperatorExpr expr = new OperatorExpr(call);
									expr.list.add(e1);
									symbol.next();
									
									FunctionExpr func = (FunctionExpr)e1;
									int length = func.func.arguments.size();
									for(int i = 0; i < length; i++) {
										Variable argument = func.func.arguments.get(i);
										
										Expression arg = e14(symbol);
										if(arg == null) throwError(symbol, "Invalid call argument value.");
										// System.out.println(argument + " /// " + arg + " [" + arg.type() + "]");
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
			
			Expression e1(Sym symbol) {
				if(symbol.groupEquals("INT")) { String value = symbol.value(); symbol.next(); return new NumberExpr(parseInteger(value)); }
				if(symbol.groupEquals("LONG")) { String value = symbol.value(); symbol.next(); return new NumberExpr(parseLong(value)); }
				if(symbol.groupEquals("DOUBLE")) { String value = symbol.value(); symbol.next(); return new NumberExpr(Double.parseDouble(value)); }
				if(symbol.groupEquals("FLOAT")) { String value = symbol.value(); symbol.next(); return new NumberExpr(Float.parseFloat(value)); }
				
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
		Variable variable = new Variable(getTypeFromSymbol(symbol));
		if(!symbol.groupEquals("IDENTIFIER")) throwError(symbol, "Invalid function argument name '" + symbol.value() + "'");
		if(curFunction.hasIdentifier(symbol.value())) throwError(symbol, "Redefinition of a variable named '" + symbol.value() + "'");
		variable.name = symbol.value();
		symbol.next();
		return variable;
	}
	
	private boolean acceptModification(Expression expr) {
		if(expr instanceof IdentifierExpr) return true;
		if(expr instanceof OperatorExpr) {
			OperatorExpr e = (OperatorExpr)expr;
			return e.type == decptr;
		}
		
		return false;
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
