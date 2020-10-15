package hardcoded.exporter.x86;

import java.util.ArrayList;
import java.util.List;

import hardcoded.assembly.x86.*;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.exporter.impl.CodeGeneratorImpl;
import hardcoded.utils.StringUtils;
import hardcoded.utils.buffer.IntBuffer;

public class AssemblyCodeGenerator implements CodeGeneratorImpl {
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
	
	public byte[] generate(IRProgram program) {
		System.out.println("\nInside the asm code generator");
		
		for(IRFunction func : program.getFunctions()) {
			System.out.println("========================");
			System.out.println("Name -> " + func.getName());
			for(IRInstruction inst : func.getInstructions()) {
				if(inst.type() == IRType.label) {
					System.out.println(" " + inst);
				} else {
					System.out.println("    " + inst);
				}
			}
		}
		
		System.out.println("\n\n\n");
		
		// Types of InstructionBlock
		//    data blocks
		//    function blocks
		List<AsmContainer> containers = new ArrayList<>();
		
		for(IRFunction func : program.getFunctions()) {
			System.out.println("Name -> " + func.getName());
			containers.add(createContainer(func));
		}
		
		for(AsmContainer container : containers) {
//			for(AsmBlock block : container.blocks) {
//				System.out.println("--------------");
//				System.out.println(block);
//			}
			
			// First step is to compile all blocks that are not special.
			for(AsmBlock block : container.blocks) {
				if(block.isJumpBlock()) continue;
				if(block.isCompiled()) continue;
				
				compileBlock(block);
			}
			
			// Second step is to compile all jump instructions.
//			for(int i = 0; i < container.size(); i++) {
//				AsmBlock block = container.blocks.get(i);
//				if(!block.isJumpBlock()) continue;
//				
//				compileSecond(container, i, block);
//			}
			
			compileSecondFull(container);
			
			System.out.println();
			System.out.println();
			
			for(AsmBlock block : container.blocks) {
				System.out.println("-------------- [SIZE = " + block.getCompiledSize() + "]");
				System.out.println(StringUtils.printHexString(" ", block.compiled_code));
			}
		}
		
		System.out.println();
		for(AsmContainer container : containers) {
			for(AsmBlock block : container.blocks) {
				System.out.print(StringUtils.printHexString("", block.compiled_code));
			}
		}
		
		/*
		for(AsmContainer asm : list) {
			System.out.println(asm);
			System.out.println("---");
			try_work(asm);
		}
		
		for(AsmContainer asm : list) {
			System.out.println("==============");
			for(AsmInst in : asm.asm_list) {
				System.out.println(in.toPlainString());
				
			}
			System.out.println("--------------");
		}
		
		for(AsmContainer asm : list) {
			for(AsmInst in : asm.asm_list) {
				int[] opcode = AsmFactory.compile(in);
				System.out.printf("%-30s%s\n", StringUtils.printHexString(" ", opcode), in.toPlainString());
			}
		}
		
		int count = 0;
		while(inst != null) {
			int idx = count++;
			if(inst.op == IRInsts.label) System.out.println();
			System.out.printf("%4d: ", idx);
			
			if(inst.op != IRInsts.label) System.out.print("  ");
			
			System.out.printf("%s\n", inst);
			inst = inst.next();
		}
		
		return null;
		 */
		
		return null;
	}
	
	// Create a container from instructions without converting it into assembly
	// the difrerent blocks will be split depending if they are jump instructions or not.
	private AsmContainer createContainer(IRFunction func) {
		AsmContainer container = new AsmContainer();
		
		// What instructions are easy to block?
		// call, bnz, brz, br, label
		AsmBlock block = new AsmBlock();
		for(IRInstruction a : func.getInstructions()) {
			switch(a.type()) {
				case call:
				case bnz:
				case brz:
				case br: {
					if(!block.isEmpty())
						container.addBlock(block);
					
					container.addJumpBlock(a);
					block = new AsmBlock();
					break;
				}
				
				case data: {
					if(!block.isEmpty())
						container.addBlock(block);
					
					container.addDataBlock(a);
					block = new AsmBlock();
					break;
				}
				
				case label: {
					if(!block.isEmpty())
						container.addBlock(block);
					
					container.addLabel(a);
					block = new AsmBlock();
					break;
				}
				
				default: {
					block.add(a);
				}
			}
		}
		
		if(!block.isEmpty())
			container.addBlock(block);
		
		return container;
	}
	
	private int findLabelIndex(AsmContainer container, LabelParam label) {
		for(int i = 0; i < container.size(); i++) {
			AsmBlock block = container.blocks.get(i);
			if(!block.isLabelBlock()) continue;
			
			if(label.getName().equals(block.getDataName()))
				return i;
		}
		
		return -1;
	}
	
	private void compileSecondFull(AsmContainer container) {
		int[] sizes = new int[container.size()];
		// NOTE: That jumps are relative to the end of the instruction.
		
		// [ j.. ] jumps are 2 bytes for 8 bit
		//                   5 bytes for 16 bit
		//                   6 bytes for 32 bit
		
		// [ jmp ] jumps are 2 bytes for 8 bit
		//                   4 bytes for 16 bit
		//                   5 bytes for 32 bit
		
		for(int i = 0; i < container.size(); i++) {
			AsmBlock block = container.blocks.get(i);
			if(!block.isJumpBlock()) {
				sizes[i] = block.getCompiledSize();
			} else {
				// Because the lowest amount of bytes a jump instructions uses
				// is two we fill those spaces with the minimal amount of space.
				sizes[i] = 2;
			}
			
			System.out.println("len: " + sizes[i]);
		}
		
		for(int i = 0; i < container.size(); i++) {
			AsmBlock block = container.blocks.get(i);
			if(block.isJumpBlock()) {
				LabelParam label = null;
				try {
					label = (LabelParam)block.list.get(0).getLastParam();
				} catch(Exception e) {
					// FIXME SUPER TEMPORARY EXCEPTION CATCH
					continue;
				}
				
				int index = findLabelIndex(container, label);
				
				System.out.println("======================================");
				
				int mul = (i > index ? -1:1);
				int dist = 0;
				
				int min = Math.min(i, index);
				int max = Math.max(index, i);
				for(int j = min; j <= max; j++) {
					AsmBlock bl = container.blocks.get(j);
					
					String extra = "";
					if(bl.isLabelBlock() || bl.isJumpBlock()) {
						extra = bl.toString();
					}
					
					dist += mul * sizes[j];
					
					if(j != i) {
						System.out.printf("      %3s%s\n", Integer.toString(sizes[j]), extra);
					} else {
						System.out.printf("this: %3s%s\n", Integer.toString(sizes[j]), extra);
					}
				}
				
				System.out.println();
				System.out.println("relative = " + dist);
				
				String command;
				if(dist < 0) {
					command = String.format("jz -0x%02x", -dist);
				} else {
					command = String.format("jz 0x%02x", dist);
				}
				
				System.out.println(command);
				
				AsmInst inst = Assembly.getInstruction(command);
				block.assembly.add(inst);
				block.compiled_code = Assembly.compile(inst);
				block.isCompiled = true;
			}
		}
		
		
	}
	
	private void compileSecond(AsmContainer container, int index, AsmBlock block) {
		// Each jump instruction is 6 bytes we say.
		// Smallest is 2 bytes.
		
		IRInstruction inst = block.list.get(0);
		IRType type = inst.type();
		
		LabelParam label = (LabelParam)inst.getLastParam();
		int idx = findLabelIndex(container, label);
		System.out.println("Jump -> " + inst);
		System.out.println("     :> index = " + idx);
		
		// Create a array with the relative offsets
		// If a offset is within a byte the jump can be encoded in a
		switch(type) {
			case br: {
				
			}
			
			default:
		}
		
	}
	
	// Converts the blocks into assembly opcodes.
	private void compileBlock(AsmBlock block) {
		for(IRInstruction inst : block.list) {
			try_work(block, inst);
		}
		
		IntBuffer buffer = new IntBuffer(block.assembly.size() * 15);
		for(AsmInst inst : block.assembly) {
			// System.out.println(inst);
			int[] opcode = Assembly.compile(inst);
			
			// FIXME: Sometimes the opcode is null because the instruction could not be encoded.
			if(opcode == null) continue;
			buffer.write(opcode);
		}
		
		block.compiled_code = buffer.toArray();
		block.isCompiled = true;
	}
	
	private void try_work(AsmBlock block, IRInstruction inst) {
		switch(inst.type()) {
			case mov: {
				// The 'mov' instruction can never move data into a memory location
				// and can only work with registers therefore, the first parameter
				// will always be a register.
				int size = calculateBits(inst);
				if(size < 1) break;
				
				Param op0 = inst.getParam(0);
				Param op1 = inst.getParam(1);
				
				if(op1 instanceof NumberReg) {
					block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
						createOperator(op0, size), createOperator(op1)
					));
				} else if(op1 instanceof RefReg) {
					RefReg ref = (RefReg)op1;
					
					RegisterX86 reg = pickRegister(size, RegisterX86.DX.index);
					block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
						new OprBuilder().reg(reg).get(),
						new OprBuilder().num(ref.index).ptr(size)
					));
					block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
						createOperator(op0, size),
						new OprBuilder().reg(reg).get()
					));
				} else {
					
				}
				
				break;
			}
			
			case shl:
			case shr:
			case and:
			case xor:
			case or:
			case sub:
			case add: {
				// 'dst' must be a register.
				Param dst = inst.getParam(0);
				Param op0 = inst.getParam(1);
				Param op1 = inst.getParam(2);
				
				AsmMnm action = convertType(inst.type());
				
				int size = calculateBits(inst);
				RegisterX86 reg = pickRegister(size, RegisterX86.DX.index);
				if(reg == null) break;
				
				if(op1 instanceof NumberReg) {
					block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
						new OprBuilder().reg(reg).get(), createOperator(op1)
					));
				} else {
					block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
						new OprBuilder().reg(reg).get(), createOperator(op1)
					));
				}
				
				// For now we say that op0 is always a register.
				if(true) {
					block.assembly.add(Assembly.getInstruction(action,
						new OprBuilder().reg(reg).get(), createOperator(op0, size)
					));
					
					block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
						createOperator(dst, size), new OprBuilder().reg(reg).get()
					));
				}
				
				break;
			}
			
			case write:
			case read: {
				// read from op1 into op0
				Param dst = inst.getParam(0);
				Param src = inst.getParam(1);
				
				int size = calculateBits(inst);
				if(inst.type() == IRType.write) {
					size = 4 << inst.getSize().size();
				}
				
				RegisterX86 reg = pickRegister(size, RegisterX86.DX.index);
				if(reg == null) break;
				
				block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
					new OprBuilder().reg(reg).get(), createOperator(src, size)
				));
				block.assembly.add(Assembly.getInstruction(AsmMnm.MOV,
					createOperator(dst, size), new OprBuilder().reg(reg).get()
				));
				
				break;
			}
			
			case data: {
				// This should output stuff raw without any encoding..
				
				// Convert the content of the data with the specified size.
				// A string should be converted into it's bytes when written...
			}
			
			default: {
				// All the jump instructions will be compiled in the next stage.
				// First stage is to give all of the jump instructions the max size.
				// and after that do elimination by compression them.
				block.assembly.add(Assembly.getInstruction("nop"));
			}
		}
	}
	
	private AsmMnm convertType(IRType type) {
		switch(type) {
			case add: return AsmMnm.ADD;
			case sub: return AsmMnm.SUB;
			case xor: return AsmMnm.XOR;
			case and: return AsmMnm.AND;
			case shl: return AsmMnm.SHL;
			case shr: return AsmMnm.SHR;
			case or: return AsmMnm.OR;
			
			default: return null;
		}
	}
	
	private int calculateBits(IRInstruction inst) {
		LowType type = inst.getSize();
		if(type == null) return 0; // FIXME This should never happen.
		int destSize = 0;
		
		if(type.isPointer()) {
			destSize = 8 * LowType.getPointerSize();
		} else {
			destSize = 8 * type.size();
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
	
	private AsmOpr createOperator(Param param) { return createOperator(param, 32); }
	private AsmOpr createOperator(Param param, int size) {
		if(param instanceof Reg) {
			return new OprBuilder().reg(RegisterX86.RBP).add().num(param.getIndex() * 0x8).ptr(size);
		}
		
		if(param instanceof NumberReg) {
			NumberReg reg = (NumberReg)param;
			return new OprBuilder().imm(reg.getValue());
		}
		
		System.out.println(param.getClass());
		throw new NullPointerException("Could not create operator for '" + param + "'");
	}
}
