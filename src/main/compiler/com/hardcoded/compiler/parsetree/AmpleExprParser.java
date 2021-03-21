package com.hardcoded.compiler.parsetree;

import java.util.function.Function;
import java.util.function.Supplier;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.expression.*;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.compiler.lexer.Token;
import com.hardcoded.utils.StringUtils;

/**
 * A recursive decent parser for expressions
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleExprParser {
	private int temp_index = 0;
	
	public AmpleExprParser() {
		
	}
	
	@SafeVarargs
	final <T> T[] a(final T... array) { return array; }
	
	<T extends Expression> Expression for_each_array(String[] values, Function<Token, Expr>[] init, Supplier<Expression> next) { return for_each_array(values, init, next, next); }
	<T extends Expression> Expression for_each_array(String[] values, Function<Token, Expr>[] init, Supplier<Expression> first, Supplier<Expression> next) {
		Expression expr = first.get();
		
		while(true) {
			boolean found = false;
			for(int i = 0; i < values.length; i++) {
				if(lang.valueEquals(values[i])) {
					found = true;
					expr = init[i].apply(lang.next()).add(expr, next.get());
					break;
				}
			}
			
			if(!found) return expr;
		}
	}
	
	<T extends Expression> Expression for_each(String value, Function<Token, Expr> init, Supplier<Expression> next) { return for_each(value, init, next, next); }
	<T extends Expression> Expression for_each(String value, Function<Token, Expr> init, Supplier<Expression> first, Supplier<Expression> next) {
		Expression expr = first.get();
		
		if(!lang.valueEquals(value)) return expr;
		Expr last = init.apply(lang.next());
		last.add(expr);
		
		while(true) {
			int start_index = lang.readerIndex();
			
			last.add(next.get());
			if(!lang.valueEquals(value)) return last;
			lang.next();
			
			if(start_index == lang.readerIndex()) {
				throw_stuck_exception();
			}
		}
	}
	
	
	boolean acceptModification(Expression e) {
		if(e instanceof AtomExpr) {
			AtomExpr atom = (AtomExpr)e;
			if(atom.isReference()) return true;
			return false;
		}
		
		if(e instanceof CommaExpr) {
			return acceptModification(((CommaExpr)e).last());
		}
		
		if(e instanceof ArrayExpr) return true;
		
		return false;
	}
	
	Reference temp() {
		return Reference.get(temp_index++);
	}
	
	private Lang lang;
	public Expression begin(Lang lang, boolean use_comma) {
		this.lang = lang;
		
		return parse(use_comma);
	}
	
	Expression parse(boolean use_comma) {
		return use_comma ? order_15():order_14();
	}
	
	Expression order_15() {
		return for_each(",", CommaExpr::get, this::order_14);
	}
	
	// Left associative
	Expression order_14() {
		Expression lhs = order_13();
		
		if(lang.valueEquals("=")) {
			Expr expr = SetExpr.get(lang.next());
			expr.add(lhs);
			expr.add(order_14());
			return expr;
		} else {
			Expr type = null;
			
			switch(lang.value()) {
				case "-=": type = SubExpr.get(lang.next()); break;
				case "+=": type = AddExpr.get(lang.next()); break;
				case "*=": type = MulExpr.get(lang.next()); break;
				case "/=": type = DivExpr.get(lang.next()); break;
				case "%=": type = ModExpr.get(lang.next()); break;
				case "&=": type = AndExpr.get(lang.next()); break;
				case "^=": type = XorExpr.get(lang.next()); break;
				case "|=": type = OrExpr.get(lang.next()); break;
				case "<<=": type = ShlExpr.get(lang.next()); break;
				case ">>=": type = ShrExpr.get(lang.next()); break;
			}
			
			if(type == null) return lhs;
			if(!acceptModification(lhs)) throw_exception("Left hand side is not modifiable '%s'", lhs);
			
			if(!lhs.isPure()) {
				AtomExpr temp = AtomExpr.get(temp());
				
				type.add(CommaExpr.get(type.getToken()).add(
					SetExpr.get(type.getToken()).add(temp, lhs),
					temp
				).add(order_13()));
				
				SetExpr expr = SetExpr.get(type.getToken());
				expr.add(temp);
				expr.add(type);
				return expr;
			}
			
			type.add(lhs);
			type.add(order_13());
			
			SetExpr expr = SetExpr.get(type.getToken());
			expr.add(lhs);
			expr.add(type);
			return expr;
		}
	}
	
	Expression order_13() {
		Expression a = order_12();
		if(!lang.valueEquals("?")) return a;
		Token token = lang.next();
		// ternary_operator
		Expression b = order_12();
		check_or_throw(":");
		Expression c = order_12();
		
		AtomExpr temp = AtomExpr.get(temp());
		
		return CommaExpr.get(token).add(
			CorExpr.get(token).add(
				CandExpr.get(token).add(
					a,
					CommaExpr.get(token).add(
						SetExpr.get(token).add(temp, b),
						AtomExpr.get(1)
					)
				),
				CommaExpr.get(token).add(
					SetExpr.get(token).add(temp, c)
				)
			),
			temp
		);
	}
	
	Expression order_12() {
		return for_each("||", CorExpr::get, this::order_11, this::order_12);
	}
	
	Expression order_11() {
		return for_each("&&", CandExpr::get, this::order_10, this::order_11);
	}
	
	Expression order_10() {
		return for_each("|", OrExpr::get, this::order_9);
	}
	
	Expression order_9() {
		return for_each("^", XorExpr::get, this::order_8);
	}
	
	Expression order_8() {
		return for_each("&", AndExpr::get, this::order_7);
	}
	
	Expression order_7() {
		return for_each_array(a("==", "!="), a(EqExpr::get, NeqExpr::get), this::order_6, this::order_7);
	}
	
	Expression order_6() {
		return for_each_array(a("<", "<=", ">", ">="), a(LtExpr::get, LteExpr::get, GtExpr::get, GteExpr::get), this::order_5);
	}
	
	Expression order_5() {
		return for_each_array(a("<<", ">>"), a(ShlExpr::get, ShrExpr::get), this::order_4);
	}
	
	Expression order_4() {
		return for_each_array(a("+", "-"), a(AddExpr::get, SubExpr::get), this::order_3);
	}
	
	Expression order_3() {
		return for_each_array(a("*", "/", "%"), a(MulExpr::get, DivExpr::get, ModExpr::get), this::order_2_2);
	}
	
	Expression order_2_2() {
		
		// lhs or rhs
		// ++, --
		
		return order_2_3();
	}
	
	Expression order_2_3() {
		Expression lhs = order_2();
		
		while(true) {
			switch(lang.value()) {
				case "[": {
					Token token = lang.next();
					Expression expr = order_15();
					check_or_throw("]");
					
					lhs = ArrayExpr.get(token).add(lhs, expr);
					continue;
				}
				
				case "(": {
					if(!(lhs instanceof AtomExpr)) {
						throw_exception("Left hand side was not a function name '%s'", lhs);
					}
					
					CallExpr call = CallExpr.get(lang.next());
					call.add(lhs);
					
					if(lang.valueEquals(")")) {
						lang.next();
						return call;
					}
					
					while(true) {
						Expression arg = parse(false);
						call.add(arg);
						
						if(lang.valueEquals(")")) {
							lang.next();
							return call;
						}
						
						check_or_throw(",");
					}
				}
			}
			
			break;
		}
		
		return lhs;
	}
	
	Expression order_2() {
		String value = lang.value();
		
		switch(value) {
			case "+": { lang.next(); return order_1(); }
			case "!": { return NotExpr.get(lang.next()).add(order_1()); }
			case "~": { return NorExpr.get(lang.next()).add(order_1()); }
			case "-": { return NegExpr.get(lang.next()).add(order_1()); }
		}
		
		return order_1();
	}
	

	Expression order_1() {
		if(lang.groupEquals("NUMBER")) return AtomExpr.get(parseNumber(lang.next().value));
		if(lang.groupEquals("STRING")) {
			String value = lang.next().value;
			return AtomExpr.get(StringUtils.unescapeString(value.substring(1, value.length() - 1)));
		}
		
		if(lang.groupEquals("CHAR")) {
			String value = lang.next().value;
			value = StringUtils.unescapeString(value.substring(1, value.length() - 1));
			if(value.length() != 1) throw_exception("Expected a single char but got '%s'", value);
			return AtomExpr.get(value.charAt(0));
		}
		
		if(lang.groupEquals("IDENTIFIER")) return AtomExpr.get(Reference.get(lang.next().value));
		
		if(lang.valueEquals("(")) {
			lang.next();
			Expression expr = order_15();
			check_or_throw(")");
			return expr;
		}
		
		return throw_invalid_exception();
	}
	
	double parseNumber(String string) {
		if(string.startsWith("0x")) {
			return Long.parseLong(string.substring(2), 16);
		}
		
		return Double.parseDouble(string);
	}
	
	/*
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
				 *
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
	*/
	
	<T> T throw_exception(String format, Object... args) {
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + format, args);
	}
	
	<T> T throw_invalid_exception() {
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + "Invalid syntax:%d", getLineIndex());
	}
	
	<T> T throw_stuck_exception() {
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		String contn = String.format("[%s] %s", lang.peakString(-5, 5), lang.peakString(0, 10));
		throw new ParseTreeException(extra + "Compiler got stuck on line:%d\n%s", getLineIndex(), contn);
	}
	
	<T> T check_or_throw(String value) {
		if(lang.valueEquals(value)) {
			lang.next();
			return null;
		}
		
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + "Expected '%s' but got '%s'", value, lang.value());
	}
	
	int getLineIndex() {
		StackTraceElement[] stack = Thread.getAllStackTraces().get(Thread.currentThread());
		if(stack == null) return -1;
		StackTraceElement last = stack[stack.length - 3];
		return last.getLineNumber();
	}
}
