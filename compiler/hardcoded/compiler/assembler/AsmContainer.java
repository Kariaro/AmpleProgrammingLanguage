package hardcoded.compiler.assembler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.assembler.AsmBlock.BlockType;
import hardcoded.compiler.instruction.IRInstruction;

// Contains a function made up of blocks.
public class AsmContainer {
	public List<AsmBlock> blocks;
	
	public AsmContainer() {
		blocks = new ArrayList<>();
	}
	
	// Add a block to the container.
	public void addBlock(AsmBlock block) {
		blocks.add(block);
	}
	
	// Add a single instruction block to the container.
	public void addJumpBlock(IRInstruction inst) {
		blocks.add(new AsmBlock(BlockType.JUMP, inst));
	}
	
	// Add a single instruction block to the container.
	public void addDataBlock(IRInstruction inst) {
		blocks.add(new AsmBlock(BlockType.DATA, inst));
	}
	
	// Add a single instruction block to the container.
	public void addLabel(IRInstruction inst) {
		blocks.add(new AsmBlock(BlockType.LABEL, inst));
	}
	
	public int size() {
		return blocks.size();
	}
	
	// Tell the size in bytes for this container.
	public int sizeInBytes() {
		return -1;
	}
	
}
