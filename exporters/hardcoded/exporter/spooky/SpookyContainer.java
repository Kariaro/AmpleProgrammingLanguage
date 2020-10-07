package hardcoded.exporter.spooky;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.instruction.IRFunction;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.exporter.impl.CodeBlockType;

class SpookyContainer {
	public final List<SpookyBlock> blocks;
	public final IRFunction func;
	public int func_offset;
	public final int id;
	
	public SpookyContainer(IRFunction func, int id) {
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
	
	// Tell the size in bytes for this container.
	public int sizeInBytes() {
		return -1;
	}
	
}
