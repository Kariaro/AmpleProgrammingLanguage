package hardcoded.compiler.instruction;

import java.util.*;

import hardcoded.compiler.expression.LowType;
import hardcoded.utils.StringUtils;

/**
 * This class is a representation of this compilers internal instruction language
 * or more commonly known as its <i>immediate representation</i> language. A <i>IR</i>
 * language allows a compiler to use more advanced optimization techniques when compiling
 * input syntax.
 * 
 * 
 * <p>Each instruction in this <i>IR</i> language is written with either two or three
 * parameters with the only exception beeing the call instruction that can have a
 * variable amount of parameters.
 * 
 * @author HardCoded
 */
public class IRInstruction {
	private final List<Param> params = new ArrayList<>();
	protected IRType op = IRType.nop;
	
	// TODO: Find a way to calculate the size of a instruction during the generation/optimization stage.
	public IRInstruction(IRType op, Param... regs) {
		this.op = Objects.requireNonNull(op, "IRInstruction type must be non null");
		this.params.addAll(Arrays.asList(regs));
	}
	
	public IRType type() {
		return op;
	}
	
	public Param getParam(int index) {
		return params.get(index);
	}
	
	public List<Param> getParams() {
		return params;
	}
	
	public int getNumParams() {
		return params.size();
	}
	
	public LowType getSize() {
		if(params.isEmpty()) return null;
		
		switch(op) {
			case call -> {
				return params.get(1).getSize();
			}
			
			case brz, bnz, br -> {
				return null;
			}
			
			default -> {
				return params.get(0).getSize();
			}
		}
	}
	
	@Override
	public String toString() {
		if(op == IRType.label) return params.get(0) + ":";
		if(params.isEmpty()) return Objects.toString(op);
		
		LowType size = getSize();
		return String.format("%-8s%-8s         [%s]", op, (size == null ? "":size), StringUtils.join("], [", params));
	}
}