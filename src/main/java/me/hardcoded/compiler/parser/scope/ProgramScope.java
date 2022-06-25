package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.*;

public class ProgramScope {
	private final FunctionScope functionScope;
	//private final LabelScope labelScope;
	private final LocalScope localScope;
	private final TypeScope typeScope;
	final Map<String, Reference> importedReference;
	final List<Reference> allReferences;
	int count;
	int tempCount;
	
	public ProgramScope() {
		this.importedReference = new HashMap<>();
		this.allReferences = new ArrayList<>();
		
		this.functionScope = new FunctionScope(this);
		this.localScope = new LocalScope(this);
		//this.labelScope = new LabelScope(this);
		this.typeScope = new TypeScope(this);
	}
	
	public void clear() {
		functionScope.clear();
		localScope.clear();
		//labelScope.clear();
		typeScope.clear();
	}
	
	public FunctionScope getFunctionScope() {
		return functionScope;
	}
	
	public LocalScope getLocalScope() {
		return localScope;
	}
	
	//public LabelScope getLabelScope() {
	//	return labelScope;
	//}
	
	public TypeScope getTypeScope() {
		return typeScope;
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
		
		reference = new Reference(name, Primitives.NONE, count++, Reference.IMPORT);
		importedReference.put(name, reference);
		allReferences.add(reference);
		return reference;
	}
	
	public Reference createEmptyReference(String name) {
		Reference reference = new Reference(name, Primitives.NONE, -1 - (tempCount++), 0);
		allReferences.add(reference);
		return reference;
	}
}
