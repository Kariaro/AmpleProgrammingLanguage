package hardcoded.compiler.constants;

class AtomType {
	public static final int NUMBER_FLAG = (1 << 0);
	public static final int SIGNED_FLAG = (1 << 1);
	
	
	public static final AtomType string = new AtomType("string", -1); // Always pointer
	public static final AtomType ident = new AtomType("ident", -1);   // Always pointer
	
	// Unsigned
	public static final AtomType u64 = new AtomType("u64", 8, NUMBER_FLAG),
								 u32 = new AtomType("u32", 4, NUMBER_FLAG),
								 u16 = new AtomType("u16", 2, NUMBER_FLAG),
								 u8  = new AtomType("u8",  1, NUMBER_FLAG);
	
	// Signed
	public static final AtomType i64 = new AtomType("i64", 8, NUMBER_FLAG | SIGNED_FLAG),
								 i32 = new AtomType("i32", 4, NUMBER_FLAG | SIGNED_FLAG),
								 i16 = new AtomType("i16", 2, NUMBER_FLAG | SIGNED_FLAG),
								 i8  = new AtomType("i8",  1, NUMBER_FLAG | SIGNED_FLAG);
	
	
	private final boolean isSigned;
	private final boolean isNumber;
	private final String name;
	private final int size;
	
	private AtomType(String name, int size) {
		this(name, size, 0);
	}
	
	private AtomType(String name, int size, int flags) {
		this.name = name;
		this.size = size;
		this.isNumber = (flags & NUMBER_FLAG) != 0;
		this.isSigned = (flags & SIGNED_FLAG) != 0;
	}
	
	public boolean isSigned() { return isSigned; }
	public boolean isNumber() { return isNumber; }
	
	public String name() { return name; }
	public int size() { return size; }
	
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
	
	public String toString() {
		return name;
	}
}
