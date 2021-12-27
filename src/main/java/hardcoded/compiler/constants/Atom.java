package hardcoded.compiler.constants;

public enum Atom {
	// size, isSigned, isFloating
	u64			(8, false, false),
	u32			(4, false, false),
	u16			(2, false, false),
	u8			(1, false, false),
	i64			(8, true, false),
	i32			(4, true, false),
	i16			(2, true, false),
	i8			(1, true, false),
	f64			(8, true, true),
	f32			(4, true, true),
	
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
	
	// Object constructor
	private Atom() {
		this.isFloating = false;
		this.isNumber = false;
		this.isSigned = false;
		this.size = 0;
	}
	
	// Number constructor
	private Atom(int size, boolean isSigned, boolean isFloating) {
		this.isFloating = isFloating;
		this.isNumber = true;
		this.isSigned = isSigned;
		this.size = size;
	}
	
	/**
	 * Returns if this atom is a floating point value.
	 */
	public boolean isFloating() {
		return isFloating;
	}
	
	/**
	 * Returns if this atom is signed.
	 */
	public boolean isSigned() {
		return isSigned;
	}
	
	/**
	 * Returns if this atom was a number.
	 */
	public boolean isNumber() {
		return isNumber;
	}
	
	/**
	 * Returns the size of this atom.
	 */
	public int size() {
		return size;
	}
	
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
