package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NamespaceScope {
	private final ProgramScope programScope;
	private final Namespace namespaceRoot;
	private final LinkedList<Reference> scopes;
	private final Map<String, Reference> namespaceMap;
	
	NamespaceScope(ProgramScope programScope) {
		this.programScope = programScope;
		this.scopes = new LinkedList<>();
		this.namespaceMap = new HashMap<>();
		this.namespaceRoot = new Namespace();
		
		// TODO: Handle root namespace better
		Reference rootReference = programScope.createNamespaceReference(namespaceRoot);
		this.namespaceMap.put("", rootReference);
		this.scopes.add(rootReference);
	}
	
	public void clear() {
		// TODO: Insert root namespace again
		scopes.clear();
		namespaceMap.clear();
	}
	
	public void pushNamespace(String name) {
		Namespace parent = scopes.getLast().getNamespace();
		Namespace child = new Namespace(parent, name);
		
		Reference ref;
		if (namespaceMap.containsKey(child.getPath())) {
			ref = namespaceMap.get(child.getPath());
		} else {
			ref = programScope.createNamespaceReference(child);
			namespaceMap.put(child.getPath(), ref);
		}
		
		scopes.add(ref);
	}
	
	public void popNamespace() {
		scopes.pollLast();
	}
	
	public Namespace getNamespaceRoot() {
		return namespaceRoot;
	}
	
	public Namespace getNamespace() {
		return getNamespaceReference().getNamespace();
	}
	
	public Reference getNamespaceReference() {
		return scopes.getLast();
	}
	
	public Namespace getRelativeNamespace(Namespace base, Namespace path) {
		Reference reference = null;
		if (path.isRoot()) {
			reference = namespaceMap.get(base.getPath());
		} else {
			// Path is not root
			if (base.isRoot()) {
				reference = namespaceMap.get(path.getPath());
			} else {
				reference = namespaceMap.get(base.getPath() + "::" + path.getPath());
			}
		}
		
		if (reference != null) {
			return reference.getNamespace();
		}
		
		return null;
	}
	
	public Namespace resolveNamespace(List<String> parts) {
		if (parts.isEmpty()) {
			return getNamespaceRoot();
		}
		
		Reference ref = namespaceMap.get(String.join("::", parts));
		
		if (ref != null) {
			return ref.getNamespace();
		}
		
		return null;
	}
}
