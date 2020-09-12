package hardcoded.compiler;

import java.util.*;

import hardcoded.compiler.constants.Printable;
import hardcoded.compiler.types.Type;
import hardcoded.utils.StringUtils;

public interface Expression extends Printable {
	public static final Expression EMPTY = new Expression() {
		public String toString() { return "nop"; }
		public String asString() { return "nop"; }
		public Object[] asList() { return new Object[] {}; }
		public ExprType type() { return ExprType.nop; }
		public boolean hasElements() { return false; }
		public List<Expression> getElements() { return null; }
		public Expression get(int index) { return null; }
		public int size() { return 0; }
	};
	
	public static enum AtomType {
		i64, i32, i16, i8,
		string,
		ident;
		
		/**
		 * Check if this <code>AtomType</code> is one of the types <code>i64, i32, i16, i8</code>.
		 * @return true if this <code>AtomType</code> is a number type
		 */
		public boolean isNumber() {
			return this == i64 || this == i32 || this == i16 || this == i8;
		}
		
		/**
		 * Get the size of this <code>AtomType</code> in bytes.
		 * @return -1 if there is no constant size
		 */
		public int size() {
			if(this == i64) return 8;
			if(this == i32) return 4;
			if(this == i16) return 2;
			if(this == i8) return 1;
			return -1;
		}
		
		/**
		 * Get the <code>AtomType</code> with the largest size.
		 * @param a
		 * @param b
		 * @return
		 */
		public static AtomType largest(AtomType a, AtomType b) {
			if(a == null) {
				if(b == null) return null;
				return b.isNumber() ? b:null;
			} else if(b == null) {
				return a.isNumber() ? a:null;
			}
			
			if(a.isNumber() && b.isNumber()) {
				return a.size() > b.size() ? a:b;
			}
			
			return null; // Invalid combination
		}
	}
	
	public static enum ExprType {
		// Memory operations
		set,	// Move a value y into x
		
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
		
		// Pointer
		addptr, // &x
		decptr, // *x
		
		// Function
		call,	// Call
		ret,	// Call return
		nop,	// No operation
		
		leave,	// Break
		loop,	// Continue
		
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
			case set:
			case ret:
				return false;
			default: return true;
		}
	}
	
	/**
	 * Checks whether or not an expression modifies any values.
	 * A modification can cause unknown side effects and thats
	 * why changing a value can give side effects.
	 * 
	 * @return 
	 */
	public default boolean hasSideEffects() {
		ExprType type = type();
		if(type == ExprType.set
		|| type == ExprType.call) return true;
		
		if(hasElements()) {
			for(Expression expr : getElements()) {
				if(expr.hasSideEffects()) return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Calculate the size of the nummerical size that this expression might hold.<br>
	 * <code>1L + 3 = (long)1 + (int)3 = (long)</code>
	 * 
	 * @return
	 */
	public default AtomType calculateNumberSize() {
		AtomType curr = null;
		
		if(hasElements()) {
			for(Expression expr : getElements()) {
				AtomType type = expr.calculateNumberSize();
				if(type == null) continue;
				
				curr = curr == null ? type:AtomType.largest(curr, type);
			}
		} else {
			AtomExpr a = (AtomExpr)this;
			
			if(a.isIdentifier()) {
				Identifier ident = a.d_value;
				
				if(ident.hasType()) {
					int size = ident.type.size();
					
					if(size == 8) return AtomType.i64;
					if(size == 4) return AtomType.i32;
					if(size == 2) return AtomType.i16;
					if(size == 1) return AtomType.i8;
					return null;
				}
			}
			
			if(a.isNumber()) {
				return a.atomType;
			}
		}
		
		return curr;
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
		
		public String toString() {
			return type + "(" + StringUtils.join(", ", list) + ")";
		}
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
		public long i_value;  // i64, i32, i16, i8
		
		public AtomExpr(long value) {
			this(value, AtomType.i64);
		}
		
		public AtomExpr(int value) {
			this(Integer.toUnsignedLong(value), AtomType.i32);
		}
		
		public AtomExpr(short value) {
			this(Short.toUnsignedLong(value), AtomType.i16);
		}
		
		public AtomExpr(byte value) {
			this(Byte.toUnsignedLong(value), AtomType.i8);
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
				
				case i8:
				case i16:
				case i32:
				case i64: i_value = ((Number)value).longValue(); break; // TODO: Signed unsigned?
				default: throw new RuntimeException("Invalid atom type '" + type + "'");
			}
		}
		
		public boolean isNumber() {
			return atomType.isNumber();
		}
		
		public boolean isString() {
			return atomType == AtomType.string;
		}
		
		public boolean isIdentifier() {
			return atomType == AtomType.ident;
		}
		
		public AtomExpr convert(AtomType type) {
			if(!isNumber()) return null; // Invalid
			
			// TODO: Signed unsigned?
			if(type == AtomType.i64) return new AtomExpr((long)i_value);
			if(type == AtomType.i32) return new AtomExpr((int)i_value);
			if(type == AtomType.i16) return new AtomExpr((short)i_value);
			if(type == AtomType.i8) return new AtomExpr((byte)i_value);
			
			throw new RuntimeException("Invalid type cast '" + type + "'");
		}
		
		public boolean isZero() {
			if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
			return i_value == 0;
		}
		
		public boolean isOne() {
			if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
			return i_value == 1;
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
			switch(atomType) { // TODO: Signed unsigned?
				case string: return '\"' + s_value + '\"';
				case ident: return Objects.toString(d_value);
				case i64: return Long.toString(i_value) + 'L';
				case i32: return Integer.toString((int)i_value) + 'i';
				case i16: return Short.toString((short)i_value) + 's';
				case i8: return Byte.toString((byte)i_value) + 'b';
				default: throw new RuntimeException("Invalid atom type '" + atomType + "'");
			}
		}
	}
	
	public default String asString() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] asList() { return new Object[] {}; }
}
