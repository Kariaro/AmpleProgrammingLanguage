package me.hardcoded.compiler.intermediate.inst;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedure class containing instruction code
 * @author HardCoded
 */
public class Procedure {
	private final List<Inst> list;
	
	public Procedure() {
		this.list = new ArrayList<>();
	}
	
	public List<Inst> getInstructions() {
		return list;
	}
	
	public void addInst(Inst inst) {
		list.add(inst);
	}
}
