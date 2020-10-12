package hardcoded.assembly.x86;

public enum RegisterType {
	// General Purpose registers
	r8(8),
	r16(16),
	r32(32),
	r64(64),
	
	x87(80),
	
	mmx(64),
	xmm(128),
	ymm(256),
	
	segment(16),
	control(32),
	debug(32),
	
	/**
	 * This type is used for declaring special or custom registers.<p>
	 * Exampes of special registers are custom defined registers.
	 */
	special(0),
	;
	
	public final int bits;
	private RegisterType(int bits) {
		this.bits = bits;
	}
}
