package hardcoded.compiler.assembler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.assembly.impl.AsmInst;
import hardcoded.compiler.instruction.IRInstruction;

class AsmBlock {
	// Contains instructions...
	List<IRInstruction> list = new ArrayList<>();
	List<AsmInst> asm_list = new ArrayList<>();
	final boolean special;
	
	// Label:
	//   AsmBlock ...
	//            ...
	//            ...
	//            ...
	//            ...
	// JUMP:
	//   AsmBlock ...
		
	
	public AsmBlock() {
		special = false;
	}
	
	public AsmBlock(IRInstruction inst) {
		special = true;
		list.add(inst);
	}
	
	public void add(IRInstruction inst) {
		list.add(inst);
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public boolean isSpecial() {
		return special;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(IRInstruction inst : list) {
			sb.append(inst).append("\n\t");
		}
		
		return "\t" + sb.toString().trim();
	}
}
