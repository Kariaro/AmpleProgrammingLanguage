package me.hardcoded.exporter.spooky;

import java.util.*;

import me.hardcoded.compiler.instruction.IRFunction;
import me.hardcoded.compiler.instruction.IRInstruction;
import me.hardcoded.compiler.instruction.Param;
import me.hardcoded.compiler.instruction.Param.RegParam;

class SpookyFunction {
	public final List<SpookyBlock> blocks;
	public final IRFunction func;
	
	/**
	 * The instruction offset of this function
	 */
	public int func_offset;
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
		if(func == null) return 0;
		return func.getNumParams();
	}
	
	public boolean isExtern() {
		if(func == null) return true;
		return func.length() == 0;
	}
	
	private int usage = -1;
	public int getUsage() {
		if(isExtern()) return 0;
		if(usage != -1) return usage;
		
		Set<Integer> set = new HashSet<>();
		
		for(SpookyBlock block : blocks) {
			for(IRInstruction ir : block.list) {
				for(Param param : ir.getParams()) {
					if(!(param instanceof RegParam)) continue;
					RegParam reg = (RegParam)param;
					
					if(!reg.isTemporary()) continue;
					
					if(!set.contains(param.getIndex())) {
						set.add(param.getIndex());
					}
				}
			}
		}
		
		// [ ReturnAddress, Params, Stack ] = Function Memory
		usage = 1 + getNumParams() + set.size();
		return usage;
	}
	
}
