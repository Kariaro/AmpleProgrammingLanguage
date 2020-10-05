package hardcoded.compiler.expression;

import java.util.List;
import java.util.Objects;

import hardcoded.compiler.Identifier;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.ExprType;

public class AtomExpr implements Expression {
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
	public int length() { return 0; }
	public Expression get(int index) { return null; }
	public void remove(int index) {}
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