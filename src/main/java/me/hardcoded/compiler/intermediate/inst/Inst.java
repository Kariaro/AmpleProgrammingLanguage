package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.ArrayList;
import java.util.List;

public class Inst {
	private final ISyntaxPos syntaxPos;
	private final List<InstParam> parameters;
	private final Opcode opcode;
	
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
				case INLINE_ASM, STACK_ALLOC -> false;
				default -> true;
			};
			
			if (keep && type.getSize() != 0) {
				sb.append(type.calculateBytes() * 8);
			}
		}
		
		if (!parameters.isEmpty()) {
			sb.append(" ");
			var iter = parameters.iterator();
			while (iter.hasNext()) {
				sb.append("(").append(iter.next()).append(")");
				
				if (iter.hasNext()) {
					sb.append(", ");
				}
			}
		}
		
		return sb.toString();
	}
}
