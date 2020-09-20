package hardcoded.compiler.assembler;

import static hardcoded.compiler.assembler.AsmRegGroup.*;

public enum AsmReg {
	RAX(reg, 64), EAX(reg, 32), AX(reg, 16), AH(reg, 8), AL(reg, 8),
	RCX(reg, 64), ECX(reg, 32), CX(reg, 16), CH(reg, 8), CL(reg, 8),
	RDX(reg, 64), EDX(reg, 32), DX(reg, 16), DH(reg, 8), DL(reg, 8),
	RBX(reg, 64), EBX(reg, 32), BX(reg, 16), BH(reg, 8), BL(reg, 8),
	
	RSP(reg, 64), ESP(reg, 32), SP(reg, 16), SPL(reg, 8),
	RBP(reg, 64), EBP(reg, 32), BP(reg, 16), BPL(reg, 8),
	RSI(reg, 64), ESI(reg, 32), SI(reg, 16), SIL(reg, 8),
	RDI(reg, 64), EDI(reg, 32), DI(reg, 16), DIL(reg, 8),
	
	R8(reg, 64), R8D(reg, 32), R8W(reg, 16), R8B(reg, 8),
	R9(reg, 64), R9D(reg, 32), R9W(reg, 16), R9B(reg, 8),
	R10(reg, 64), R10D(reg, 32), R10W(reg, 16), R10B(reg, 8),
	R11(reg, 64), R11D(reg, 32), R11W(reg, 16), R11B(reg, 8),
	R12(reg, 64), R12D(reg, 32), R12W(reg, 16), R12B(reg, 8),
	R13(reg, 64), R13D(reg, 32), R13W(reg, 16), R13B(reg, 8),
	R14(reg, 64), R14D(reg, 32), R14W(reg, 16), R14B(reg, 8),
	R15(reg, 64), R15D(reg, 32), R15W(reg, 16), R15B(reg, 8),
	
	// MM0, MM1, MM2, MM3, MM4, MM5, MM6, MM7,
	XMM0(128), XMM1(128), XMM2(128), XMM3(128), XMM4(128), XMM5(128), XMM6(128), XMM7(128),
	XMM8(128), XMM9(128), XMM10(128), XMM11(128), XMM12(128), XMM13(128), XMM14(128), XMM15(128),
	
	CF(flag, 1), /*--------*/ PF(flag, 1), /*----------*/ AF(flag, 1), ZF(flag, 1), SF(1), TF(flag, 1),
	IF(flag, 1), DF(flag, 1), OF(flag, 1), IOPL(flag, 1),
	
	ES(seg, 8), CS(seg, 8), SS(seg, 8), DS(seg, 8), FS(seg, 8), GS(seg, 8),
	
	CR0(controlr, 64), CR2(controlr, 64), CR3(controlr, 64), CR4(controlr, 64), CR8(controlr, 64),
	
	DR0(debugr, 64), DR1(debugr, 64), DR2(debugr, 64), DR3(debugr, 64), DR4(debugr, 64),
	DR5(debugr, 64), DR6(debugr, 64), DR7(debugr, 64),
	
	
	INVALID(null, -1)
	
	;
	
	public final AsmRegGroup group;
	public final int bits;
	private AsmReg(int bits) { this(null, bits); }
	private AsmReg(AsmRegGroup group, int bits) {
		this.group = group;
		this.bits = bits;
	}
}
