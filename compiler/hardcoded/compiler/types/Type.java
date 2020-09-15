package hardcoded.compiler.types;

import java.util.Objects;

import hardcoded.compiler.constants.AtomType;

/**
 * This is a the high representation of a type inside the programming language.<br>
 * The coresponding low level representation is a {@linkplain hardcoded.compiler.constants.AtomType}
 * 
 * @author HardCoded
 */
public class Type {
	private final String name;
	private final int size;
	private final boolean signed;
	private final AtomType atomType;
	
	public Type(String name, AtomType type, int size) {
		this(name, type, size, true);
	}
	
	public Type(String name, AtomType type, int size, boolean signed) {
		this.name = name;
		this.size = size;
		this.signed = signed;
		this.atomType = type;
	}
	
	public Type type() {
		return this;
	}
	
	public AtomType atomType() {
		return atomType;
	}
	
	public String name() {
		return name;
	}
	
	public boolean isSigned() {
		return signed;
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public boolean equals(Object obj) {
		return Objects.equals(this, obj);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
