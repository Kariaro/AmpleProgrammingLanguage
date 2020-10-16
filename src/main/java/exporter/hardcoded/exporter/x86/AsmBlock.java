package hardcoded.exporter.x86;

import java.util.ArrayList;
import java.util.List;

import hardcoded.assembly.x86.AsmInst;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.IRInstruction.DataParam;
import hardcoded.compiler.instruction.IRInstruction.Param;
import hardcoded.exporter.impl.CodeBlockType;

/**
 * There are three types of assembly blocks.
 * 
 *<PRE>
 *(0): Data blocks that contain a map linking a label and some data.
 *(1): Jump blocks that contain single instructions with a label offset.
 *(2): Inst blocks that contain basic assembly instructions.
 *(3): Label block that contains a name.
 *</PRE>
 *
 *
 *@author HardCoded
 */
class AsmBlock {
	protected List<IRInstruction> list = new ArrayList<>();
	protected List<AsmInst> assembly = new ArrayList<>();
	protected int[] compiled_code;
	protected boolean isCompiled;
	
	private CodeBlockType blockType = CodeBlockType.INST;
	
	/**
	 * For label blocks this is a label name.
	 * For data blocks this is the name of the pointer data.
	 */
	private String dataName;
	
	
	public AsmBlock() {}
	public AsmBlock(CodeBlockType type, IRInstruction inst) {
		blockType = type;
		
		switch(type) {
			case DATA: {
				// TODO: Get the name of this data block together with the offset.
				
				Param reg = inst.getLastParam();
				
				if(reg instanceof DataParam) {
					Object obj = ((DataParam)reg).getValue();
					
					System.out.println(obj + "/" + obj.getClass());
					if(obj instanceof String) {
						String string = (String)obj;
						
						compiled_code = new int[string.length()];
						for(int i = 0; i < string.length(); i++) {
							compiled_code[i] = (string.charAt(i) & 0xff);
						}
					}
				}
				
				isCompiled = true;
				break;
			}
			case LABEL: {
				dataName = inst.getParam(0).getName();
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
	
	public boolean isCompiled() {
		return isCompiled;
	}
	
	public String getDataName() {
		return dataName;
	}
	
	public int getCompiledSize() {
		if(compiled_code == null) return -1;
		return compiled_code.length;
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
