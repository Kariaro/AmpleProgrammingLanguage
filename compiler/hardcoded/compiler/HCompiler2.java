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
		keywords.addAll(Arrays.asList("if", "for", "while", "asm", "return", "break", "continue", "as", "true", "false"));
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
	
	
	public HCompiler2() {
		try {
			lexer = TokenizerFactory.loadFromFile(new File("res/project/lexer.lex"));
			
			build();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void build() throws Exception {
		Sym symbol = new Sym(TokenizerOld.generateTokenChain(lexer, FileUtils.readFileBytes(new File("res/project/src/main.hc"))));
		System.out.println("Tokens: '" + symbol.token().toString(" ", Integer.MAX_VALUE) + "'");
		
		Program program = parseProgram(symbol);
		
		System.out.println();
		System.out.println("Program: '" + program + "'");
		for(Function func : program.functions) {
			System.out.println("  Function: '" + func + "'");
		}
		
		HC2Visualization hc2 = new HC2Visualization();
		hc2.show(program);
	}
	
	private Tokenizer lexer;
	private Program parseProgram(Sym symbol) {
		Program program = new Program();
		
		// export void main( ... ) {}
		// void main( ... ) {}
		Function func;
		while((func = parseFunction(symbol)) != null) {
			System.out.println("  Function: '" + func + "'");
			program.functions.add(func);
		}
		
		return program;
	}
	
	private boolean isValidName(Sym symbol) {
		if(!symbol.groupEquals("IDENTIFIER")) return false;
		if(isModifier(symbol)) return false;
		if(isKeyword(symbol)) return false;
		if(isPrimitive(symbol)) return false;
		return true;
	}
	
	private boolean isType(Sym symbol) {
		return isPrimitive(symbol) || TYPE_NAMES.contains(symbol.value());
	}
	
	private Set<String> TYPE_NAMES = new HashSet<>();
	private Function parseFunction(Sym symbol) {
		if(symbol.remaining() < 1) return null;
		
		Function func = new Function();
		if(isModifier(symbol)) func.modifier = createNewModifier(symbol);
		if(!isType(symbol)) throwError(symbol, "Invalid function return type '" + symbol.value() + "'");
		
		func.returnType = createNewType(symbol);
		
		if(!isValidName(symbol)) throwError(symbol, "Invalid function name '" + symbol.value() + "'");
		func.name = symbol.value();
		symbol.next();
		
		if(!symbol.valueEquals("(")) throwError(symbol, "Invalid function declaration. Did you forget a open bracket here ? '" + symbol + "'");
		else symbol.next();
		
		while(!symbol.valueEquals(")")) {
			Argument argument = createNewArgument(symbol);
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
			symbol.next();
			return func;
		} else if(!symbol.valueEquals("{")) {
			throwError(symbol, "Invalid function body. Expected a open bracket '{'.");
		}
		
		func.body = getStatements(symbol);
		return func;
	}
	
	private Statement createIfStatement(Sym symbol) {
		symbol.next();
		if(!symbol.equals("DELIMITER", "(")) throwError(symbol, "Invalid if statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		symbol.next();
		IfStatement stat = new IfStatement();
		stat.condition = parseExpression(symbol);
		if(!symbol.equals("DELIMITER", ")")) throwError(symbol, "Invalid if statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		stat.body = parseStatement(symbol.next());
		if(symbol.valueEquals("else")) stat.elseBody = parseStatement(symbol.next());
		return stat;
	}
	
	private Statement createWhileStatement(Sym symbol) {
		symbol.next();
		if(!symbol.equals("DELIMITER", "(")) throwError(symbol, "Invalid while statement definition. Expected open bracket '(' but got '" + symbol.value() + "'");
		symbol.next();
		WhileStatement stat = new WhileStatement();
		stat.condition = parseExpression(symbol);
		if(!symbol.equals("DELIMITER", ")")) throwError(symbol, "Invalid while statement definition. Expected close bracket ')' but got '" + symbol.value() + "'");
		stat.body = parseStatement(symbol.next());
		return stat;
	}
	
	private Statement createForStatement(Sym symbol) {
		symbol.next();
		if(!symbol.equals("DELIMITER", "(")) throwError(symbol, "Invalid for statement. Expected open bracket '(' but got '" + symbol.value() + "'");
		symbol.next();
		ForStatement stat = new ForStatement(); {
			if(!symbol.equals("DELIMITER", ";")) { stat.variables = getVariableDefinition(symbol); symbol.prev(); }
			if(!symbol.equals("DELIMITER", ";")) throwError(symbol, "Invalid for statement (variables). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.equals("DELIMITER", ";")) stat.condition = parseExpression(symbol);
			if(!symbol.equals("DELIMITER", ";")) throwError(symbol, "Invalid for statement (condition). Expected semicolon but got '" + symbol.value() + "'");
			symbol.next();
			if(!symbol.equals("DELIMITER", ")")) stat.action = parseExpression(symbol);
			if(!symbol.equals("DELIMITER", ")")) throwError(symbol, "Invalid for statement (action). Expected closing bracket ')' but got '" + symbol.value() + "'");
			symbol.next();
		}
		stat.body = getStatements(symbol);
		return stat;
	}
	
	private Statement parseStatement(Sym symbol) {
		symbol.mark();
		
		if(isType(symbol)) {
			Statement stat = getVariableDefinition(symbol);
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
			if(!symbol.equals("DELIMITER", ";")) throwError(symbol, "Invalid break statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new BreakStatement();
		} else if(symbol.valueEquals("continue")) {
			symbol.next();
			if(!symbol.equals("DELIMITER", ";")) throwError(symbol, "Invalid continue statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return new ContinueStatement();
		} else if(symbol.valueEquals("return")) {
			symbol.next();
			ReturnStatement stat = new ReturnStatement();
			if(!symbol.equals("DELIMITER", ";")) stat.value = parseExpression(symbol);
			if(!symbol.equals("DELIMITER", ";")) throwError(symbol, "Invalid return statement. Expected semicolon but got '" + symbol.value() + "'");
			symbol.nextClear();
			return stat;
		} else if(symbol.valueEquals("{")) {
			return getStatements(symbol);
		} else {
			Expression expr = parseExpression(symbol);
			if(symbol.equals("DELIMITER", ";")) {
				symbol.nextClear();
				return new ExprStatement(expr);
			}
			throwError(symbol, "Invalid expression statement. Expected semicolon but got '" + symbol.value() + "'");
		}
		
		return null;
	}
	
	private MultiVariableStatement getVariableDefinition(Sym symbol) {
		symbol.mark();
		Type type = createNewType(symbol);
		MultiVariableStatement define = new MultiVariableStatement();
		define.type = type;
		
		do {
			Variable stat = define.create();
			if(!isValidName(symbol)) throwError(symbol, "Invalid local variable name '" + symbol.value() + "'");
			stat.name = symbol.value();
			symbol.next();
			
			if(symbol.equals("DELIMITER", "[")) {
				// TODO: Disallow variables in the size..
				Expression expr = parseExpression(symbol.next());
				stat.arraySize = evaluate(expr);
				stat.isArray = true;
				
				if(!symbol.equals("DELIMITER", "]")) throwError(symbol, "Invalid array variable definition. Expected array closure ']' but got '" + symbol.value() + "'");
				symbol.next();
				if(!symbol.equals("DELIMITER", ";")) throwError(symbol, "Invalid array variable definition. Expected a semicolon ';' but got '" + symbol.value() + "'");
				symbol.nextClear();
				return define;
			}
			
			if(symbol.equals("DELIMITER", ";")) {
				stat.initialized = false;
				symbol.nextClear();
				return define;
			} else if(symbol.equals("DELIMITER", "=")) {
				symbol.next();
				stat.value = parseExpression(symbol);
				
				if(symbol.equals("DELIMITER", ";")) {
					stat.initialized = true;
					symbol.nextClear();
					return define;
				} else if(!symbol.equals("DELIMITER", ",")) {
					throwError(symbol, "Invalid variable definition. Expected a comma or semicolon but got '" + symbol.value() + "'");
				}
				
				symbol.next();
				continue;
			}
		} while(true);
	}
	
	private Statement getStatements(Sym symbol) {
		symbol.mark();
		if(symbol.equals("DELIMITER", ";")) {
			symbol.nextClear();
			return new EmptyBracketStatement();
		}
		
		if(!symbol.equals("DELIMITER", "{")) {
			Statement stat = parseStatement(symbol);
			symbol.resetMarked();
			return stat;
		}
		
		symbol.next();
		Statements stat = new Statements();
		
		for(;;) {
			if(symbol.equals("DELIMITER", ";")) { symbol.next(); continue; }
			if(symbol.equals("DELIMITER", "}")) break;
			stat.list.add(parseStatement(symbol));
		}
		
		if(!symbol.equals("DELIMITER", "}")) {
			throwError(symbol, "Invalid statement closing bracket. Expected '}' but got '" + symbol.value() + "'");
		}
		
		symbol.nextClear();
		return stat;
	}
	
	private class MultiVariableStatement implements Statement {
		private Type type;
		private List<Variable> define;
		
		public MultiVariableStatement() {
			define = new ArrayList<>();
		}
		
		private Variable create() {
			Variable stat = new Variable();
			stat.type = type;
			define.add(stat);
			return stat;
		}
		
		@Override
		public String toString() {
			if(define.size() == 1) {
				return type + " " + define.get(0).toString() + ";";
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(type).append(" ").append(StringUtils.join(", ", define)).append(";");
			return sb.toString();
		}
		

		public String listnm() { return "DEFINE"; }
		public Object[] listme() { return define.toArray(); }
	}
	
	private class ForStatement implements Statement {
		private MultiVariableStatement variables;
		public Expression condition;
		public Expression action;
		public Statement body;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("for(");
			if(variables != null) {
				String str = variables.toString();
				sb.append(str.substring(0, str.length() - 1));
			}
			sb.append("; ");
			if(condition != null) sb.append(condition);
			sb.append("; ");
			if(action != null) sb.append(action);
			sb.append(") ").append(body);
			return sb.toString();
		}
	}
	
	private class EmptyBracketStatement implements Statement {
		public String toString() {
			return "{ }";
		}
	}
	
	private Expression parseExpression(Sym symbol) {
		symbol.mark();
		
		Expression expr = new Object() {
			// expr: _exp15
			Expression parse(Sym symbol) {
				Expression expr = e15(symbol);
				
				// System.out.println("Parsed: '" + expr + "'");
				return expr;
			}
			
			// _exp15: _exp14 | _exp15 ',' _exp14
			Expression e15(Sym symbol) {
				Expression e14 = e14(symbol);
				if(symbol.equals("DELIMITER", ",")) {
					symbol.next();
					return new BiExpr(e14, "COMMA OP", e14(symbol));
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
			Expression e14(Sym symbol) {
				Expression e13 = e13(symbol);
				
				switch(symbol.value()) {
					case "=": case "<<=": case ">>=":
					case "+=": case "-=": case "*=":
					case "/=": case "&=": case "^=": case "|=":
						return new BiExpr(e13, symbol.value(), e14(symbol.next()));
					default: return e13;
				}
			}
			
			// _exp13: _exp12 | _exp13 '?' _exp13 ':' _exp12
			Expression e13(Sym symbol) { // a?b:c
				Expression e13 = e12(symbol);
				
				if(symbol.equals("DELIMITER", "?")) {
					symbol.next();
					Expression a = e12(symbol);
					if(!symbol.equals("DELIMITER", ":")) {
						throwError(symbol, "Invalid ternary operation a ? b : c. Missing the colon. '" + symbol + "'");
					} else symbol.next();
					return new TeExpr(e13, "?", a, ":", e12(symbol));
				}
				
				return e13;
			}
			
			// _exp12: _exp11 | _exp12 '||' _exp11
			Expression e12(Sym symbol) {
				Expression e11 = e11(symbol);
				for(;symbol.equals("DELIMITER", "||");) e11 = new BiExpr(e11, symbol.value(), e11(symbol.next()));
				return e11;
			}
			
			// _exp11: _exp10 | _exp11 '&&' _exp10
			Expression e11(Sym symbol) {
				Expression e10 = e10(symbol);
				for(;symbol.equals("DELIMITER", "&&");) e10 = new BiExpr(e10, symbol.value(), e10(symbol.next()));
				return e10;
			}
			
			// _exp10: _exp9 | _exp10 '|' _exp9
			Expression e10(Sym symbol) {
				Expression e9 = e9(symbol);
				for(;symbol.equals("DELIMITER", "|");) e9 = new BiExpr(e9, symbol.value(), e9(symbol.next()));
				return e9;
			}
			
			// _exp9: _exp8 | _exp9 '^' _exp8
			Expression e9(Sym symbol) {
				Expression e8 = e8(symbol);
				for(;symbol.equals("DELIMITER", "^");) e8 = new BiExpr(e8, symbol.value(), e8(symbol.next()));
				return e8;
			}
			
			// _exp8: _exp7 | _exp8 '&' _exp7
			Expression e8(Sym symbol) {
				Expression e7 = e7(symbol);
				for(;symbol.equals("DELIMITER", "&");) e7 = new BiExpr(e7, symbol.value(), e7(symbol.next()));
				return e7;
			}
			
			// _exp7: _exp6 | _exp7 '==' _exp6 | _exp7 '!=' _exp6
			Expression e7(Sym symbol) {
				Expression e6 = e6(symbol);
				for(;;) {
					switch(symbol.value()) {
						case "==": case "!=": e6 = new BiExpr(e6, symbol.value(), e6(symbol.next())); continue;
					}
					break;
				}
				return e6;
			}
			
			// _exp6: _exp5 | _exp6 '<' _exp5 | _exp6 '<=' _exp5 | _exp6 '>' _exp5 | _exp6 '>=' _exp5
			Expression e6(Sym symbol) {
				Expression e5 = e5(symbol);
				for(;;) {
					switch(symbol.value()) {
						case "<": case "<=": case ">": case "=>": e5 = new BiExpr(e5, symbol.value(), e5(symbol.next())); continue;
					}
					break;
				}
				return e5;
			}
			
			// _exp5: _exp4 | _exp5 '>>' _exp4 | _exp5 '<<' _exp4
			Expression e5(Sym symbol) {
				Expression e4 = e4(symbol);
				for(;;) {
					switch(symbol.value()) {
						case "<<": case ">>": e4 = new BiExpr(e4, symbol.value(), e4(symbol.next())); continue;
					}
					break;
				}
				return e4;
			}
			
			// _exp4: _exp3 | _exp4 '+' _exp3 | _exp4 '-' _exp3
			Expression e4(Sym symbol) {
				Expression e3 = e3(symbol);
				for(;;) {
					switch(symbol.value()) {
						case "+": case "-": e3 = new BiExpr(e3, symbol.value(), e3(symbol.next())); continue;
					}
					break;
				}
				return e3;
			}
			
			// _exp3: _exp2 | _exp3 '*' _exp2 | _exp3 '/' _exp2 | _exp3 '%' _exp2
			Expression e3(Sym symbol) {
				Expression e2 = e2(symbol);
				for(;;) {
					switch(symbol.value()) {
						case "*": case "/": case "%": e2 = new BiExpr(e2, symbol.value(), e2(symbol.next())); continue;
					}
					break;
				}
				return e2;
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
					case "&": case "*": case "!":
					case "~": case "-": case "++":
					case "--": return new UnExpr(e1(symbol.next()), value);
					case "(": {
						if(isType(symbol.next())) {
							return new CastExpr(createNewType(symbol), e1(symbol.next()));
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
									// Array
									Expression expr = e15(symbol.next());
									if(!symbol.valueEquals("]")) throwError(symbol, "Invalid array close character. Expected '[' but got '" + symbol.value() + "'");
									symbol.next();
									e1 = new BiExpr(e1, "ARRAY", expr);
									continue;
								}
								case ".": e1 = new BiExpr(e1, "MEMBER", e1(symbol.next())); continue;
								case "->": e1 = new BiExpr(e1, "PMEMBER", e1(symbol.next())); continue;
								case "::": e1 = new BiExpr(e1, "NMEMBER", e1(symbol.next())); continue;
								case "(": {
									// Call
									// throwError(symbol, "Implement inline method calling");
									symbol.next();
									
									CallExpr expr = new CallExpr();
									expr.pointer = e1;
									
									for(;;) {
										Expression arg = e14(symbol);
										expr.args.add(arg);
										if(symbol.valueEquals(",")) {
											symbol.next();
											continue;
										}
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
			
			private long parseInteger(String value) {
				if(value.startsWith("0x")) {
					return Long.parseLong(value.substring(2), 16);
				} else {
					return Long.parseLong(value);
				}
			}
			
			private double parseDecimal(String value) {
				return Double.parseDouble(value);
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
				//System.out.println("e1 -> '" + symbol + "'");
				
				if(symbol.groupEquals("INTEGERLITERAL")) {
					String value = symbol.value();
					symbol.next();
					return new ValExpr(parseInteger(value));
				}
				
				if(symbol.groupEquals("DECIMALLITERAL")) {
					String value = symbol.value();
					symbol.next();
					return new ValExpr(parseDecimal(value));
				}
				
				if(symbol.groupEquals("STRINGLITERAL")) {
					String value = symbol.value();
					symbol.next();
					return new ValExpr(value);
				}
				
				if(symbol.groupEquals("IDENTIFIER")) {
					String value = symbol.value();
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
	
	private int evaluate(Expression expr) {
		// TODO: Global variables....
		
		if(expr instanceof UnExpr) {
			UnExpr e = (UnExpr)expr;
			int a = evaluate(e.a);
			switch(e.operation) {
				case "~": return ~a;
				case "-": return -a;
				case "+": return a;
				case "!": return (a == 0 ? 1:0);
//				case "*": return a * b;
//				case "&": return a / b;
				default: throw new RuntimeException("Invalid unary operation -> '" + e.operation + "'");
			}
		}
		
		if(expr instanceof BiExpr) {
			// +, -, *, /, &, |, ^, %
			BiExpr e = (BiExpr)expr;
			int a = evaluate(e.a);
			int b = evaluate(e.b);
			switch(e.operation) {
				case "+": return a + b;
				case "-": return a - b;
				case "*": return a * b;
				case "/": return a / b;
				case "&": return a & b;
				case "|": return a | b;
				case "^": return a ^ b;
				case "%": return a % b;
				case "==": return (a == b ? 1:0);
				case "!=": return (a != b ? 1:0);
				case "<": return (a < b ? 1:0);
				case "<=": return (a <= b ? 1:0);
				case ">": return (a > b ? 1:0);
				case ">=": return (a >= b ? 1:0);
				case "&&": return (((a != 0) && (b != 0)) ? 1:0);
				case "||": return (((a != 0) || (b != 0)) ? 1:0);
				default: throw new RuntimeException("Invalid operation -> '" + e.operation + "'");
			}
		}
		
		if(expr instanceof TeExpr) {
			TeExpr e = (TeExpr)expr;
			int a = evaluate(e.a);
			int b = evaluate(e.b);
			int c = evaluate(e.c);
			return (a != 0) ? b:c;
		}
		
		if(expr instanceof ValExpr) {
			ValExpr e = (ValExpr)expr;
			
			if(e.value instanceof Number) {
				return ((Number)e.value).intValue();
			}
			
			throw new RuntimeException("Invalid value. You cannot have local variables in compiler time expressions -> '" + expr + "'");
		}
		
		throw new RuntimeException("Invalid expression -> '" + expr + "'");
	}
	
	private class ValExpr implements Expression {
		private Object value;
		
		public ValExpr(Object value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return Objects.toString(value);
		}
		
		public String listnm() { return Objects.toString(value); }
	}
	
	private class IdentifierExpr implements Expression {
		private String name;
		
		public IdentifierExpr(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public String listnm() { return name; }
	}
	
	public Argument createNewArgument(Sym symbol) {
		Argument argument = new Argument();
		argument.type = createNewType(symbol);
		if(!symbol.groupEquals("IDENTIFIER")) throwError(symbol, "Invalid function argument name '" + symbol.value() + "'");
		argument.name = symbol.value();
		symbol.next();
		return argument;
	}
	
	public Type createNewType(Sym symbol) {
		Type type = new Type();
		
		if(!isType(symbol)) throwError(symbol, "Invalid type '" + symbol.toString() + "'");
		type.typeName = symbol.value();
		symbol.next();
		
		while(symbol != null && symbol.valueEquals("*")) {
			type.pointerSize++;
			symbol.next();
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
