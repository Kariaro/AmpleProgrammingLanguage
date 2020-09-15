package hardcoded.compiler;

import java.util.*;

import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.Printable;
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
		public void set(int index, Expression e) {}
		public Expression clone() { return this; }
		public int size() { return 0; }
	};
	
	public static enum ExprType {
		// Memory
		set,	// x = y
		
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
		not,	// !x
		nor,	// ~x
		neg,	// -x
		
		// Compares				[Only returns zero or one]
		eq,		// x == y
		neq,	// x != y
		gt,		// x >  y
		gte,	// x >= y
		lt,		// x <  y
		lte,	// x <= y
		cor,	// x || y
		cand,	// x && y
		
		// Pointer
		addptr, // &x
		decptr, // *x
		
		// Function
		call,	// Call
		ret,	// Return
		nop,	// No operation
		
		leave,	// Break
		loop,	// Continue
		
		atom,	// Atom
		cast,	// Cast
		comma,
		invalid, // Invalid expression type
	}
	
	public ExprType type();
	public boolean hasElements();
	public List<Expression> getElements();
	
	public Expression get(int index);
	public void set(int index, Expression e);
	public int size();
	
	public default Expression clone() {
		return null;
	}
	
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
	public default AtomType calculateSize() {
		AtomType curr = null;
		
		if(hasElements()) {
			for(Expression expr : getElements()) {
				AtomType type = expr.calculateSize();
				if(type == null) continue;
				
				curr = curr == null ? type:AtomType.largest(curr, type);
			}
		} else {
			AtomExpr a = (AtomExpr)this;
			
			if(a.isIdentifier()) {
				Identifier ident = a.d_value;
				
				if(ident.hasType()) {
					return ident.atomType();
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
		
		public void set(int index, Expression expr) {
			list.set(index, expr);
		}
		
		public Expression get(int index) {
			return list.get(index);
		}
		
		public int size() {
			return list.size();
		}
		
		public AtomType override_size;
		public AtomType calculateSize() {
			if(type == ExprType.cast) {
				return override_size;
			}
			
			return Expression.super.calculateSize();
		}
		
		public OpExpr clone() {
			OpExpr expr = new OpExpr(type);
			expr.override_size = override_size;
			for(Expression e : list) expr.add(e.clone());
			return expr;
		}
		
		public ExprType type() { return type; }
		public boolean hasElements() { return true; }
		public List<Expression> getElements() { return list; }
		
		public String asString() { return type.toString(); }
		public Object[] asList() { return list.toArray(); }
		
		public String toString() {
			return type + "(" + StringUtils.join(", ", list) + ")" + ":" + this.calculateSize();
		}
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
			
			if(type == AtomType.string) {
				s_value = value.toString();
			} else if(type == AtomType.ident) {
				d_value = (Identifier)value;
			} else if(type.isNumber()) {
				
				// TODO: Signed unsigned?
				i_value = ((Number)value).longValue();
			} else {
				throw new RuntimeException("Invalid atom type '" + type + "'");
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
			
			if(type.isPointer()) {
				AtomExpr expr = new AtomExpr((long)i_value);
				expr.override_size = type;
				return expr;
			}
			
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
		public void set(int index, Expression e) {}
		
		public boolean isPure() { return true; }
		public AtomType atomType() { return atomType; }
		public ExprType type() { return ExprType.atom; }
		
		public AtomType override_size;
		public AtomType calculateSize() {
			if(override_size != null) {
				return override_size;
			}
			
			return Expression.super.calculateSize();
		}
		
		public AtomExpr clone() {
			AtomExpr expr = new AtomExpr(0);
			expr.atomType = atomType;
			expr.override_size = override_size;
			if(isIdentifier()) {
				expr.d_value = d_value.clone();
				
			}
			expr.i_value = i_value;
			expr.s_value = s_value;
			return expr;
		}
		
		public String asString() { return toString() + ":" + atomType(); }
		public String toString() {
			if(atomType == AtomType.string) return '\"' + s_value + '\"';
			if(atomType == AtomType.ident)  return Objects.toString(d_value);
			
			// TODO: Signed unsigned?
			if(atomType.isNumber()) {
				String postfix = (atomType.isSigned() ? "":"u");
				
				// TODO: Print unsigned values correctly.
				switch(atomType.size()) {
					case 8: return Long.toString(i_value) + postfix + 'L';
					case 4: return Integer.toString((int)i_value) + postfix + 'i';
					case 2: return Short.toString((short)i_value) + postfix + 's';
					case 1: return Byte.toString((byte)i_value) + postfix + 'b';
				}
			}
			
			throw new RuntimeException("Invalid atom type '" + atomType + "'");
		}
	}
	
	public default String asString() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] asList() { return new Object[] {}; }
}
