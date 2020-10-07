package hardcoded.exporter.x86;

import hardcoded.utils.StringUtils;

public final class AssemblyConsts {
	private AssemblyConsts() {}
	
	public static enum OprTy {
		// [ rXX ] is a register that can have either 16/32 or 64 bit sizes.
		rAX				("rAX", 0, 1, 1, 1),
		rCX				("rCX", 0, 1, 1, 1),
		rDX				("rDX", 0, 1, 1, 1),
		rBX				("rBX", 0, 1, 1, 1),
		rSP				("rSP", 0, 1, 1, 1),
		rBP				("rBP", 0, 1, 1, 1),
		rSI				("rSI", 0, 1, 1, 1),
		rDI				("rDI", 0, 1, 1, 1),
		r8				("r8" , 0, 1, 1, 1),
		r9				("r9" , 0, 1, 1, 1),
		r10				("r10", 0, 1, 1, 1),
		r11				("r11", 0, 1, 1, 1),
		r12				("r12", 0, 1, 1, 1),
		r13				("r13", 0, 1, 1, 1),
		r14				("r14", 0, 1, 1, 1),
		r15				("r15", 0, 1, 1, 1),
		
		// [ eXX ] is a register that can have either 16 or 32 bit sizes.
		eAX				("eAX", 0, 1, 1, 0),
		eCX				("eCX", 0, 1, 1, 0),
		eDX				("eDX", 0, 1, 1, 0),
		eBX				("eBX", 0, 1, 1, 0),
		eSP				("eSP", 0, 1, 1, 0),
		eBP				("eBP", 0, 1, 1, 0),
		eSI				("eSI", 0, 1, 1, 0),
		eDI				("eDI", 0, 1, 1, 0),
		
		AL				("AL" , 1, 0, 0, 0),
		CL				("CL" , 1, 0, 0, 0),
		DL				("DL" , 1, 0, 0, 0),
		BL				("BL" , 1, 0, 0, 0),
		AH				("AH" , 1, 0, 0, 0),
		CH				("CH" , 1, 0, 0, 0),
		DH				("DH" , 1, 0, 0, 0),
		BH				("BH" , 1, 0, 0, 0),
		
		R8B				("R8B" , 1, 0, 0, 0),
		R9B				("R9B" , 1, 0, 0, 0),
		R10B			("R10B", 1, 0, 0, 0),
		R11B			("R11B", 1, 0, 0, 0),
		R12B			("R12B", 1, 0, 0, 0),
		R13B			("R13B", 1, 0, 0, 0),
		R14B			("R14B", 1, 0, 0, 0),
		R15B			("R15B", 1, 0, 0, 0),

		DX				("DX" , 0, 1, 0, 0),
		AX				("AX" , 0, 1, 0, 0),
		EAX				("EAX", 0, 0, 1, 0),
		RAX				("RAX", 0, 0, 0, 1),
		
		
		FS				("FS" , 0, 1, 0, 0),
		GS				("GS" , 0, 1, 0, 0),
		
		
		// ModR/M but 'r/m' cannot point to memory
		Rd				(0, 0, 1, 0),
		Rq				(0, 0, 0, 1),
		
		// Control register
		Cd				(0, 0, 1, 0),
		Cq				(0, 0, 0, 1),
		
		// Debug register
		Dd				(0, 0, 1, 0),
		Dq				(0, 0, 0, 1),
		
		// General register
		Gb				(1, 0, 0, 0),
		Gw				(0, 1, 0, 0),
		Gd				(0, 0, 1, 0),
		Gq				(0, 0, 0, 1),
		Gv				(0, 1, 1, 1),
		
		// ModR/M register
		Eb				(1, 0, 0, 0),
		Ew				(0, 1, 0, 0),
		Ed				(0, 0, 1, 0),
		Eq				(0, 0, 0, 1),
		Ev				(0, 1, 1, 1),
		Ep				(0, 1, 1, 1),	// 16:16/32/64 bit pointer
		
		// Memory pointer
		M				(1, 1, 1, 1),
		Mb				(1, 0, 0, 0),
		Mw				(0, 1, 0, 0),
		Md				(0, 0, 1, 0),
		Mq				(0, 0, 0, 1),
		Mv				(0, 1, 1, 1),
		Mp				(0, 1, 1, 1),	// 16:16/32/64 bit pointer
		
		// Immediate value
		Ib				(1, 0, 0, 0),
		Iw				(0, 1, 0, 0),
		Id				(0, 0, 1, 0),
		Iq				(0, 0, 0, 1),
		Iv				(0, 1, 1, 1),
		Iz				(0, 1, 1, 0),	// WORD for 16 bit operand-size, otherwise (DWORD for 32/64 bit)
		
		// Relative offset
		Jb				(1, 0, 0, 0),
		Jz				(0, 1, 1, 0),
		
		// Memory addressed by the ES:eDI
		Yb				(1, 0, 0, 0),
		Yw				(0, 1, 0, 0),
		Yd				(0, 0, 1, 0),
		Yq				(0, 0, 0, 1),
		Yv				(0, 1, 1, 1),
		Yz				(0, 1, 1, 0),
		
		// Memory addressed by the DS:eSI
		Xb				(1, 0, 0, 0),
		Xw				(0, 1, 0, 0),
		Xd				(0, 0, 1, 0),
		Xq				(0, 0, 0, 1),
		Xv				(0, 1, 1, 1),
		Xz				(0, 1, 1, 0),
		
		// Segment
		Sw				(0, 1, 0, 0),
		
		// Flags
		Fw				(0, 1, 0, 0),
		Fv				(0, 1, 1, 1),
		
		// The instruction has no ModR/M byte.
		Ob				(1, 0, 0, 0),
		Od				(0, 0, 1, 0),
		Oq				(0, 0, 1, 0),
		Ov				(0, 1, 1, 1),
		
		;
		
		private final String string;
		private final char type;
		private final boolean hasByte;
		private final boolean hasWord;
		private final boolean hasDword;
		private final boolean hasQword;
		
		private OprTy(int r8, int r16, int r32, int r64) {
			this(null, r8, r16, r32, r64);
		}
		
		private OprTy(String string, int r8, int r16, int r32, int r64) {
			this.hasByte = (r8 != 0);
			this.hasWord = (r16 != 0);
			this.hasDword = (r32 != 0);
			this.hasQword = (r64 != 0);
			
			if(string == null) {
				// The type has more data than visible.
				
				this.type = name().charAt(0);
				String size = name().substring(1);
				String mnemonic = null;
				
				switch(type) {
					case 'G': mnemonic = "r"; break;
					case 'M':
					case 'X':
					case 'Y': mnemonic = "m"; break;
					case 'R':
					case 'E': mnemonic = "r/m"; break;
					case 'O': mnemonic = "moffs"; break;
					case 'I': mnemonic = "imm"; break;
					case 'F': mnemonic = "flag"; break;
					case 'J': mnemonic = "rel"; break;
					case 'S': mnemonic = "seg"; break;
					case 'C': mnemonic = "control"; break;
					case 'D': mnemonic = "debug"; break;
					default: mnemonic = name();
				}
				
				switch(size) {
					case "b": size = "8"; break;
					case "w": size = "16"; break;
					case "d": size = "32"; break;
					case "q": size = "64"; break;
					case "v": size = "16/32/64"; break;
					case "p": size = "16:16/32/64"; break;
					case "z": size = "16/32"; break;
					case "": size = ""; break;
					default: size = "????";
				}
				
				this.string = mnemonic + size;
			} else {
				this.string = string;
				this.type = 'K';
			}
		}
		
		/**
		 * Returns a character specifying the type of this operator.
		 *<pre>
		 *C    : Control register
		 *D    : Debug register
		 *E    : ModR/M register
		 *F    : Flags register
		 *G    : General purpose register
		 *I    : Immediate value
		 *J    : Relative offset value
		 *K    : <b>[Encodes a rXX/eXX register or is a direct register]</b>
		 *M    : Memory pointer
		 *O    : Operator encoded register
		 *R    : ModR/M register but 'r/m' cannot be memory
		 *S    : Segment register
		 *X    : Memory pointer by DS:eSI
		 *Y    : Memory pointer by ES:eDI
		 *</pre>
		 * @return a character specifying the type of this operator
		 */
		public char type() { return type; }
		
		public char postfix() {
			if(type == 'K' || name().length() != 2) return 0;
			return name().charAt(1);
		}
		
		public boolean isModrm() { return type == 'R' || type == 'E' || type == 'M'; }
		public boolean isMemory() { return type == 'X' || type == 'Y' || type == 'M'; }
		public boolean isRegister() { return type == 'C' || type == 'D' || type == 'E' || type == 'R' || type == 'K'; }
		public boolean isImmediate() { return type == 'I' || type == 'J'; }
		
		public boolean hasByte() { return hasByte; }
		public boolean hasWord() { return hasWord; }
		public boolean hasDword() { return hasDword; }
		public boolean hasQword() { return hasQword; }
		
		public boolean isVarying() {
			return ((hasByte ? 1:0)
				  + (hasWord ? 1:0)
				  + (hasDword ? 1:0)
				  + (hasQword ? 1:0)) > 1;
		}
		
		public int getSizeAboveOrEqual(int bits) {
			if(bits <  9 && hasByte)  return 8;
			if(bits < 17 && hasWord)  return 16;
			if(bits < 33 && hasDword) return 32;
			if(bits < 65 && hasQword) return 64;
			return -1;
		}
		
		public boolean hasSizeAboveOrEqual(int bits) {
			return (bits < 9 && hasByte)
				|| (bits < 17 && hasWord)
				|| (bits < 33 && hasDword)
				|| (bits < 65 && hasQword);
		}
		
		public boolean hasSize(int bits) {
			return (bits ==  8 && hasByte)
				|| (bits == 16 && hasWord)
				|| (bits == 32 && hasDword)
				|| (bits == 64 && hasQword);
		}
		
		public String toString() {
			return string;
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
		
		public int[] getOpcode() {
			return opcode.clone();
		}
		
		public String getOpcodeString() {
			return StringUtils.printHexString(" ", opcode).toUpperCase();
		}
		
		public OprTy getOperand(int index) {
			return operand_types[index];
		}
		
		public boolean hasLockFlag() { return (flags & ALLOW_LOCK) != 0; }
		public boolean hasRMEXFlag() { return (flags & NEED_RMEXT) != 0; }
		public boolean has64bitFlag() { return (flags & DEFAULT_64) != 0; }
		
		public int getRMEX() {
			return (flags & RM_EXT_MASK) / RM_EXT_OFFSET;
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
			
			sb.deleteCharAt(sb.length() - 2);
			
			return sb.toString().trim();
		}
		
		public String toPlainString() {
			if(getNumOperands() < 1) return mnemonic;
			
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%s ", mnemonic));
			
			for(OprTy t : operand_types) {
				sb.append(String.format("%s", t)).append(", ");
			}
			
			sb.deleteCharAt(sb.length() - 2);
			return sb.toString().trim();
		}
	}
	
	/** {@code L} */	public static final int ALLOW_LOCK		= (1 << 0);
	/** {@code r} */	@Deprecated public static final int USES_MODRM		= (1 << 1);
	/** {@code e.} */	public static final int NEED_RMEXT		= (1 << 2);
	public static final int RM_EXT_OFFSET	= (1 << 3);
	public static final int RM_EXT_MASK		= 7 * RM_EXT_OFFSET;
	
	/** {@code d} */	public static final int DEFAULT_64		= (1 << 6);
	
	public static int flags(
			boolean allow_lock,
			boolean uses_modrm,
			boolean need_rmext,
			boolean default_64,
			int rm_ext_value
	) {
		int mask = 0;
		if(allow_lock) mask |= ALLOW_LOCK;
		if(uses_modrm) mask |= USES_MODRM;
		if(default_64) mask |= DEFAULT_64;
		
		if(need_rmext) {
			mask |= NEED_RMEXT;
			mask |= ((rm_ext_value * RM_EXT_OFFSET) & RM_EXT_MASK);
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
	 *[ d  ] The instruction is default 64 bit and cannot encode 32 bit values
	 *</pre>
	 *
	 * @param string
	 * @return
	 */
	public static int flags(String string) {
		boolean allow_lock = false;
		boolean uses_modrm = false;
		boolean need_rmext = false;
		boolean default_64 = false;
		int modrm_val = 0;
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			char next = (i + 1 < string.length() ? string.charAt(i + 1):'\0');
			
			if(c == 'L') allow_lock = true;
			if(c == 'r') uses_modrm = true;
			if(c == 'd') default_64 = true;
			if(c == 'e') {
				modrm_val = next - '0';
				need_rmext = true;
				i++;
				continue;
			}
		}
		
		return flags(
			allow_lock,
			uses_modrm,
			need_rmext,
			default_64,
			modrm_val
		);
	}
}
