package hardcoded.compiler.instruction;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.types.Type;

public class InstructionBlock {
	public Instruction start;
	public Type returnType;
	public String name;
	public String extra;
	
	public InstructionBlock(String name, Instruction inst) {
		this.name = name;
		this.start = inst;
		
		init();
	}
	
	public InstructionBlock(String name, Type returnType, Instruction inst) {
		this.returnType = returnType;
		this.name = name;
		this.start = inst;
		
		init();
	}
	
	public InstructionBlock(Function func, Instruction inst) {
		this.returnType = func.returnType;
		this.name = func.name;
		this.start = inst;
		
		init();
	}
	
	private void init() {
		// Get all branches...
		
	}
}
