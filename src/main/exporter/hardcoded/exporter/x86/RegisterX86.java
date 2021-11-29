package hardcoded.exporter.x86;

import static hardcoded.exporter.x86.RegisterType.*;

/**
 * Registers was fetched from:<br>
 * 
 * <a href="https://wiki.osdev.org/X86-64_Instruction_Encoding#Registers">
 *   https://wiki.osdev.org/X86-64_Instruction_Encoding#Registers
 * </a>
 * 
 * @author HardCoded
 */
enum RegisterX86 {
	AL(r8, 0),    AX(r16, 0),    EAX(r32, 0),   RAX(r64, 0), ST0(x87, 0), MMX0(mmx, 0), XMM0(xmm, 0),   YMM0(ymm, 0),  ES(segment, 0), CR0(control, 0),   DR0(debug, 0),
	CL(r8, 1),    CX(r16, 1),    ECX(r32, 1),   RCX(r64, 1), ST1(x87, 1), MMX1(mmx, 1), XMM1(xmm, 1),   YMM1(ymm, 1),  CS(segment, 1), CR1(control, 1),   DR1(debug, 1),
	DL(r8, 2),    DX(r16, 2),    EDX(r32, 2),   RDX(r64, 2), ST2(x87, 2), MMX2(mmx, 2), XMM2(xmm, 2),   YMM2(ymm, 2),  SS(segment, 2), CR2(control, 2),   DR2(debug, 2),
	BL(r8, 3),    BX(r16, 3),    EBX(r32, 3),   RBX(r64, 3), ST3(x87, 3), MMX3(mmx, 3), XMM3(xmm, 3),   YMM3(ymm, 3),  DS(segment, 3), CR3(control, 3),   DR3(debug, 3),
	AH(r8, 4),    SP(r16, 4),    ESP(r32, 4),   RSP(r64, 4), ST4(x87, 4), MMX4(mmx, 4), XMM4(xmm, 4),   YMM4(ymm, 4),  FS(segment, 4), CR4(control, 4),   DR4(debug, 4),
	CH(r8, 5),    BP(r16, 5),    EBP(r32, 5),   RBP(r64, 5), ST5(x87, 5), MMX5(mmx, 5), XMM5(xmm, 5),   YMM5(ymm, 5),  GS(segment, 5), CR5(control, 5),   DR5(debug, 5),
	DH(r8, 6),    SI(r16, 6),    ESI(r32, 6),   RSI(r64, 6), ST6(x87, 6), MMX6(mmx, 6), XMM6(xmm, 6),   YMM6(ymm, 6),                  CR6(control, 6),   DR6(debug, 6),
	BH(r8, 7),    DI(r16, 7),    EDI(r32, 7),   RDI(r64, 7), ST7(x87, 7), MMX7(mmx, 7), XMM7(xmm, 7),   YMM7(ymm, 7),                  CR7(control, 7),   DR7(debug, 7),
	R8B(r8, 8),   R8W(r16, 8),   R8D(r32, 8),   R8(r64, 8),            /* MMX0 */       XMM8(xmm, 8),   YMM8(ymm, 8),  /* ES */        CR8(control, 8),   DR8(debug, 8),
	R9B(r8, 9),   R9W(r16, 9),   R9D(r32, 9),   R9(r64, 9),            /* MMX1 */       XMM9(xmm, 9),   YMM9(ymm, 9),  /* CS */        CR9(control, 9),   DR9(debug, 9),
	R10B(r8, 10), R10W(r16, 10), R10D(r32, 10), R10(r64, 10),          /* MMX2 */       XMM10(xmm, 10), YMM10(ymm, 10),/* SS */        CR10(control, 10), DR10(debug, 10),
	R11B(r8, 11), R11W(r16, 11), R11D(r32, 11), R11(r64, 11),          /* MMX3 */       XMM11(xmm, 11), YMM11(ymm, 11),/* DS */        CR11(control, 11), DR11(debug, 11),
	R12B(r8, 12), R12W(r16, 12), R12D(r32, 12), R12(r64, 12),          /* MMX4 */       XMM12(xmm, 12), YMM12(ymm, 12),/* FS */        CR12(control, 12), DR12(debug, 12),
	R13B(r8, 13), R13W(r16, 13), R13D(r32, 13), R13(r64, 13),          /* MMX5 */       XMM13(xmm, 13), YMM13(ymm, 13),/* GS */        CR13(control, 13), DR13(debug, 13),
	R14B(r8, 14), R14W(r16, 14), R14D(r32, 14), R14(r64, 14),          /* MMX6 */       XMM14(xmm, 14), YMM14(ymm, 14),                CR14(control, 14), DR14(debug, 14),
	R15B(r8, 15), R15W(r16, 15), R15D(r32, 15), R15(r64, 15),          /* MMX7 */       XMM15(xmm, 15), YMM15(ymm, 15),                CR15(control, 15), DR15(debug, 15),
	
	// If any REX prefix was specified we will use these
	SPL(r8, 4),
	BPL(r8, 5),
	SIL(r8, 6),
	DIL(r8, 7),
	
	
	RIP(special, 64, 5), // 64-bit register pointer
	EIP(special, 32, 5), // 32-bit register pointer
	
	INVALID(special, -1, -1)
	;
	
	public final RegisterType type;
	public final int index;
	public final int bits;
	
	private RegisterX86(RegisterType type, int index) {
		this.type = type;
		this.bits = (type == null ? 0:type.bits);
		this.index = index;
	}
	
	private RegisterX86(RegisterType type, int bits, int index) {
		this.type = type;
		this.bits = bits;
		this.index = index;
	}
	
	/**
	 * Iterrating over the elements returned by {@link Enum#values()}
	 * will be much slower than looping over each {@code RegisterType}.
	 * By splitting each register into types we achieved a 50 times
	 * speedup when searching for registers.
	 */
	private static final RegisterX86[][] registers = { // 11 types
		{ AL, CL, DL, BL, AH, CH, DH, BH, R8B, R9B, R10B, R11B, R12B, R13B, R14B, R15B },							// r8
		{ AX, CX, DX, BX, SP, BP, SI, DI, R8W, R9W, R10W, R11W, R12W, R13W, R14W, R15W },							// r16
		{ EAX, ECX, EDX, EBX, ESP, EBP, ESI, EDI, R8D, R9D, R10D, R11D, R12D, R13D, R14D, R15D },					// r32
		{ RAX, RCX, RDX, RBX, RSP, RBP, RSI, RDI, R8, R9, R10, R11, R12, R13, R14, R15 },							// r64
		{ ST0, ST1, ST2, ST3, ST4, ST5, ST6, ST7 },																	// x87
		{ MMX0, MMX1, MMX2, MMX3, MMX4, MMX5, MMX6, MMX7 },															// mmx
		{ XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7, XMM8, XMM9, XMM10, XMM11, XMM12, XMM13, XMM14, XMM15 },	// xmm
		{ YMM0, YMM1, YMM2, YMM3, YMM4, YMM5, YMM6, YMM7, YMM8, YMM9, YMM10, YMM11, YMM12, YMM13, YMM14, YMM15 },	// ymm
		{ ES, CS, SS, DS, FS, GS },																					// segment
		{ CR0, CR1, CR2, CR3, CR4, CR5, CR6, CR7, CR8, CR9, CR10, CR11, CR12, CR13, CR14, CR15 },					// control
		{ DR0, DR1, DR2, DR3, DR4, DR5, DR6, DR7, DR8, DR9, DR10, DR11, DR12, DR13, DR14, DR15 },					// debug
	};
	
	/**
	 * Returns a register with matching type and index value or
	 * {@code INVALID} if no match was found.<br>
	 * 
	 * Note that this method will <b>never</b> return any of these registers
	 * {@code SPL}, {@code BPL}, {@code SIL}, {@code DIL}, {@code RIP}, {@code EIP}.
	 * 
	 * @param	type	the register type you are looking for
	 * @param	index	the index of the register
	 * @return	a register with matching type and index value
	 */
	public static RegisterX86 get(RegisterType type, int index) {
		if(type == null || type.ordinal() >= 11) return INVALID;
		
		// The 'MMX' and 'Segment' type registers does not have
		// registers defined for the forth bit.
		if(type == mmx || type == segment) index &= 7;
		
		RegisterX86[] array = registers[type.ordinal()];
		for(RegisterX86 reg : array) {
			if(reg.index == index) return reg;
		}
		
		return INVALID;
	}

	public boolean matchesAny(RegisterType... types) {
		for(RegisterType type : types) {
			if(this.type == type) return true;
		}
		return false;
	}
	
	public boolean matchesAny(RegisterX86... registers) {
		for(RegisterX86 register : registers) {
			if(this == register) return true;
		}
		return false;
	}
}
