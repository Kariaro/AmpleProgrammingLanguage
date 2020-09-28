package hardcoded.assembly.impl;

import java.util.Arrays;
import java.util.List;

import hardcoded.assembly.x86.*;
import hardcoded.compiler.assembler.Assembly;
import hardcoded.compiler.assembler.AssemblyConsts;
import hardcoded.compiler.assembler.AssemblyConsts.AsmOp;
import hardcoded.compiler.assembler.AssemblyConsts.OprTy;
import hardcoded.utils.IntBuffer;
import hardcoded.utils.NumberUtils;
import hardcoded.utils.StringUtils;

public final class AsmFactory {
	private AsmFactory() {}
	
	public static AsmInst getInstruction(AsmMnm mnemonic, AsmOpr... operators) {
		return new AsmInst(mnemonic, operators);
	}
	
	@SafeVarargs
	public static AsmInst getInstruction(AsmMnm mnemonic, java.util.function.Function<OprBuilder, Object>... regs) {
		AsmOpr[] operators = new AsmOpr[regs.length];
		
		for(int i = 0; i < regs.length; i++) {
			Object o = regs[i].apply(new OprBuilder());
			
			if(o instanceof OprBuilder) {
				operators[i] = ((OprBuilder)o).get();
			} else if(o instanceof RegisterX86) {
				operators[i] = new OprBuilder().reg((RegisterX86)o).get();
			} else if(o instanceof AsmOpr) {
				operators[i] = (AsmOpr)o;
			} else {
				operators[i] = null;
			}
		}
		
		return new AsmInst(mnemonic, operators);
	}
	
	public static AsmInst getInstruction(String value) {
		value = value.replaceAll("\\s+", " ").trim();
		
		int index = value.indexOf(' ');
		if(index < 0) {
			return new AsmInst(AsmMnm.valueOf(value.toUpperCase()));
		}
		
		AsmMnm mnemonic = AsmMnm.valueOf(value.substring(0, index).toUpperCase());
		value = value.substring(index);
		
		AsmOpr[] operators = Arrays.asList(value.split(","))
			.stream().map(s -> new OprBuilder().fromString(s.trim()))
			.toArray(AsmOpr[]::new);
		
		return new AsmInst(mnemonic, operators);
	}
	
	/**
	 * Convert a size type into a string.
	 * 
	 * @param	bits	the size
	 * @return a serialized version of a size type.
	 */
	public static String getSizeString(int bits) {
		switch(bits) {
			case 8: return "byte";
			case 16: return "word";
			case 32: return "dword";
			case 64: return "qword";
			case 128: return "xmmword";
			case 256: return "ymmword";
			default: return "???";
		}
	}
	
	private static int getMemorySize(AsmOpr opr) {
		if(!opr.isMemory()) return 0;
		
		for(int i = 0; i < opr.length(); i++) {
			RegisterX86 reg = opr.getRegister(i);
			
			if(reg != null) {
				return reg.bits;
			}
		}
		
		return 0;
	}
	
	private static boolean needsRexPrefix(AsmOpr opr) {
		for(int i = 0; i < opr.length(); i++) {
			RegisterX86 reg = opr.getRegister(i);
			
			if(reg != null) {
				return reg.index > 7;
			}
		}
		
		return false;
	}
	
	public static int[] compile(AsmInst inst) {
		List<AsmOp> list = Assembly.lookup(inst);
		if(list.isEmpty()) return null;
		
		AsmOp first = list.get(0);
		for(AsmOp op : list) {
			if(op != first)
				System.out.println("      :> " + op.toComplexString());
		}
		System.out.println("-------------------------------");
		System.out.println("Using :> " + first.toComplexString() + "\n");
		
		int length = first.getNumOperands();
		
		if(length == 0) return first.getOpcode();
		if(length == 1) return _compile_1(first, inst);
		if(length == 2) return _compile_2(first, inst);
		
		throw new UnsupportedOperationException("Can't compile " + length + " argument instructions");
	}
	
	private static int[] _compile_1(AsmOp op, AsmInst inst) {
		IntBuffer buffer = new IntBuffer(15);
		
		OprTy ty0 = op.getOperand(0);
		AsmOpr op0 = inst.getOperand(0);
		
		boolean opsize = false; // Change operator to 16 bit. [0x66]
		
		int imm_size = 0;
		long imm_val = 0;
		
		if(ty0.isImmediate()) {
			Object obj = op0.getObject(0);
			if(obj instanceof Long) {
				// Could be a scalar.....
				
				imm_val = (long)obj;
				imm_size = NumberUtils.getBitsSize(imm_val);
				
				if(imm_size == 16) opsize = true;
			}
		}
		
		if(opsize) buffer.write(0x66);
		
		buffer.write(op.getOpcode());
		if(ty0.type() == 'Z') {
			// TODO:
		}
		
		if(imm_size != 0) {
			for(long i = 0; i < imm_size; i += 8) {
				buffer.write((imm_val >>> i) & 0xff);
			}
		}
		
		int[] array = buffer.toArray();

		System.out.println("opcode = " + StringUtils.printHexString(" ", array));
		return array;
	}
	
	private static int[] _compile_2(AsmOp op, AsmInst inst) {
		if((op.getFlags() & AssemblyConsts.USES_MODRM) != 0) return _compile_2_rm(op, inst);
		if((op.getFlags() & AssemblyConsts.NEED_RMEXT) != 0) return _compile_2_rmext(op, inst);

		return null;
	}
	
	private static int[] _compile_2_rmext(AsmOp op, AsmInst inst) {
		OprTy ty0 = op.getOperand(0);
		OprTy ty1 = op.getOperand(1);
		AsmOpr op0 = inst.getOperand(0);
		AsmOpr op1 = inst.getOperand(1);
		
		IntBuffer buffer = new IntBuffer(15);
		
		if(ty1.isImmediate()) {
			
		}
		
		buffer.write(op.getOpcode());
		buffer.write(generate_modrm(op, inst));
		
		int[] array = buffer.toArray();
		System.out.println("opcode = " + StringUtils.printHexString(" ", array));
		return array;
	}
	
	private static int[] _compile_2_rm(AsmOp op, AsmInst inst) {
		OprTy ty0 = op.getOperand(0);
		OprTy ty1 = op.getOperand(1);
		AsmOpr op0 = inst.getOperand(0);
		AsmOpr op1 = inst.getOperand(1);
		
		AsmOpr mem;
		AsmOpr reg;
		
		boolean mem_first = op0.isMemory();
		if(mem_first) {
			mem = op0;
			reg = op1;
		} else {
			mem = op1;
			reg = op0;
		}
		
		if(op0.getSize() != op1.getSize())
			return null;
		
		int rex = 0;
		boolean addrex = false; // Use the rex prefix.            [0x40 + WRXB]
		boolean adsize = false; // Change address size to 32 bit. [0x67]
		boolean opsize = false; // Change operand size to 16 bit. [0x66]
		
		if(reg.getSize() == 64) rex |= 8; // W: Change operand size to 64 bit.
		if(needsRexPrefix(reg)) rex |= 4; // B: extend rm  1 bit.
		/// SIB index
		if(needsRexPrefix(mem)) rex |= 1; // R: extend reg 1 bit.
		
		
		if(getMemorySize(mem) == 32) adsize = true; // Change address size to 32 bit. [0x67]
		if(reg.getSize() == 16) opsize = true;      // Change operand size to 16 bit. [0x66]

		int[] modrm = generate_modrm(op, inst);
		
		{
			if(rex != 0) addrex = true;
			
			System.out.println(inst.toPlainString());
			// String bits = Integer.toBinaryString(rex | 0b10000);
			// System.out.printf("addrex = %-5s | %s\n", addrex, bits.substring(bits.length() - 4));
			// System.out.printf("adsize = %-5s\n", adsize);
			// System.out.printf("opsize = %-5s\n", opsize);
			System.out.printf("modrm  = %s\n", StringUtils.printHexString(" ", modrm));
		}
		
		{
			IntBuffer buffer = new IntBuffer(15);
			
			if(adsize) buffer.write(0x67);
			if(opsize) buffer.write(0x66);
			if(addrex) buffer.write(0x40 + (rex));
			buffer.write(op.getOpcode());
			
			if(ty0.type() == 'Z') {
				// TODO: Is this working?
				buffer.writeOffset(buffer.read(0) | (op0.getRegister(0).index & 7), -1);
			}
			
			if(modrm != null)
				buffer.write(modrm);
			
			int[] array = buffer.toArray();

			System.out.println("opcode = " + StringUtils.printHexString(" ", array));
			return array;
		}
	}
	
	private static boolean hasSib(AsmOpr opr) {
		if(!opr.isMemory()) return false;
		
		for(int i = 0; i < opr.length(); i++) {
			Object obj = opr.getObject(i);
			
			if(obj instanceof Character) {
				char c = (char)obj;
				
				if(c == '*') return true;
			}
		}
		
		return false;
	}
	
	// TODO: This should give the prefixes and modrm
	private static int[] generate_modrm(AsmOp op, AsmInst inst) {
		IntBuffer buffer = new IntBuffer(32);
		OprTy ty0 = op.getOperand(0);
		OprTy ty1 = op.getOperand(1);
		
		AsmOpr op0 = inst.getOperand(0);
		AsmOpr op1 = inst.getOperand(1);
		
		// NOTE: The only exception to values is BP, R13 gives [RIP/EIP + disp32]
		int disp_size = 0;
		long disp_val = 0;
		
		boolean use_regrm = true;
		int mod = 0b11;
		int reg = 0;
		int rm  = 0;
		
		if(ty0.type() == 'Z') use_regrm = false;
		
		if(op0.isRegister()) {
			reg = op0.getRegister(0).index;
			
			if(op1.isRegister()) {
				rm  = op1.getRegister(0).index;
			} else if(op1.isMemory()) {
				// Check for SIB has * inside it .....
				
				// Memory.... size does not matter... Disp size does....
				// TODO: Just get memory working and you'll be done.......
				
				// Check last part and get if it's a immediate value
				Object obj = op1.getObject(op1.length() - 1);
				if(obj instanceof Long) {
					// Could be a scalar.....
					
					disp_val = (long)obj;
					disp_size = NumberUtils.getBitsSize(disp_val);
					
					// Only 8, 32 allowed
					if(disp_size == 16) disp_size = 32;
					if(disp_size == 64); // FIXME: Invalid
					
					if(disp_size ==  8) mod = 0b01;
					if(disp_size == 32) mod = 0b10;
				}
				
				// Only index 5 encodes SIB
				System.out.println(" -> " + op1);
			} else {
				disp_val = (long)op1.getObject(0);
				disp_size = NumberUtils.getBitsSize(disp_val);
				
				// TODO: Expand value to a size that is allowed.
				if(!ty1.hasSize(disp_size)) {
					if(disp_size == 8) {
						disp_size = 16;
					}
				}
			}
		} else {
			// This can never be Immediate
		}
		
		if((op.getFlags() & AssemblyConsts.NEED_RMEXT) != 0) {
			rm = reg;
			reg = (op.getFlags() & AssemblyConsts.RM_EXT_MASK) / AssemblyConsts.RM_EXT_OFFSET;
		}
		
		if(use_regrm) {
			buffer.write(
				((mod & 0x3) << 6) |
				((reg & 0xf) << 3) |
				((rm  & 0xf) << 0)
			);
		}
		
		if(disp_size != 0) {
			for(long i = 0; i < disp_size; i += 8) {
				buffer.write((disp_val >>> i) & 0xff);
			}
		}
		
		return buffer.toArray();
	}
}
