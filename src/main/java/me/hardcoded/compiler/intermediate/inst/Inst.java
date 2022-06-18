package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.utils.Position;

import java.util.ArrayList;
import java.util.List;

public class Inst {
	// Store if this instruction explicitly volatile
	
	private final ISyntaxPosition syntaxPosition;
	private final List<InstParam> parameters;
	private Opcode opcode;
	private int flags;
	
	public Inst(Opcode opcode, ISyntaxPosition syntaxPosition) {
		this.syntaxPosition = syntaxPosition;
		this.parameters = new ArrayList<>();
		this.opcode = opcode;
	}
	
	public ISyntaxPosition getSyntaxPosition() {
		return syntaxPosition;
	}
	
	public Inst addParam(InstParam param) {
		parameters.add(param);
		return this;
	}
	
	public InstParam.Ref getRefParam(int index) {
		return (InstParam.Ref) parameters.get(index);
	}
	
	public InstParam getParam(int index) {
		return parameters.get(index);
	}
	
	public Opcode getOpcode() {
		return opcode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(opcode);
		
		if (!parameters.isEmpty()) {
			for (InstParam param : parameters) {
				sb.append(", ").append(param);
			}
		}
		
		return sb.toString();
	}
}
