package me.hardcoded.compiler.intermediate.inst;

import java.util.ArrayList;
import java.util.List;

public class IntermediateFile {
	private final List<Procedure> procedures;
	
	public IntermediateFile() {
		this.procedures = new ArrayList<>();
	}
	
	public void addProcedure(Procedure procedure) {
		this.procedures.add(procedure);
	}
	
	public List<Procedure> getProcedures() {
		return procedures;
	}
}
