package hardcoded.compiler.constants;

public class AtomType {
	public static final int NUMBER_FLAG		= (1 << 0);
	public static final int SIGNED_FLAG		= (1 << 1);
	public static final int POINTER_FLAG	= (1 << 2);
	public static final int NOP_FLAG		= (1 << 10);
	
	private static final int POINTER_MASK = 255 * POINTER_FLAG;
	
	
	public static final AtomType string = new AtomType("string", -1); // Always pointer
	public static final AtomType ident = new AtomType("ident", -1);   // Always pointer
	
	// TODO: Implement unsigned atom types.
//	public static final AtomType u64 = new AtomType("u64", 8, NUMBER_FLAG),
//								 u32 = new AtomType("u32", 4, NUMBER_FLAG),
//								 u16 = new AtomType("u16", 2, NUMBER_FLAG),
//								 u8  = new AtomType("u8",  1, NUMBER_FLAG);
	
	// Signed
	public static final AtomType i64 = new AtomType("i64", 8, NUMBER_FLAG | SIGNED_FLAG),
								 i32 = new AtomType("i32", 4, NUMBER_FLAG | SIGNED_FLAG),
								 i16 = new AtomType("i16", 2, NUMBER_FLAG | SIGNED_FLAG),
								 i8  = new AtomType("i8",  1, NUMBER_FLAG | SIGNED_FLAG);
	
	
	private final boolean isSigned;
	private final boolean isNumber;
	private final String name;
	
	/**
	 * If this atomType is a pointer type this will be its size.
	 * The maximum allowed size is 255 after that it needs to be
	 * casted.
	 */
	private final int pointer;
	private final int size;
	
	private AtomType(String name, int size) {
		this(name, size, 0);
	}
	
	private AtomType(String name, int size, int flags) {
		this.size = size;
		this.isNumber = (flags & NUMBER_FLAG) != 0;
		this.isSigned = (flags & SIGNED_FLAG) != 0;
		this.pointer = (flags & POINTER_MASK) / POINTER_FLAG;
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < pointer; i++) sb.append('*');
		this.name = name + sb.toString();
	}
	
	public boolean isSigned() { return isSigned; }
	public boolean isNumber() { return isNumber; }
	public boolean isPointer() { return pointer != 0; }
	
	public String name() { return name; }
	public int size() { return size; }
	public int pointerDepth() { return pointer; }
	
	/**
	 * Get the <code>AtomType</code> with the largest size.
	 * 
	 * @param a
	 * @param b
	 * 
	 * @return
	 */
	public static AtomType largest(AtomType a, AtomType b) {
		if(a == null || b == null) return largest_null(a, b);
		
		// TODO: Calculate unsigned comparisons.
		// largest( [unsigned int] , [long] ) == [unsigned long]
		
		if(a.isNumber && b.isNumber) {
			if(a.isPointer()) {
				if(b.isPointer()) {
					return a.size > b.size ? a:b;
				}
				
				return a;
			}
			
			if(b.isPointer()) {
				return b;
			}
			
			return a.size > b.size ? a:b;
		}
		
		return null;
	}
	
	private static AtomType largest_null(AtomType a, AtomType b) {
		if(a == null) {
			if(b == null) return null;
			return b.isNumber ? b:null;
		}
		
		return a.isNumber ? a:null;
	}
	
	public static AtomType getPointer(AtomType type, int length) {
		length = type.pointer + length;
		if(length > 255) throw new RuntimeException("Pointer type to deep (" + length + ").");
		if(length < 0) throw new RuntimeException("Pointer length cannot be negative.");
		
		int flags = (type.isNumber ? NUMBER_FLAG:0) |
					(type.isSigned ? SIGNED_FLAG:0) |
					(length * POINTER_FLAG);
		
		// TODO: AtomType.name.replace("*", "") should be replaced by something more official
		return new AtomType(type.name.replace("*", ""), type.size, flags);
	}
	
	@Deprecated
	public static AtomType get(int size, int pointerDepth) {
		return new AtomType("i" + size, size);
	}
	
	public static AtomType getNumberType(int size) {
		switch(size) {
			case 8: return i8;
			case 16: return i16;
			case 32: return i32;
			case 64: return i64;
		}
		
		throw new RuntimeException("Invalid number type size '" + size + "'");
	}
	
	public String toString() {
		return name;
	}
}
