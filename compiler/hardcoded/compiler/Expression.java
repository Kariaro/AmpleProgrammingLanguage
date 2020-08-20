package hardcoded.compiler;

import java.util.*;

import hardcoded.utils.StringUtils;

// TODO: Evaluate expression at compiler time.
public interface Expression extends Stable {
	public static enum ExprType {
		// Atoms
		string,
		int1, int2, int4, int8,
		float4, float8,
		
		// Math
		add,	// x + y
		sub,	// x - y
		div,	// x / y
		mul,	// x * y
		mod,	// x % y      SAME      x - (int)(x / y) * y
		
		xor,	// x ^ y
		and,	// x & y
		or,		// x | y
		shl,	// x << y
		shr,	// x >> y
		
		// Unary
		not,	// !x
		nor,	// ~x
		neg,	// -x
		
		// Compares
		eq,		// x == y
		neq,	// x != y
		gt,		// x > y
		gte,	// x >= y
		lt,		// x < y
		lte,	// x <= y
		cor,	// x || y
		cand,	// x && y
		
		// Memory operations
		mov,	// Move a value y into x
		
		// Pointer
		addptr, // &x
		decptr, // *x
		
		// Function
		// br,	// Branch
		// cbr,	// Conditional branch
		call,	// Call
		ret,	// Call return
		nop,	// No operation
		loop,	// Looping
		
		
		comma,
		invalid, // Invalid expression type
	}
	
	/** ExprType */
	public default ExprType type() {
		return ExprType.invalid;
	}
	
	/** Elements */
	public default List<Expression> elements() {
		return Collections.emptyList();
	}
	
	public default List<List<Expression>> stat_expressionsAll() {
		List<List<Expression>> list = new ArrayList<>();
		List<Expression> exprs = elements();
		if(exprs.size() > 0) {
			list.add(exprs);
			for(Expression e : exprs) list.addAll(e.stat_expressionsAll());
		}
		return list;
	}
	
	public default Expression first() {
		List<Expression> list = elements();
		if(list == null || list.size() < 1) return null;
		return list.get(0);
	}
	
	public default Expression last() {
		List<Expression> list = elements();
		if(list == null || list.size() < 1) return null;
		return list.get(list.size() - 1);
	}
	
	/** This is true if the expression can be reduced while compiling. */
	public default boolean isPure() {
		List<Expression> list = elements();
		if(list != null) {
			for(Expression expr : list) {
				if(!expr.isPure()) return false;
			}
		}
		
		if(type() == null) return false;
		
		switch(type()) {
			case invalid:
			case call:
			case mov:
			case ret:
				return false;
			default: return true;
		}
	}
	
	public static class OperatorExpr implements Expression {
		public List<Expression> list;
		public ExprType type;
		
		public OperatorExpr(ExprType type, Expression... array) {
			this.type = type;
			
			list = new ArrayList<>();
			for(Expression expr : array) {
				list.add(expr);
			}
		}
		
		public OperatorExpr add(Expression expr) {
			list.add(expr);
			return this;
		}
		
		public OperatorExpr set(int index, Expression expr) {
			list.set(index, expr);
			return this;
		}
		
		public Expression get(int index) {
			return list.get(index);
		}
		
		public int size() {
			return list.size();
		}
		
		@Override
		public ExprType type() {
			return type;
		}
		
		@Override
		public List<Expression> elements() {
			return list;
		}
		
		@Override
		public String toString() {
			return type + "(" + StringUtils.join(", ", list) + ")";
		}
		
		public String listnm() { return type.toString(); }
		public Object[] listme() { return list.toArray(); }
	}
	
	// Ternary
	// TODO: Remove with operators.
	@Deprecated
	public static class TeExpr implements Expression {
		public Expression a;
		public String op1;
		public Expression b;
		public String op2;
		public Expression c;
		
		public TeExpr(Expression a, String op1, Expression b, String op2, Expression c) {
			this.a = a;
			this.op1 = op1;
			this.b = b;
			this.op2 = op2;
			this.c = c;
		}
		
		@Override
		public String toString() {
			return a + " " + op1 + " " + b + " " + op2 + " " + c;
		}
	}
	
	public static class CallExpr implements Expression {
		public List<Expression> args;
		public Expression pointer;
		
		public CallExpr() {
			args = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return pointer + "(" + StringUtils.join(", ", args) + ")";
		}
		
		public ExprType getType() {
			return ExprType.call;
		}
		
		public String listnm() { return "CALL [" + pointer + "]"; }
		public Object[] listme() { return args.toArray(); }
	}
	
	public static class CastExpr implements Expression {
		public Type type;
		public Expression a;
		
		public CastExpr(Type type, Expression a) {
			this.type = type;
			this.a = a;
		}
		
		public String toString() { return "(" + type.type() + ")" + a; }
		public String listnm() { return "CAST"; }
		public Object[] listme() { return new Object[] { type, a }; };
	}
	
	public static class NumberExpr implements Expression {
		public ExprType type;
		
		// TODO: Use something like this and then cast...
		public long integer_value;
		public double floating_value;
		
		public NumberExpr(Number value) {
			if(value instanceof Double) type = ExprType.float8;
			if(value instanceof Float) type = ExprType.float4;
			if(value instanceof Long) type = ExprType.int8;
			if(value instanceof Integer) type = ExprType.int4;
			if(value instanceof Short) type = ExprType.int2;
			if(value instanceof Byte) type = ExprType.int1;
			
			if(isFloating()) floating_value = value.doubleValue();
			else integer_value = value.longValue();
		}
		
		public NumberExpr(Number value, ExprType type) {
			this.type = type;
			
			if(isFloating()) floating_value = value.doubleValue();
			else integer_value = value.longValue();
		}
		
		public boolean isFloating() { return type == ExprType.float8 || type == ExprType.float4; }
		public boolean isDouble() { return type == ExprType.float8; }
		public boolean isFloat() { return type == ExprType.float4; }
		public boolean isLong() { return type == ExprType.int8; }
		public boolean isInteger() { return type == ExprType.int4; }
		public boolean isShort() { return type == ExprType.int2; }
		public boolean isByte() { return type == ExprType.int1; }
		
		public boolean isZero() {
			if(isFloating()) return Double.doubleToRawLongBits(floating_value) == 0;
			return integer_value == 0;
		}
		
		public boolean isPure() { return true; }
		
		@Override
		public ExprType type() {
			return type;
		}
		
		public Number value() {
			if(isFloating()) return floating_value;
			return integer_value;
		}
		
		@Override
		public String toString() {
			if(isFloating()) return Double.toString(floating_value);
			return Long.toString(integer_value);
		}
		
		public String listnm() {
			return toString() + ":" + type();
		}
	}
	
	public static class StringExpr implements Expression {
		public String value;
		
		public StringExpr(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return Objects.toString(value);
		}
		
		public ExprType type() { return ExprType.string; }
		public String listnm() { return value + ":" + type(); }
	}
	
	public static class FunctionExpr implements Expression {
		public Function func;
		
		public FunctionExpr(Function func) {
			this.func = func;
		}
		
		@Override
		public String toString() {
			return func.name;
		}
		
		public String listnm() { return func.name; }
	}
	
	public static class IdentifierExpr implements Expression {
		public String name;
		
		public IdentifierExpr(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public String listnm() { return name; }
	}
	
	
	
	
	
	
	
	
	
	
	public static boolean isNumber(Expression expr) {
		return expr instanceof NumberExpr;
	}
	
	public static Expression optimize(Expression expr) {
		if(expr instanceof OperatorExpr) {
			return optimizeTestExpr((OperatorExpr)expr);
		}
		
		if(expr instanceof TeExpr) {
			return optimizeTeExpr((TeExpr)expr);
		}
		
		if(expr instanceof CastExpr) {
			return optimizeCastExpr((CastExpr)expr);
		}
		
		// TODO: Unary
		
		return expr;
	}
	
	public static Expression optimizeTestExpr(OperatorExpr expr) {
		for(int i = 0; i < expr.list.size(); i++) {
			expr.list.set(i, optimize(expr.list.get(i)));
		}
		
		if(canReduce(expr)) return calulateTestExpr(expr);
		if(canHalfReduce(expr)) return calulateHalfTestExpr(expr);
		
		if(expr.size() == 2) {
			Expression av = expr.list.get(0);
			Expression bv = expr.list.get(1);
			boolean an = av instanceof NumberExpr;
			boolean bn = bv instanceof NumberExpr;
			
			NumberExpr a = null;
			Expression b = null;
			if(an && !bn) {
				a = (NumberExpr)av;
				b = bv;
			}
			
			if(bn && !an) {
				a = (NumberExpr)bv;
				b = av;
			}
			
			if(a != null && b != null) {
				switch(expr.type) {
					case add: if(a.value().doubleValue() == 0) return b;
					case sub: if(a.value().doubleValue() == 0) return b;
					default: {
						
					}
				}
			}
		}
		
		return expr;
	}
	
	public static Expression optimizeCastExpr(CastExpr expr) {
		if(expr.type.size() == 0) throw new RuntimeException("Invalid type cast. Cannot cast into a zero byte type '" + expr.type.name() + "'");
		expr.a = optimize(expr.a);
		
		if(expr.a instanceof NumberExpr) {
			NumberExpr a = (NumberExpr)expr.a;
			Type type = expr.type;
			
			switch(type.size()) {
				case 1: {
					if(type.isFloating()) throw new RuntimeException("Invalid");
					return new NumberExpr(a.value().byteValue());
				}
				case 2: {
					if(type.isFloating()) throw new RuntimeException("Invalid");
					return new NumberExpr(a.value().shortValue());
				}
				case 4: {
					if(type.isFloating()) return new NumberExpr(a.value().floatValue());
					return new NumberExpr(a.value().intValue());
				}
				case 8: {
					if(type.isFloating()) return new NumberExpr(a.value().doubleValue());
					return new NumberExpr(a.value().longValue());
				}
				default: {
					throw new RuntimeException("Invalid type size. (" + type.size() + ")");
				}
			}
		}
		
		return expr;
	}
	
	public static Expression optimizeTeExpr(TeExpr expr) {
		expr.a = optimize(expr.a);
		expr.b = optimize(expr.b);
		expr.c = optimize(expr.c);
		
		if(expr.a instanceof NumberExpr) { // a?b:c
			NumberExpr a = (NumberExpr)expr.a;
			if(a.isDouble()) return (a.value().doubleValue() == 0 ? expr.c:expr.b);
			if(a.isFloat()) return (a.value().floatValue() == 0 ? expr.c:expr.b);
			if(a.isLong()) return (a.value().longValue() == 0 ? expr.c:expr.b);
			if(a.isInteger()) return (a.value().intValue() == 0 ? expr.c:expr.b);
		}
		
		return expr;
	}

	public static Expression calulateTestExpr(OperatorExpr expr) {
		NumberExpr a = (NumberExpr)expr.list.get(0);
		NumberExpr b = (NumberExpr)expr.list.get(1);
		
		boolean isDouble = a.isDouble() || b.isDouble();
		boolean isFloat = a.isFloat() || b.isFloat();
		boolean isLong = a.isLong() || b.isLong();
		boolean isInteger = a.isInteger() || b.isInteger();
		boolean isShort = a.isShort() || b.isShort();
		boolean isByte = a.isByte() || b.isByte();
		
		switch(expr.type) {
			case add: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() + b.value().doubleValue());
				if(isFloat) return new NumberExpr(a.value().floatValue() + b.value().floatValue());
				if(isLong) return new NumberExpr(a.value().longValue() + b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() + b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() + b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() + b.value().byteValue());
			}
			case sub: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() - b.value().doubleValue());
				if(isFloat) return new NumberExpr(a.value().floatValue() - b.value().floatValue());
				if(isLong) return new NumberExpr(a.value().longValue() - b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() - b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() - b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() - b.value().byteValue());
			}
			case mul: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() * b.value().doubleValue());
				if(isFloat) return new NumberExpr(a.value().floatValue() * b.value().floatValue());
				if(isLong) return new NumberExpr(a.value().longValue() * b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() * b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() * b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() * b.value().byteValue());
			}
			case div: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() / b.value().doubleValue());
				if(isFloat) return new NumberExpr(a.value().floatValue() / b.value().floatValue());
				if(isLong) return new NumberExpr(a.value().longValue() / b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() / b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() / b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() / b.value().byteValue());
			}
			case mod: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() % b.value().doubleValue());
				if(isFloat) return new NumberExpr(a.value().floatValue() % b.value().floatValue());
				if(isLong) return new NumberExpr(a.value().longValue() % b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() % b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() % b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() % b.value().byteValue());
			}
			
			case xor: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply XOR between floating point values!");
				if(isLong) return new NumberExpr(a.value().longValue() ^ b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() ^ b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() ^ b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() ^ b.value().byteValue());
			}
			case and: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply AND between floating point values!");
				if(isLong) return new NumberExpr(a.value().longValue() & b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() & b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() & b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() & b.value().byteValue());
			}
			case or: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply OR between floating point values!");
				if(isLong) return new NumberExpr(a.value().longValue() | b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() | b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() | b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() | b.value().byteValue());
			}
			case shr: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply SHR between floating point values!");
				if(isLong) return new NumberExpr(a.value().longValue() >> b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() >> b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() >> b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() >> b.value().byteValue());
			}
			case shl: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply SHL between floating point values!");
				if(isLong) return new NumberExpr(a.value().longValue() << b.value().longValue());
				if(isInteger) return new NumberExpr(a.value().intValue() << b.value().intValue());
				if(isShort) return new NumberExpr(a.value().shortValue() << b.value().shortValue());
				if(isByte) return new NumberExpr(a.value().byteValue() << b.value().byteValue());
			}
			
			case gt: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() > b.value().doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value().floatValue() > b.value().floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value().longValue() > b.value().longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value().intValue() > b.value().intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value().shortValue() > b.value().shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value().byteValue() > b.value().byteValue() ? 1:0);
			}
			case gte: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() >= b.value().doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value().floatValue() >= b.value().floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value().longValue() >= b.value().longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value().intValue() >= b.value().intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value().shortValue() >= b.value().shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value().byteValue() >= b.value().byteValue() ? 1:0);
			}
			case lt: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() < b.value().doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value().floatValue() < b.value().floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value().longValue() < b.value().longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value().intValue() < b.value().intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value().shortValue() < b.value().shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value().byteValue() < b.value().byteValue() ? 1:0);
			}
			case lte: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() <= b.value().doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value().floatValue() <= b.value().floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value().longValue() <= b.value().longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value().intValue() <= b.value().intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value().shortValue() <= b.value().shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value().byteValue() <= b.value().byteValue() ? 1:0);
			}
			case eq: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() == b.value().doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value().floatValue() == b.value().floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value().longValue() == b.value().longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value().intValue() == b.value().intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value().shortValue() == b.value().shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value().byteValue() == b.value().byteValue() ? 1:0);
			}
			case neq: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() != b.value().doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value().floatValue() != b.value().floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value().longValue() != b.value().longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value().intValue() != b.value().intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value().shortValue() != b.value().shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value().byteValue() != b.value().byteValue() ? 1:0);
			}
			
			case cor: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() != 0 ? 1:(b.value().doubleValue() != 0 ? 1:0));
				if(isFloat) return new NumberExpr(a.value().floatValue() != 0 ? 1:(b.value().floatValue() != 0 ? 1:0));
				if(isLong) return new NumberExpr(a.value().longValue() != 0 ? 1:(b.value().longValue() != 0 ? 1:0));
				if(isInteger) return new NumberExpr(a.value().intValue() != 0 ? 1:(b.value().intValue() != 0 ? 1:0));
				if(isShort) return new NumberExpr(a.value().shortValue() != 0 ? 1:(b.value().shortValue() != 0 ? 1:0));
				if(isByte) return new NumberExpr(a.value().byteValue() != 0 ? 1:(b.value().byteValue() != 0 ? 1:0));
			}
			case cand: {
				if(isDouble) return new NumberExpr(a.value().doubleValue() != 0 ? (b.value().doubleValue() != 0 ? 1:0):0);
				if(isFloat) return new NumberExpr(a.value().floatValue() != 0 ? (b.value().floatValue() != 0 ? 1:0):0);
				if(isLong) return new NumberExpr(a.value().longValue() != 0 ? (b.value().longValue() != 0 ? 1:0):0);
				if(isInteger) return new NumberExpr(a.value().intValue() != 0 ? (b.value().intValue() != 0 ? 1:0):0);
				if(isShort) return new NumberExpr(a.value().shortValue() != 0 ? (b.value().shortValue() != 0 ? 1:0):0);
				if(isByte) return new NumberExpr(a.value().byteValue() != 0 ? (b.value().byteValue() != 0 ? 1:0):0);
			}
			
			default: {
				
			}
		}
		
		return expr;
	}
	
	public static Expression calulateHalfTestExpr(OperatorExpr expr) {
		Expression a = expr.list.get(0);
		Expression b = expr.list.get(1);
		
//		NumberExpr n;
//		Expression k;
//		if(a instanceof NumberExpr) {
//			n = (NumberExpr)a;
//			k = b;
//		} else {
//			n = (NumberExpr)b;
//			k = a;
//		}
		
		// Reduce All
		//   IDENT + 0         == IDENT
		//   IDENT - 0         == IDENT
		//   IDENT / 1         == IDENT
		//   IDENT * 1         == IDENT
		
		//   IDENT << 0        == IDENT
		//   IDENT >> 0        == IDENT
		//   IDENT ^ 0         == IDENT
		//   IDENT & 0         == 0
		//   IDENT | 0         == IDENT
		// What if the operator changes ?
		
		switch(expr.type) {
//			case add: return n.value.doubleValue() == 0 ? k:expr;
//			case sub: return n.value.doubleValue() == 0 ? k:expr;
//			case mul: {
//				if(n.isDouble()) return (Double.compare(n.value.doubleValue(), 1D) == 0 ? b:expr);
//				if(n.isFloat()) return (Float.compare(n.value.floatValue(), 1F) == 0 ? b:expr);
//				if(n.isLong()) return (n.value.longValue() == 1 ? b:expr);
//				if(n.isInteger()) return (n.value.intValue() == 1 ? b:expr);
//				if(n.isShort()) return (n.value.shortValue() == 1 ? b:expr);
//				if(n.isByte()) return (n.value.byteValue() == 1 ? b:expr);
//				break;
//			}
//			case div: {
//				if(n.isDouble()) return (Double.compare(n.value.doubleValue(), 1D) == 0 ? b:expr);
//				if(n.isFloat()) return (Float.compare(n.value.floatValue(), 1F) == 0 ? b:expr);
//				if(n.isLong()) return (n.value.longValue() == 1 ? b:expr);
//				if(n.isInteger()) return (n.value.intValue() == 1 ? b:expr);
//				if(n.isShort()) return (n.value.shortValue() == 1 ? b:expr);
//				if(n.isByte()) return (n.value.byteValue() == 1 ? b:expr);
//				break;
//			}
//			
//			case shl:
//			case shr:
//			case xor:
//			case or: {
//				if(n.isFloating()) throw new RuntimeException("You cannot apply " + expr.type.toString().toUpperCase() + " between integers and floats!");
//				return n.value.doubleValue() == 0 ? k:expr;
//			}
//			
//			case and: {
//				if(n.isFloating()) throw new RuntimeException("You cannot apply AND between integers and floats!");
//				return n.value.doubleValue() == 0 ? new NumberExpr(0):expr;
//			}
			
			case cor: {
				if(a instanceof NumberExpr) { // Should never encounter this
					NumberExpr A = (NumberExpr)a;
					if(A.isDouble()) return (A.value().doubleValue() != 0 ? new NumberExpr(1):b);
					if(A.isFloat()) return (A.value().floatValue() != 0 ? new NumberExpr(1):b);
					if(A.isLong()) return (A.value().longValue() != 0 ? new NumberExpr(1):b);
					if(A.isInteger()) return (A.value().intValue() != 0 ? new NumberExpr(1):b);
					if(A.isShort()) return (A.value().shortValue() != 0 ? new NumberExpr(1):b);
					if(A.isByte()) return (A.value().byteValue() != 0 ? new NumberExpr(1):b);
				}
				break;
			}
			case cand: {
				if(a instanceof NumberExpr) { // Should never encounter this
					NumberExpr A = (NumberExpr)a;
					if(A.isDouble()) return new NumberExpr(A.value().doubleValue() != 0 ? 1:0);
					if(A.isFloat()) return new NumberExpr(A.value().floatValue() != 0 ? 1:0);
					if(A.isLong()) return new NumberExpr(A.value().longValue() != 0 ? 1:0);
					if(A.isInteger()) return new NumberExpr(A.value().intValue() != 0 ? 1:0);
					if(A.isShort()) return new NumberExpr(A.value().shortValue() != 0 ? 1:0);
					if(A.isByte()) return new NumberExpr(A.value().byteValue() != 0 ? 1:0);
				}
				break;
			}
			
			default: break;
		}
		
		return expr;
	}
	
	public static Expression calulateHalfTestExpr2(OperatorExpr expr) {
		NumberExpr a = (NumberExpr)expr.list.get(0);
		Expression b = expr.list.get(1);
		
		// Reduce All
		//   IDENT + 0         == IDENT
		//   IDENT - 0         == IDENT
		//   IDENT << 0        == IDENT
		//   IDENT >> 0        == IDENT
		//   IDENT ^ 0         == IDENT
		//   IDENT & 0         == 0
		//   IDENT | 0         == IDENT
		//   IDENT / 1         == IDENT
		//   IDENT * 1         == IDENT
		
		
		switch(expr.type) {
			case cor: {
				if(a.isDouble()) return (a.value().doubleValue() != 0 ? new NumberExpr(1):b);
				if(a.isFloat()) return (a.value().floatValue() != 0 ? new NumberExpr(1):b);
				if(a.isLong()) return (a.value().longValue() != 0 ? new NumberExpr(1):b);
				if(a.isInteger()) return (a.value().intValue() != 0 ? new NumberExpr(1):b);
				if(a.isShort()) return (a.value().shortValue() != 0 ? new NumberExpr(1):b);
				if(a.isByte()) return (a.value().byteValue() != 0 ? new NumberExpr(1):b);
			}
			case cand: {
				if(a.isDouble()) return new NumberExpr(a.value().doubleValue() != 0 ? 1:0);
				if(a.isFloat()) return new NumberExpr(a.value().floatValue() != 0 ? 1:0);
				if(a.isLong()) return new NumberExpr(a.value().longValue() != 0 ? 1:0);
				if(a.isInteger()) return new NumberExpr(a.value().intValue() != 0 ? 1:0);
				if(a.isShort()) return new NumberExpr(a.value().shortValue() != 0 ? 1:0);
				if(a.isByte()) return new NumberExpr(a.value().byteValue() != 0 ? 1:0);
			}
			default: {
				
			}
		}
		
		return expr;
	}
	
	public static boolean canReduce(Expression expr) {
		if(expr instanceof OperatorExpr) {
			OperatorExpr e = (OperatorExpr)expr;
			if(e.list.size() == 2) {
				return (e.list.get(0) instanceof NumberExpr) && (e.list.get(1) instanceof NumberExpr);
			}
		}
		
		return false;
	}
	
	public static boolean canHalfReduce(Expression expr) {
		if(expr instanceof OperatorExpr) {
			OperatorExpr e = (OperatorExpr)expr;
			if(e.list.size() == 2) {
				return (e.list.get(0) instanceof NumberExpr);
			}
		}
		
		return false;
	}
	
	public static boolean canHalfReduce2(Expression expr) {
		if(expr instanceof OperatorExpr) {
			OperatorExpr e = (OperatorExpr)expr;
			if(e.list.size() == 2) {
				return (e.list.get(0) instanceof NumberExpr) || (e.list.get(1) instanceof NumberExpr);
			}
		}
		
		return false;
	}
	
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; }
}
