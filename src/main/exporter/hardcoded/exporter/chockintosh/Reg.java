package hardcoded.exporter.chockintosh;

import java.util.List;

enum Reg {
	PGC      (0x0), // Program counter           [16]
	PGC2     (0x1), // Program counter byte 2
	W        (0x2), // Working register          [16]
	W2       (0x3), // Working register byte 2
	X        (0x4), // Second ALU input          [16]
	X2       (0x5), // Second ALU input byte 2
	POINTER  (0x6), // Pointer in ROM            [16]
	POINTER2 (0x7), // Pointer in ROM byte 2
	AUX      (0x8), // Auxiliary register        [16]
	AUX2     (0x9), // Auxiliary register byte 2
	RAMAD    (0xa), // RAM address               [8]
	RAM      (0xb), // Output from RAM           [8]
	STACK    (0xc), // IO of the stack           [8]
	IO       (0xd), // IO                        [8]
	FLAGMASK (0xe), // Mask flag reg for skip    [8]
	FLAG     (0xf), // Contains flags            [8]
	
	// PGC2BUF
	// MOVEVAL
	;
	
	private static final List<String> NAMES = List.of(values()).stream().map(i -> i.name()).toList();
	
	public final int id;
	private Reg(int id) {
		this.id = id;
	}
	
	public static String getName(int index) {
		return NAMES.get(index);
	}
}
