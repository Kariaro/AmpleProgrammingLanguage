package hardcoded.compiler.constants;

class AtomType {
	public static final AtomType string = new AtomType("string", -1);
	public static final AtomType ident = new AtomType("ident", -1);
	public static final AtomType i64 = new AtomType("i64", 8, true);
	public static final AtomType i32 = new AtomType("i32", 4, true);
	public static final AtomType i16 = new AtomType("i16", 2, true);
	public static final AtomType i8  = new AtomType("i8", 1, true);
	
	private final boolean isNumber;
	private final String name;
	private final int size;
	
	private AtomType(String name, int size) {
		this(name, size, false);
	}
	
	private AtomType(String name, int size, boolean isNumber) {
		this.name = name;
		this.size = size;
		this.isNumber = isNumber;
	}
	
	public boolean isNumber() { return isNumber; }
	public String name() { return name; }
	public int size() { return size; }
	
	/**
	 * Get the <code>AtomType</code> with the largest size.
	 * @param a
	 * @param b
	 * @return
	 */
	public static AtomType largest(AtomType a, AtomType b) {
		if(a == null) {
			return (b != null && b.isNumber) ? a:null;
		} else if(b == null) {
			return a.isNumber ? a:null;
		}
		
		if(a.isNumber() && b.isNumber()) {
			return a.size() > b.size() ? a:b;
		}
		
		return null;
	}
	
	public String toString() {
		return name;
	}
}
