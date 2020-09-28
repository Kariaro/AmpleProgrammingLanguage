package hardcoded.compiler.assembler;

import java.util.List;

import hardcoded.assembly.impl.AsmFactory;
import hardcoded.compiler.constants.IRInsts;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.InstructionBlock;

public class AssemblyCodeGenerator {
	public static void main(String[] args) {
		// AsmInst inst = AsmFactory.getInstruction(AsmMnm.MOV, $->RegisterX86.ECX, $->RegisterX86.EAX);
		// AsmInst inst = AsmFactory.getInstruction(AsmMnm.MOV, $->$.fromString("byte [RAX + -0x3232]"), $->RegisterX86.EAX);
		
		// $->$.fromString("byte [RAX + EAX + EIP]")
		// $->$.fromString("EAX")
		// $->RegisterX86.EAX
		
		AsmFactory.compile(AsmFactory.getInstruction("mov r15, qword [edx - 0x7f]"));
		AsmFactory.compile(AsmFactory.getInstruction("xor rcx, 0x7f"));
		AsmFactory.compile(AsmFactory.getInstruction("push -0x80"));
	}
	
	public AssemblyCodeGenerator() {
		
	}
	
	public void generate(List<InstructionBlock> blocks) {
		for(InstructionBlock block : blocks) {
			IRInstruction inst = block.start;
			
			System.out.println("\n" + block.returnType + ", " + block.name + (block.extra != null ? (", " + block.extra):""));
			
			int count = 0;
			while(inst != null) {
				int idx = count++;
				if(inst.op == IRInsts.label) System.out.println();
				System.out.printf("%4d: ", idx);
				
				if(inst.op != IRInsts.label) System.out.print("  ");
				
				System.out.printf("%s\n", inst);
				inst = inst.next();
			}
		}
	}
}
