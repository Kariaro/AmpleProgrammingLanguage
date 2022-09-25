package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramScope {
	private final NamespaceScope namespaceScope;
	private final FunctionScope functionScope;
	private final LocalScope localScope;
	private final TypeScope typeScope;
	final Map<Reference, ISyntaxPosition> firstReferencePosition;
	final Map<String, Reference> importedReference;
	final List<Reference> allReferences;
	int count;
	int tempCount;
	
	public ProgramScope() {
		this.firstReferencePosition = new HashMap<>();
		this.importedReference = new HashMap<>();
		this.allReferences = new ArrayList<>();
		
		this.namespaceScope = new NamespaceScope(this);
		this.functionScope = new FunctionScope(this);
		this.localScope = new LocalScope(this);
		this.typeScope = new TypeScope(this);
	}
	
	public void clear() {
		namespaceScope.clear();
		functionScope.clear();
		localScope.clear();
		typeScope.clear();
	}
	
	public NamespaceScope getNamespaceScope() {
		return namespaceScope;
	}
	
	public FunctionScope getFunctionScope() {
		return functionScope;
	}
	
	public LocalScope getLocalScope() {
		return localScope;
	}
	
	public TypeScope getTypeScope() {
		return typeScope;
	}
	
	public List<Reference> getAllReferences() {
		return allReferences;
	}
	
	public Map<String, Reference> getImportedReferences() {
		return importedReference;
	}
	
	public ISyntaxPosition getFirstReferencePosition(Reference reference) {
		return firstReferencePosition.get(reference);
	}
	
	public void setReferencePosition(Reference reference, ISyntaxPosition syntaxPosition) {
		if (firstReferencePosition.putIfAbsent(reference, syntaxPosition) != null) {
			throw new RuntimeException("Reference was added multiple times");
		}
	}
	
	public Reference createImportedReference(String name) {
		Reference reference = importedReference.get(name);
		if (reference != null) {
			return reference;
		}
		
		reference = new Reference(name, namespaceScope.getNamespace(), Primitives.NONE, count++, Reference.IMPORT);
		importedReference.put(name, reference);
		allReferences.add(reference);
		return reference;
	}
	
	public Reference createEmptyReference(String name) {
		Reference reference = new Reference(name, namespaceScope.getNamespace(), Primitives.NONE, -1 - (tempCount++), 0);
		allReferences.add(reference);
		return reference;
	}
	
	protected Reference createNamespaceReference(Namespace namespace) {
		Reference reference = new Reference(namespace.getPath(), namespace, Primitives.NONE, -1 - (tempCount++), Reference.NAMESPACE);
		allReferences.add(reference);
		return reference;
	}
}
