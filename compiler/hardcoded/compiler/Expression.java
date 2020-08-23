package hardcoded.compiler;

import java.util.*;

import hardcoded.utils.StringUtils;

public interface Expression extends Printable {
	public static enum ExprType {
		// Atoms
		string, ident,
		int1, int2, int4, int8,
		float4, float8,
		
		// Math
		add,	// x + y
		
		@Deprecated
		sub,	// x - y
		div,	// x / y
		mul,	// x * y
		
		mod,	// x % y     (If removed it will make optimization harder)
		
		xor,	// x ^ y
		and,	// x & y
		or,		// x | y
		shl,	// x << y
		shr,	// x >> y
		
		// Unary
		not,	// !x  --> same as (eq == 0)
		nor,	// ~x
		neg,	// -x
		
		// Compares
		eq,		// x == y
		//@Deprecated neq,	// x != y  same as (not(eq(x, y)))
		//@Deprecated gt,		// x > y
		//@Deprecated gte,	// x >= y

		lt,		// x < y		SAME as (not(gte(x, y)))
		lte,	// x <= y		SAME as (not(gt(x, y)))
		
		cor,	// x || y
		cand,	// x && y
		
		// Memory operations
		mov,	// Move a value y into x
		
		
		// Pointer
		addptr, // &x
		decptr, // *x
		
		// Function
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
	
	public default boolean hasElements() {
		return false;
	}
	
	public default List<Expression> getElements() {
		return null;
	}
	
	public default Expression first() {
		List<Expression> list = getElements();
		if(list == null || list.size() < 1) return null;
		return list.get(0);
	}
	
	public default Expression last() {
		List<Expression> list = getElements();
		if(list == null || list.size() < 1) return null;
		return list.get(list.size() - 1);
	}
	
	/** This is true if the expression can be reduced while compiling. */
	public default boolean isPure() {
		List<Expression> list = getElements();
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
	
	public static class OpExpr implements Expression {
		public List<Expression> list;
		public ExprType type;
		
		public OpExpr(ExprType type, Expression... array) {
			this.list = new ArrayList<>(Arrays.asList(array));
			this.type = type;
		}
		
		public OpExpr add(Expression expr) {
			list.add(expr);
			return this;
		}
		
		public OpExpr set(int index, Expression expr) {
			list.set(index, expr);
			return this;
		}
		
		public Expression get(int index) {
			return list.get(index);
		}
		
		public int size() {
			return list.size();
		}
		
		public ExprType type() { return type; }
		public boolean hasElements() { return true; }
		public List<Expression> getElements() { return list; }
		
		public String listnm() { return type.toString(); }
		public Object[] listme() { return list.toArray(); }
		public String toString() { return type + "(" + StringUtils.join(", ", list) + ")"; }
	}

	public static class CastExpr implements Expression {
		public List<Expression> list = new ArrayList<>();
		public Type type;
		
		public CastExpr(Type type, Expression a) {
			list.add(a);
			this.type = type;
		}
		
		public void setValue(Expression expr) {
			list.set(0, expr);
		}
		
		public Expression value() {
			return list.get(0);
		}
		
		public boolean hasElements() { return true; }
		public List<Expression> getElements() { return list; }
		
		public String toString() { return "(" + type.type() + ")" + value(); }
		public String listnm() { return "CAST"; }
		public Object[] listme() { return new Object[] { type, value() }; };
	}
	
	public static class AtomExpr implements Expression {
		public ExprType atomType;
		
		public Identifier d_value; // ident
		public String s_value; // string
		public double f_value; // float8, float4
		public long i_value;  // int8, int4, int2, int1
		
		public AtomExpr(double value) {
			this.atomType = ExprType.float8;
			this.f_value = value;
		}
		
		public AtomExpr(float value) {
			this.atomType = ExprType.float4;
			this.f_value = value;
		}
		
		public AtomExpr(long value) {
			this.atomType = ExprType.int8;
			this.i_value = value;
		}
		
		public AtomExpr(int value) {
			this.atomType = ExprType.int4;
			this.i_value = Integer.toUnsignedLong(value);
		}
		
		public AtomExpr(short value) {
			this.atomType = ExprType.int2;
			this.i_value = Short.toUnsignedLong(value);
		}
		
		public AtomExpr(byte value) {
			this.atomType = ExprType.int1;
			this.i_value = Byte.toUnsignedLong(value);
		}
		
		public AtomExpr(Identifier value) {
			this.atomType = ExprType.ident;
			this.d_value = value;
		}

		public AtomExpr(String value) {
			this.atomType = ExprType.string;
			this.s_value = value;
		}
		
		public AtomExpr(Number value, ExprType type) {
			this.atomType = type;
			
			if(isFloating()) f_value = value.doubleValue();
			else i_value = value.longValue();
		}
		
		public boolean isNumber() {
			return isFloating() ||
				atomType == ExprType.int8 ||
				atomType == ExprType.int4 ||
				atomType == ExprType.int2 ||
				atomType == ExprType.int1;
		}
		
		public AtomExpr convert(ExprType type) {
			if(!isNumber()) return null; // Invalid
			
			if(isFloating()) {
				if(type == ExprType.float8) return new AtomExpr((double)f_value);
				if(type == ExprType.float4) return new AtomExpr((float)f_value);
				if(type == ExprType.int8) return new AtomExpr((long)f_value);
				if(type == ExprType.int4) return new AtomExpr((int)f_value);
				if(type == ExprType.int2) return new AtomExpr((short)f_value);
				if(type == ExprType.int1) return new AtomExpr((byte)f_value);
			} else {
				if(type == ExprType.float8) return new AtomExpr((double)i_value);
				if(type == ExprType.float4) return new AtomExpr((float)i_value);
				if(type == ExprType.int8) return new AtomExpr((long)i_value);
				if(type == ExprType.int4) return new AtomExpr((int)i_value);
				if(type == ExprType.int2) return new AtomExpr((short)i_value);
				if(type == ExprType.int1) return new AtomExpr((byte)i_value);
			}
			
			throw new RuntimeException("Invalid type cast '" + type + "'");
		}
		
		public boolean isFloating() {
			return atomType == ExprType.float8 ||
				   atomType == ExprType.float4;
		}
		
		public boolean isIntegern() {
			return isNumber() && (!isFloating());
		}
		
		public boolean isDouble() { return atomType == ExprType.float8; }
		public boolean isFloat() { return atomType == ExprType.float4; }
		public boolean isLong() { return atomType == ExprType.int8; }
		public boolean isInteger() { return atomType == ExprType.int4; }
		public boolean isShort() { return atomType == ExprType.int2; }
		public boolean isByte() { return atomType == ExprType.int1; }
		
		// Only for numbers
		public boolean isZero() {
			if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
			
			if(isFloating()) return Double.doubleToRawLongBits(f_value) == 0;
			return i_value == 0;
		}
		
		public boolean isOne() {
			if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
			
			switch(atomType) {
				case float8: return Double.compare(f_value, 1D) == 0;
				case float4: return Float.compare((float)f_value, 1F) == 0;
				case int8: return i_value == 1;
				case int4: return i_value == 1;
				case int2: return i_value == 1;
				case int1: return i_value == 1;
				default: throw new RuntimeException("You cannot check non numbers if they are one.");
			}
		}
		
		public boolean isPure() { return true; }
		public ExprType type() { return atomType; }
		
		public Number value() {
			if(isFloating()) return f_value;
			return i_value;
		}
		
		public String listnm() { return toString() + ":" + type(); }
		
		public String toString() {
			switch(atomType) {
				case string: return '\"' + s_value + '\"';
				case ident: return d_value.name();
				case float8: return Double.toString(f_value) + 'D';
				case float4: return Float.toString((float)f_value) + 'F';
				case int8: return Long.toString(i_value) + 'L';
				case int4: return Integer.toString((int)i_value);
				case int2: return Short.toString((short)i_value);
				case int1: return Byte.toString((byte)i_value);
				default: throw new RuntimeException("Invalid atom type '" + atomType + "'");
			}
		}
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
		

		// TODO: Return list here!
		
		@Override
		public String toString() {
			return a + " " + op1 + " " + b + " " + op2 + " " + c;
		}
	}
	
	
	
	
	
	
	
	
	
	public static boolean isNumber(Expression expr) {
		return expr instanceof AtomExpr;
	}
	
	@Deprecated
	public static Expression optimize_DO_NOT_USE(Expression expr) {
		if(expr instanceof OpExpr) {
			return optimizeTestExpr((OpExpr)expr);
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
	
	public static Expression optimizeTestExpr(OpExpr expr) {
		for(int i = 0; i < expr.list.size(); i++) {
			expr.list.set(i, optimize_DO_NOT_USE(expr.list.get(i)));
		}
		
		if(canReduce(expr)) return calulateTestExpr(expr);
		if(canHalfReduce(expr)) return calulateHalfTestExpr(expr);
		
		if(expr.size() == 2) {
			Expression av = expr.list.get(0);
			Expression bv = expr.list.get(1);
			boolean an = av instanceof AtomExpr;
			boolean bn = bv instanceof AtomExpr;
			
			AtomExpr a = null;
			Expression b = null;
			if(an && !bn) {
				a = (AtomExpr)av;
				b = bv;
			}
			
			if(bn && !an) {
				a = (AtomExpr)bv;
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
		expr.setValue(optimize_DO_NOT_USE(expr.value()));
		
		if(expr.value() instanceof AtomExpr) {
			AtomExpr a = (AtomExpr)expr.value();
			Type type = expr.type;
			
			switch(type.size()) {
				case 1: {
					if(type.isFloating()) throw new RuntimeException("Invalid");
					return new AtomExpr(a.value().byteValue());
				}
				case 2: {
					if(type.isFloating()) throw new RuntimeException("Invalid");
					return new AtomExpr(a.value().shortValue());
				}
				case 4: {
					if(type.isFloating()) return new AtomExpr(a.value().floatValue());
					return new AtomExpr(a.value().intValue());
				}
				case 8: {
					if(type.isFloating()) return new AtomExpr(a.value().doubleValue());
					return new AtomExpr(a.value().longValue());
				}
				default: {
					throw new RuntimeException("Invalid type size. (" + type.size() + ")");
				}
			}
		}
		
		return expr;
	}
	
	public static Expression optimizeTeExpr(TeExpr expr) {
		expr.a = optimize_DO_NOT_USE(expr.a);
		expr.b = optimize_DO_NOT_USE(expr.b);
		expr.c = optimize_DO_NOT_USE(expr.c);
		
		if(expr.a instanceof AtomExpr) { // a?b:c
			AtomExpr a = (AtomExpr)expr.a;
			if(a.isDouble()) return (a.value().doubleValue() == 0 ? expr.c:expr.b);
			if(a.isFloat()) return (a.value().floatValue() == 0 ? expr.c:expr.b);
			if(a.isLong()) return (a.value().longValue() == 0 ? expr.c:expr.b);
			if(a.isInteger()) return (a.value().intValue() == 0 ? expr.c:expr.b);
		}
		
		return expr;
	}

	public static Expression calulateTestExpr(OpExpr expr) {
		AtomExpr a = (AtomExpr)expr.list.get(0);
		AtomExpr b = (AtomExpr)expr.list.get(1);
		
		boolean isDouble = a.isDouble() || b.isDouble();
		boolean isFloat = a.isFloat() || b.isFloat();
		boolean isLong = a.isLong() || b.isLong();
		boolean isInteger = a.isInteger() || b.isInteger();
		boolean isShort = a.isShort() || b.isShort();
		boolean isByte = a.isByte() || b.isByte();
		
		switch(expr.type) {
			case add: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() + b.value().doubleValue());
				if(isFloat) return new AtomExpr(a.value().floatValue() + b.value().floatValue());
				if(isLong) return new AtomExpr(a.value().longValue() + b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() + b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() + b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() + b.value().byteValue());
			}
			case sub: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() - b.value().doubleValue());
				if(isFloat) return new AtomExpr(a.value().floatValue() - b.value().floatValue());
				if(isLong) return new AtomExpr(a.value().longValue() - b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() - b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() - b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() - b.value().byteValue());
			}
			case mul: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() * b.value().doubleValue());
				if(isFloat) return new AtomExpr(a.value().floatValue() * b.value().floatValue());
				if(isLong) return new AtomExpr(a.value().longValue() * b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() * b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() * b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() * b.value().byteValue());
			}
			case div: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() / b.value().doubleValue());
				if(isFloat) return new AtomExpr(a.value().floatValue() / b.value().floatValue());
				if(isLong) return new AtomExpr(a.value().longValue() / b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() / b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() / b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() / b.value().byteValue());
			}
			case mod: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() % b.value().doubleValue());
				if(isFloat) return new AtomExpr(a.value().floatValue() % b.value().floatValue());
				if(isLong) return new AtomExpr(a.value().longValue() % b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() % b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() % b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() % b.value().byteValue());
			}
			
			case xor: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply XOR between floating point values!");
				if(isLong) return new AtomExpr(a.value().longValue() ^ b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() ^ b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() ^ b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() ^ b.value().byteValue());
			}
			case and: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply AND between floating point values!");
				if(isLong) return new AtomExpr(a.value().longValue() & b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() & b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() & b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() & b.value().byteValue());
			}
			case or: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply OR between floating point values!");
				if(isLong) return new AtomExpr(a.value().longValue() | b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() | b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() | b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() | b.value().byteValue());
			}
			case shr: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply SHR between floating point values!");
				if(isLong) return new AtomExpr(a.value().longValue() >> b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() >> b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() >> b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() >> b.value().byteValue());
			}
			case shl: {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply SHL between floating point values!");
				if(isLong) return new AtomExpr(a.value().longValue() << b.value().longValue());
				if(isInteger) return new AtomExpr(a.value().intValue() << b.value().intValue());
				if(isShort) return new AtomExpr(a.value().shortValue() << b.value().shortValue());
				if(isByte) return new AtomExpr(a.value().byteValue() << b.value().byteValue());
			}
			
			case lt: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() < b.value().doubleValue() ? 1:0);
				if(isFloat) return new AtomExpr(a.value().floatValue() < b.value().floatValue() ? 1:0);
				if(isLong) return new AtomExpr(a.value().longValue() < b.value().longValue() ? 1:0);
				if(isInteger) return new AtomExpr(a.value().intValue() < b.value().intValue() ? 1:0);
				if(isShort) return new AtomExpr(a.value().shortValue() < b.value().shortValue() ? 1:0);
				if(isByte) return new AtomExpr(a.value().byteValue() < b.value().byteValue() ? 1:0);
			}
			case lte: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() <= b.value().doubleValue() ? 1:0);
				if(isFloat) return new AtomExpr(a.value().floatValue() <= b.value().floatValue() ? 1:0);
				if(isLong) return new AtomExpr(a.value().longValue() <= b.value().longValue() ? 1:0);
				if(isInteger) return new AtomExpr(a.value().intValue() <= b.value().intValue() ? 1:0);
				if(isShort) return new AtomExpr(a.value().shortValue() <= b.value().shortValue() ? 1:0);
				if(isByte) return new AtomExpr(a.value().byteValue() <= b.value().byteValue() ? 1:0);
			}
			case eq: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() == b.value().doubleValue() ? 1:0);
				if(isFloat) return new AtomExpr(a.value().floatValue() == b.value().floatValue() ? 1:0);
				if(isLong) return new AtomExpr(a.value().longValue() == b.value().longValue() ? 1:0);
				if(isInteger) return new AtomExpr(a.value().intValue() == b.value().intValue() ? 1:0);
				if(isShort) return new AtomExpr(a.value().shortValue() == b.value().shortValue() ? 1:0);
				if(isByte) return new AtomExpr(a.value().byteValue() == b.value().byteValue() ? 1:0);
			}
			
			case cor: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() != 0 ? 1:(b.value().doubleValue() != 0 ? 1:0));
				if(isFloat) return new AtomExpr(a.value().floatValue() != 0 ? 1:(b.value().floatValue() != 0 ? 1:0));
				if(isLong) return new AtomExpr(a.value().longValue() != 0 ? 1:(b.value().longValue() != 0 ? 1:0));
				if(isInteger) return new AtomExpr(a.value().intValue() != 0 ? 1:(b.value().intValue() != 0 ? 1:0));
				if(isShort) return new AtomExpr(a.value().shortValue() != 0 ? 1:(b.value().shortValue() != 0 ? 1:0));
				if(isByte) return new AtomExpr(a.value().byteValue() != 0 ? 1:(b.value().byteValue() != 0 ? 1:0));
			}
			case cand: {
				if(isDouble) return new AtomExpr(a.value().doubleValue() != 0 ? (b.value().doubleValue() != 0 ? 1:0):0);
				if(isFloat) return new AtomExpr(a.value().floatValue() != 0 ? (b.value().floatValue() != 0 ? 1:0):0);
				if(isLong) return new AtomExpr(a.value().longValue() != 0 ? (b.value().longValue() != 0 ? 1:0):0);
				if(isInteger) return new AtomExpr(a.value().intValue() != 0 ? (b.value().intValue() != 0 ? 1:0):0);
				if(isShort) return new AtomExpr(a.value().shortValue() != 0 ? (b.value().shortValue() != 0 ? 1:0):0);
				if(isByte) return new AtomExpr(a.value().byteValue() != 0 ? (b.value().byteValue() != 0 ? 1:0):0);
			}
			
			default: {
				
			}
		}
		
		return expr;
	}
	
	public static Expression calulateHalfTestExpr(OpExpr expr) {
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
				if(a instanceof AtomExpr) { // Should never encounter this
					AtomExpr A = (AtomExpr)a;
					if(A.isDouble()) return (A.value().doubleValue() != 0 ? new AtomExpr(1):b);
					if(A.isFloat()) return (A.value().floatValue() != 0 ? new AtomExpr(1):b);
					if(A.isLong()) return (A.value().longValue() != 0 ? new AtomExpr(1):b);
					if(A.isInteger()) return (A.value().intValue() != 0 ? new AtomExpr(1):b);
					if(A.isShort()) return (A.value().shortValue() != 0 ? new AtomExpr(1):b);
					if(A.isByte()) return (A.value().byteValue() != 0 ? new AtomExpr(1):b);
				}
				break;
			}
			case cand: {
				if(a instanceof AtomExpr) { // Should never encounter this
					AtomExpr A = (AtomExpr)a;
					if(A.isDouble()) return new AtomExpr(A.value().doubleValue() != 0 ? 1:0);
					if(A.isFloat()) return new AtomExpr(A.value().floatValue() != 0 ? 1:0);
					if(A.isLong()) return new AtomExpr(A.value().longValue() != 0 ? 1:0);
					if(A.isInteger()) return new AtomExpr(A.value().intValue() != 0 ? 1:0);
					if(A.isShort()) return new AtomExpr(A.value().shortValue() != 0 ? 1:0);
					if(A.isByte()) return new AtomExpr(A.value().byteValue() != 0 ? 1:0);
				}
				break;
			}
			
			default: break;
		}
		
		return expr;
	}
	
	public static Expression calulateHalfTestExpr2(OpExpr expr) {
		AtomExpr a = (AtomExpr)expr.list.get(0);
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
				if(a.isDouble()) return (a.value().doubleValue() != 0 ? new AtomExpr(1):b);
				if(a.isFloat()) return (a.value().floatValue() != 0 ? new AtomExpr(1):b);
				if(a.isLong()) return (a.value().longValue() != 0 ? new AtomExpr(1):b);
				if(a.isInteger()) return (a.value().intValue() != 0 ? new AtomExpr(1):b);
				if(a.isShort()) return (a.value().shortValue() != 0 ? new AtomExpr(1):b);
				if(a.isByte()) return (a.value().byteValue() != 0 ? new AtomExpr(1):b);
			}
			case cand: {
				if(a.isDouble()) return new AtomExpr(a.value().doubleValue() != 0 ? 1:0);
				if(a.isFloat()) return new AtomExpr(a.value().floatValue() != 0 ? 1:0);
				if(a.isLong()) return new AtomExpr(a.value().longValue() != 0 ? 1:0);
				if(a.isInteger()) return new AtomExpr(a.value().intValue() != 0 ? 1:0);
				if(a.isShort()) return new AtomExpr(a.value().shortValue() != 0 ? 1:0);
				if(a.isByte()) return new AtomExpr(a.value().byteValue() != 0 ? 1:0);
			}
			default: {
				
			}
		}
		
		return expr;
	}
	
	public static boolean canReduce(Expression expr) {
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			if(e.list.size() == 2) {
				return (e.list.get(0) instanceof AtomExpr) && (e.list.get(1) instanceof AtomExpr);
			}
		}
		
		return false;
	}
	
	public static boolean canHalfReduce(Expression expr) {
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			if(e.list.size() == 2) {
				return (e.list.get(0) instanceof AtomExpr);
			}
		}
		
		return false;
	}
	
	public static boolean canHalfReduce2(Expression expr) {
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			if(e.list.size() == 2) {
				return (e.list.get(0) instanceof AtomExpr) || (e.list.get(1) instanceof AtomExpr);
			}
		}
		
		return false;
	}
	
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; }
}
