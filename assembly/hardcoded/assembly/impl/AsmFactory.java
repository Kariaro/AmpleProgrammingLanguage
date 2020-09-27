package hardcoded.assembly.impl;

import java.util.Arrays;

import hardcoded.assembly.x86.*;

public final class AsmFactory {
	private AsmFactory() {}
	
	public static AsmInst getInstruction(AsmMnm mnemonic, AsmOpr... operators) {
		return new AsmInst(mnemonic, operators);
	}
	
	@SafeVarargs
	public static AsmInst getInstruction(AsmMnm mnemonic, java.util.function.Function<OprBuilder, Object>... regs) {
		AsmOpr[] operators = new AsmOpr[regs.length];
		
		for(int i = 0; i < regs.length; i++) {
			Object o = regs[i].apply(new OprBuilder());
			
			if(o instanceof OprBuilder) {
				operators[i] = ((OprBuilder)o).get();
			} else if(o instanceof RegisterX86) {
				operators[i] = new OprBuilder().reg((RegisterX86)o).get();
			} else if(o instanceof AsmOpr) {
				operators[i] = (AsmOpr)o;
			} else {
				operators[i] = null;
			}
		}
		
		return new AsmInst(mnemonic, operators);
	}
	
	public static AsmInst getInstruction(String value) {
		value = value.replaceAll("\\s+", " ").trim();
		
		int index = value.indexOf(' ');
		if(index < 0) {
			return new AsmInst(AsmMnm.valueOf(value.toUpperCase()));
		}
		
		AsmMnm mnemonic = AsmMnm.valueOf(value.substring(0, index).toUpperCase());
		value = value.substring(index);
		
		AsmOpr[] operators = Arrays.asList(value.split(","))
			.stream().map(s -> new OprBuilder().fromString(s.trim()))
			.toArray(AsmOpr[]::new);
		
		return new AsmInst(mnemonic, operators);
	}
	
	/**
	 * Convert a size type into a string.
	 * 
	 * @param	bits	the size
	 * @return a serialized version of a size type.
	 */
	public static String getSizeString(int bits) {
		switch(bits) {
			case 8: return "byte";
			case 16: return "word";
			case 32: return "dword";
			case 64: return "qword";
			case 128: return "xmmword";
			case 256: return "ymmword";
			default: return "???";
		}
	}
}
