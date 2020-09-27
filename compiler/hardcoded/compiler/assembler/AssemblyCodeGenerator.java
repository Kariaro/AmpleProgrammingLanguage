package hardcoded.compiler.assembler;

import java.util.List;

import hardcoded.assembly.impl.AsmFactory;
import hardcoded.assembly.impl.AsmInst;
import hardcoded.assembly.x86.AsmMnm;
import hardcoded.assembly.x86.OprBuilder;
import hardcoded.assembly.x86.RegisterX86;
import hardcoded.compiler.assembler.AssemblyConsts.AsmOp;
import hardcoded.compiler.constants.IRInsts;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.InstructionBlock;

public class AssemblyCodeGenerator {
	// This will use the x86 instruction set..
	
	public static void main(String[] args) {
		// AsmInst inst = new AsmInst(AsmMnm.ADD, AsmReg.AX, new AsmOpr.OprBuilder().imm16(0x1122).get());
		// AsmInst inst = new AsmInst(AsmMnm.ADD, AsmReg.ESP, new AsmOpr.OprBuilder().imm8(0x8).get());
		// AsmInst inst = new AsmInst(AsmMnm.MOV, AsmReg.ECX, AsmReg.ESI);
		
		System.out.println(new OprBuilder().fromString("byte [RAX + EAX + EIP * 0x9]"));
		
		// AsmInst inst = AsmFactory.getInstruction(AsmMnm.MOV, $->RegisterX86.ECX, $->RegisterX86.EAX);
		AsmInst inst = AsmFactory.getInstruction(AsmMnm.MOV, $->$.fromString("byte [RAX]"), $->RegisterX86.EAX);
		// AsmInst inst = AsmFactory.getInstruction("MOV byte [RAX], EAX");
		
		// $->$.fromString("byte [RAX + EAX + EIP]")
		// $->$.fromString("EAX")
		// $->RegisterX86.EAX
		List<AsmOp> list = Assembly.lookup(inst);
		
		if(!list.isEmpty()) {
			AsmOp first = list.get(0);
			for(AsmOp op : list) {
				if(op != first)
					System.out.println("      :> " + op.toComplexString());
			}
			System.out.println("Using :> " + first.toComplexString() + "\n");
		}
		
		System.out.println();
		System.out.println();
		System.out.println(inst);
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
