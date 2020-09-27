package hardcoded.compiler.assembler;

import static hardcoded.compiler.assembler.AssemblyConsts.*;

import java.util.ArrayList;
import java.util.List;

import hardcoded.assembly.impl.AsmInst;
import hardcoded.assembly.x86.AsmOpr;
import hardcoded.compiler.assembler.AssemblyConsts.AsmOp;
import hardcoded.compiler.assembler.AssemblyConsts.OprTy;

/**
 * This is x86 assembly instructions
 * 
 * https://wiki.osdev.org/X86-64_Instruction_Encoding
 * https://reverseengineering.stackexchange.com/questions/19693/how-many-registers-does-an-x86-64-cpu-actually-have
 * 
 * @author HardCoded
 */
public final class Assembly {
	public static final Assembly PREFIX = new Assembly(
		prf("REX",     opcode(0x40)),												// REX
		prf("REX.B",   opcode(0x41)),												// REX.B
		prf("REX.X",   opcode(0x42)),												// REX.X
		prf("REX.XB",  opcode(0x43)),												// REX.XB
		prf("REX.R",   opcode(0x44)),												// REX.R
		prf("REX.RB",  opcode(0x45)),												// REX.RB
		prf("REX.RX",  opcode(0x46)),												// REX.RX
		prf("REX.RXB", opcode(0x47)),												// REX.RXB
		prf("REX.W",   opcode(0x48)),												// 64 Bit operand override
		prf("REX.WB",  opcode(0x49)),												// REX.WB
		prf("REX.WX",  opcode(0x4A)),												// REX.WX
		prf("REX.WXB", opcode(0x4B)),												// REX.WXB
		prf("REX.WR",  opcode(0x4C)),												// REX.WR
		prf("REX.WRB", opcode(0x4D)),												// REX.WRB
		prf("REX.WRX", opcode(0x4E)),												// REX.WRX
		prf("REX.WRXB",opcode(0x4F)),												// REX.WRXB
		
		prf("FS", opcode(0x63)),													// FS segment override prefix
		prf("GS", opcode(0x65)),													// GS segment override prefix
		prf("", opcode(0x66)),														// Operand-size override prefix
		prf("", opcode(0x67)),														// Address-size override prefix
		

		prf("LOCK", opcode(0xF0)),													// ...
		
		null
	);
			
	public static final Assembly ALL = new Assembly(
		opr("ADD", opcode(0x00), flags("Lr"), OprTy.Eb, OprTy.Gb),					// ADD			r/m8			, r8
		opr("ADD", opcode(0x01), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// ADD			r/m16/32/64		, r16/32/64
		opr("ADD", opcode(0x02), flags("r"), OprTy.Gb, OprTy.Eb),					// ADD			r8				, r/m8
		opr("ADD", opcode(0x03), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// ADD			r16/32/64		, r/m16/32/64
		opr("ADD", opcode(0x04), OprTy.AL, OprTy.Ib),								// ADD			AL				, imm8
		opr("ADD", opcode(0x05), OprTy.rAX, OprTy.Ivds),							// ADD			rAX				, imm16/32
// 0x06
// 0x07
		opr("OR", opcode(0x08), flags("Lr"), OprTy.Eb, OprTy.Gb),					// OR			r/m8			, r8
		opr("OR", opcode(0x09), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// OR			r/m16/32/64		, r16/32/64
		opr("OR", opcode(0x0A), flags("r"), OprTy.Gb, OprTy.Eb),					// OR			r8				, r/m8
		opr("OR", opcode(0x0B), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// OR			r16/32/64		, r/m16/32/64
		opr("OR", opcode(0x0C), OprTy.AL, OprTy.Ib),								// OR			AL				, imm8
		opr("OR", opcode(0x0D), OprTy.rAX, OprTy.Ivds),								// OR			rAX				, imm16/32
// 0x0E
// 0x0F	Two byte opcodes
		opr("ADC", opcode(0x10), flags("Lr"), OprTy.Eb, OprTy.Gb),					// ADC			r/m8			, r8
		opr("ADC", opcode(0x11), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// ADC			r/m16/32/64		, r16/32/64
		opr("ADC", opcode(0x12), flags("r"), OprTy.Gb, OprTy.Eb),					// ADC			r8				, r/m8
		opr("ADC", opcode(0x13), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// ADC			r16/32/64		, r/m16/32/64
		opr("ADC", opcode(0x14), OprTy.AL, OprTy.Ib),								// ADC			AL				, imm8
		opr("ADC", opcode(0x15), OprTy.rAX, OprTy.Ivds),							// ADC			rAX				, imm16/32
// 0x16
// 0x17
		opr("SBB", opcode(0x18), flags("Lr"), OprTy.Eb, OprTy.Gb),					// SBB			r/m8			, r8
		opr("SBB", opcode(0x19), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// SBB			r/m16/32/64		, r16/32/64
		opr("SBB", opcode(0x1A), flags("r"), OprTy.Gb, OprTy.Eb),					// SBB			r8				, r/m8
		opr("SBB", opcode(0x1B), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// SBB			r16/32/64		, r/m16/32/64
		opr("SBB", opcode(0x1C), OprTy.AL, OprTy.Ib),								// SBB			AL				, imm8
		opr("SBB", opcode(0x1D), OprTy.rAX, OprTy.Ivds),							// SBB			rAX				, imm16/32
// 0x1E
// 0x1F
		opr("AND", opcode(0x20), flags("Lr"), OprTy.Eb, OprTy.Gb),					// AND			r/m8			, r8
		opr("AND", opcode(0x21), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// AND			r/m16/32/64		, r16/32/64
		opr("AND", opcode(0x22), flags("r"), OprTy.Gb, OprTy.Eb),					// AND			r8				, r/m8
		opr("AND", opcode(0x23), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// AND			r16/32/64		, r/m16/32/64
		opr("AND", opcode(0x24), OprTy.AL, OprTy.Ib),								// AND			AL				, imm8
		opr("AND", opcode(0x25), OprTy.rAX, OprTy.Ivds),							// AND			rAX				, imm16/32
// 0x26
// 0x27
		opr("SUB", opcode(0x28), flags("Lr"), OprTy.Eb, OprTy.Gb),					// SUB			r/m8			, r8
		opr("SUB", opcode(0x29), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// SUB			r/m16/32/64		, r16/32/64
		opr("SUB", opcode(0x2A), flags("r"), OprTy.Gb, OprTy.Eb),					// SUB			r8				, r/m8
		opr("SUB", opcode(0x2B), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// SUB			r16/32/64		, r/m16/32/64
		opr("SUB", opcode(0x2C), OprTy.AL, OprTy.Ib),								// SUB			AL				, imm8
		opr("SUB", opcode(0x2D), OprTy.rAX, OprTy.Ivds),							// SUB			rAX				, imm16/32
// 0x2E
// 0x2F
		opr("XOR", opcode(0x30), flags("Lr"), OprTy.Eb, OprTy.Gb),					// XOR			r/m8			, r8
		opr("XOR", opcode(0x31), flags("Lr"), OprTy.Evqp, OprTy.Gvqp),				// XOR			r/m16/32/64		, r16/32/64
		opr("XOR", opcode(0x32), flags("r"), OprTy.Gb, OprTy.Eb),					// XOR			r8				, r/m8
		opr("XOR", opcode(0x33), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// XOR			r16/32/64		, r/m16/32/64
		opr("XOR", opcode(0x34), OprTy.AL, OprTy.Ib),								// XOR			AL				, imm8
		opr("XOR", opcode(0x35), OprTy.rAX, OprTy.Ivds),							// XOR			rAX				, imm16/32
// 0x36
// 0x37
		opr("CMP", opcode(0x38), flags("r"), OprTy.Eb, OprTy.Gb),					// CMP			r/m8			, r8
		opr("CMP", opcode(0x39), flags("r"), OprTy.Evqp, OprTy.Gvqp),				// CMP			r/m16/32/64		, r16/32/64
		opr("CMP", opcode(0x3A), flags("r"), OprTy.Gb, OprTy.Eb),					// CMP			r8				, r/m8
		opr("CMP", opcode(0x3B), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// CMP			r16/32/64		, r/m16/32/64
		opr("CMP", opcode(0x3C), OprTy.AL, OprTy.Ib),								// CMP			AL				, imm8
		opr("CMP", opcode(0x3D), OprTy.rAX, OprTy.Ivds),							// CMP			rAX				, imm16/32
// 0x3E
// 0x3F
		opr("INC", opcode(0x40), OprTy.Zv),											// INC			r16/32
		opr("DEC", opcode(0x48), OprTy.Zv),											// DEC			r16/32
		opr("PUSH", opcode(0x50), OprTy.Zv),										// PUSH			r16/32
		opr("PUSH", opcode(0x50), flags("E"), OprTy.Zvq),							// PUSH			r64/16
		opr("POP", opcode(0x58), OprTy.Zv),											// POP			r16/32
		opr("POP", opcode(0x58), flags("E"), OprTy.Zvq),							// POP			r64/16
// 0x60
// 0x61
// 0x62
		opr("PUSH", opcode(0x68), OprTy.Ivs),										// PUSH			imm16/32
		opr("IMUL", opcode(0x69), flags("r"), OprTy.Gvqp, OprTy.Evqp, OprTy.Ivds),	// IMUL			r16/32/64		, r/m16/32/64	, imm16/32
		opr("PUSH", opcode(0x6A), OprTy.Ibss),										// PUSH			imm8
		opr("IMUL", opcode(0x6B), flags("r"), OprTy.Gvqp, OprTy.Evqp, OprTy.Ibs),	// IMUL			r16/32/64		, r/m16/32/64	, imm8
		opr("INSB", opcode(0x6C), OprTy.Yb, OprTy.DX),								// INSB			m8				, DX
		opr("INSW", opcode(0x6D), OprTy.Ywo, OprTy.DX),								// INSW			m16				, DX		TODO: COMBINE
		opr("INTD", opcode(0x6D), OprTy.Ydo, OprTy.DX),								// INSD			m32				, DX
		opr("OUTSB", opcode(0x6E), OprTy.DX, OprTy.Xb),								// OUTSB		DX				, m8
		opr("OUTSW", opcode(0x6F), OprTy.DX, OprTy.Xwo),							// OUTSW		DX				, m16		TODO: COMBINE
		opr("OUTSD", opcode(0x6F), OprTy.DX, OprTy.Xdo),							// OUTSD		DX				, m32
		
		opr("JO", opcode(0x70), OprTy.Jbs),											// JO			rel8/16						Jump if (OF=1)
		opr("JNO", opcode(0x71), OprTy.Jbs),										// JNO			rel8/16						Jump if (OF=0)
		opr("JC", opcode(0x72), OprTy.Jbs),											// JC			rel8/16						Jump if (CF=1)
		opr("JNC", opcode(0x73), OprTy.Jbs),										// JNC			rel8/16						Jump if (CF=0)
		opr("JZ", opcode(0x74), OprTy.Jbs),											// JZ			rel8/16						Jump if (ZF=1)
		opr("JNZ", opcode(0x75), OprTy.Jbs),										// JNZ			rel8/16						Jump if (ZF=0)
		opr("JNA", opcode(0x76), OprTy.Jbs),										// JNA			rel8/16						Jump if (CF=1 OR  ZF=1)
		opr("JA", opcode(0x77), OprTy.Jbs),											// JA			rel8/16						Jump if (CF=0 AND ZF=0)
		opr("JS", opcode(0x78), OprTy.Jbs),											// JS			rel8/16						Jump if (SF=1)
		opr("JNS", opcode(0x79), OprTy.Jbs),										// JNS			rel8/16						Jump if (SF=0)
		opr("JP", opcode(0x7A), OprTy.Jbs),											// JP			rel8/16						Jump if (PF=1)
		opr("JNP", opcode(0x7B), OprTy.Jbs),										// JNP			rel8/16						Jump if (PF=0)
		opr("JL", opcode(0x7C), OprTy.Jbs),											// JL			rel8/16						Jump if (SF != OF)
		opr("JNL", opcode(0x7D), OprTy.Jbs),										// JNL			rel8/16						Jump if (SF == OF)
		opr("JNG", opcode(0x7E), OprTy.Jbs),										// JNG			rel8/16						Jump if (ZF=1 OR  SF != OF)
		opr("JG", opcode(0x7F), OprTy.Jbs),											// JG			rel8/16						Jump if (ZF=0 AND SF == OF)
		
		opr("ADD", opcode(0x80), flags("Le0"), OprTy.Eb, OprTy.Ib),					// ADD			r/m8			, imm8
		opr("OR",  opcode(0x80), flags("Le1"), OprTy.Eb, OprTy.Ib),					// OR			r/m8			, imm8
		opr("ADC", opcode(0x80), flags("Le2"), OprTy.Eb, OprTy.Ib),					// ADC			r/m8			, imm8
		opr("SBB", opcode(0x80), flags("Le3"), OprTy.Eb, OprTy.Ib),					// SBB			r/m8			, imm8
		opr("AND", opcode(0x80), flags("Le4"), OprTy.Eb, OprTy.Ib),					// AND			r/m8			, imm8
		opr("SUB", opcode(0x80), flags("Le5"), OprTy.Eb, OprTy.Ib),					// SUB			r/m8			, imm8
		opr("XOR", opcode(0x80), flags("Le6"), OprTy.Eb, OprTy.Ib),					// XOR			r/m8			, imm8
		opr("CMP", opcode(0x80), flags("e7"), OprTy.Eb, OprTy.Ib),					// CMP			r/m8			, imm8
		
		opr("ADD", opcode(0x81), flags("Le0"), OprTy.Evqp, OprTy.Ivds),				// ADD			r/m16/32/64		, imm16/32
		opr("OR",  opcode(0x81), flags("Le1"), OprTy.Evqp, OprTy.Ivds),				// OR			r/m16/32/64		, imm16/32
		opr("ADC", opcode(0x81), flags("Le2"), OprTy.Evqp, OprTy.Ivds),				// ADC			r/m16/32/64		, imm16/32
		opr("SBB", opcode(0x81), flags("Le3"), OprTy.Evqp, OprTy.Ivds),				// SBB			r/m16/32/64		, imm16/32
		opr("AND", opcode(0x81), flags("Le4"), OprTy.Evqp, OprTy.Ivds),				// AND			r/m16/32/64		, imm16/32
		opr("SUB", opcode(0x81), flags("Le5"), OprTy.Evqp, OprTy.Ivds),				// SUB			r/m16/32/64		, imm16/32
		opr("XOR", opcode(0x81), flags("Le6"), OprTy.Evqp, OprTy.Ivds),				// XOR			r/m16/32/64		, imm16/32
		opr("CMP", opcode(0x81), flags("e7"), OprTy.Evqp, OprTy.Ivds),				// CMP			r/m16/32/64		, imm16/32
// 0x82
		opr("ADD", opcode(0x83), flags("Le0"), OprTy.Evqp, OprTy.Ibs),				// ADD		 	r/m16/32/64		, imm8
		opr("OR",  opcode(0x83), flags("Le1"), OprTy.Evqp, OprTy.Ibs),				// OR			r/m16/32/64		, imm8
		opr("ADC", opcode(0x83), flags("Le2"), OprTy.Evqp, OprTy.Ibs),				// ADC			r/m16/32/64		, imm8
		opr("SBB", opcode(0x83), flags("Le3"), OprTy.Evqp, OprTy.Ibs),				// SBB			r/m16/32/64		, imm8
		opr("AND", opcode(0x83), flags("Le4"), OprTy.Evqp, OprTy.Ibs),				// AND			r/m16/32/64		, imm8
		opr("SUB", opcode(0x83), flags("Le5"), OprTy.Evqp, OprTy.Ibs),				// SUB			r/m16/32/64		, imm8
		opr("XOR", opcode(0x83), flags("Le6"), OprTy.Evqp, OprTy.Ibs),				// XOR			r/m16/32/64		, imm8
		opr("CMP", opcode(0x83), flags("e7"), OprTy.Evqp, OprTy.Ibs),				// CMP			r/m16/32/64		, imm8
		
		opr("TEST", opcode(0x84), flags("r"), OprTy.Eb, OprTy.Gb),					// TEST			r/m8			, r8
		opr("TEST", opcode(0x85), flags("r"), OprTy.Evqp, OprTy.Gvqp),				// TEST			r/m16/32/64		, r16/32/64
		opr("XCHG", opcode(0x86), flags("Lr"), OprTy.Gb, OprTy.Eb),					// XCHG			r8				, r/m8
		opr("XCHG", opcode(0x87), flags("Lr"), OprTy.Gvqp, OprTy.Evqp),				// XCHG			r16/32/64		, r/m16/32/64
		
		opr("MOV", opcode(0x88), flags("r"), OprTy.Eb, OprTy.Gb),					// MOV			r/m8			, r8
		opr("MOV", opcode(0x89), flags("r"), OprTy.Evqp, OprTy.Gvqp),				// MOV			r/m16/32/64		, r16/32/64
		opr("MOV", opcode(0x8A), flags("r"), OprTy.Gb, OprTy.Eb),					// MOV			r8				, r/m8
		opr("MOV", opcode(0x8B), flags("r"), OprTy.Gvqp, OprTy.Evqp),				// MOV			r16/32/64		, r/m16/32/64
		
		opr("MOV", opcode(0x8C), flags("r"), OprTy.Mw, OprTy.Sw),					// MOV			m16				, s16
		opr("MOV", opcode(0x8C), flags("r"), OprTy.Rvqp, OprTy.Sw),					// MOV			r16/32/64		, s16
		
		opr("LEA", opcode(0x8D), flags("r"), OprTy.Gvqp, OprTy.M),					// LEA			r16/32/64		, m
		opr("MOV", opcode(0x8E), flags("r"), OprTy.Sw, OprTy.Ew),					// MOV			s16				, r/m16
		opr("MOV", opcode(0x8F), flags("e0"), OprTy.Evq),							// POP			r/m64/16
		
		opr("XCHR", opcode(0x90), OprTy.Zvqp, OprTy.rAX),							// LEA			r16/32/64		, rAX
		opr("NOP", opcode(0x90)),													// NOP
		
		// Controlled by size attribute
		opr("CBW", opcode(0x98), OprTy.AX, OprTy.AL),								// CBW			AX				, AL
		opr("CWDE", opcode(0x98), OprTy.EAX, OprTy.AX),								// CWDE			EAX				, AX
		opr("CDQE", opcode(0x98), OprTy.RAX, OprTy.EAX),							// CDEQ			RAX				, EAX
// 0x99
		
		opr("CALLF", opcode(0x9A), OprTy.Ap),										// CALLF		ptr32/48
// ....
		opr("MOV", opcode(0xA0), OprTy.AL, OprTy.Ob),								// MOV			AL				, moffs8
		opr("MOV", opcode(0xA1), OprTy.RAX, OprTy.Ovqp),							// MOV			RAX				, moffs16/32/64
		opr("MOV", opcode(0xA2), OprTy.Ob, OprTy.AL),								// MOV			moffs8			, AL
		opr("MOV", opcode(0xA3), OprTy.Ovqp, OprTy.rAX),							// MOV			moffs16/32/64	, rAX
// ....
		opr("TEST", opcode(0xA8), OprTy.AL, OprTy.Ib),								// TEST			AL				, imm8
		opr("TEST", opcode(0xA9), OprTy.rAX, OprTy.Ivds),							// TEST			rAX				, imm16/32
// ....
		opr("MOV", opcode(0xB0), OprTy.Zb, OprTy.Ib),								// MOV			r8				, imm8
		opr("MOV", opcode(0xB8), OprTy.Zvqp, OprTy.Ivqp),							// MOV			r16/32/64		, imm16/32/64
		
		opr("ROL", opcode(0xC0), flags("e0"), OprTy.Eb, OprTy.Ib),					// ROL			r/m8			, imm8
		opr("ROR", opcode(0xC0), flags("e1"), OprTy.Eb, OprTy.Ib),					// ROR			r/m8			, imm8
		opr("RCL", opcode(0xC0), flags("e2"), OprTy.Eb, OprTy.Ib),					// RCL			r/m8			, imm8
		opr("RCR", opcode(0xC0), flags("e3"), OprTy.Eb, OprTy.Ib),					// RCR			r/m8			, imm8
		opr("SHL", opcode(0xC0), flags("e4"), OprTy.Eb, OprTy.Ib),					// SHL			r/m8			, imm8
		opr("SHR", opcode(0xC0), flags("e5"), OprTy.Eb, OprTy.Ib),					// SHR			r/m8			, imm8
		opr("SAL", opcode(0xC0), flags("e6"), OprTy.Eb, OprTy.Ib),					// SAL			r/m8			, imm8
		opr("SAR", opcode(0xC0), flags("e7"), OprTy.Eb, OprTy.Ib),					// SAR			r/m8			, imm8
		
		opr("ROL", opcode(0xC1), flags("e0"), OprTy.Evqp, OprTy.Ib),				// ROL			r/m16/32/64		, imm8
		opr("ROR", opcode(0xC1), flags("e1"), OprTy.Evqp, OprTy.Ib),				// ROR			r/m16/32/64		, imm8
		opr("RCL", opcode(0xC1), flags("e2"), OprTy.Evqp, OprTy.Ib),				// RCL			r/m16/32/64		, imm8
		opr("RCR", opcode(0xC1), flags("e3"), OprTy.Evqp, OprTy.Ib),				// RCR			r/m16/32/64		, imm8
		opr("SHL", opcode(0xC1), flags("e4"), OprTy.Evqp, OprTy.Ib),				// SHL			r/m16/32/64		, imm8
		opr("SHR", opcode(0xC1), flags("e5"), OprTy.Evqp, OprTy.Ib),				// SHR			r/m16/32/64		, imm8
		opr("SAL", opcode(0xC1), flags("e6"), OprTy.Evqp, OprTy.Ib),				// SAL			r/m16/32/64		, imm8
		opr("SAR", opcode(0xC1), flags("e7"), OprTy.Evqp, OprTy.Ib),				// SAR			r/m16/32/64		, imm8

		opr("RETN", opcode(0xC2), OprTy.Iw),										// RETN			imm16
		opr("RETN", opcode(0xC3)),													// RETN
// 0xC4
// 0xC5
		opr("MOV", opcode(0xC6), flags("e0"), OprTy.Eb, OprTy.Ib),					// MOV			r/m8			, imm8
		opr("MOV", opcode(0xC7), flags("e0"), OprTy.Evqp, OprTy.Ivds),				// MOV			r/m16/32/64		, imm16/32
// 0xC8 NOTE: implement opcode
// 0xC9 NOTE: implement opcode
		opr("RETF", opcode(0xCA), OprTy.Iw),										// RETF			imm16
		opr("RETF", opcode(0xCB)),													// RETF
//		opr("INT", opcode(0xCC), OprTy.Fv),											// INT			3				, flags16/32
		opr("INT", opcode(0xCD), OprTy.Ib, OprTy.Fv),								// INT			imm8			, flags16/32
// ....
		opr("CALL", opcode(0xE8), OprTy.Jvds),										// CALL			rel16/32
		opr("JMP", opcode(0xE9), OprTy.Jvds),										// JMP			rel16/32
// 0xEA
		opr("JMP", opcode(0xEB), OprTy.Jbs),										// JMP			rel8
		opr("IN", opcode(0xEC), OprTy.AL, OprTy.DX),								// IN			AL				, DX
		opr("IN", opcode(0xED), OprTy.eAX, OprTy.DX),								// IN			eAX				, DX
		opr("OUT", opcode(0xEE), OprTy.DX, OprTy.AL),								// OUT			DX				, AL
		opr("OUT", opcode(0xEF), OprTy.DX, OprTy.eAX),								// OUT			DX				, eAX
// 0xF1 NOTE: implement opcode
// 0xF2 NOTE: implement opcode
// 0xF3 NOTE: implement opcode
		opr("HLT", opcode(0xF4), flags("l0")),										// HLT
// 0xF5 NOTE: implement opcode
		opr("TEST", opcode(0xF6), flags("e0"), OprTy.Eb, OprTy.Ib),					// TEST			r/m8			, imm8
//	   dopr("TEST", opcode(0xF6), flags("e1"), OprTy.Eb, OprTy.Ib),					// TEST			r/m8			, imm8
		opr("NOT",  opcode(0xF6), flags("Le2"), OprTy.Eb),							// NOT			r/m8
		opr("NEG",  opcode(0xF6), flags("Le3"), OprTy.Eb),							// NEG			r/m8
		opr("MUL",  opcode(0xF6), flags("e4"), OprTy.AX, OprTy.AL, OprTy.Eb),		// MUL			AX				, AL			, r/m8
		opr("IMUL", opcode(0xF6), flags("e5"), OprTy.AX, OprTy.AL, OprTy.Eb),		// IMUL			AX				, AL			, r/m8
		opr("DIV",  opcode(0xF6), flags("e6"), OprTy.AL,OprTy.AH,OprTy.AX,OprTy.Eb),// DIV			AL				, AH			, AX			, r/m8
		opr("IDIV", opcode(0xF6), flags("e7"), OprTy.AL,OprTy.AH,OprTy.AX,OprTy.Eb),// IDIV			AL				, AH			, AX			, r/m8
		
		opr("TEST", opcode(0xF7), flags("e0"), OprTy.Evqp, OprTy.Ivds),				// TEST			r/m16_32_64		, imm16/32
//	   dopr("TEST", opcode(0xF7), flags("e1"), OprTy.Evqp, OprTy.Ivds),				// TEST			r/m16_32_64		, imm16/32
		opr("NOT",  opcode(0xF7), flags("Le2"), OprTy.Evqp),						// NOT			r/m16_32_64
		opr("NEG",  opcode(0xF7), flags("Le3"), OprTy.Evqp),						// NEG			r/m16_32_64
		opr("MUL",  opcode(0xF7), flags("e4"), OprTy.rDX, OprTy.rAX, OprTy.Evqp),	// MUL			rDX				, rAX			, r/m16_32_64
		opr("IMUL", opcode(0xF7), flags("e5"), OprTy.rDX, OprTy.rAX, OprTy.Evqp),	// IMUL			rDX				, rAX			, r/m16_32_64
		opr("DIV",  opcode(0xF7), flags("e6"), OprTy.rDX, OprTy.rAX, OprTy.Evqp),	// DIV			rDX				, rAX			, r/m16_32_64
		opr("IDIV", opcode(0xF7), flags("e7"), OprTy.rDX, OprTy.rAX, OprTy.Evqp),	// IDIV			rDX				, rAX			, r/m16_32_64
		
		opr("CLC", opcode(0xF8)),													// CLC
		opr("STC", opcode(0xF9)),													// STC
		opr("CLI", opcode(0xFA)),													// CLI
		opr("STI", opcode(0xFB)),													// STI
		opr("CLD", opcode(0xFC)),													// CLD
		opr("STD", opcode(0xFD)),													// STD
		
		opr("INC", opcode(0xFE), flags("e0"), OprTy.Eb),							// INC			r/m8
		opr("DEC", opcode(0xFE), flags("e1"), OprTy.Eb),							// DEC			r/m8
		opr("INC", opcode(0xFF), flags("e0"), OprTy.Evqp),							// INC			r/m16/32/64
		opr("DEC", opcode(0xFF), flags("e1"), OprTy.Evqp),							// DEC			r/m16/32/64
		
		opr("CALL", opcode(0xFF), flags("e2"), OprTy.Ev),							// CALL			r/m16/32
		opr("CALL", opcode(0xFF), flags("Ee2"), OprTy.Eq),							// CALL			r/m64
		opr("CALLF", opcode(0xFF), flags("e3"), OprTy.Evqp),						// CALLF		r/m16/32/64
		opr("JMP", opcode(0xFF), flags("e4"), OprTy.Ev),							// JMP			r/m16/32
		opr("JMP", opcode(0xFF), flags("Ee4"), OprTy.Eq),							// JMP			r/m64
//		opr("JMPF", opcode(0xFF), flags("e5"), OprTy.Evqp),							// JMPF			r/m16/32/64
		opr("PUSH", opcode(0xFF), flags("e6"), OprTy.Ev),							// PUSH			r/m16/32
		opr("PUSH", opcode(0xFF), flags("Ee6"), OprTy.Evq),							// PUSH			r/m64/16
		
		null
	);
	
//	public static final Assembly MOV = new Assembly(
//		opr("MOV", opcode(0x0F, 0x20), flags("Erl0"), OprTy.r64, OprTy.CRn),		// MOV			r64				, CRn
//		opr("MOV", opcode(0x0F, 0x21), flags("Erl0"), OprTy.r64, OprTy.DRn),		// MOV			r64				, DRn
//		opr("MOV", opcode(0x0F, 0x22), flags("Erl0"), OprTy.CRn, OprTy.r64),		// MOV			CRn				, r64
//		opr("MOV", opcode(0x0F, 0x23), flags("Erl0"), OprTy.DRn, OprTy.r64)			// MOV			DRn				, r64
//	);
//	
//	// DONE
//	public static final Assembly PUSH = new Assembly(
//		opr("PUSH", opcode(0x0F, 0xA0), OprTy.FS),									// PUSH			FS
//		opr("PUSH", opcode(0x0F, 0xA8), OprTy.GS)									// PUSH			GS
//	);
	
	public static List<AsmOp> lookup(AsmInst inst) {
		List<AsmOp> list = new ArrayList<>();
		
		for(AsmOp op : Assembly.ALL.items) {
			if(op == null) continue;
			
			if(op.getMnemonic().equals(inst.getMnemonic().toString())) {
				list.add(op);
			}
		}
		
		if(list.isEmpty()) return null;
		int ops = inst.getNumOperators();
		
		for(int i = 0; i < list.size(); i++) {
			AsmOp op = list.get(i);
			
			if(op.getNumOperands() != ops) {
				list.remove(i);
				i--;
				continue;
			}
			
			boolean matches = true;
			for(int j = 0; j < ops; j++) {
				OprTy type = op.getOperand(j);
				AsmOpr opr = inst.getOperator(j);
				if(!matches(type, opr)) {
					matches = false;
					break;
				}
			}
			
			if(!matches) {
				list.remove(i);
				i--;
				continue;
			}
		}
		
		return list;
	}
	
	private static boolean matches(OprTy type, AsmOpr opr) {
		boolean sizeMatches = type.hasSize(opr.getSize());
		
		if(type.hasData()) {
			// A -> ptr
			// O -> moffs
			// J -> rel
			
			char c = type.name().charAt(0);
			
			
			switch(c) {
				case 'J':
				case 'O':
				case 'I': {
					return opr.isImmediate() && sizeMatches;
				}
				
				case 'M': {
					return opr.isMemory() && sizeMatches;
				}
				case 'E': {
					if(opr.isMemory()) return sizeMatches;
				}
				case 'G':
				case 'Z':
				case 'R': return opr.isRegister() && sizeMatches;
			}
		} else {
			if(!opr.isRegister()) return false;
			String name = type.name();
			
			if(Character.isLowerCase(name.charAt(0))) {
				// Variable size.
				
				String cmps = opr.toString();
				return cmps.endsWith(name.substring(1))
					&& sizeMatches;
			}
			
			return opr.toString().equals(type.name())
				&& sizeMatches;
		}
		
		return true;
	}
	
	
	
	public AsmOp[] items;
	private Assembly(AsmOp... objects) {
		this.items = objects;
	}
	
	
	public static void main(String[] args) {
		for(AsmOp asm : ALL.items) {
			if(asm == null) continue;
			
			System.out.println(asm.toComplexString());
		}
	}
}
