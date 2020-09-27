package hardcoded.compiler.assembler;

public final class AssemblyConsts {
	private AssemblyConsts() {}
	
	public static final int OF = (1 << 0);
	public static final int DF = (1 << 1);
	public static final int IF = (1 << 2);
	public static final int TF = (1 << 3);
	public static final int SF = (1 << 4);
	public static final int ZF = (1 << 5);
	public static final int AF = (1 << 6);
	public static final int PF = (1 << 7);
	public static final int CF = (1 << 8);
	
//	private static int decode_octal(int octal_binary) {
//		return (((octal_binary &    07) != 0) ? 1:0)
//			 | (((octal_binary &   070) != 0) ? 2:0)
//			 | (((octal_binary &  0700) != 0) ? 4:0)
//			 | (((octal_binary & 07000) != 0) ? 8:0);
//	}
	
	public static enum OprTy {
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
				
				if(size.equals("bss")) size = "8"; // TODO: What is the correct size for 'bss'
				if(size.equals("bs")) size = "8/16";
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
	
	public static int[] opcode(int... array) { return array; }
	
	public static AsmPf prf(String mnemonic, int[] opcode) { return new AsmPf(mnemonic, opcode, 0); }
	public static AsmPf prf(String mnemonic, int[] opcode, int flags) { return new AsmPf(mnemonic, opcode, flags); }
	public static AsmOp opr(String mnemonic, int[] opcode, OprTy... operators) { return new AsmOp(mnemonic, opcode, 0, operators); }
	public static AsmOp opr(String mnemonic, int[] opcode, int flags, OprTy... operators) { return new AsmOp(mnemonic, opcode, flags, operators); }
	@Deprecated public static AsmOp dopr(String mnemonic, int[] opcode, int flags, OprTy... operators) { return opr(mnemonic, opcode, flags, operators); }
	
	/** {@code L} */	public static final int ALLOW_LOCK		= (1 << 0);
	/** {@code e.} */	public static final int USES_MODRM		= (1 << 1);
	/** {@code e.} */	public static final int NEED_RMEXT		= (1 << 2);
	public static final int RM_EXT_OFFSET	= (1 << 3);
	public static final int RM_EXT_MASK		= 7 * RM_EXT_OFFSET;
	
	/** {@code l.} */	public static final int RING_LEVEL		= (1 << 6);
	public static final int RL_OFFSET		= (1 << 7);
	public static final int RL_MASK			= 3 * RL_OFFSET;
	
	public static int flags(
			boolean allow_lock,
			boolean uses_modrm,
			boolean need_rmext,
			int rm_ext_value,
			int ring_level
	) {
		int mask = 0;
		if(allow_lock) mask |= ALLOW_LOCK;
		if(uses_modrm) mask |= USES_MODRM;
		
		if(need_rmext) {
			mask |= NEED_RMEXT;
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
	 *[ r  ] Uses both the rm and reg field of <i>ModR/M</i> to
	 *       encode the operands
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
		boolean uses_modrm = false;
		boolean need_rmext = false;
		int modrm_val = 0;
		int ring_level = -1;
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			char next = (i + 1 < string.length() ? string.charAt(i + 1):'\0');
			
			if(c == 'L') allow_lock = true;
			if(c == 'r') uses_modrm = true;
			if(c == 'e') {
				modrm_val = next - '0';
				need_rmext = true;
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
			uses_modrm,
			need_rmext,
			modrm_val,
			ring_level
		);
	}
	
//	/**
//	 * Calculate the flags from a string.<br>
//	 *<pre>
//	 *[ b  ] BYTE size regardless of operand-size attribute
//	 *[ w  ] WORD size regardless of operand-size attribute
//	 *[ d  ] DWORD size regardless of operand-size attribute
//	 *[ q  ] QWORD size regardless of operand-size attribute
//	 *[ r  ] First operand byte uses <i>ModR/M</i>
//	 *[ e. ] The instruction uses <i>ModR/M</i> and '.' represents a number
//	 *</pre>
//	 *
//	 * @param string
//	 * @return
//	 */
//	public static int operand(String string) {
//		// b  - BYTE
//		// w  - WORD
//		// d  - DWORD
//		// q  - QWORD
//		// v  - WORD or DWORD depending on size operand-size attribute
//		// vq - QWORD or WORD depending on size operand-size attribute
//		// vs - WORD or DWORD depending on size operand-size attribute
//		boolean uses_modrm = false;
//		boolean reg_opcode = false;
//		
//
//		boolean reg_value = false;	// r
//		boolean imm_value = false;	// I
//		boolean rm_value = false;	// G, H
//		boolean rel_value = false;	// J
//		
//		boolean has_rm_extension = false;
//		int rm_ext_value = 0;
//		
//		for(int i = 0; i < string.length(); i++) {
//			char c = string.charAt(i);
//			char next = (i + 1 < string.length() ? string.charAt(i + 1):'\0');
//			
//			if(c == 'Z') reg_opcode = true;
//			if(c == 'E'); // Modifier
//			if(c == 'r') uses_modrm = true;
//			
//			if(c == 'e') {
//				rm_ext_value = next - '0';
//				uses_modrm = true;
//				has_rm_extension = true;
//				i++;
//				continue;
//			}
//			
//			if(c == 'm') {
//				// Mode of operation, R E S A
//				i++;
//				continue;
//			}
//		}
//		
//		return 0;
//	}
}
