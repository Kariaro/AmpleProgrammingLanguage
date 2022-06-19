package me.hardcoded.compiler.intermediate.inst;

import java.util.ArrayList;
import java.util.List;

// TODO: Function procedures
public class InstFile {
	private final List<Procedure> procedures;
	
	public InstFile() {
		this.procedures = new ArrayList<>();
	}
	
	public void addProcedure(Procedure procedure) {
		this.procedures.add(procedure);
	}
	
	public List<Procedure> getProcedures() {
		return procedures;
	}
}
