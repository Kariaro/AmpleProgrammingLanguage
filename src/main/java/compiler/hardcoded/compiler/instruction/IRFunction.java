package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hardcoded.compiler.expression.LowType;
import hardcoded.utils.StringUtils;

public class IRFunction {
	private final LowType[] params;
	private final LowType type;
	private final String name;
	
	protected List<IRInstruction> list;
	
	protected IRFunction(LowType type, String name, LowType[] params) {
		Objects.requireNonNull(type); // A type can never be null
		if(params.length > 255)
			throw new IllegalArgumentException("Function argument length cannot exceed 255");
		
		this.list = new ArrayList<>();
		this.params = params;
		this.type = type;
		this.name = name;
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
	
	// FIXME: Change to getReturn() maybe?
	public LowType getType() {
		return type;
	}
	
	public IRInstruction[] getInstructions() {
		return list.toArray(new IRInstruction[0]);
	}
	
	public int length() {
		return list.size();
	}
	
	@Override
	public String toString() {
		if(params.length == 0) return type + " " + name + "():";
		return type + " " + name + "( " + StringUtils.join(", ", params) + " ):";
	}
}
