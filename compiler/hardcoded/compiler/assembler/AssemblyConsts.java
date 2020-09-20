package hardcoded.compiler.assembler;

public final class AssemblyConsts {
	private AssemblyConsts() {}
	
	// Opperand values offset
	private static final int OPR_OFFSET = (1 << 8);
	
	
	/** Error flags */
	public static final int OF = (1 << 0);
	public static final int DF = (1 << 1);
	public static final int IF = (1 << 2);
	public static final int TF = (1 << 3);
	public static final int SF = (1 << 4);
	public static final int ZF = (1 << 5);
	public static final int AF = (1 << 6);
	public static final int PF = (1 << 7);
	public static final int CF = (1 << 8);
	
	
	
	/** X.REG prefix */
	public static final int X_REG_0000 = 0; // AL		AX		EAX		RAX		ST0		MMX0	XMM0	YMM0	ES	CR0		DR0
	public static final int X_REG_0001 = 1; // CL		CX		ECX		RCX		ST1		MMX1	XMM1	YMM1	CS	CR1		DR1
	public static final int X_REG_0010 = 2; // DL		DX		EDX		RDX		ST2		MMX2	XMM2	YMM2	SS	CR2		DR2
	public static final int X_REG_0011 = 3; // BL		BX		EBX		RBX		ST3		MMX3	XMM3	YMM3	DS	CR3		DR3
	public static final int X_REG_0100 = 4; // AH, SPL	SP		ESP		RSP		ST4		MMX4	XMM4	YMM4	FS	CR4		DR4
	public static final int X_REG_0101 = 5; // CH, BPL	BP		EBP		RBP		ST5		MMX5	XMM5	YMM5	GS	CR5		DR5
	public static final int X_REG_0110 = 6; // DH, SIL	SI		ESI		RSI		ST6		MMX6	XMM6	YMM6	-	CR6		DR6
	public static final int X_REG_0111 = 7; // BH, DIL	DI		EDI		RDI		ST7		MMX7	XMM7	YMM7	-	CR7		DR7
	public static final int X_REG_1000 = 8; // R8L		R8W		R8D		R8		-		MMX0	XMM8	YMM8	ES	CR8		DR8
	public static final int X_REG_1001 = 9; // R9L 		R9W		R9D		R9		-		MMX1	XMM9	YMM9	CS	CR9		DR9
	public static final int X_REG_1010 =10; // R10L		R10W	R10D	R10		-		MMX2	XMM10	YMM10	SS	CR10	DR10
	public static final int X_REG_1011 =11; // R11L		R11W	R11D	R11		-		MMX3	XMM11	YMM11	DS	CR11	DR11
	public static final int X_REG_1100 =12; // R12L		R12W	R12D	R12		-		MMX4	XMM12	YMM12	FS	CR12	DR12
	public static final int X_REG_1101 =13; // R13L		R13W	R13D	R13		-		MMX5	XMM13	YMM13	GS	CR13	DR13
	public static final int X_REG_1110 =14; // R14L		R14W	R14D	R14		-		MMX6	XMM14	YMM14	-	CR14	DR14
	public static final int X_REG_1111 =15; // R15L		R15W	R15D	R15		-		MMX7	XMM15	YMM15	-	CR15	DR15
	
	
	private static int decode_octal(int octal_binary) {
		return (((octal_binary &    07) != 0) ? 1:0)
			 | (((octal_binary &   070) != 0) ? 2:0)
			 | (((octal_binary &  0700) != 0) ? 4:0)
			 | (((octal_binary & 07000) != 0) ? 8:0);
	}
	
	/**
	 * 
	 *<pre>  7   6   5   4   3   2   1   0
	 *+---+---+---+---+---+---+---+---+
	 *|  mod  |    reg    |    rm     |
	 *+---+---+---+---+---+---+---+---+</pre>
	 *
	 *<a href="https://wiki.osdev.org/X86-64_Instruction_Encoding#ModR.2FM">ModR/M</a>
	 */
	public static final Object $MODRM = null;
	
	/**
	 * Used for linking opcodes with their operand types.
	 *
	 * @author HardCoded
	 */
	public static enum OperandType {
		/** Using a register operand */
		REG,
		/** Using a ModR/M operand */
		MRM,
		/** Using a Immediate value operand */
		IMM,
		
		;
		
		public final int id;
		private OperandType() {
			id = ordinal();
		}
		
		/**
		 * Create a new mask that tells the assembler
		 * what input a instruction is expecting.
		 * 
		 * @param	octal_binary
		 * 			the bit inputs written as a octal binary value.
		 *			<br><code>01234</code> is treated the same as
		 *			calling <code>get(1, 2, 3, 4)</code>
		 * 
		 * @return	the constructed bit mask
		 * 
		 * @see #get(int, int, int, int)
		 */
		public int get(int octal_binary) {
			return get(
				octal_binary &    07,
				octal_binary &   070,
				octal_binary &  0700,
				octal_binary & 07000
			);
		}
		
		/**
		 * Create a new mask that tells the assembler
		 * what input a instruction is expecting.
		 * 
		 * @param	r8	enable 8 bit wide input
		 * @param	r16	enable 16 bit wide input
		 * @param	r32	enable 32 bit wide input
		 * @param	r64	enable 64 bit wide input
		 * 
		 * @return	the constructed bit mask
		 */
		public int get(int r8, int r16, int r32, int r64) {
			int mask = ((r8 != 0) ? 1:0)
					| ((r16 != 0) ? 2:0)
					| ((r32 != 0) ? 4:0)
					| ((r64 != 0) ? 8:0);
			
			return (mask * OPR_OFFSET) | id;
		}
	}
	
	public static enum OprTy {
		rel8			(1, 0, 0, 0),
		rel16_32		(0, 1, 1, 0),
		
		r8				(1, 0, 0, 0),
		r16				(0, 1, 0, 0),
		r32				(0, 0, 1, 0),
		r64				(0, 0, 0, 1),
		r16_32			(0, 1, 1, 0),
		r32_64			(0, 0, 1, 1),
		r64_16			(0, 1, 0, 1),
		r16_32_64		(0, 1, 1, 1),
		
		rm8				(1, 0, 0, 0),
		rm16			(0, 1, 0, 0),
		rm32			(0, 0, 1, 0),
		rm64			(0, 0, 0, 1),
		rm16_32			(0, 1, 1, 0),
		rm64_16			(1, 0, 0, 1),
		rm16_32_64		(0, 1, 1, 1),
		
		imm8			(1, 0, 0, 0),
		imm16			(0, 1, 0, 0),
		imm16_32		(0, 1, 1, 0),
		imm16_32_64		(0, 1, 1, 1),
		
		m8				(1, 0, 0, 0),
		m16				(0, 1, 0, 0),
		m32				(0, 0, 1, 0),
		m64				(0, 0, 0, 1),
		m16_32			(0, 1, 1, 0),
		m16_32_64		(0, 1, 1, 1),
		
		moffs8			(1, 0, 0, 0),
		moffs16_32_64	(0, 1, 1, 1),
		
		Sreg			(0, 0, 0, 0),
		CRn				(0, 0, 0, 0), // Control register
		DRn				(0, 0, 0, 0), // Debug register
		
		
		AL				(1, 0, 0, 0),
		AH				(1, 0, 0, 0),
		AX				(0, 1, 0, 0),
		EAX				(0, 0, 1, 0),
		RAX				(0, 0, 0, 1),
		
		rAX				(0, 1, 1, 1), //	RAX, EAX, AX
		eAX				(1, 1, 1, 0), //	EAX, AX, AL
		rDX				(0, 1, 1, 1), //	RDX, EDX, DX
		
		//ECX				(0, 0, 1, 0),
		//RCX				(0, 0, 0, 1),

		DX				(0, 1, 0, 0),
		EDX				(0, 0, 1, 0),
		RDX				(0, 0, 0, 1),
		
		FS				(0, 0, 0, 0),
		GS				(0, 0, 0, 0),
		

		Fv				(0, 1, 1, 0), //	flags16/32
		Ap				(0, 0, 1, 1), //	ptr32/64			32 or 48 bit
		Ob				(1, 0, 0, 0), //	moffs8
		Ovqp			(0, 1, 1, 1), //	moffs16/32/64
		
		Zb				(1, 0, 0, 0), //	r8
		Zv				(0, 1, 1, 0), //	r16/32
		Zvq				(0, 1, 0, 1), //	r64/16				QWORD default otherwise WORD
		Zvqp			(0, 1, 1, 1), //	r16/32/64
		
		Eb				(1, 0, 0, 0), //	r/m8
		Ew				(0, 1, 0, 0), //	r/m16
		Eq				(0, 0, 0, 1), //	r/m64
		Ev				(0, 1, 1, 0), //	r/m16/32
		Evq				(0, 1, 0, 1), //	r/m64/16			QWORD default otherwise WORD
		Evqp			(0, 1, 1, 1), //	r/m16/32/64
		
		Gb				(1, 0, 0, 0), //	r8
		Gvqp			(0, 1, 1, 1), //	r16/32/64
		
		Yb				(1, 0, 0, 0), //	m8					memory addressed by the ES:eDI
		Ywo				(0, 1, 0, 0), //	m16
		Ydo				(0, 0, 1, 0), //	m32
		
		Xb				(1, 0, 0, 0), //	m8					memory addressed by the DS:eSI
		Xwo				(0, 1, 0, 0), //	m16
		Xdo				(0, 0, 1, 0), //	m32
		
		M				(0, 0, 0, 0), //	m
		Mw				(0, 1, 0, 0), //	m16
		Sw				(0, 1, 0, 0), //	seg16
		Rvqp			(0, 1, 1, 1), //	r16/32/64
		
		Jbs				(1, 0, 0, 0), //	rel8				sign-extended to the size of the dest oprerand
		Jvds			(0, 1, 1, 0), //	rel16/32			sign-extended to 64 bits for 64-bit operand size
		
		Ib				(1, 0, 0, 0), //	imm8
		Iw				(0, 1, 0, 0), //	imm16
		Ibs				(1, 0, 0, 0), //	imm8				sign-extended to the size of the dest oprerand
		Ibss			(1, 0, 0, 0), //	imm8				sign-extended to the size of the stack pointer
		Ivs				(0, 1, 1, 0), //	imm16/32
		Ivds			(0, 1, 1, 0), //	imm16/32			sign-extended to 64 bits for 64-bit operand size
		Ivqp			(0, 1, 1, 1), //	imm16/32/64
		
		;
		
		// How this will will be printed using toString();
		private final String string;
		private final boolean encodes_data;
		
		public final int mask;
		private OprTy(int r8, int r16, int r32, int r64) {
			mask = ((r8 != 0) ? 1:0)
				| ((r16 != 0) ? 2:0)
				| ((r32 != 0) ? 4:0)
				| ((r64 != 0) ? 8:0);
			
			String str = name()
				.replace('_', '/');
			
			if(str.startsWith("rm")) {
				string = "r/m" + str.substring(2);
				encodes_data = false;
			} else if(
				str.equals("GS") || str.equals("AX") || str.equals("AL")
			|| str.equals("AH") || str.equals("EAX") || str.equals("RAX")
			|| str.equals("RDX")) {
				string = str;
				encodes_data = false;
			} else {
				String size = str.substring(1);
				
				if(size.equals("bss")) size = "8"; // TODO
				if(size.equals("bs")) size = "8"; // TODO
				if(size.equals("b")) size = "8";
				
				if(size.equals("w")) size = "16";
				if(size.equals("wo")) size = "16/32";
				if(size.equals("do")) size = "32/64";
				if(size.equals("vqp")) size = "16/32/64";
				if(size.equals("vds")) size = "16/32";
				if(size.equals("vq")) size = "64/16";
				if(size.equals("vs")) size = "16/32";
				if(size.equals("v")) size = "16/32";
				
				if(size.equals("p")) size = "32/48";
				if(size.equals("q")) size = "64";
				
				String nmn = null;
				switch(str.charAt(0)) {
					case 'I': nmn = "imm"; break;
					case 'O': nmn = "moffs"; break;
					case 'J': nmn = "rel"; break;
					case 'R':
					case 'Z': nmn = "r"; break;
					case 'F': nmn = "flags"; break;
					case 'A': nmn = "ptr"; break;
					case 'S': nmn = "seg"; break;
					case 'G': nmn = "r"; break;
					case 'E': nmn = "r/m"; break;
					
					case 'M':
					case 'X':
					case 'Y': nmn = "m"; break;
				}
				
				if(nmn != null) {
					string = nmn + size;
					encodes_data = true;
				} else {
					string = str;
					encodes_data = false;
				}
			}
		}
		
		public boolean hasByte() {
			return (mask & 1) != 0;
		}
		
		public boolean hasWord() {
			return (mask & 2) != 0;
		}
		
		public boolean hasDword() {
			return (mask & 4) != 0;
		}
		
		public boolean hasQword() {
			return (mask & 8) != 0;
		}
		
		public boolean hasSize(int size) {
			if(size == 8) return hasByte();
			if(size == 16) return hasWord();
			if(size == 32) return hasDword();
			if(size == 64) return hasQword();
			return false;
		}
		
		
		
		public boolean hasData() {
			return encodes_data;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	public static class AsmPf extends AsmOp {
		public AsmPf(String mnemonic, int[] opcode, int flags) {
			super(mnemonic, opcode, flags, new OprTy[0]);
		}
	}
	
	public static class AsmOp {
		private final String mnemonic;
		private final int[] opcode;
		private final OprTy[] operand_types;
		private final int flags;
		
		public AsmOp(String mnemonic, int[] opcode, int flags, OprTy[] types) {
			this.mnemonic = mnemonic;
			this.operand_types = types.clone();
			this.opcode = opcode.clone();
			this.flags = flags;
		}
		
		public String getMnemonic() {
			return mnemonic;
		}
		
		public int getFlags() {
			return flags;
		}
		
		public int getNumOperands() {
			return operand_types.length;
		}
		
		public String getOpcodeString() {
			StringBuilder sb = new StringBuilder();
			
			for(int op : opcode) {
				sb.append(String.format("%02X", op)).append(" ");
			}
			
			return sb.toString().trim();
		}
		
		public OprTy getOperand(int index) {
			return operand_types[index];
		}
		
		public String toComplexString() { return toComplexString(16); }
		public String toComplexString(int opcode_padding) {
			if(opcode_padding < 3 || opcode_padding > 1024) {
				return String.format("%s %s", getOpcodeString(), toString());
			}
			
			String bits = Integer.toString(flags, 2);
			bits = "00000000".substring(bits.length()) + bits;
			
			return String.format("%-" + (opcode_padding) + "s%s %s", getOpcodeString(), bits, toString());
		}
		
		public String toString() {
			if(getNumOperands() < 1) return mnemonic;
			
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-16s", mnemonic));
			
			for(OprTy t : operand_types) {
				sb.append(String.format("%-16s", t)).append(", ");
			}
			
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
			
			return sb.toString().trim();
		}
	}
	
	// Prefix
	public static AsmPf prf(String mnemonic, int[] opcode, int flags) {
		return new AsmPf(mnemonic, opcode, flags);
	}
	
	public static AsmOp opr(String mnemonic, int[] opcode, int flags, OprTy... operators) {
		return new AsmOp(mnemonic, opcode, flags, operators);
	}
	
	@Deprecated public static AsmOp dopr(String mnemonic, int[] opcode, int flags, OprTy... operators) { return opr(mnemonic, opcode, flags, operators); }
	
	
	public static int[] opcode(int... array) { return array; }
	
	/** {@code L} */	public static final int ALLOW_LOCK		= (1 << 0);
	/** {@code S} */	public static final int ALLOW_SIZE		= (1 << 1);
	/** {@code Z} */	public static final int OPCODE_REG		= (1 << 2);
	/** {@code e.} */	public static final int USES_MODRM		= (1 << 3);
	public static final int RM_EXT_OFFSET	= (1 << 4);
	public static final int RM_EXT_MASK		= 7 * RM_EXT_OFFSET;
	
	/** {@code l.} */	public static final int RING_LEVEL		= (1 << 7);
	public static final int RL_OFFSET		= (1 << 8);
	public static final int RL_MASK			= 3 * RL_OFFSET;
	
	public static int flags(
			boolean allow_lock,
			boolean allow_size,
			boolean opcode_reg,
			boolean uses_modrm,
			int rm_ext_value,
			int ring_level
	) {
		int mask = 0;
		if(allow_lock) mask |= ALLOW_LOCK;
		if(allow_size) mask |= ALLOW_SIZE;
		if(opcode_reg) mask |= OPCODE_REG;
		
		if(uses_modrm) {
			mask |= USES_MODRM;
			mask |= ((rm_ext_value * RM_EXT_OFFSET) & RM_EXT_MASK);
		}
		
		if(ring_level != -1) {
			mask |= RING_LEVEL;
			mask |= ((rm_ext_value * RL_OFFSET) & RL_MASK);
		}
		
		return mask;
	}
	
	/**
	 * Calculate the flags from a string.<br>
	 *<pre>
	 *[ L  ] Allow LOCK prefix
	 *[ S  ] Allow operand-size prefix
	 *[ Z  ] The instruction has no <i>ModR/M</i>
	 *       The last three bits of the opcode encode the the register
	 *[ e. ] The instruction uses <i>ModR/M</i> and '.' represents
	 *       the extension a number
	 *[ l. ] The instruction can only be called in ring '.'
	 *</pre>
	 *
	 * @param string
	 * @return
	 */
	public static int flags(String string) {
		boolean allow_lock = false;
		boolean allow_size = false;
		boolean opcode_reg = false;
		
		boolean modrm_ext = false;
		int modrm_val = 0;
		int ring_level = -1;
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			char next = (i + 1 < string.length() ? string.charAt(i + 1):'\0');
			
			if(c == 'L') allow_lock = true;
			if(c == 'S') allow_size = true;
			if(c == 'Z') opcode_reg = true;
			if(c == 'e') {
				modrm_val = next - '0';
				modrm_ext = true;
				i++;
				continue;
			}
			if(c == 'l') {
				ring_level = next - '0';
				i++;
				continue;
			}
		}
		
		return flags(
			allow_lock,
			allow_size,
			opcode_reg,
			modrm_ext,
			modrm_val,
			ring_level
		);
	}
	
	/**
	 * Calculate the flags from a string.<br>
	 *<pre>
	 *[ b  ] BYTE size regardless of operand-size attribute
	 *[ w  ] WORD size regardless of operand-size attribute
	 *[ d  ] DWORD size regardless of operand-size attribute
	 *[ q  ] QWORD size regardless of operand-size attribute
	 *[ r  ] First operand byte uses <i>ModR/M</i>
	 *[ e. ] The instruction uses <i>ModR/M</i> and '.' represents a number
	 *</pre>
	 *
	 * @param string
	 * @return
	 */
	public static int operand(String string) {
		// TODO: Complete a list of all flags.
		// b  - BYTE
		// w  - WORD
		// d  - DWORD
		// q  - QWORD
		// v  - WORD or DWORD depending on size operand-size attribute
		// vq - QWORD or WORD depending on size operand-size attribute
		// vs - WORD or DWORD depending on size operand-size attribute
		boolean uses_modrm = false;
		boolean reg_opcode = false;
		

		boolean reg_value = false;	// r
		boolean imm_value = false;	// I
		boolean rm_value = false;	// G, H
		boolean rel_value = false;	// J
		
		boolean has_rm_extension = false;
		int rm_ext_value = 0;
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			char next = (i + 1 < string.length() ? string.charAt(i + 1):'\0');
			
			if(c == 'Z') reg_opcode = true;
			if(c == 'E'); // Modifier
			if(c == 'r') uses_modrm = true;
			
			if(c == 'e') {
				rm_ext_value = next - '0';
				uses_modrm = true;
				has_rm_extension = true;
				i++;
				continue;
			}
			
			if(c == 'm') {
				// Mode of operation, R E S A
				i++;
				continue;
			}
		}
		
		return 0;
	}
	
	
//	public static void main(String[] args) {
//		System.out.println(opr1("PUSH", opcode(0x68), flags(""), OprTy.imm16_32).toComplexString());
//		System.out.println(opr1("PUSH", opcode(0xFF), flags(""), OprTy.imm16_32).toComplexString());
//	}
}
