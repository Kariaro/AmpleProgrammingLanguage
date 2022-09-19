package me.hardcoded.compiler.intermediate.inst;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedure class containing instruction code
 *
 * @author HardCoded
 */
public class Procedure {
	private final List<Inst> list;
	private final ProcedureType type;
	private InstRef reference;
	private List<InstRef> parameters;
	
	public Procedure(ProcedureType type) {
		this.list = new ArrayList<>();
		this.type = type;
	}
	
	public ProcedureType getType() {
		return type;
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
	
	/**
	 * Procedure types
	 */
	public enum ProcedureType {
		/**
		 * Invalid procedure
		 * Used instead of null
		 */
		INVALID,
		
		/**
		 * A function procedure
		 * Will contain a function reference
		 */
		FUNCTION,
		
		/**
		 * A code procedure
		 * Will not be callable but always run
		 */
		CODE,
		
		/**
		 * A variable procedure
		 * Will contain a variable name handle
		 */
		VARIABLE
	}
	
	@Override
	public String toString() {
		return reference.getPath();
	}
	
}
