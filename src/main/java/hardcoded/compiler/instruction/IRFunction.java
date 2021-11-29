package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.expression.LowType;

/**
 * @author HardCoded
 */
public class IRFunction {
	private final LowType[] params;
	private final String[] paramNames;
	private final LowType type;
	private final String name;
	
	protected List<IRInstruction> list;
	
	protected IRFunction(LowType type, String name, List<Identifier> list) {
		if(list.size() > 255) {
			throw new IllegalArgumentException("Function argument length cannot exceed 255");
		}
		
		this.type = Objects.requireNonNull(type);
		this.name = Objects.requireNonNull(name);
		this.list = new ArrayList<>();
		
		this.params = new LowType[list.size()];
		this.paramNames = new String[list.size()];
		for(int i = 0; i < list.size(); i++) {
			Identifier ident = list.get(i);
			this.params[i] = ident.getLowType();
			this.paramNames[i] = ident.getName();
		}
	}
	
	protected IRFunction(LowType type, String name, LowType[] params, String[] paramNames) {
		if(params == null || paramNames == null) {
			throw new IllegalArgumentException("'params' and 'paramNames' must not be null");
		}
		
		if(params.length != paramNames.length) {
			throw new IllegalArgumentException("'params' and 'paramNames' must have equal lengths");
		}
		
		if(params.length > 255) {
			throw new IllegalArgumentException("'params' or 'paramNames' length cannot exceed 255");
		}
		
		this.type = Objects.requireNonNull(type);
		this.name = Objects.requireNonNull(name);
		this.list = new ArrayList<>();
		
		// Could this create contamination if the paramNames strings are references?
		this.params = params.clone();
		this.paramNames = paramNames.clone();
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumParams() {
		return params.length;
	}
	
	public LowType[] getParams() {
		return params;
	}
	
	public String[] getParamNames() {
		return paramNames;
	}
	
	public LowType getReturnType() {
		return type;
	}
	
	public IRInstruction[] getInstructions() {
		return list.toArray(IRInstruction[]::new);
	}
	
	public List<IRInstruction> getInstructionsList() {
		return list;
	}
	
	public int length() {
		return list.size();
	}
	
	@Override
	public String toString() {
		if(params.length == 0) return "%s %s():".formatted(type, name);
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < params.length; i++) {
			sb.append(params[i]).append(" @").append(paramNames[i]).append(", ");
		}
		sb.deleteCharAt(sb.length() - 2);
		
		return "%s %s( %s ):".formatted(type, name, sb.toString().trim());
	}
}
