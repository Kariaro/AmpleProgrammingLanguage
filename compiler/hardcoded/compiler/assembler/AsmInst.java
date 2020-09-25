package hardcoded.compiler.assembler;

import hardcoded.compiler.assembler.AsmOpr.OprBuilder;
import hardcoded.compiler.assembler.operator.Register;

public class AsmInst {
	private AsmMnm mnemonic;
	private AsmOpr[] operators;
	
	public AsmInst(AsmMnm mnm, Object... regs) {
		mnemonic = mnm;
		
		operators = new AsmOpr[regs.length];
		for(int i = 0; i < regs.length; i++) {
			Object o = regs[i];
			
			if(o instanceof Register) {
				operators[i] = new AsmOpr((Register)o);
			} else if(o instanceof AsmOpr) {
				operators[i] = (AsmOpr)o;
			} else {
				operators[i] = null;
			}
		}
	}
	
	@SafeVarargs
	public AsmInst(AsmMnm mnm, java.util.function.Function<OprBuilder, Object>... regs) {
		mnemonic = mnm;
		
		operators = new AsmOpr[regs.length];
		for(int i = 0; i < regs.length; i++) {
			Object o = regs[i].apply(new OprBuilder());
			
			if(o instanceof OprBuilder) {
				operators[i] = ((OprBuilder)o).get();
			} else  if(o instanceof Register) {
				operators[i] = new AsmOpr((Register)o);
			} else if(o instanceof AsmOpr) {
				operators[i] = (AsmOpr)o;
			} else {
				operators[i] = null;
			}
		}
	}
	
	
	public void setMnemonic(AsmMnm m) {
		mnemonic = m;
	}
	
	public AsmMnm getMnemonic() {
		return mnemonic;
	}
	
	public int getNumOperators() {
		return operators.length;
	}
	
	public AsmOpr getOperand(int index) {
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
		
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString().trim();
	}
}
