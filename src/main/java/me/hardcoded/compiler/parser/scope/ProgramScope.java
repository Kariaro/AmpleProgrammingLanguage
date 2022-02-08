package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.*;

public class ProgramScope {
	private final FunctionScope functionScope;
	private final LabelScope labelScope;
	private final LocalScope localScope;
	final Map<String, Reference> importedReference;
	final List<Reference> allReferences;
	int count;
	int tempCount;
	
	public ProgramScope() {
		this.importedReference = new HashMap<>();
		this.allReferences = new ArrayList<>();
		
		this.functionScope = new FunctionScope(this);
		this.localScope = new LocalScope(this);
		this.labelScope = new LabelScope(this);
	}
	
	public void clear() {
		functionScope.clear();
		localScope.clear();
		labelScope.clear();
	}
	
	public FunctionScope getFunctionScope() {
		return functionScope;
	}
	
	public LocalScope getLocalScope() {
		return localScope;
	}
	
	public LabelScope getLabelScope() {
		return labelScope;
	}
	
	public List<Reference> getAllReferences() {
		return allReferences;
	}
	
	public Map<String, Reference> getImportedReferences() {
		return importedReference;
	}
	
	public Reference createImportedReference(String name) {
		Reference reference = importedReference.get(name);
		if (reference != null) {
			return reference;
		}
		
		reference = new Reference(name, ValueType.UNDEFINED, count++, Reference.IMPORT);
		importedReference.put(name, reference);
		allReferences.add(reference);
		return reference;
	}
	
	public Reference createEmptyReference(String name) {
		Reference reference = new Reference(name, ValueType.UNDEFINED, -1 - (tempCount++), 0);
		allReferences.add(reference);
		return reference;
	}
}
