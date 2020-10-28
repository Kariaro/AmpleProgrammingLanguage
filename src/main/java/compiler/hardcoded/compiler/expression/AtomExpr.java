package hardcoded.compiler.expression;

import java.util.List;
import java.util.Objects;

import hardcoded.compiler.Identifier;
import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.constants.ExprType;
import hardcoded.utils.StringUtils;

public class AtomExpr implements Expression {
	public LowType atomType;
	
	private Identifier d_value;	// ident
	private String s_value;		// string
	private long n_value;		// number
	
	public AtomExpr(long value) {
		this(value, Atom.i64);
	}
	
	public AtomExpr(int value) {
		this((long)value, Atom.i32);
	}
	
	public AtomExpr(short value) {
		this((long)value, Atom.i16);
	}
	
	public AtomExpr(byte value) {
		this((long)value, Atom.i8);
	}
	
	public AtomExpr(boolean value) {
		this(value ? 1:0, Atom.i8);
	}
	
	public AtomExpr(Identifier value) {
		this(value, Atom.ident);
	}

	public AtomExpr(String value) {
		this(value, Atom.string);
	}
	
	/**
	 * Create a new atom expression type from a picked type and value.
	 * 
	 * @param value
	 * @param type
	 */
	public AtomExpr(Object value, Atom type) {
		this.atomType = LowType.create(type);
		
		if(type == Atom.string) {
			s_value = value.toString();
		} else if(type == Atom.ident) {
			d_value = (Identifier)value;
		} else if(type.isNumber()) {
			// FIXME: Double/Float values??
			n_value = ((Number)value).longValue();
		} else {
			throw new RuntimeException("Invalid atom type '" + type + "'");
		}
	}
	
	public boolean isNumber() {
		return atomType.isNumber();
	}
	
	public boolean isString() {
		return atomType.type() == Atom.string;
	}
	
	public boolean isIdentifier() {
		return atomType.type() == Atom.ident;
	}
	
	public AtomExpr convert(LowType type) {
		if(!isNumber()) return null; // Invalid
		
		if(type.isPointer()) {
			AtomExpr expr = new AtomExpr((long)n_value);
			expr.atomType = type;
			return expr;
		}
		
		// TODO: Signed unsigned?
		if(type.type() == Atom.i64) return new AtomExpr((long)n_value);
		if(type.type() == Atom.i32) return new AtomExpr((int)n_value);
		if(type.type() == Atom.i16) return new AtomExpr((short)n_value);
		if(type.type() == Atom.i8) return new AtomExpr((byte)n_value);
		
		throw new RuntimeException("Invalid type cast '" + type + "'");
	}
	
	public boolean isZero() {
		if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
		return n_value == 0;
	}
	
	public boolean isOne() {
		if(!isNumber()) throw new RuntimeException("You cannot check a non number if it is zero.");
		return n_value == 1;
	}
	
	public List<Expression> getElements() { return null; }
	public boolean hasElements() { return false; }
	public int length() { return 0; }
	public Expression get(int index) { return null; }
	public void remove(int index) {}
	public void set(int index, Expression e) {}
	
	public boolean isPure() { return true; }
	public LowType atomType() { return atomType; }
	public ExprType type() { return ExprType.atom; }
	
	
	
	public Identifier identifier() {
		return d_value;
	}
	
	public String string() {
		return s_value;
	}
	
	public long number() {
		return n_value;
	}
	
	
	
	public AtomExpr clone() {
		AtomExpr expr = new AtomExpr(0);
		expr.atomType = atomType;
		
		if(isIdentifier()) {
			expr.d_value = d_value.clone();
		}
		
		expr.n_value = n_value;
		expr.s_value = s_value;
		return expr;
	}
	
	public String asString() { return toString() + ":" + atomType(); }
	public String toString() {
		if(atomType.type() == Atom.string) return '\"' + StringUtils.escapeString(s_value) + '\"';
		if(atomType.type() == Atom.ident)  return Objects.toString(d_value);
		
		// TODO: Signed unsigned?
		if(atomType.isNumber()) {
			String postfix = (atomType.isSigned() ? "":"u");
			
			// TODO: Print unsigned values correctly.
			switch(atomType.size()) {
				case 8: return Long.toString(n_value) + postfix + 'L';
				case 4: return Integer.toString((int)n_value) + postfix + 'i';
				case 2: return Short.toString((short)n_value) + postfix + 's';
				case 1: return Byte.toString((byte)n_value) + postfix + 'b';
			}
		}
		
		throw new RuntimeException("Invalid atom type '" + atomType + "'");
	}
}