package hardcoded.compiler.assembler;

import java.util.List;

import hardcoded.compiler.constants.Insts;
import hardcoded.compiler.instruction.Instruction;
import hardcoded.compiler.instruction.InstructionBlock;

public class AssemblyCodeGenerator {
	// This will use the x86 instruction set..
	// RAX, RCX, RDX, RBX, RSP, RBP, RSI, RDI
	// EAX, ECX, EDX, EBX, ESP, EBP, ESI, EDI
	//  AX,  CX,  DX,  BX
	
	public AssemblyCodeGenerator() {
		
	}
	
	public void generate(List<InstructionBlock> blocks) {
		for(InstructionBlock block : blocks) {
			Instruction inst = block.start;
			
			System.out.println("\n" + block.returnType + ", " + block.name);
			
			int count = 0;
			while(inst != null) {
				int idx = count++;
				if(inst.op == Insts.label) System.out.println();
				System.out.printf("%4d: ", idx);
				
				if(inst.op != Insts.label) System.out.print("  ");
				
				System.out.printf("%s\n", inst);
				inst = inst.next;
			}
		}
	}
}
