package com.hardcoded.compiler.parsetree;

import java.util.function.Function;
import java.util.function.Supplier;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.context.Reference.Type;
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
	
	Expr for_each_array(String[] values, Function<Token, Expr>[] init, Supplier<Expr> next) { return for_each_array(values, init, next, next); }
	Expr for_each_array(String[] values, Function<Token, Expr>[] init, Supplier<Expr> first, Supplier<Expr> next) {
		Expr expr = first.get();
		
		while(true) {
			boolean found = false;
			for(int i = 0; i < values.length; i++) {
				if(lang.valueEquals(values[i])) {
					found = true;
					Expr tmp = init[i].apply(expr.getToken());
					lang.next();
					tmp.add(expr, next.get());
					expr = tmp.end(lang.peek(-1));
					break;
				}
			}
			
			if(!found) return expr;
		}
	}
	
	Expr for_each(String value, Function<Token, Expr> init, Supplier<Expr> next) { return for_each(value, init, next, next); }
	Expr for_each(String value, Function<Token, Expr> init, Supplier<Expr> first, Supplier<Expr> next) {
		Expr expr = first.get();
		
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
	
	Reference temp(Reference.Type type) {
		return Reference.get(temp_index++);
	}
	
	private Lang lang;
	public Expression begin(Lang lang, boolean use_comma) {
		this.lang = lang;
		
		return parse(use_comma);
	}
	
	Expr parse(boolean use_comma) {
		return use_comma ? order_15():order_14();
	}
	
	Expr order_15() {
		return for_each(",", CommaExpr::get, this::order_14);
	}
	
	// Left associative
	Expr order_14() {
		Expr lhs = order_13();
		
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

			Token empty = type.getToken().empty();
			if(!lhs.isPure()) {
				AtomExpr temp = AtomExpr.get(empty, temp(Reference.Type.VAR));
				
				type.add(CommaExpr.get(empty).add(
					SetExpr.get(empty).add(temp, lhs),
					temp
				).add(order_13()));
				
				SetExpr expr = SetExpr.get(empty);
				expr.add(temp);
				expr.add(type);
				return expr;
			}
			
			type.add(lhs);
			type.add(order_13());
			
			SetExpr expr = SetExpr.get(empty);
			expr.add(lhs);
			expr.add(type);
			return expr;
		}
	}
	
	Expr order_13() {
		Expr a = order_12();
		if(!lang.valueEquals("?")) return a;
		Token token = lang.next();
		// ternary_operator
		Expr b = order_12();
		check_or_throw(":");
		Expr c = order_12();
		
		Token empty = token.empty();
		AtomExpr temp = AtomExpr.get(empty, temp(Reference.Type.VAR));
		return CommaExpr.get(a.getToken()).add(
			CorExpr.get(empty).add(
				CandExpr.get(empty).add(
					a,
					CommaExpr.get(empty).add(
						SetExpr.get(empty).add(temp, b),
						AtomExpr.get(empty, 1)
					)
				),
				CommaExpr.get(empty.empty()).add(
					SetExpr.get(empty).add(temp, c)
				)
			),
			temp
		).end(lang.peek(-1));
	}
	
	Expr order_12() {
		return for_each("||", CorExpr::get, this::order_11, this::order_12);
	}
	
	Expr order_11() {
		return for_each("&&", CandExpr::get, this::order_10, this::order_11);
	}
	
	Expr order_10() {
		return for_each("|", OrExpr::get, this::order_9);
	}
	
	Expr order_9() {
		return for_each("^", XorExpr::get, this::order_8);
	}
	
	Expr order_8() {
		return for_each("&", AndExpr::get, this::order_7);
	}
	
	Expr order_7() {
		return for_each_array(a("==", "!="), a(EqExpr::get, NeqExpr::get), this::order_6, this::order_7);
	}
	
	Expr order_6() {
		return for_each_array(a("<", "<=", ">", ">="), a(LtExpr::get, LteExpr::get, GtExpr::get, GteExpr::get), this::order_5);
	}
	
	Expr order_5() {
		return for_each_array(a("<<", ">>"), a(ShlExpr::get, ShrExpr::get), this::order_4);
	}
	
	Expr order_4() {
		return for_each_array(a("+", "-"), a(AddExpr::get, SubExpr::get), this::order_3);
	}
	
	Expr order_3() {
		return for_each_array(a("*", "/", "%"), a(MulExpr::get, DivExpr::get, ModExpr::get), this::order_2_2);
	}
	
	Expr order_2_2() {
		
		// lhs or rhs
		// ++, --
		
		return order_2_3();
	}
	
	Expr order_2_3() {
		Expr lhs = order_2();
		
		while(true) {
			switch(lang.value()) {
				case "[": {
					Token token = lang.next();
					Expression expr = order_15();
					lhs = ArrayExpr.get(token).add(lhs, expr).end(lang.token());
					check_or_throw("]");
					continue;
				}
				
				case "(": {
					if(!(lhs instanceof AtomExpr)) {
						throw_exception("Left hand side was not a function name '%s'", lhs);
					}
					
					AtomExpr atom = (AtomExpr)lhs;
					if(atom.isReference()) {
						atom.set(atom.getReference().as(Type.FUN));
					}
					
					CallExpr call = CallExpr.get(lhs.getToken());
					call.add(lhs);
					lang.next();
					
					if(lang.valueEquals(")")) {
						return call.end(lang.next());
					}
					
					while(true) {
						Expression arg = parse(false);
						call.add(arg);
						
						if(lang.valueEquals(")")) {
							return call.end(lang.next());
						}
						
						check_or_throw(",");
					}
				}
			}
			
			break;
		}
		
		return lhs;
	}
	
	Expr order_2() {
		String value = lang.value();
		
		switch(value) {
			case "+": { lang.next(); return order_1(); }
			case "!": { return NotExpr.get(lang.next()).add(order_1()); }
			case "~": { return NorExpr.get(lang.next()).add(order_1()); }
			case "-": { return NegExpr.get(lang.next()).add(order_1()); }
		}
		
		return order_1();
	}
	

	Expr order_1() {
		if(lang.groupEquals("NUMBER")) return AtomExpr.get(lang.token(), parseNumber(lang.next().value)).end(lang.peek(-1));
		if(lang.groupEquals("STRING")) {
			Token token = lang.next();
			String value = token.value;
			return AtomExpr.get(token, StringUtils.unescapeString(value.substring(1, value.length() - 1)));
		}
		
		if(lang.groupEquals("CHAR")) {
			Token token = lang.next();
			String value = token.value;
			value = StringUtils.unescapeString(value.substring(1, value.length() - 1));
			if(value.length() != 1) throw_exception("Expected a single char but got '%s'", value);
			return AtomExpr.get(token, value.charAt(0));
		}
		
		if(lang.groupEquals("BOOL")) {
			return AtomExpr.get(lang.token(), Boolean.parseBoolean(lang.next().value) ? 1:0);
		}
		
		if(lang.groupEquals("IDENTIFIER")) return AtomExpr.get(lang.token(), Reference.get(lang.next().value));
		
		if(lang.valueEquals("(")) {
			lang.next();
			Expr expr = order_15();
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
		String contn = String.format("[%s] %s", lang.peekString(-5, 5), lang.peekString(0, 10));
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
