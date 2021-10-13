package hardcoded.compiler.instruction;

public class IRUtils {
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
