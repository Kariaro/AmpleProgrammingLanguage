package me.hardcoded.compiler.parser.inst;

import me.hardcoded.compiler.impl.ISyntaxPosition;

public class Inst {
	// Store if this instruction explicitly volatile
	
	private final ISyntaxPosition syntaxPosition;
	private Opcode opcode;
	private int flags;
	
	public Inst(Opcode opcode, ISyntaxPosition syntaxPosition) {
		this.syntaxPosition = syntaxPosition;
		this.opcode = opcode;
	}
	
	public ISyntaxPosition getSyntaxPosition() {
		return syntaxPosition;
	}
	
	public Opcode getOpcode() {
		return opcode;
	}
	
	@Override
	public String toString() {
		return opcode + " ";
	}
}
