package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hardcoded.compiler.expression.LowType;

public class IRFunction {
	private final IRProgram program;
	private final LowType type;
	private final String name;
	private final LowType[] params;
	
	List<IRInstruction> list;
	String extra;
	
	IRFunction(IRProgram program, LowType type, String name, LowType[] params) {
		Objects.requireNonNull(type); // A type can never be null
		
		this.program = program;
		this.list = new ArrayList<>();
		this.params = params;
		this.type = type;
		this.name = name;
	}
	
	public IRProgram getProgram() {
		return program;
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
	
	public LowType getType() {
		return type;
	}
	
	public IRInstruction[] getInstructions() {
		return list.toArray(new IRInstruction[0]);
	}
	
	/**
	 * Combine all instructions that have been disconnected.
	 */
	void fixInstructions() {
		if(list.isEmpty()) return;
		
		list.get(0).prev = null;
		list.get(list.size() - 1).next = null;
		if(list.size() == 1) return;
		if(list.size() == 2) {
			IRInstruction first = list.get(0);
			IRInstruction last = list.get(1);
			first.next = last;
			last.prev = first;
			return;
		}
		
		for(int i = 1; i < list.size() - 1; i++) {
			IRInstruction a = list.get(i - 1);
			IRInstruction b = list.get(i);
			a.next = b;
			b.prev = a;
		}
	}
	
	public int length() {
		return list.size();
	}
	
	
	@Override
	public String toString() {
		return type + " " + name + "( ... ); [" + extra + "]";
	}
}
