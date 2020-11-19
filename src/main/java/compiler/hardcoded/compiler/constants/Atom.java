package hardcoded.compiler.constants;

public enum Atom {
	// size, isNumber, isSigned, isFloating
	u64			(8, true, false, false),
	u32			(4, true, false, false),
	u16			(2, true, false, false),
	u8			(1, true, false, false),
	i64			(8, true, true, false),
	i32			(4, true, true, false),
	i16			(2, true, true, false),
	i8			(1, true, true, false),
	f64			(8, true, true, true),
	f32			(4, true, true, true),
	
	// Object Types
	string		(),
	ident		(),
	clazz		(),
	unf			(),
	;
	
	private final boolean isSigned;
	private final boolean isNumber;
	private final boolean isFloating;
	private final int size;
	
	private Atom() {
		this(0, false, false, false);
	}
	
	private Atom(int size, boolean isNumber, boolean isSigned, boolean isFloating) {
		this.isFloating = isFloating;
		this.isNumber = isNumber;
		this.isSigned = isSigned;
		this.size = size;
	}
	
	public boolean isFloating() { return isFloating; }
	public boolean isSigned() { return isSigned; }
	public boolean isNumber() { return isNumber; }
	public int size() { return size; }
	
	/**
	 * Computes the <i>largest</i> {@code Atom} from {@code a} and {@code b}.
	 * The atom will have the size of {@code Math.max(a.size(), b.size())} and type
	 * of the leftmost group <i>{@code floating}, {@code unsigned}</i> or <i>{@code signed}</i>
	 * present in either {@code a} or {@code b}.
	 * 
	 * @param	a	element to check
	 * @param	b	element to check
	 * @return	the <i>largest</i> {@code Atom} computed from {@code a} and {@code b}
	 */
	public static Atom largest(Atom a, Atom b) {
		if(a == null) return (b == null ? null:(b.isNumber ? b:null));
		if(b == null) return (a == null ? null:(a.isNumber ? a:null));
		if(!a.isNumber() && !b.isNumber()) return Atom.unf;
		
		return get(Math.max(a.size, b.size), a.isSigned && b.isSigned, a.isFloating || b.isFloating);
	}
	
	public static Atom get(int size, boolean isSigned, boolean isFloating) {
		return Atom.valueOf((isFloating ? "f":(isSigned ? "i":"u")) + (size * 8));
	}
}
