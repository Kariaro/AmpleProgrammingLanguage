package hardcoded.assembly.impl;

import hardcoded.assembly.x86.*;

public final class AsmInst {
	private final AsmMnm mnemonic;
	private final AsmOpr[] operators;
	
	AsmInst(AsmMnm mnemonic) {
		this.mnemonic = mnemonic;
		this.operators = new AsmOpr[0];
	}
	
	AsmInst(AsmMnm mnemonic, AsmOpr... operators) {
		this.mnemonic = mnemonic;
		this.operators = operators.clone();
	}
	
	public AsmMnm getMnemonic() {
		return mnemonic;
	}
	
	public int getNumOperators() {
		return operators.length;
	}
	
	public AsmOpr getOperator(int index) {
		return operators[index];
	}
	
	public String toString() {
		if(getNumOperators() < 1) {
			return mnemonic.toString();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%-16s", mnemonic));
		
		for(AsmOpr o : operators) {
			sb.append(String.format("%-16s", o)).append(", ");
		}
		
		sb.deleteCharAt(sb.length() - 2);
		
		return sb.toString().trim();
	}
}
