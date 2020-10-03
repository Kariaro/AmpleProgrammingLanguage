package hardcoded.compiler.assembler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.assembly.impl.AsmFactory;
import hardcoded.assembly.impl.AsmInst;
import hardcoded.assembly.x86.*;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.IRInsts;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.compiler.instruction.InstructionBlock;
import hardcoded.compiler.types.PointerType;
import hardcoded.utils.StringUtils;

public class AssemblyCodeGenerator {
//	public static void main(String[] args) {
//		AsmInst inst = AsmFactory.getInstruction("neg qword [RAX]");
//		System.out.println(Assembly.canEncodeInstruction(inst));
//		AsmFactory.compile(inst);
//		AsmFactory.compile(AsmFactory.getInstruction("neg qword [RAX]"));
//		AsmFactory.compile(AsmFactory.getInstruction("neg dword [ECX]"));
		
		// ADD       AL                  imm8                {AL  , Ib  }
//		AsmFactory.compile(AsmFactory.getInstruction("add R8B, 0x7f"));
//		AsmFactory.compile(AsmFactory.getInstruction("add SIL, 0x7f"));
//		AsmFactory.compile(AsmFactory.getInstruction("add AH, 0x7f"));
//		AsmFactory.compile(AsmFactory.getInstruction("mov rax, cr0"));
		
//		AsmGenerator.setSeed(1);
//		StringBuilder in_data = new StringBuilder();
//		StringBuilder op_data = new StringBuilder();
//		
//		for(int i = 0; i < 100; i++) {
//			AsmInst inst = AsmGenerator.generate();
////			if(inst.getNumOperands() != 2) {
////				i--;
////				continue;
////			}
//			
//			int[] opcode = AsmFactory.compile(inst);
//			op_data.append(StringUtils.printHexString(" ", opcode)).append(" ");
//			in_data.append(inst).append("\n");
//		}
//		
//		String opcode_data = op_data.toString().trim();
//		String input_data = in_data.toString().trim();
//		
//		System.out.println(input_data);
//	}
	
	public AssemblyCodeGenerator() {
		
	}
	
	public void generate(List<InstructionBlock> blocks) {
		System.out.println("Inside the asm code generator");
		
		// NOTE: String block is the first block.
		
		for(InstructionBlock block : blocks) {
			IRInstruction inst = block.start;
			System.out.println("\n\n\nName -> " + block.name);
			
			// System.out.println("\n" + block.returnType + ", " + block.name + (block.extra != null ? (", " + block.extra):""));
			
			
			List<AsmBlock> list = new ArrayList<>();
			AsmBlock asm_block = new AsmBlock();
			
			// What instructions are easy to block?
			// call, bnz, brz, br, label
			
			for(int i = 0; i < inst.length(); i++) {
				IRInstruction a = inst.get(i);
				
				switch(a.type()) {
					case call:
					case bnz:
					case brz:
					case br:
					case label: {
						if(!asm_block.isEmpty()) {
							list.add(asm_block);
							asm_block = new AsmBlock();
						}
						
						asm_block.add(a);
						list.add(asm_block);
						asm_block = new AsmBlock();
						break;
					}
					default: {
						asm_block.add(a);
					}
				}
			}
			
			if(!asm_block.isEmpty())
				list.add(asm_block);
			
			for(AsmBlock asm : list) {
				System.out.println(asm);
				System.out.println("---");
				try_work(asm);
			}
			
			for(AsmBlock asm : list) {
				System.out.println("==============");
				for(AsmInst in : asm.asm_list) {
					System.out.println(in.toPlainString());
					
				}
				System.out.println("--------------");
			}
			
			for(AsmBlock asm : list) {
				for(AsmInst in : asm.asm_list) {
					int[] opcode = AsmFactory.compile(in);
					System.out.printf("%-30s%s\n", StringUtils.printHexString(" ", opcode), in.toPlainString());
				}
			}
			
//			int count = 0;
//			while(inst != null) {
//				int idx = count++;
//				if(inst.op == IRInsts.label) System.out.println();
//				System.out.printf("%4d: ", idx);
//				
//				if(inst.op != IRInsts.label) System.out.print("  ");
//				
//				System.out.printf("%s\n", inst);
//				inst = inst.next();
//			}
		}
	}
	
	private void try_work(AsmBlock block) {
		for(IRInstruction inst : block.list) {
			try_work(block, inst);
		}
	}
	
	private void try_work(AsmBlock block, IRInstruction inst) {
		switch(inst.type()) {
			case mov: {
				// The 'mov' instruction can never move data into a memory location
				// and can only work with registers therefore, the first parameter
				// will always be a register.
				int size = calculateBits(inst);
				if(size < 1) break;
				
				Reg op0 = inst.getParam(0);
				Reg op1 = inst.getParam(1);
				
				if(op1 instanceof NumberReg) {
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
						createOperator(op0, size), createOperator(op1)
					));
				} else if(op1 instanceof RefReg) {
					RefReg ref = (RefReg)op1;
					
					RegisterX86 reg = pickRegister(size, RegisterX86.DX.index);
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
						new OprBuilder().reg(reg).get(),
						new OprBuilder().num(ref.index).ptr(size)
					));
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
						createOperator(op0, size),
						new OprBuilder().reg(reg).get()
					));
				} else {
					
				}
				
				break;
			}
			
			case add: {
				// 'dst' must be a register.
				Reg dst = inst.getParam(0);
				Reg op0 = inst.getParam(1);
				Reg op1 = inst.getParam(2);
				
				int size = calculateBits(inst);
				RegisterX86 reg = pickRegister(size, RegisterX86.DX.index);
				if(reg == null) break;
				
				if(op1 instanceof NumberReg) {
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
						new OprBuilder().reg(reg).get(), createOperator(op1)
					));
				} else {
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
						new OprBuilder().reg(reg).get(), createOperator(op1)
					));
				}
				
				// For now we say that op0 is always a register.
				if(true) {
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.ADD,
						new OprBuilder().reg(reg).get(), createOperator(op0, size)
					));
					
					block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
						createOperator(dst), new OprBuilder().reg(reg).get()
					));
				}
				
				break;
			}
			
			case write:
			case read: {
				// read from op1 into op0
				Reg dst = inst.getParam(0);
				Reg src = inst.getParam(1);
				
				int size = calculateBits(inst);
				if(inst.type() == IRInsts.write) {
					size = 4 << inst.calculateSize().size();
				}
				
				RegisterX86 reg = pickRegister(size, RegisterX86.DX.index);
				if(reg == null) break;
				
				block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
					new OprBuilder().reg(reg).get(), createOperator(src, size)
				));
				block.asm_list.add(AsmFactory.getInstruction(AsmMnm.MOV,
					createOperator(dst, size), new OprBuilder().reg(reg).get()
				));
				
				break;
			}
			
			// All the jump instructions will be compiled in the next stage.
			
			default: {
				block.asm_list.add(AsmFactory.getInstruction("nop"));
			}
		}
	}
	
	private int calculateBits(IRInstruction inst) {
		AtomType size = inst.calculateSize();
		if(size == null) return 0;
		
		int destSize = 0;
		if(size.isPointer()) {
			destSize = 4 * PointerType.POINTER_SIZE;
		} else {
			destSize = 4 << size.size();
		}
		
		return destSize;
	}
	
	private RegisterX86 pickRegister(int size, int index) {
		switch(size) {
			case 8: return RegisterX86.get(RegisterType.r8, index);
			case 16: return RegisterX86.get(RegisterType.r16, index);
			case 32: return RegisterX86.get(RegisterType.r32, index);
			case 64: return RegisterX86.get(RegisterType.r64, index);
		}
		
		return null;
	}
	
	private AsmOpr createOperator(Reg reg) { return createOperator(reg, 32); }
	private AsmOpr createOperator(Reg reg, int size) {
		if(reg instanceof NumberReg) {
			NumberReg num = (NumberReg)reg;
			return new OprBuilder().imm(num.value);
		} else if(reg instanceof ObjectReg) {
			ObjectReg obj = (ObjectReg)reg;
			
			if(obj.obj instanceof Number) {
				return new OprBuilder().imm(((Number)obj.obj).longValue());
			}
//			else if(obj.obj instanceof AtomExpr) {
//				return new OprBuilder().imm(((AtomExpr)obj.obj).i_value);
//			}
			
			System.out.println("::::::::::::::::::::::::" + obj.obj.getClass());
			return null;
		} else {
			return new OprBuilder().reg(RegisterX86.RBP).add().num(reg.index * 0x8).ptr(size);
		}
	}
}
