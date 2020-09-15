package hardcoded.compiler.types;

import hardcoded.compiler.constants.AtomType;

public class PointerType extends Type {
	private Type type;
	public int pointerLength;
	private AtomType ptAtomType;
	
	public static final int POINTER_SIZE = 8;
	
	public PointerType(Type type, int pointerLength) {
		super(null, type.atomType(), POINTER_SIZE);
		this.pointerLength = pointerLength;
		this.type = type;
		
		this.ptAtomType = AtomType.getPointer(type.atomType(), pointerLength);
	}
	
	public String name() {
		return type.name();
	}
	
	public AtomType atomType() {
		return ptAtomType;
	}
	
	public Type type() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PointerType)) return false;
		PointerType pt = (PointerType)obj;
		return pointerLength == pt.pointerLength && type.equals(pt.type);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < pointerLength; i++) sb.append("*");
		return type + sb.toString();
	}
}
