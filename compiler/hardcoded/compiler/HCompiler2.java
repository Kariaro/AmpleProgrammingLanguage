package hardcoded.compiler;

import java.io.File;
import java.util.*;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Identifier.*;
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
		// lbreak, break out of a branch directly. Jump to end of branch..
		keywords.addAll(Arrays.asList(
			"switch", "while", "for", "if",
			"return", "break", "continue",
			"case", "default", "asm"
		));
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
	
	
	private void addPrimitive(String name, ExprType type, int size, boolean floating) {
		TYPES.put(name, new PrimitiveType(name, type, size, floating, false));
	}
	
	private void addPrimitive(String name, ExprType type, int size) {
		TYPES.put(name, new PrimitiveType(name, type, size));
	}
	
	private Map<String, Type> TYPES;
	private Map<String, Expression> GLOBAL;
	private Map<String, Function> FUNCTIONS; // TODO: Remove
	public HCompiler2() {
		FUNCTIONS = new HashMap<>();
		GLOBAL = new HashMap<>();
		TYPES = new HashMap<>();
		
		addPrimitive("void", null, 0);
		addPrimitive("long", int8, 8);
		addPrimitive("int", int4, 4);
		addPrimitive("short", int2, 2);
		addPrimitive("byte", int1, 1);
		
		addPrimitive("char", int1, 1);
		addPrimitive("bool", int1, 1);
		
		addPrimitive("double", float8, 8, true);
		addPrimitive("float", float4, 4, true);
		
		try {
			lexer = TokenizerFactory.loadFromFile(new File("res/project/lexer.lex"));
			
			build();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private File projectPath = new File("res/project/src/");
	public void build() throws Exception {
		if(curProgram != null) throw new RuntimeException("Building twice is not allowed.........");
		curProgram = new Program();
		
		importFile(curProgram, "main.hc");
		doConstantFolding(); // test_syntax
		
		HC2Visualization hc2 = new HC2Visualization();
		hc2.show(curProgram);
		
		for(Block block : curProgram.list()) {
			if(!(block instanceof Function)) continue;
			
			System.out.println("========================================================");
			Function func = (Function)block;
			String str = printPretty(func);
			System.out.println(str.replace("\t", "    "));
		}
		
		System.out.println("========================================================");
	}
	
	private Set<String> importedFiles = new HashSet<>();
	private Program curProgram;
	
	private String printPretty(Function func) {
		String str = func.toString();
		StringBuilder sb = new StringBuilder().append(str.substring(0, str.length() - 1));
		if(func.isPlaceholder()) return sb.append(";").toString();
		return sb.append(" {").append(printPretty(func.body)).append("\n}").toString();
	}
	
	private String printPretty(Statement stat) {
		StringBuilder sb = new StringBuilder();
		if(stat == null) return "?"; // Invalid
		
		if(stat instanceof IfStat) {
			IfStat is = (IfStat)stat;
			String str = is.toString();
			sb.append(str.substring(0, str.length() - 1)).append(" {").append(printPretty(is.body())).append("\n}");
			if(is.elseBody() != null) sb.append(" else {").append(printPretty(is.elseBody())).append("\n}");
			return sb.toString();
		}
		
		if(stat instanceof WhileStat || stat instanceof ForStat) {
			NestedStat ns = (NestedStat)stat;
			String str = ns.toString();
			return sb.append(str.substring(0, str.length() - 1)).append(" {").append(printPretty(ns.body())).append("\n}").toString();
		}
		
		if(stat.hasStatements()) {
			for(Statement s : stat.getStatements()) {
				String str = printPretty(s);
				if(str.startsWith("\n\t")) str = str.substring(2);
				sb.append("\n\t").append(str.replace("\n", "\n\t"));
			}
			
			return sb.toString();
		}
		
		return "\n\t" + stat.toString();
	}
	
	@FunctionalInterface
	private interface FoldingFunc {
		void constantFolding(List<Expression> parent, int index, Function func);
	}
	
	// TODO: Design feature. A break statement can break out of any nested branch. if, switch, while, for
	//       the keyword is 'lbreak' or 'lbreak <expr>' leaves <expr> branches.
	private void constantFolding(List<Expression> parent, int index, Function func) {
		Expression expr = parent.get(index);
		// System.out.println("Folding: [" + func.name + "], [" + expr + "]");
		
		if(expr instanceof CastExpr) {
			CastExpr e = (CastExpr)expr;
			
			// TODO: Operator overrides..
			if(e.first() instanceof AtomExpr) {
				AtomExpr a = (AtomExpr)e.first();
				
				if(a.isNumber()) {
					Type cast = e.type;
					
					if(cast instanceof PrimitiveType) {
						PrimitiveType pt = (PrimitiveType)cast;
						parent.set(index, a.convert(pt.getType()));
					} else if(cast instanceof PointerType) {
						// Casting into a pointer is always allowed
						// PointerType pt = (PointerType)cast;
						
						if(a.isFloating()) throw new RuntimeException("Cannot cast a floating point value into a pointer");
						// (int*)((int)(double));
						parent.set(index, a.convert(int8));
					}
				}
			}
		}
		
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			
			if(e.type == add || e.type == cor || e.type == cand || e.type == comma) {
				for(int i = e.size() - 1; i >= 0; i--) {
					Expression ex = e.get(i);
					if(ex instanceof OpExpr) {
						OpExpr nx = (OpExpr)ex;
						
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
					List<AtomExpr> list = new ArrayList<>();
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								list.add(a);
								e.list.remove(i);
								i--;
							} else {
								// if(a.atomType == string) throw new RuntimeException("You cannot add string and a number");
								// TODO: Operator overrides.
								// TODO: Identifiers.
								//throw new RuntimeException("You cannot add a non number value");
							}
						}
					}
					
					if(!list.isEmpty()) {
						for(; list.size() > 1;) {
							AtomExpr c = (AtomExpr)ExpressionParser.add(list.get(0), list.get(1));
							list.remove(0);
							list.set(0, c);
						}
						
						if(!(list.get(0).isZero() && e.size() > 0)) {
							e.list.add(0, list.get(0));
						}
					}
					
					if(e.size() == 1) {
						parent.set(index, e.first());
					}
					
					// int value = (int)e.list.stream().filter(x -> x instanceof NumberExpr).map(x -> (NumberExpr)x).map(x -> x.value).mapToDouble(x -> x.doubleValue()).sum();
					// System.out.println("value: " + value);
					break;
				}
				
				case cand: {
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isZero()) {
								for(; i < e.size(); ) {
									e.list.remove(i);
								}
							}
						}
					}
					
					break;
				}
				
				case cor: {
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isZero()) {
								for(; i < e.size(); ) {
									e.list.remove(i);
								}
							}
						}
					}
					
					break;
				}
				
				case neg: { Expression next = ExpressionParser.neg(e.first()); if(next != null) parent.set(index, next); break; }
				case not: { Expression next = ExpressionParser.not(e.first()); if(next != null) parent.set(index, next); break; }
				
				case mul: { Expression next = ExpressionParser.mul(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case div: { Expression next = ExpressionParser.div(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				
				
				case nor: { Expression next = ExpressionParser.nor(e.first()); if(next != null) parent.set(index, next); break; }
				case xor: { Expression next = ExpressionParser.xor(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case or: { Expression next = ExpressionParser.or(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case and: { Expression next = ExpressionParser.and(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case shr: { Expression next = ExpressionParser.shr(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case shl: { Expression next = ExpressionParser.shl(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				
				case eq: { Expression next = ExpressionParser.eq(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case lt: { Expression next = ExpressionParser.lt(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				case lte: { Expression next = ExpressionParser.lte(e.first(), e.last()); if(next != null) parent.set(index, next); break; }
				
				default: {
					
				}
			}
		}
		
		// Good now we can change the object in question..
		// parent.set(index, null); // Testing
		// Expression next = Expression.optimize(expr);
		// parent.set(index, next);
		// System.out.println("       : [" + next + "]");
	}
	
	private void doConstantFolding() {
		for(int i = 0; i < curProgram.size(); i++) {
			Block block = curProgram.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			
			execute_for_all_expressions(func, (parent, index, function) -> {
				String bef = "" + parent.get(index);
				constantFolding(parent, index, function);
				System.out.println("[" + index + "] (" + bef + ") -> (" + parent.get(index) + ")");
			});
		}
	}
	
	private void execute_for_all_expressions(Function func, FoldingFunc fc) {
		getAllExpressions(func, func.body, fc);
	}
	
	private void getAllExpressions(Function func, Statement stat, FoldingFunc fc) {
		if(stat.hasStatements()) {
			for(Statement s : stat.getStatements()) getAllExpressions(func, s, fc);
		}
		
		if(stat instanceof ExprStat) {
			ExprStat es = (ExprStat)stat;
			for(int i = 0; i < es.list.size(); i++) {
				getAllExpressions(func, es.list.get(i), fc);
				fc.constantFolding(es.list, i, func);
			}
		}
		
		if(stat instanceof Variable) {
			Variable var = (Variable)stat;
			for(int i = 0; i < var.list.size(); i++) {
				getAllExpressions(func, var.list.get(i), fc);
				fc.constantFolding(var.list, i, func);
			}
		}
	}
	
	private void getAllExpressions(Function func, Expression expr, FoldingFunc fc) {
		if(expr.hasElements()) {
			List<Expression> list = expr.getElements();
			for(int i = 0; i < list.size(); i++) {
				getAllExpressions(func, list.get(i), fc);
				fc.constantFolding(list, i, func);
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
			
			TYPES.put(name, new Type(name, type.size(), type.isFloating(), type.isSigned()));
			// System.out.println("#TYPE [" + name + "] as [" + type + "]");
		} else if(symbol.valueEquals("import")) {
			if(!symbol.next().groupEquals("STRING")) throwError(symbol, "Invalid import syntax. Expected a string but got '" + symbol + "'.");
			String path = symbol.value().substring(1, symbol.value().length() - 1);
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid import syntax. Expected a semicolon but got '" + symbol + "'");
			symbol.next();
			
			// System.out.println("#IMPORT [" + path + "]");
			try {
				importFile(curProgram, path);
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
				
//				System.out.println("  Function: '" + func + "'");
//				if(!func.isPlaceholder()) {
//					program.add(func);
//				}
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
			Variable arg = createNewArgument(symbol);
			func.arguments.add(new ParamIdent(arg.type, arg.name, func.arguments.size()));
			if(!symbol.valueEquals(",")) {
				if(symbol.valueEquals(")")) break;
				throwError(symbol, "Invalid function argument separator '" + symbol.value() + "' did you forget a comma ? ','");
			} else symbol.next();
		}
		
		if(!curProgram.hasFunction(func.name)) {
			curProgram.add(func);
		}
		
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid closing of the funtion arguments. Expected a closing bracket ')'.");
		if(symbol.next().valueEquals(";")) {
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
	
	private Statement createWhileExpr(Sym symbol) {
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid while statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		WhileStat stat = new WhileStat();
		stat.setCondition(parseExpression(symbol.next()));
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid while statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		stat.setBody(parseStatement(symbol.next()));
		return stat;
	}
	
	private Statement createForStatement(Sym symbol) {
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid for statement. Expected open bracket '(' but got '" + symbol.value() + "'");
		ForStat stat = new ForStat(); {
			symbol.next();
			if(!symbol.valueEquals(";")) { stat.setVariables(getVariableDefinition(symbol)); symbol.prev(); }
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid for statement (variables). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.valueEquals(";")) stat.setCondition(parseExpression(symbol));
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid for statement (condition). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.valueEquals(")")) stat.setAction(parseExpression(symbol));
			if(!symbol.valueEquals(")")) throwError(symbol, "Invalid for statement (action). Expected closing bracket ')' but got '" + symbol.value() + "'");
		}
		stat.setBody(getStatements(symbol.next()));
		return stat;
	}
	
	private Statement createIfStatement(Sym symbol) {
		if(!symbol.next().valueEquals("(")) throwError(symbol, "Invalid if statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		IfStat stat = new IfStat();
		stat.setCondition(parseExpression(symbol.next()));
		if(!symbol.valueEquals(")")) throwError(symbol, "Invalid if statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		stat.setBody(parseStatement(symbol.next()));
		if(symbol.valueEquals("else")) stat.setElseBody(parseStatement(symbol.next()));
		return stat;
	}
	
	private Statement parseStatement(Sym symbol) {
		symbol.mark();
		
		if(isType(symbol)) {
			Statement stat = getVariableDefinition(symbol);
			
			if(stat instanceof StatementList) {
				StatementList list = (StatementList)stat;
				for(int i = 0; i < list.list.size(); i++) {
					Variable var = (Variable)list.list.get(i);
					Identifier ident = curFunction.add(var);
					
					if(!var.isInitialized()) {
						list.list.remove(i--);
					} else {
						// FIXME: Replace var with a move expression!!!!!
						list.list.set(i, new ExprStat(new OpExpr(ExprType.mov, new AtomExpr(ident), var.value())));
					}
				}
			}
			
			symbol.resetMarked();
			return stat;
		}
		
		if(symbol.valueEquals("if")) {
			Statement stat = createIfStatement(symbol); symbol.resetMarked(); return stat;
		} else if(symbol.valueEquals("while")) {
			Statement stat = createWhileExpr(symbol); symbol.resetMarked(); return stat;
		} else if(symbol.valueEquals("for")) {
			Statement stat = createForStatement(symbol); symbol.resetMarked(); return stat;
		}
		
		
		if(symbol.valueEquals("break")) {
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid break statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new BreakStat();
		} else if(symbol.valueEquals("continue")) {
			if(!symbol.next().valueEquals(";")) throwError(symbol, "Invalid continue statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new ContinueStat();
		} else if(symbol.valueEquals("return")) {
			ReturnStat stat = new ReturnStat();
			
			symbol.next();
			if(!symbol.valueEquals(";")) stat.setValue(parseExpression(symbol));
			if(!symbol.valueEquals(";")) throwError(symbol, "Invalid return statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return stat;
		}
		
		
		if(symbol.valueEquals("{")) {
			return getStatements(symbol);
		} else {
			Expression expr = parseExpression(symbol);
			if(symbol.valueEquals(";")) {
				symbol.nextClear();
				return new ExprStat(expr);
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
				if(!(expr instanceof AtomExpr)) {
					throwError(symbol, "Invalid array variable definition. Expected a integer expression but got '" + expr + "'");
				} else {
					AtomExpr number = (AtomExpr)expr;
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
		
		return new StatementList(list);
	}
	
	private Statement getStatements(Sym symbol) {
		symbol.mark();
		if(symbol.valueEquals(";")) {
			symbol.nextClear();
			return Statement.EMPTY;
		}
		
		if(!symbol.valueEquals("{")) {
			Statement stat = parseStatement(symbol);
			symbol.resetMarked();
			return stat;
		}
		
		NestedStat stat = new NestedStat();
		symbol.next();
		for(;;) {
			if(symbol.valueEquals(";")) { symbol.next(); continue; }
			if(symbol.valueEquals("}")) break;
			
			Statement s = parseStatement(symbol);
			if(s == Statement.EMPTY) continue;
			
			if(s.hasStatements() && s.getStatements().size() == 0) continue;
			if(s instanceof StatementList) {
				stat.list.addAll(((StatementList)s).list);
				continue;
			}
			
			stat.list.add(s);
		}
		
		
		if(!symbol.valueEquals("}")) {
			throwError(symbol, "Invalid statement closing bracket. Expected '}' but got '" + symbol.value() + "'");
		}
		
		symbol.nextClear();
		
		if(stat.list.isEmpty()) return Statement.EMPTY;
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
			Expression e_read(Sym symbol, String[] values, ExprType[] exprs, java.util.function.Function<Sym, Expression> func) { return e_read(symbol, values, exprs, func, func); }
			Expression e_read(Sym symbol, String[] values, ExprType[] exprs, java.util.function.Function<Sym, Expression> entry, java.util.function.Function<Sym, Expression> func) {
				Expression expr = entry.apply(symbol);
				for(;;) {
					boolean found = false;
					for(int i = 0; i < values.length; i++) {
						String value = values[i];
						ExprType type = exprs[i];
						
						if(symbol.valueEquals(value)) {
							found = true;
							expr = new OpExpr(type, expr, func.apply(symbol.next()));
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
			
			Expression e15(Sym symbol) {
				return e_read(symbol, _s(","), _e(comma), this::e14);
			}
			
			Expression e14(Sym symbol) { // Left associative
				Expression e13 = e13(symbol);
				
				// If the next expression modifies the assigner then
				//   a temporary variable should get created that holds
				//   the value and adds it in the end.
				
				ExprType type = null;
				switch(symbol.value()) {
					case "=": {
						if(!acceptModification(e13)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
						return new OpExpr(mov, e13, e14(symbol.next()));
					}
					
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
				
				// if(!validAssign) throwError(symbol, "Invalid placement of a assign expression.");
				// return new TestExpr(ExprType.mov, e13, e14(symbol.next()));
				if(type == null) return e13;
				switch(type) {
					case mod:
					
					case shl: case shr:
					case mul: case div:
					case add: case and:
					case xor: case or: {
						if(!acceptModification(e13)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
						Expression next = e14(symbol.next());
						if(!next.isPure()) {
							AtomExpr temp = new AtomExpr(curFunction.temp(new UndefinedType()));
							
							return new OpExpr(comma,
								new OpExpr(mov, temp, new OpExpr(addptr, e13)),
								new OpExpr(mov,
									new OpExpr(decptr, temp),
									new OpExpr(type,
										new OpExpr(decptr, temp),
										next
									)
								)
							);
						}
						
						return new OpExpr(mov, e13, new OpExpr(type, e13, next));
					}
					default:
				}
				
				return e13;
			}
			
			Expression e13(Sym symbol) { // _exp13: _exp12 | _exp13 '?' _exp13 ':' _exp12
				Expression e13 = e12(symbol);
				if(symbol.valueEquals("?")) {
					Expression a = e12(symbol.next());
					if(!symbol.valueEquals(":")) throwError(symbol, "Invalid ternary operation a ? b : c. Missing the colon. '" + symbol + "'");
					
					// Cannot optimize easyly
					// (e13 && (temp = a, 1) || (temp = b), temp)
					Expression b = e12(symbol.next());
					
					// return new TeExpr(e13, "?", a, ":", b);
					AtomExpr temp = new AtomExpr(curFunction.temp(new UndefinedType()));
					return new OpExpr( // TODO: Check if this is correct
						comma,
						new OpExpr(cor,
							new OpExpr(cand,
								e13,
								new OpExpr(comma, new OpExpr(mov, temp, a), new AtomExpr(1))
							),
							new OpExpr(comma, new OpExpr(mov, temp, b))
						),
						temp
					);
				}
				
				return e13;
			}
			
			Expression e12(Sym symbol) { return e_read(symbol, _s("||"), _e(cor), this::e11, this::e12); }
			Expression e11(Sym symbol) { return e_read(symbol, _s("&&"), _e(cand), this::e10, this::e11); }
			Expression e10(Sym symbol) { return e_read(symbol, _s("|"), _e(or), this::e9); }
			Expression e9(Sym symbol) { return e_read(symbol, _s("^"), _e(xor), this::e8); }
			Expression e8(Sym symbol) { return e_read(symbol, _s("&"), _e(and), this::e7); }
			Expression e7(Sym symbol) {
				Expression expr = e6(symbol);
				for(;;) {
					if(symbol.valueEquals("==")) {
						expr = new OpExpr(eq, expr, e7(symbol.next()));
					} else if(symbol.valueEquals("!=")) {
						expr = new OpExpr(not, new OpExpr(eq, expr, e7(symbol.next())));
					} else return expr;
				}
			}
			
			Expression e6(Sym symbol) {
				for(Expression expr = e5(symbol);;) {
					if(symbol.valueEquals("<")) {
						expr = new OpExpr(lt, expr, e5(symbol.next()));
					} else if(symbol.valueEquals("<=")) {
						expr = new OpExpr(lte, expr, e5(symbol.next()));
					} else if(symbol.valueEquals(">")) {
						expr = new OpExpr(not, new OpExpr(lte, expr, e5(symbol.next())));
					} else if(symbol.valueEquals(">=")) {
						expr = new OpExpr(not, new OpExpr(lt, expr, e5(symbol.next())));
					} else return expr;
				}
			}
			Expression e5(Sym symbol) { return e_read(symbol, _s("<<", ">>"), _e(shl, shr), this::e4); }
			Expression e4(Sym symbol) {
				for(Expression expr = e3(symbol);;) {
					if(symbol.valueEquals("+")) {
						expr = new OpExpr(add, expr, e3(symbol.next()));
					} else if(symbol.valueEquals("-")) {
						expr = new OpExpr(neg, new OpExpr(add, expr, e3(symbol.next())));
					} else return expr;
				}
			}
			
			Expression e3(Sym symbol) { return e_read(symbol, _s("*", "/", "%"), _e(mul, div, mod), this::e2); }
			
			Expression e2(Sym symbol) {
				String value = symbol.value();
				
				switch(value) {
					case "&": return new OpExpr(addptr, e2(symbol.next()));
					case "*": return new OpExpr(decptr, e2(symbol.next()));
					case "+": return e1(symbol.next());
					
					case "!": return new OpExpr(not, e1(symbol.next()));
					case "~": return new OpExpr(nor, e1(symbol.next()));
					case "-": return new OpExpr(neg, e1(symbol.next()));
					
					// FIXME: Add this later!!!!
					case "++": case "--": {
						Expression expr = e1(symbol.next());
						if(!acceptModification(expr)) throwError(symbol, "The expression you are trying to modify is not modifiable.");
						
						int direction = value.equals("++") ? 1:-1;
						
						if(!expr.isPure()) {
							AtomExpr ident = new AtomExpr(curFunction.temp(new UndefinedType()));
							
							return new OpExpr(comma,
								new OpExpr(mov, ident, new OpExpr(addptr, expr)),
								new OpExpr(mov,
									new OpExpr(decptr, ident),
									new OpExpr(add,
										new OpExpr(decptr, ident),
										new AtomExpr(direction)
									)
								)
							);
						}
						
						return new OpExpr(mov, expr, new OpExpr(add, expr, new AtomExpr(direction)));
					}
					
					case "(": {
						if(isType(symbol.next())) {
							Type type = getTypeFromSymbol(symbol);
							if(type instanceof PrimitiveType) {
								PrimitiveType pt = (PrimitiveType)type;
								
								if(pt.getType() == null) throwError(symbol, "Invalid cast expression. You cannot cast to the type 'void'.");
							}
							
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
								int direction = symbol.value().equals("++") ? 1:-1;
								symbol.next();
								
								AtomExpr ident2 = new AtomExpr(curFunction.temp(new UndefinedType()));
								if(!e1.isPure()) {
									AtomExpr ident1 = new AtomExpr(curFunction.temp(new UndefinedType()));
									
									return new OpExpr(comma,
										new OpExpr(mov, ident2, new OpExpr(addptr, e1)),
										new OpExpr(mov, ident1, new OpExpr(decptr, ident2)),
										new OpExpr(mov, new OpExpr(decptr, ident2), new OpExpr(add, new OpExpr(decptr, ident2), new AtomExpr(direction))),
										new OpExpr(mov, e1, new OpExpr(add, e1, new AtomExpr(direction))),
										ident1
									);
								}
								
								return new OpExpr(comma,
									new OpExpr(mov, ident2, e1),
									new OpExpr(mov, e1, new OpExpr(add, e1, new AtomExpr(direction))),
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
									
									e1 = new OpExpr(decptr, new OpExpr(add, e1, expr));
									continue;
								}
								
								// TODO: Later
								// case ".": e1 = new BiExpr("MEMBER", e1, e1(symbol.next())); continue;
								// case "->": e1 = new BiExpr("PMEMBER", e1, e1(symbol.next())); continue;
								// case "::": e1 = new BiExpr("NMEMBER", e1, e1(symbol.next())); continue;
								
								case "(": {
									OpExpr expr = new OpExpr(call);
									expr.list.add(e1);
									symbol.next();
									
									AtomExpr ae = (AtomExpr)e1;
									Function func = FUNCTIONS.get(ae.d_value.name());
									
									int length = func.arguments.size();
									for(int i = 0; i < length; i++) {
										//Variable argument = func.arguments.get(i);
										
										Expression arg = e14(symbol);
										if(arg == null) throwError(symbol, "Invalid call argument value.");
										// System.out.println(argument + " /// " + arg + " [" + arg.type() + "]");
										expr.list.add(arg);
										
										if(symbol.valueEquals(",")) {
											if(i == length - 1) throwError(symbol, "Too many arguments calling function '" + func.name + "' Expected " + length + " argument" + (length == 1 ? "":"s") + ".");
											symbol.next();
											continue;
										}
										
										if(i != length - 1) throwError(symbol, "Not enough arguments to call function '" + func.name + "' expected " + length + " argument" + (length == 1 ? "":"s")+ " but got " + (i + 1) + "");
										
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
				if(symbol.groupEquals("LONG")) { String value = symbol.value(); symbol.next(); return new AtomExpr(parseLong(value)); }
				if(symbol.groupEquals("INT")) { String value = symbol.value(); symbol.next(); return new AtomExpr(parseInteger(value)); }
				if(symbol.groupEquals("DOUBLE")) { String value = symbol.value(); symbol.next(); return new AtomExpr(Double.parseDouble(value)); }
				if(symbol.groupEquals("FLOAT")) { String value = symbol.value(); symbol.next(); return new AtomExpr(Float.parseFloat(value)); }
				if(symbol.groupEquals("STRING")) { String value = symbol.value(); symbol.next(); return new AtomExpr(value.substring(1, value.length() - 1)); } // TODO: Unicode ?
				
				if(symbol.groupEquals("CHAR")) { // TODO: Unicode
					String value = StringUtils.unescapeString(symbol.value().substring(1, symbol.value().length() - 1));
					if(value.length() != 1) throwError(symbol, "Invalid char expression. Expected only one character.");
					symbol.next();
					
					char c = value.charAt(0);
					return new AtomExpr((byte)(c & 0xff));
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
							return new AtomExpr(curProgram.getFunction(value));
						}
						
						throwError(symbol, "Undeclared variable name. Could not find the variable '" + value + "'");
					} else {
						symbol.next();
						return new AtomExpr(curFunction.getIdentifier(value));
					}
					
					// TODO: Dead code.
					symbol.next();
					return null; // TODO: Error
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
	
	public Variable createNewArgument(Sym symbol) {
		Variable variable = new Variable(getTypeFromSymbol(symbol));
		if(!symbol.groupEquals("IDENTIFIER")) throwError(symbol, "Invalid function argument name '" + symbol.value() + "'");
		if(curFunction.hasIdentifier(symbol.value())) throwError(symbol, "Redefinition of a variable named '" + symbol.value() + "'");
		variable.name = symbol.value();
		symbol.next();
		return variable;
	}
	
	private boolean acceptModification(Expression expr) {
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			return e.type() == ident;
		}
		
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
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
