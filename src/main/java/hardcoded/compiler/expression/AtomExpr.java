package hardcoded.compiler.expression;

import java.util.Objects;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.utils.StringUtils;

public class AtomExpr extends Expression {
	private Identifier i_value;
	private String s_value;
	private long n_value;
	private LowType atomType;
	
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
	
	public AtomExpr(char value) {
		this((long)(value & 0xff), Atom.i8);
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
	 * Create a new atom expression type from a type and value.
	 * 
	 * @param value	the value of this atom
	 * @param type	the type of this atom
	 */
	public AtomExpr(Object value, Atom type) {
		super(ExprType.atom, false);
		this.atomType = LowType.create(type);
		
		switch(type) {
			case string -> s_value = value.toString();
			case ident -> i_value = (Identifier)value;
			case i8, i16, i32, i64, u8, u16, u32, u64 -> {
				n_value = ((Number)value).longValue();
			}
			default -> {
				throw new CompilerException("Invalid atom type '%s'", type);
			}
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
	
	public LowType getAtomType() {
		return atomType;
	}
	
	public AtomExpr convert(LowType type) {
		if(!isNumber()) {
			return null;
		}
		
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
		
		throw new CompilerException("Invalid type cast '%s'", type);
	}
	
	public boolean isZero() {
		if(!isNumber()) {
			throw new CompilerException("You cannot check a non number if it is zero");
		}
		
		return n_value == 0;
	}
	
	public boolean isOne() {
		if(!isNumber()) {
			throw new CompilerException("You cannot check a non number if it is zero");
		}
		
		return n_value == 1;
	}

	@Override
	public boolean isPure() {
		return true;
	}
	
	public Identifier identifier() {
		return i_value;
	}
	
	public String string() {
		return s_value;
	}
	
	public long number() {
		return n_value;
	}

	@Override
	public Expression clone() {
		AtomExpr expr = new AtomExpr(0);
		expr.atomType = atomType;
		
		if(isIdentifier()) {
			expr.i_value = i_value.clone();
		}
		
		expr.n_value = n_value;
		expr.s_value = s_value;
		return expr;
	}

	@Override
	public String asString() {
		return toString() + ":" + size();
	}
	
	@Override
	public String toString() {
		if(atomType.type() == Atom.string) {
			return '\"' + StringUtils.escapeString(s_value) + '\"';
		}
		
		if(atomType.type() == Atom.ident) {
			return Objects.toString(i_value);
		}
		
		// TODO: Signed unsigned?
		if(atomType.isNumber()) {
			String postfix = (atomType.isSigned() ? "":"u");
			
			if(atomType.isPointer()) return String.format("0x%016x", n_value);
			
			switch(atomType.size()) {
				case 8: return Long.toString(n_value) + postfix + 'L';
				case 4: return Integer.toString((int)n_value) + postfix + 'i';
				case 2: return Short.toString((short)n_value) + postfix + 's';
				case 1: return Byte.toString((byte)n_value) + postfix + 'b';
			}
		}
		
		throw new CompilerException("Invalid atom type '%s'", atomType);
	}
}