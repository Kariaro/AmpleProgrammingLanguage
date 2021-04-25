package com.hardcoded.compiler.parsetree;

import static com.hardcoded.compiler.api.Expression.Type.*;

import java.util.function.Supplier;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.context.IRefContainer;
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
class AmpleExprParser {
	private int temp_index = 0;
	
	public AmpleExprParser() {
		
	}
	
	@SafeVarargs
	final <T> T[] a(final T... array) { return array; }
	
	Expr for_each_array_varying(String[] values, Expression.Type[] init, Supplier<Expr> next) { return for_each_array_varying(values, init, next, next); }
	Expr for_each_array_varying(String[] values, Expression.Type[] init, Supplier<Expr> first, Supplier<Expr> next) {
		Expr expr = first.get();
		VaryingExpr varying = null;
		while(true) {
			boolean found = false;
			for(int i = 0; i < values.length; i++) {
				if(lang.valueEquals(values[i])) {
					Expression.Type vary_type = init[i];
					found = true;
					lang.next();
					
					if(varying == null) {
						varying = VaryingExpr.get(vary_type, Token.EMPTY);
						varying.add(expr);
						varying.setLocation(expr.getStartOffset(), expr.getStartOffset());
					}
					
					if(vary_type == varying.getType()) {
						varying.add(next.get());
						varying.end(lang.peek(-1));
					} else {
						VaryingExpr old = varying;
						varying = VaryingExpr.get(vary_type, Token.fromOffset(old.getStartOffset()));
						varying.add(old);
						varying.add(next.get());
						varying.end(lang.peek(-1));
					}
					
					break;
				}
			}
			
			if(!found) {
				if(varying != null) return varying;
				return expr;
			}
		}
	}
	
	Expr for_each_array(String[] values, Expression.Type[] init, Supplier<Expr> next) { return for_each_array(values, init, next, next); }
	Expr for_each_array(String[] values, Expression.Type[] init, Supplier<Expr> first, Supplier<Expr> next) {
		Expr expr = first.get();
		
		while(true) {
			boolean found = false;
			for(int i = 0; i < values.length; i++) {
				if(lang.valueEquals(values[i])) {
					found = true;
					Expr tmp = BinaryExpr.get(init[i], Token.EMPTY);
					tmp.setLocation(expr.getStartOffset(), expr.getStartOffset());
					lang.next();
					tmp.add(expr, next.get());
					expr = tmp.end(lang.peek(-1));
					break;
				}
			}
			
			if(!found) return expr;
		}
	}
	
	Expr for_each(String value, Expression.Type init, Supplier<Expr> next) { return for_each(value, init, next, next); }
	Expr for_each(String value, Expression.Type init, Supplier<Expr> first, Supplier<Expr> next) {
		Expr expr = first.get();
		
		if(!lang.valueEquals(value)) return expr.end(lang.peek(-1));
		Expr last = VaryingExpr.get(init, lang.next());
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
		
		if(e instanceof BinaryExpr) {
			BinaryExpr expr = (BinaryExpr)e;
			
			switch(expr.getType()) {
				case COMMA: return acceptModification(expr.last());
				case ARRAY: return true;
				default: return false;
			}
		}
		
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
		return for_each(",", COMMA, this::order_14);
	}
	
	// Left associative
	Expr order_14() {
		Expr lhs = order_13();
		
		if(lang.valueEquals("=")) {
			if(!acceptModification(lhs)) throw_exception("Left hand side is not modifiable '%s'", lhs);
			
			Expr expr = BinaryExpr.get(SET, lang.next());
			expr.add(lhs);
			expr.add(order_14());
			return expr;
		} else {
			Expression.Type expr_type = null;
			
			switch(lang.value()) {
				case "-=": expr_type = SUB; break;
				case "+=": expr_type = ADD; break;
				case "&=": expr_type = AND; break;
				case "^=": expr_type = XOR; break;
				case "|=": expr_type = OR; break;
				
				case "*=": expr_type = MUL; break;
				case "/=": expr_type = DIV; break;
				case "%=": expr_type = MOD; break;
				case "<<=": expr_type = SHL; break;
				case ">>=": expr_type = SHR; break;
			}
			
			if(expr_type == null) return lhs;
			if(!acceptModification(lhs)) throw_exception("Left hand side is not modifiable '%s'", lhs);
			
			Token token = lang.next();
			Expr type = null;
			
			switch(expr_type) {
				case ADD:
				case SUB:
				case AND:
				case XOR:
				case OR: type = VaryingExpr.get(expr_type, token); break;
				
				case MUL:
				case DIV:
				case MOD:
				case SHR:
				case SHL: type = BinaryExpr.get(expr_type, token); break;
				
				default: throw_invalid_exception();
			}
			
			if(lhs.isPure()) {
				type.add(lhs);
				type.add(order_13());
				
				Expr expr = BinaryExpr.get(SET, Token.fromOffset(lhs.getStartOffset()));
				expr.add(lhs);
				expr.add(type);
				return expr;
			}
			
			Token empty = token.empty();
			AtomExpr temp = AtomExpr.get(empty, temp(Reference.Type.VAR));
			
			type.add(BinaryExpr.get(COMMA, empty).add(
				BinaryExpr.get(SET, empty).add(temp, lhs),
				temp
			).add(order_13()));
			
			BinaryExpr expr = BinaryExpr.get(SET, empty);
			expr.add(temp);
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
		
		Token start = Token.fromOffset(a.getStartOffset());
		Token empty = token.empty();
		AtomExpr temp = AtomExpr.get(empty, temp(Reference.Type.VAR));
		return VaryingExpr.get(COMMA, start).add(
			BinaryExpr.get(SET, empty).add(temp, a),
			BinaryExpr.get(COR, empty).add(
				BinaryExpr.get(CAND, empty).add(
					temp,
					VaryingExpr.get(COMMA, empty).add(
						BinaryExpr.get(SET, empty).add(temp, b),
						AtomExpr.get(empty, 1)
					)
				),
				BinaryExpr.get(SET, empty).add(temp, c)
			),
			temp
		).end(lang.peek(-1));
	}
	
	Expr order_12() {
		return for_each("||", COR, this::order_11, this::order_12);
	}
	
	Expr order_11() {
		return for_each("&&", CAND, this::order_10, this::order_11);
	}
	
	Expr order_10() {
		return for_each("|", OR, this::order_9);
	}
	
	Expr order_9() {
		return for_each("^", XOR, this::order_8);
	}
	
	Expr order_8() {
		return for_each("&", AND, this::order_7);
	}
	
	Expr order_7() {
		return for_each_array(a("==", "!="), a(EQ, NEQ), this::order_6, this::order_7);
	}
	
	Expr order_6() {
		return for_each_array(a("<", "<=", ">", ">="), a(LT, LTE, GT, GTE), this::order_5);
	}
	
	Expr order_5() {
		return for_each_array(a("<<", ">>"), a(SHL, SHR), this::order_4);
	}
	
	Expr order_4() {
		return for_each_array_varying(a("+", "-"), a(ADD, SUB), this::order_3);
	}
	
	Expr order_3() {
		return for_each_array(a("*", "/", "%"), a(MUL, DIV, MOD), this::order_2_2);
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
					lang.next();
					
					Expression expr = order_15();
					lhs = BinaryExpr.get(ARRAY, Token.fromOffset(lhs.getStartOffset())).add(lhs, expr).end(lang.token());
					check_or_throw("]");
					continue;
				}
				
				case ".": {
					if(!(lhs instanceof AtomExpr) && !(lhs.getType() == MEMBER)) {
						throw_exception("Left hand side was not a identifier name '%s'", lhs);
					}
					
					lang.next();
					Expression rhs = order_2();
					if(!(rhs instanceof AtomExpr)) {
						throw_exception("Right hand side was not a identifier name '%s'", lhs);
					}
					
					AtomExpr atom = (AtomExpr)rhs;
					atom.setReference(atom.getReference().as(Reference.Type.MEMBER));
					
					Token token = Token.fromOffset(lhs.getStartOffset());
					lhs = BinaryExpr.get(MEMBER, token).add(lhs, rhs).end(lang.peek(-1));
					continue;
				}
				
				case "(": {
					if(!(lhs instanceof IRefContainer)) {
						throw_exception("Left hand side was not a function name '%s'", lhs);
					}
					
					if(lhs instanceof AtomExpr) {
						if(!((AtomExpr)lhs).isReference())
							throw_exception("Left hand side was not a function name '%s'", lhs);
					}
					
					IRefContainer ref = (IRefContainer)lhs;
					ref.setReference(ref.getReference().as(Reference.Type.FUN));
					
					Token token = Token.fromOffset(lhs.getStartOffset());
					VaryingExpr call = VaryingExpr.get(CALL, token);
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
			case "!": { return UnaryExpr.get(NOT, lang.next()).add(order_1()); }
			case "~": { return UnaryExpr.get(NOR, lang.next()).add(order_1()); }
			case "-": { return UnaryExpr.get(NEG, lang.next()).add(order_1()); }
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
