package hardcoded.compiler.instruction;

import hardcoded.utils.StringUtils;

public class IRUtils {
	public static String printPretty(IRProgram program) {
		StringBuilder sb = new StringBuilder();
		sb.append(".data.strings:\n");
		{
			int index = 0;
			for(String s : program.context.strings) {
				sb.append("%4d:   \"%s\"\n".formatted(index++, StringUtils.escapeString(s)));
			}
			
			sb.append("\n");
		}
		
		for(IRFunction func : program.getFunctions()) {
			sb.append(printPretty(func)).append("\n");
		}
		
		return sb.toString().trim();
	}
	
	public static String printPretty(IRFunction func) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n").append(func).append("\n");
		for(int i = 0, line = 0; i < func.length(); i++) {
			IRInstruction inst = func.list.get(i);
			
			if(inst.op == IRType.label) {
				sb.append("\n%4d: %s\n".formatted(line, inst));
			} else {
				sb.append("%4d:   %s\n".formatted(line, inst));
				line++;
			}
		}
		
		return sb.toString().trim();
	}
}
