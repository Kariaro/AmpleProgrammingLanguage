package hardcoded.exporter.spooky;

import java.util.*;

import hardcoded.compiler.instruction.IRFunction;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.IRInstruction.Param;
import hardcoded.compiler.instruction.IRInstruction.Reg;
import hardcoded.exporter.impl.CodeBlockType;

class SpookyFunction {
	public final List<SpookyBlock> blocks;
	public final IRFunction func;
	
	/**
	 * The instruction offset of this function
	 */
	public int func_offset;
	
	/**
	 * How many local variables inside this function
	 */
	public int stack_size;
	
	public final int id;
	
	public SpookyFunction(IRFunction func, int id) {
		this.blocks = new ArrayList<>();
		this.func = func;
		this.id = id;
	}
	
	public void addBlock(SpookyBlock block) {
		blocks.add(block);
	}
	
	public void addJumpBlock(IRInstruction inst) {
		blocks.add(new SpookyBlock(CodeBlockType.JUMP, inst));
	}
	
	public void addLabel(IRInstruction inst) {
		blocks.add(new SpookyBlock(CodeBlockType.LABEL, inst));
	}
	
	public int size() {
		return blocks.size();
	}
	
	public int getNumParams() {
		return func.getNumParams();
	}
	
	// Tell the size in bytes for this container.
	public int sizeInBytes() {
		return -1;
	}

	public int getStackSize() {
		if(stack_size != 0) return stack_size;
		Set<Integer> set = new HashSet<>();
		
		for(SpookyBlock block : blocks) {
			for(IRInstruction ir : block.list) {
				for(Param param : ir.params) {
					if(param instanceof Reg) {
						
						if(!set.contains(param.getIndex())) {
							set.add(param.getIndex());
						}
					}
				}
			}
		}
		
		stack_size = set.size() + 1 + getNumParams();
		// [ RetAddr, Params, Stack ]
		return stack_size;
	}
	
}
