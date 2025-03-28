package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.ArrayList;
import java.util.List;

public class Inst {
	private final ISyntaxPos syntaxPos;
	private final List<InstParam> parameters;
	private Opcode opcode;
	
	public Inst(Opcode opcode, ISyntaxPos syntaxPos) {
		this.syntaxPos = syntaxPos;
		this.parameters = new ArrayList<>();
		this.opcode = opcode;
	}
	
	public ISyntaxPos getSyntaxPosition() {
		return syntaxPos;
	}
	
	public Inst addParam(InstParam param) {
		parameters.add(param);
		return this;
	}
	
	public void setOpcode(Opcode opcode) {
		this.opcode = opcode;
	}
	
	public InstParam.Ref getRefParam(int index) {
		return (InstParam.Ref) parameters.get(index);
	}
	
	public InstParam.Str getStrParam(int index) {
		return (InstParam.Str) parameters.get(index);
	}
	
	public InstParam.Num getNumParam(int index) {
		return (InstParam.Num) parameters.get(index);
	}
	
	public InstParam getParam(int index) {
		return parameters.get(index);
	}
	
	public List<InstParam> getParameters() {
		return parameters;
	}
	
	public int getParamCount() {
		return parameters.size();
	}
	
	public Opcode getOpcode() {
		return opcode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(opcode.toString().toLowerCase());
		
		if (parameters.size() > 0) {
			InstParam param = getParam(0);
			ValueType type = param.getSize();
			
			boolean keep = switch (opcode) {
				case INLINE_ASM, STACK_ALLOC, LABEL, MEMBER_PTR -> false;
				default -> true;
			};
			
			int size = type.calculateBytes() * 8;
			if (keep && size != 0) {
				sb.append(size);
			}
		}
		
		if (!parameters.isEmpty()) {
			String data = sb.toString();
			sb.replace(0, sb.length(), "");
			sb.append("%-12s ".formatted(data));
			
			List<String> annotators = switch (opcode) {
				case MOV, ADD, SUB -> List.of("dst", "src");
				case CALL -> List.of("dst", "name");
				case MEMBER_PTR -> List.of("dst", "src", "index", "memberIndex");
				case STORE -> List.of("ptr", "index", "src");
				case LOAD -> List.of("dst", "ptr", "index");
				default -> List.of();
			};
			final int annotatorLen = annotators.size();
			final int size = parameters.size();
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				
				if (i < annotatorLen) {
					String txt = annotators.get(i);
					if (txt != null) {
						sb.append(txt).append("=");
					}
				}
				
				sb.append("<").append(parameters.get(i).toSimple()).append(">");
			}
		}
		
		return sb.toString();
	}
}
