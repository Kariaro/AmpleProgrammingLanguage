package me.hardcoded.exporter.x86;

import java.util.Arrays;
import java.util.List;

import me.hardcoded.exporter.x86.AssemblyConsts.AsmOp;
import me.hardcoded.exporter.x86.AssemblyConsts.OprTy;
import me.hardcoded.utils.IntBuffer;

final class Assembly {
	private Assembly() {}
	
	public static AsmInst getInstruction(AsmMnm mnemonic, AsmOpr... operators) {
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
	
	private static int getMemorySize(AsmOpr opr) {
		if(!opr.isMemory()) return 0;
		
		for(int i = 0; i < opr.length(); i++) {
			Object obj = opr.getObject(i);
			
			if(obj instanceof RegisterX86) {
				RegisterX86 reg = opr.getRegister(i);
				
				if(reg != null) {
					return reg.bits;
				}
			}
		}
		
		return 0;
	}
	
	private static boolean mustUseRex(AsmInst inst) {
		for(int i = 0; i < inst.getNumOperands(); i++) {
			AsmOpr opr = inst.getOperand(i);
			
			for(int j = 0; j < opr.length(); j++) {
				Object obj = opr.getObject(j);
				
				if(obj instanceof RegisterX86) {
					RegisterX86 reg = (RegisterX86)obj;
					
					if(reg.index > 7 || reg.matchesAny(RegisterX86.SPL, RegisterX86.BPL, RegisterX86.SIL, RegisterX86.DIL)) return true;
				}
			}
		}
		
		return false;
	}
	
	public static int[] compile(AsmInst inst) {
		List<AsmOp> list = AsmLoader.lookup(inst);
//		System.out.println("-----------------------------------------------------");
		if(list.isEmpty()) {
			System.out.println("Instruction could not be encoded");
			return null; // TODO: Throw exception?
		}
		
		
		int[] opcode = new int[32];
		AsmOp using = null;
		for(AsmOp op : list) {
			int[] array = try_compile(op, inst);
//			System.out.printf("%-6s : %-30s %s\n", op.getOpcodeString(), StringUtils.printHexString(" ", array), op.toPlainString());
			
			if(array == null) continue;
			if(array.length < opcode.length) {
				opcode = array;
				using = op;
			}
		}
		
		if(using != null) {
//			System.out.println();
//			System.out.println("Using :> " + using.toComplexString());
//			System.out.println("string = " + inst.toPlainString());
//			System.out.println("opcode = " + StringUtils.printHexString(" ", opcode));
			
			return opcode;
		}
		
		return null;
	}
	
	/**
	 * Try compile a instruction with a selected {@code AsmOp}.
	 * 
	 * @param op
	 * @param inst
	 * @return
	 */
	private static int[] try_compile(AsmOp op, AsmInst inst) {
		int length = op.getNumOperands();
		if(length == 0) return op.getOpcode(); // Fully Done
		if(length == 1) return _compile_1(op, inst); // Done except ModR/M memory with SIB bytes
		
		if(length == 2) {
			return _compile_2(op, inst);
		}
		
		return null;
	}
	
	private static int[] _compile_1(AsmOp op, AsmInst inst) {
		Opcode opcode = new Opcode();
		opcode.setOpcode(op.getOpcode());
		
		OprTy ty0 = op.getOperand(0);
		AsmOpr op0 = inst.getOperand(0);
		
		switch(ty0.type()) {
			case 'K': {
				RegisterX86 reg = op0.getRegister(0);
				
				String typeName = ty0.name();
				if(Character.isLowerCase(typeName.charAt(0))) {
					if(reg.bits == 16) opcode.setOperandSize(true);
					if(!op.has64bitFlag() && reg.bits == 64) opcode.setRexW();
					break;
				}
				
				// Register is already encoded
				break;
			}
			
			case 'M':
			case 'E': {
				apply_modrm_rm(opcode, op, ty0, op0);
				break;
			}
			
			
			case 'J':
			case 'I': {
				int size = ty0.getSizeAboveOrEqual(op0.getSize());
				apply_immediate(opcode, (long)op0.getObject(0), size);
				
				if(size == 16) opcode.setOperandSize(true);
			}
			
			// These are never encoded for single operand instructions
			case 'O': case 'R': case 'C': case 'D':
			case 'S': case 'F': case 'X': case 'Y':
				break;
		}
		
		// TODO: This line should not be necessary.
		if(op.has64bitFlag()) opcode.unsetRexW(); // Is already 64 bit
		if(mustUseRex(inst)) opcode.setRex(true);
		
		return opcode.build();
	}
	
	private static int[] _compile_2(AsmOp op, AsmInst inst) {
		// if((op.getFlags() & AssemblyConsts.USES_MODRM) != 0) return _compile_2_rm(op, inst);
		OprTy ty0 = op.getOperand(0);
		OprTy ty1 = op.getOperand(1);
		AsmOpr op0 = inst.getOperand(0);
		AsmOpr op1 = inst.getOperand(1);
		
		// Instruction is ModR/M if it contains a 'r/m' register 'E' type operand.
		Opcode opcode = new Opcode();
		opcode.setOpcode(op.getOpcode());
		
		if(ty0.isModrm() || ty1.isModrm()) {
			// X, Y, E, M, R
			
			apply_modrm_rm(opcode, op, inst);
		} else {
			switch(ty0.type()) {
				case 'K': {
					// The register is already encoded in the register
					// 64 bit must add [ 48 ]
					// 16 bit must add [ 66 ]
					
					if(ty0.isVarying()) {
						if(op0.getSize() == 16) opcode.setOperandSize(true);
						if(!op.has64bitFlag() && op0.getSize() == 64) opcode.setRexW();
					}
					
					
					break;
				}
				
				case 'I': { // ENTER uses two imm values...
					int size;
					switch(ty1.postfix()) {
						case 'z': {
							size = (op0.getSize() > 16) ? 32:16;
							break;
						}
						case 'v': {
							size = ty1.getSizeAboveOrEqual(op0.getSize());
							break;
						}
						default: {
							size = op1.getSize();
						}
					}
					
					apply_immediate(opcode, (long)op1.getObject(0), size);
				}
			}
		}
		
		switch(ty1.type()) {
			case 'J': // Relative and immediate values can be encoded outside.
			case 'I': {
				int size;
				switch(ty1.postfix()) {
					case 'z': {
						size = (op0.getSize() > 16) ? 32:16;
						break;
					}
					case 'v': {
						size = ty1.getSizeAboveOrEqual(op0.getSize());
						break;
					}
					default: {
						size = op1.getSize();
					}
				}
				
				apply_immediate(opcode, (long)op1.getObject(0), size);
			}
		}
		
		if(mustUseRex(inst)) opcode.setRex(true);
		
		return opcode.build();
	}
	
	private static void apply_immediate(Opcode opcode, long value, long bits) {
		IntBuffer buffer = opcode.getPostfix();
		for(long i = 0; i < bits; i += 8) {
			buffer.write((value >>> i) & 0xff);
		}
	}
	
	/**
	 *<pre>
	 *array[ 0   ] == mod index
	 *array[ 1   ] == index
	 *array[ ... ] extra data that should be added
	 *</pre>
	 *
	 * @param opcode
	 * @param ty
	 * @param opr
	 * @return
	 */
	private static int[] encode_modrm_memory(Opcode opcode, OprTy ty, AsmOpr opr) {
		IntBuffer buffer = new IntBuffer(10);
		buffer.write(0); // Reserve index 0 for mod value.
		buffer.write(0); // Reserve index 1 for index value.
		
		int mod = 3;
		int idx = 0;
		
		// If the length of the operand is ONE then it is either a number or a register.
		if(opr.length() == 1) {
			mod = 0;
			
			if(opr.hasRegisterAt(0)) {
				RegisterX86 reg = opr.getRegister(0);
				
				if((reg.index & 7) == 5) {
					// Change to [bp + 0x0]
					buffer.write(0);
					if(reg == RegisterX86.RIP || reg == RegisterX86.EIP) {
						buffer.write(0);
						buffer.write(0);
						buffer.write(0);
					} else {
						mod = 1;
					}
				}
				
				if((reg.index & 7) == 4) {
					// SP only works with SIB
					buffer.write(0b100100);
				}
				
				idx = reg.index;
				
				if(reg.bits == 32) {
					opcode.setAddressSize(true);
				}
			} else {
				long imm = opr.getImmediate(0);
				
				// SIB,    scale, index, base
				mod = 0;
				idx = 4; // [SIB]
				buffer.write(0b100101);
				
				// Only 32 bits are allowed
				for(long i = 0; i < 32; i += 8) {
					buffer.write((imm >>> i) & 0xff);
				}
			}
		}
		
		if(opr.length() == 3) { // TODO: This is not fully done yet. Fix SIB bytes.
			// R/M:  'reg' '+' 'imm'
			// SIB:  'base' '*' 'index'
			
			// Test
			// r/m + disp8
			// r/m + disp32
			// Can only be SIB for [base + disp8]
			// Otherwise there are no.. And this should only
			//   should be used for [RBP/EBP + disp8]
			//   should be used for [RSP/ESP + disp8]
			
			// Base will always be register
			RegisterX86 reg = opr.getRegister(0);
			
			idx = reg.index;
			
			if(opr.getObject(1).equals('+')) {
				long disp_val = (long)opr.getObject(2);
				int disp_size = NumberUtils.getBitsSize(disp_val);
				
				// There are no 16 bit values
				if(disp_size == 16) disp_size = 32;
				if(disp_size ==  8) mod = 1;
				if(disp_size == 32) mod = 2;
				
				if(reg == RegisterX86.RIP || reg == RegisterX86.EIP) {
					disp_size = 32;
					mod = 0;
				}
				
				for(long i = 0; i < disp_size; i += 8) {
					buffer.write((disp_val >>> i) & 0xff);
				}
			}
		}
		
		int[] array = buffer.toArray();
		array[0] = mod;
		array[1] = idx;
		return array;
	}
	
	private static void apply_modrm_rm(Opcode opcode, AsmOp op, OprTy ty, AsmOpr opr) {
		int mod = 3, reg = 0, rm = 0;
		int[] array = null;
		
		if(!op.has64bitFlag() && opr.getSize() == 64) opcode.setRexW();
		if(opr.getSize() == 16) opcode.setOperandSize(true);
		
		if(opr.isMemory()) {
			array = encode_modrm_memory(opcode, ty, opr);
			mod = array[0];
			rm = array[1];
			
			if(getMemorySize(opr) == 32) opcode.setAddressSize(true);
		} else if(opr.isRegister()) {
			rm = opr.getRegister(0).index;
		}
		
		if(op.hasRMEXFlag()) {
			reg = op.getRMEX();
		} else {
			if(reg > 7) opcode.setRexR();
		}
		
		if(rm  > 7) opcode.setRexB();
		
		IntBuffer buffer = opcode.getPostfix();
		buffer.write(
			((mod & 3) << 6) |
			((reg & 7) << 3) |
			((rm  & 7) << 0)
		);
		
		if(array != null) {
			// note: Extra data is array[2+]
			for(int i = 2; i < array.length; i++) {
				buffer.write(array[i]);
			}
		}
	}
	
	private static void apply_modrm_rm(Opcode opcode, AsmOp op, AsmInst inst) {
		if(op.getNumOperands() == 1) {
			apply_modrm_rm(opcode, op, op.getOperand(0), inst.getOperand(0));
			return;
		}
		
		IntBuffer buffer = opcode.getPostfix();
		OprTy ty0 = op.getOperand(0);
		OprTy ty1 = op.getOperand(1);
		
		AsmOpr op0 = inst.getOperand(0);
		AsmOpr op1 = inst.getOperand(1);
		
		int[] extra_data = null;
		
		int mod = 0b11;
		int reg = 0;
		int rm  = 0;
		
		if(ty1.isModrm()) {
			OprTy tmp0 = ty0;
			ty0 = ty1;
			ty1 = tmp0;
			
			AsmOpr tmp1 = op0;
			op0 = op1;
			op1 = tmp1;
		}
		
		
		// NOTE: The only exception to values is BP, R13 gives [RIP/EIP + disp32]
		
		if(op1.isRegister()) {
			reg = op1.getRegister(0).index;
			
			if(ty1.isVarying()) {
				if(!op.has64bitFlag() && op1.getSize() == 64) opcode.setRexW();
				if(op1.getSize() == 16) opcode.setOperandSize(true);
			}
			
		} else if(op1.isImmediate()) {
			// do nothing..
		}
		
		
		if(op0.isRegister()) {
			rm = op0.getRegister(0).index;
			
			if(ty0.isVarying()) {
				if(!op.has64bitFlag() && op0.getSize() == 64) opcode.setRexW();
				if(op0.getSize() == 16) opcode.setOperandSize(true);
			}
		} else {
			int[] array = encode_modrm_memory(opcode, ty0, op0);
			mod = array[0];
			rm = array[1];
			extra_data = array;
			
			if(getMemorySize(op0) == 32) opcode.setAddressSize(true);
			
			if(ty0.isVarying()) {
				if(!op.has64bitFlag() && op0.getSize() == 64) opcode.setRexW();
				if(op0.getSize() == 16) opcode.setOperandSize(true);
			}
		}
		
		if(op.hasRMEXFlag()) {
			reg = op.getRMEX();
		}
		
		if(rm  > 7) opcode.setRexB();
		if(reg > 7) opcode.setRexR();
		
		buffer.write(
			((mod & 3) << 6) |
			((reg & 7) << 3) |
			((rm  & 7) << 0)
		);
		
		if(extra_data != null) {
			// TODO: Extra data is array[2+]
			for(int i = 2; i < extra_data.length; i++) {
				buffer.write(extra_data[i]);
			}
		}
	}
}
