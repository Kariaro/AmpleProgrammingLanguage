package hardcoded.compiler;

import java.util.*;

import hardcoded.compiler.constants.Printable;
import hardcoded.utils.StringUtils;

public interface Expression extends Printable {
	public static enum AtomType {
		int8, int4, int2, int1,
		string,
		ident,
	}
	
	public static enum ExprType {
		// Math
		add,	// x + y
		sub,	// x - y
		
		div,	// x / y
		mul,	// x * y
		mod,	// x % y
		
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
		call,	// Call
		ret,	// Call return
		nop,	// No operation
		loop,	// Looping
		
		atom,	// Atom Value
		cast,	// Cast Type
		comma,
		invalid, // Invalid expression type
	}
	
	public ExprType type();
	public boolean hasElements();
	public List<Expression> getElements();
	
	public Expression get(int index);
	public int size();
	
	public default Expression first() {
		if(!hasElements() || size() < 1) return null;
		return getElements().get(0);
	}
	
	public default Expression last() {
		if(!hasElements() || size() < 1) return null;
		return getElements().get(size() - 1);
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
	
	/**
	 * This method checks if the expression is modifying any value.
	 * 
	 * @return true if any modifications happen.
	 */
	public default boolean hasSideEffects() {
		ExprType type = type();
		if(type == ExprType.mov
		|| type == ExprType.call) return true;
		
		if(hasElements()) {
			for(Expression expr : getElements()) {
				if(expr.hasSideEffects()) return true;
			}
		}
		
		return false;
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
		
		public String asString() { return type.toString(); }
		public Object[] asList() { return list.toArray(); }
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
		
		public ExprType type() { return ExprType.cast; }
		
		public boolean hasElements() { return true; }
		public List<Expression> getElements() { return list; }
		public Expression get(int index) { return null; }
		public int size() { return list.size(); }
		
		public String toString() { return "(" + type.type() + ")" + value(); }
		public String asString() { return "CAST"; }
		public Object[] asList() { return new Object[] { type, value() }; };
	}
	
	public static class AtomExpr implements Expression {
		public AtomType atomType;
		
		public Identifier d_value; // ident
		public String s_value; // string
		public long i_value;  // int8, int4, int2, int1
		
		public AtomExpr(long value) {
			this(value, AtomType.int8);
		}
		
		public AtomExpr(int value) {
			this(Integer.toUnsignedLong(value), AtomType.int4);
		}
		
		public AtomExpr(short value) {
			this(Short.toUnsignedLong(value), AtomType.int2);
		}
		
		public AtomExpr(byte value) {
			this(Byte.toUnsignedLong(value), AtomType.int1);
		}
		
		public AtomExpr(Identifier value) {
			this(value, AtomType.ident);
		}

		public AtomExpr(String value) {
			this(value, AtomType.string);
		}
		
		/**
		 * Create a new atom expression type from a picked type and value.
		 * 
		 * @param value
		 * @param type
		 */
		public AtomExpr(Object value, AtomType type) {
			this.atomType = type;
			
			switch(type) {
				case string: s_value = value.toString(); break;
				case ident: d_value = (Identifier)value; break;
				
				case int1:
				case int2:
				case int4:
				case int8: i_value = ((Number)value).longValue(); break;
				default: throw new RuntimeException("Invalid atom type '" + type + "'");
			}
		}
		
		public boolean isNumber() {
			return atomType == AtomType.int8 ||
				   atomType == AtomType.int4 ||
				   atomType == AtomType.int2 ||
				   atomType == AtomType.int1;
		}
		
		public boolean isString() {
			return atomType == AtomType.string;
		}
		
		public AtomExpr convert(AtomType type) {
			if(!isNumber()) return null; // Invalid
			
			if(type == AtomType.int8) return new AtomExpr((long)i_value);
			if(type == AtomType.int4) return new AtomExpr((int)i_value);
			if(type == AtomType.int2) return new AtomExpr((short)i_value);
			if(type == AtomType.int1) return new AtomExpr((byte)i_value);
			
			throw new RuntimeException("Invalid type cast '" + type + "'");
		}
		
		public boolean isZero() {
			if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
			return i_value == 0;
		}
		
		public boolean isOne() {
			if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
			
			switch(atomType) {
				case int8: return i_value == 1;
				case int4: return i_value == 1;
				case int2: return i_value == 1;
				case int1: return i_value == 1;
				default: throw new RuntimeException("You cannot check non numbers if they are one.");
			}
		}
		
		public List<Expression> getElements() { return null; }
		public boolean hasElements() { return false; }
		public int size() { return 0; }
		public Expression get(int index) { return null; }
		
		public boolean isPure() { return true; }
		public AtomType atomType() { return atomType; }
		public ExprType type() { return ExprType.atom; }
		
		public String asString() { return toString() + ":" + atomType(); }
		public String toString() {
			switch(atomType) {
				case string: return '\"' + s_value + '\"';
				case ident: return d_value.name();
				case int8: return Long.toString(i_value) + 'L';
				case int4: return Integer.toString((int)i_value) + 'i';
				case int2: return Short.toString((short)i_value) + 's';
				case int1: return Byte.toString((byte)i_value) + 'b';
				default: throw new RuntimeException("Invalid atom type '" + atomType + "'");
			}
		}
	}
	
	public default String asString() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] asList() { return new Object[] {}; }
}
