package hardcoded.compiler.optimizer;

import static hardcoded.compiler.Expression.AtomType.*;
import static hardcoded.compiler.Expression.ExprType.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hardcoded.compiler.*;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Expression.*;
import hardcoded.compiler.Identifier.ParamIdent;
import hardcoded.compiler.Statement.*;
import hardcoded.compiler.constants.Keywords;
import hardcoded.compiler.constants.Modifiers;
import hardcoded.compiler.constants.Primitives;
import hardcoded.compiler.context.Lang;
import hardcoded.compiler.types.*;
import hardcoded.errors.CompilerException;
import hardcoded.lexer.Tokenizer;
import hardcoded.lexer.TokenizerFactory;
import hardcoded.lexer.TokenizerOld;
import hardcoded.utils.FileUtils;
import hardcoded.utils.StringUtils;

// TODO: Try unrolling recursion.
public class HCompilerInit {
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
	
//	public Program getProgram() {
//		return current_program;
//	}
	
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
		// TODO: Classes and global variables
		
		if(reader.valueEquals("#")) {
			reader.next();
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
				if(!reader.valueEquals(";")) syntaxError("Invalid type syntax. Expected a semicolon but got '%s'", reader);
				reader.next();
				
				if(defined_types.containsKey(name)) syntaxError("Type is already defined '%s'", name);
				defined_types.put(name, new Type(name, type.size(), type.isSigned()));
				
				// System.out.println("#TYPE [" + name + "] as [" + type + "]");
				break;
			}
			case "import": {
				if(!reader.next().groupEquals("STRING")) syntaxError("Invalid import syntax. Expected a string but got '%s'", reader);
				String filename = reader.value().substring(1, reader.value().length() - 1);
				if(!reader.next().valueEquals(";")) syntaxError("Invalid import syntax. Expected a semicolon but got '%s'", reader);
				reader.next();
				
				// System.out.println("#IMPORT [" + filename + "]");
				importFile(filename);
				break;
			}
			case "set": {
				if(!isValidName(reader.next())) syntaxError("Invalid type name. The value '%s' is not a valid type name.", reader);
				String name = reader.value();
				reader.next();
				Expression expr = nextExpression();
				if(!reader.valueEquals(";")) syntaxError("Invalid set syntax. Expected a semicolon but got '%s'", reader);
				reader.next();
				// System.out.println("#SET [" + name + "] as [" + expr + "]");
				
				// TODO: Check that the expression is a compiler value that does not use variables.
				GLOBAL.put(name, expr);
				break;
			}
			case "unset": {
				String name = reader.next().value();
				if(!reader.next().valueEquals(";")) syntaxError("Did you forget a semicolon here?");
				reader.next();
				
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
		if(Modifiers.contains(reader.value())) func.modifier = createNewModifier();
		if(!isType(reader)) syntaxError("Invalid function return type '%s'", reader);
		func.returnType = getTypeFromSymbol();
		
		if(!isValidName(reader)) syntaxError("Invalid function name '%s'", reader);
		
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
		
		if(!reader.next().valueEquals("(")) syntaxError("Invalid function declaration. Did you forget a open bracket here ? '%s'", reader);
		reader.next();
		
		while(!reader.valueEquals(")")) {
			Variable arg = createNewArgument();
			func.arguments.add(new ParamIdent(arg.type, arg.name, func.arguments.size()));
			if(!reader.valueEquals(",")) {
				if(reader.valueEquals(")")) break;
				syntaxError("Invalid function argument separator '%s' did you forget a comma ? ','", reader);
			} else reader.next();
		}
		
		if(!current_program.hasFunction(func.name)) {
			current_program.add(func);
		}
		
		if(!reader.valueEquals(")")) syntaxError("Invalid closing of the funtion arguments. Expected a closing bracket ')'.");
		if(reader.next().valueEquals(";")) {
			if(needsBody) syntaxError("Invalid function declaration. Expected a body.");
			reader.next();
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
						list.list.set(i, new ExprStat(new OpExpr(ExprType.mov, new AtomExpr(ident), var.value())));
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
		
		
		if(reader.valueEquals("break")) {
			if(!reader.next().valueEquals(";")) syntaxError("Invalid break statement. Expected semicolon but got '%s'", reader);
			reader.nextClear();
			return new BreakStat();
		} else if(reader.valueEquals("continue")) {
			if(!reader.next().valueEquals(";")) syntaxError("Invalid continue statement. Expected semicolon but got '%s'", reader);
			reader.nextClear();
			return new ContinueStat();
		} else if(reader.valueEquals("return")) {
			ReturnStat stat = new ReturnStat();
			
			reader.next();
			if(!reader.valueEquals(";")) stat.setValue(nextExpression());
			if(!reader.valueEquals(";")) syntaxError("Invalid return statement. Expected semicolon but got '%s'", reader);
			reader.nextClear();
			return stat;
		}
		
		
		if(reader.valueEquals("{")) {
			return getStatements();
		} else {
			Expression expr = nextExpression();
			if(reader.valueEquals(";")) {
				reader.nextClear();
				return new ExprStat(expr);
			}
			syntaxError("Invalid expression statement. Expected semicolon but got '%s'", reader);
		}
		
		return null;
	}
	
	private Statement makeWhileStatement() {
		if(!reader.next().valueEquals("(")) syntaxError("Invalid while statement definition. Expected open bracket '(' but got '%s'", reader);
		WhileStat stat = new WhileStat();
		reader.next();
		stat.setCondition(nextExpression());
		if(!reader.valueEquals(")")) syntaxError("Invalid while statement definition. Expected close bracket ')' but got '%s'", reader);
		reader.next();
		stat.setBody(nextStatement());
		reader.resetMarked();
		return stat;
	}
	
	private Statement makeForStatement() {
		if(!reader.next().valueEquals("(")) syntaxError("Invalid for statement. Expected open bracket '(' but got '%s'", reader);
		ForStat stat = new ForStat(); {
			reader.next();
			if(!reader.valueEquals(";")) { stat.setVariables(getVariableDefinition()); reader.prev(); }
			if(!reader.valueEquals(";")) syntaxError("Invalid for statement (variables). Expected semicolon but got '%s'", reader);
			reader.next();
			if(!reader.valueEquals(";")) stat.setCondition(nextExpression());
			if(!reader.valueEquals(";")) syntaxError("Invalid for statement (condition). Expected semicolon but got '%s'", reader);
			reader.next();
			if(!reader.valueEquals(")")) stat.setAction(nextExpression());
			if(!reader.valueEquals(")")) syntaxError("Invalid for statement (action). Expected closing bracket ')' but got '%s'", reader);
		}
		reader.next();
		stat.setBody(getStatements());
		reader.resetMarked();
		return stat;
	}
	
	private Statement makeIfStatement() {
		if(!reader.next().valueEquals("(")) syntaxError("Invalid if statement definition. Expected open bracket '(' but got '%s'", reader);
		IfStat stat = new IfStat();
		reader.next();
		stat.setCondition(nextExpression());
		if(!reader.valueEquals(")")) syntaxError("Invalid if statement definition. Expected close bracket ')' but got '%s'", reader);
		reader.next();
		stat.setBody(nextStatement());
		if(reader.valueEquals("else")) {
			reader.next();
			stat.setElseBody(nextStatement());
		}
		reader.resetMarked();
		return stat;
	}
	
	private Statement getVariableDefinition() {
		reader.mark();
		Type type = getTypeFromSymbol();
		
		List<Variable> list = new ArrayList<Variable>();
		do {
			Variable var = new Variable(type);
			list.add(var);
			
			if(!isValidName(reader)) syntaxError("Invalid local variable name '%s'", reader);
			if(current_function.hasIdentifier(reader.value())) syntaxError("Redeclaration of a local variable '%s'", reader);
			var.name = reader.value();
			reader.next();
			
			if(reader.valueEquals("[")) {
				reader.next();
				Expression expr = nextExpression();
				if(!(expr instanceof AtomExpr)) {
					syntaxError("Invalid array variable definition. Expected a integer expression but got '%s'", expr);
				} else {
					AtomExpr number = (AtomExpr)expr;
					if(!number.isNumber()) {
						syntaxError("Invalid array variable definition. Expected a integer expression. But got '%s'", expr);
					}
					
					// TODO: Negative numbers
					
					var.arraySize = (int)number.i_value;
				}
				var.isArray = true;
				
				if(!reader.valueEquals("]")) syntaxError("Invalid array variable definition. Expected array closure ']' but got '%s'", reader);
				reader.next();
				if(!reader.valueEquals(";")) syntaxError("Invalid array variable definition. Expected a semicolon ';' but got '%s'", reader);
				reader.nextClear();
				break;
			}
			
			if(reader.valueEquals(";")) {
				reader.nextClear();
				break;
			} else if(reader.valueEquals("=")) {
				reader.next();
				var.setValue(nextExpression(true));
				
				if(reader.valueEquals(";")) {
					reader.nextClear();
					break;
				} else if(!reader.valueEquals(",")) {
					syntaxError("Invalid variable definition. Expected a comma or semicolon but got '%s'", reader);
				}
				
				reader.next();
				continue;
			}
		} while(true);
		
		return new StatementList(list);
	}
	
	private Statement getStatements() {
		reader.mark();
		if(reader.valueEquals(";")) {
			reader.nextClear();
			return Statement.EMPTY;
		}
		
		if(!reader.valueEquals("{")) {
			Statement stat = nextStatement();
			reader.resetMarked();
			return stat;
		}
		
		NestedStat stat = new NestedStat();
		reader.next();
		for(;;) {
			if(reader.valueEquals(";")) { reader.next(); continue; }
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
		
		
		if(!reader.valueEquals("}")) {
			syntaxError("Invalid statement closing bracket. Expected '}' but got '%s'", reader);
		}
		
		reader.nextClear();
		
		if(stat.list.isEmpty()) return Statement.EMPTY;
		return stat;
	}
	
	private Expression nextExpression() {
		return nextExpression(false);
	}
	
	private Expression nextExpression(boolean skipComma) {
		reader.mark();
		
		Expression expr = new Object() {
			private ExprType[] _e(ExprType... array) { return array; }
			private String[] _s(String... array) { return array; }
			
			Expression e_read(String[] values, ExprType[] exprs, java.util.function.Supplier<Expression> func) { return e_read(values, exprs, func, func); }
			Expression e_read(String[] values, ExprType[] exprs, java.util.function.Supplier<Expression> entry, java.util.function.Supplier<Expression> func) {
				for(Expression expr = entry.get();;) {
					boolean found = false;
					for(int i = 0; i < values.length; i++) {
						if(reader.valueEquals(values[i])) {
							found = true;
							reader.next();
							expr = new OpExpr(exprs[i], expr, func.get());
							break;
						}
					}
					
					if(!found) return expr;
				}
			}
			
			Expression parse() {
				return skipComma ? e14():e15();
			}
			
			Expression e15() { return e_read(_s(","), _e(comma), this::e14); }
			Expression e14() { // Left associative
				Expression e13 = e13();
				
				// If the next expression modifies the assigner then
				//   a temporary variable should get created that holds
				//   the value and adds it in the end.
				
				ExprType type = null;
				switch(reader.value()) {
					case "=": {
						if(!acceptModification(e13)) syntaxError("The expression you are trying to modify is not modifiable.");
						reader.next();
						return new OpExpr(mov, e13, e14());
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
				
				if(type == null) return e13;
				switch(type) {
					case mod: case sub:
					
					case shl: case shr:
					case mul: case div:
					case add: case and:
					case xor: case or: {
						if(!acceptModification(e13)) syntaxError("The expression you are trying to modify is not modifiable.");
						reader.next();
						Expression next = e14();
						if(!next.isPure()) {
							AtomExpr temp = new AtomExpr(current_function.temp(new UndefinedType()));
							
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
			
			Expression e13() {
				Expression e13 = e12();
				if(reader.valueEquals("?")) {
					reader.next();
					Expression b = e12();
					if(!reader.valueEquals(":")) syntaxError("Invalid ternary operation a ? b : c. Missing the colon. '%s'", reader);
					
					// Cannot optimize easyly
					// (e13 && (temp = b, 1) || (temp = c), temp)
					reader.next();
					Expression c = e12();
					
					AtomExpr temp = new AtomExpr(current_function.temp(new UndefinedType()));
					return new OpExpr(
						comma,
						new OpExpr(cor,
							new OpExpr(cand,
								e13,
								new OpExpr(comma, new OpExpr(mov, temp, b), new AtomExpr(1))
							),
							new OpExpr(comma, new OpExpr(mov, temp, c))
						),
						temp
					);
				}
				
				return e13;
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
			Expression e3() { return e_read(_s("*", "/", "%"), _e(mul, div, mod), this::e2); }
			
			Expression e2() {
				String value = reader.value();
				
				switch(value) {
					case "&": { reader.next(); return new OpExpr(addptr, e2()); }
					case "*": { reader.next(); return new OpExpr(decptr, e2()); }
					case "+": { reader.next(); return e1(); }
					
					case "!": { reader.next(); return new OpExpr(not, e1()); }
					case "~": { reader.next(); return new OpExpr(nor, e1()); }
					case "-": { reader.next(); return new OpExpr(neg, e1()); }
					
					// FIXME: Add this later!!!!
					case "++": case "--": {
						reader.next();
						Expression expr = e1();
						if(!acceptModification(expr)) syntaxError("The expression you are trying to modify is not modifiable.");
						
						int direction = value.equals("++") ? 1:-1;
						
						if(!expr.isPure()) {
							AtomExpr ident = new AtomExpr(current_function.temp(new UndefinedType()));
							
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
						if(isType(reader.next())) {
							Type type = getTypeFromSymbol();
							if(type instanceof PrimitiveType) {
								PrimitiveType pt = (PrimitiveType)type;
								
								if(pt.getType() == null) syntaxError("Invalid cast expression. You cannot cast to the type 'void'.");
							}
							
							if(!reader.valueEquals(")")) syntaxError("Invalid cast expression. Expected closing bracket ')' but got '%s'", reader);
							reader.next();
							return new CastExpr(type, e2());
						} else reader.prev();
					}
					
					default: {
						Expression e1 = e1();
						
						// FIXME: Add this later!!!!
						switch(reader.value()) {
							case "++": case "--": {
								if(!acceptModification(e1)) syntaxError("The expression you are trying to modify is not modifiable.");
								int direction = reader.value().equals("++") ? 1:-1;
								reader.next();
								
								AtomExpr ident2 = new AtomExpr(current_function.temp(new UndefinedType()));
								if(!e1.isPure()) {
									AtomExpr ident1 = new AtomExpr(current_function.temp(new UndefinedType()));
									
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
							switch(reader.value()) {
								case "[": {
									reader.next();
									Expression expr = e15();
									if(!reader.valueEquals("]")) syntaxError("Invalid array close character. Expected '[' but got '%s'", reader);
									reader.next();
									
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
									reader.next();
									
									AtomExpr ae = (AtomExpr)e1;
									Function func = FUNCTIONS.get(ae.d_value.name());
									
									int length = func.arguments.size();
									for(int i = 0; i < length; i++) {
										//Variable argument = func.arguments.get(i);
										
										Expression arg = e14();
										if(arg == null) syntaxError("Invalid call argument value.");
										// System.out.println(argument + " /// " + arg + " [" + arg.type() + "]");
										expr.list.add(arg);
										
										if(reader.valueEquals(",")) {
											if(i == length - 1) syntaxError("Too many arguments calling function '%s' expected %s", func.name, length + (length == 1 ? " argument":"arguments"));
											reader.next();
											continue;
										}
										
										if(i != length - 1) syntaxError("Not enough arguments to call function '%s' expected %s but got %d", func.name, length + (length == 1 ? " argument":"arguments"), i + 1);
										
										if(!reader.valueEquals(")")) syntaxError("Invalid inline calling argument. Expected closing bracket ')' but got '%s'", reader);
										reader.next();
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
			
			private long parseLong(String value) {
				if(value.startsWith("0x")) return Long.parseLong(value.substring(2, value.length() - 1), 16);
				else return Long.parseLong(value);
			}
			
			private int parseInteger(String value) {
				if(value.startsWith("0x")) return Integer.parseInt(value.substring(2), 16);
				else return Integer.parseInt(value);
			}
			
			Expression e1() {
				if(reader.groupEquals("DOUBLE") || reader.groupEquals("FLOAT")) syntaxError("Float data types are not implemented in this language."); // TODO: Implement
				if(reader.groupEquals("LONG")) { String value = reader.value(); reader.next(); return new AtomExpr(parseLong(value)); }
				if(reader.groupEquals("INT")) { String value = reader.value(); reader.next(); return new AtomExpr(parseInteger(value)); }
				if(reader.groupEquals("STRING")) { String value = reader.value(); reader.next(); return new AtomExpr(value.substring(1, value.length() - 1)); } // TODO: Unicode ?
				
				if(reader.groupEquals("CHAR")) { // TODO: Unicode
					String value = StringUtils.unescapeString(reader.value().substring(1, reader.value().length() - 1));
					if(value.length() != 1) syntaxError("Invalid char expression. Expected only one character.");
					reader.next();
					
					char c = value.charAt(0);
					return new AtomExpr((byte)(c & 0xff));
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
					
					// TODO: Dead code.
					reader.next();
					return null;
				}
				
				if(reader.valueEquals("(")) {
					reader.next();
					Expression expr = e15();
					if(!reader.valueEquals(")")) syntaxError("Invalid bracket close on expression. Expected close bracket ')' but got '%s'", reader);
					reader.next();
					return expr;
				}
				
				return null;
			}
		}.parse();
		
		reader.resetMarked();
		return expr;
	}
	
	public Variable createNewArgument() {
		Variable variable = new Variable(getTypeFromSymbol());
		if(!reader.groupEquals("IDENTIFIER")) syntaxError("Invalid function argument name '%s'", reader);
		if(current_function.hasIdentifier(reader.value())) syntaxError("Redefinition of a variable named '%s'", reader);
		variable.name = reader.value();
		reader.next();
		return variable;
	}
	
	private boolean acceptModification(Expression expr) {
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			return e.atomType() == ident;
		}
		
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			return e.type == decptr;
		}
		
		return false;
	}
	
	private boolean isType(Lang reader) {
		return defined_types.containsKey(reader.value());
	}
	
	public Type getTypeFromSymbol() {
		if(!isType(reader)) syntaxError("Invalid type '%s'", reader);
		Type type = defined_types.get(reader.value());
		reader.next();
		
		if(reader.valueEquals("*")) {
			int size = 0;
			
			while(reader.valueEquals("*")) {
				size++;
				reader.next();
			}
			
			return new PointerType(type, size);
		}
		
		return type;
	}
	
	public Modifier createNewModifier() {
		Modifier modifier = new Modifier();
		modifier.name = reader.toString();
		reader.next();
		return modifier;
	}

	private boolean isValidName(Lang reader) {
		if(!reader.groupEquals("IDENTIFIER")) return false;
		if(defined_types.containsKey(reader.value())) return false;
		if(GLOBAL.containsKey(reader.value())) return false; // Should only be for globals... 
		if(Modifiers.contains(reader.value())) return false;
		if(Keywords.contains(reader.value())) return false;
		return true;
	}
	
	public void syntaxError(String format, Object... args) {
		StringBuilder message = new StringBuilder();
		message.append("(line:").append(reader.line()).append(", column:").append(reader.column()).append(") ")
			.append(String.format(format, args));
		
		System.err.println(message);
		hasErrors = true;
		throw new CompilerException();
	}
}
