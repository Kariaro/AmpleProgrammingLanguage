package hardcoded.exporter.spooky;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.exporter.impl.CodeBlockType;

class SpookyBlock {
	List<IRInstruction> list = new ArrayList<>();
	List<SpookyInst> insts = new ArrayList<>();
	
	private CodeBlockType blockType = CodeBlockType.INST;
	
	/**
	 * For label blocks this is a label name.
	 * For data blocks this is the name of the pointer data.
	 */
	private String dataName;
	
	
	SpookyBlock() {
	}
	
	SpookyBlock(CodeBlockType type) {
		blockType = type;
	}
	
	SpookyBlock(CodeBlockType type, IRInstruction inst) {
		blockType = type;
		
		switch(type) {
			case LABEL: {
				dataName = ((IRInstruction.LabelParam)inst.getParam(0)).name;
				break;
			}
			
			default: {
				list.add(inst);
			}
		}
	}
	
	void add(IRInstruction inst) {
		list.add(inst);
	}
	
	boolean isEmpty() {
		return list.isEmpty();
	}

	public boolean isJumpBlock() { return blockType == CodeBlockType.JUMP; }
	public boolean isDataBlock() { return blockType == CodeBlockType.DATA; }
	public boolean isInstBlock() { return blockType == CodeBlockType.INST; }
	public boolean isLabelBlock() { return blockType == CodeBlockType.LABEL; }
	
	public byte[] compile() {
		if(insts.isEmpty()) return new byte[0];
		ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
		
		for(SpookyInst inst : insts) {
			byte[] bytes = inst.compile();
			stream.write(bytes, 0, bytes.length);
		}
		
		return stream.toByteArray();
	}
	
	public String getDataName() {
		return dataName;
	}
	
	public String toString() {
		if(blockType == CodeBlockType.LABEL) {
			return "\t" + dataName;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(IRInstruction inst : list) {
			sb.append(inst).append("\n\t");
		}
		
		return "\t" + sb.toString().trim();
	}
}
