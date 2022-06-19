package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.stat.FuncStat;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedure class containing instruction code
 * @author HardCoded
 */
public class Procedure {
	private final List<Inst> list;
	private InstRef reference;
	private List<InstRef> parameters;
	
	public Procedure() {
		this.list = new ArrayList<>();
	}
	
	public InstRef getReference() {
		return reference;
	}
	
	public List<InstRef> getParameters() {
		return parameters;
	}
	
	@Deprecated
	public void fillData(InstRef reference, List<InstRef> parameters) {
		this.reference = reference;
		this.parameters = parameters;
	}
	
	public List<Inst> getInstructions() {
		return list;
	}
	
	public void addInst(Inst inst) {
		list.add(inst);
	}
	
	@Override
	public String toString() {
		return reference.getName();
	}
}
