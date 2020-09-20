package hardcoded.compiler.assembler;

import java.util.List;

import hardcoded.compiler.assembler.AssemblyConsts.AsmOp;
import hardcoded.compiler.constants.Insts;
import hardcoded.compiler.instruction.Instruction;
import hardcoded.compiler.instruction.InstructionBlock;

public class AssemblyCodeGenerator {
	// This will use the x86 instruction set..
	
	// 16 instructions where only 12 will be used
	// Reserved for other use: RSP, RBP, RSI, RDI
	// RAX, RCX, RDX, RBX, R15, R14, R13, R12
	// R11, R10, R9,  R8
	
	
	public static void main(String[] args) {
		// AsmInst inst = new AsmInst(AsmMnm.ADD, AsmReg.AX, new AsmOpr.OprBuilder().imm16(0x1122).get());
		// AsmInst inst = new AsmInst(AsmMnm.ADD, AsmReg.ESP, new AsmOpr.OprBuilder().imm8(0x8).get());
		AsmInst inst = new AsmInst(AsmMnm.MOV, AsmReg.ECX, AsmReg.ESI);
		
		List<AsmOp> list = Assembly.lookup(inst);
		
		for(AsmOp op : list) {
			System.out.println("Matching instruction -> " + op.toComplexString());
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println(inst);
	}
	
	public AssemblyCodeGenerator() {
		
	}
	
	public void generate(List<InstructionBlock> blocks) {
		for(InstructionBlock block : blocks) {
			Instruction inst = block.start;
			
			System.out.println("\n" + block.returnType + ", " + block.name + (block.extra != null ? (", " + block.extra):""));
			
			int count = 0;
			while(inst != null) {
				int idx = count++;
				if(inst.op == Insts.label) System.out.println();
				System.out.printf("%4d: ", idx);
				
				if(inst.op != Insts.label) System.out.print("  ");
				
				System.out.printf("%s\n", inst);
				inst = inst.next();
			}
		}
	}
}
