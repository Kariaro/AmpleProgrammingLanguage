package hardcoded.assembly.x86;

public final class AsmInst {
	private final AsmMnm mnemonic;
	private final AsmOpr[] operands;
	
	protected AsmInst(AsmMnm mnemonic) {
		this.mnemonic = mnemonic;
		this.operands = new AsmOpr[0];
	}
	
	protected AsmInst(AsmMnm mnemonic, AsmOpr... operands) {
		this.mnemonic = mnemonic;
		this.operands = operands.clone();
	}
	
	public AsmMnm getMnemonic() {
		return mnemonic;
	}
	
	public int getNumOperands() {
		return operands.length;
	}
	
	public AsmOpr getOperand(int index) {
		return operands[index];
	}
	
	public String toString() {
		if(getNumOperands() < 1) return mnemonic.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%-16s", mnemonic));
		
		for(AsmOpr o : operands)
			sb.append(String.format("%-16s", o)).append(", ");
		
		sb.deleteCharAt(sb.length() - 2);
		return sb.toString().trim();
	}

	public String toPlainString() {
		if(getNumOperands() < 1) return mnemonic.toString().toLowerCase();
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s ", mnemonic).toLowerCase());
		
		for(AsmOpr o : operands)
			sb.append(String.format("%s, ", o));
		
		sb.deleteCharAt(sb.length() - 2);
		return sb.toString().trim();
	}
}
