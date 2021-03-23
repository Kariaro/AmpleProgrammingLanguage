package com.hardcoded.compiler.impl.context;

import java.util.*;

import com.hardcoded.compiler.impl.context.Reference.Type;

/**
 * A linker scope used to define imported and exported references
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class LinkerScope {
	private final List<Reference> importing;
	private final List<Reference> exporting;
	private int unique;
	
	// Have a scope for FUN/VAR references
	private final LinkedList<Map<String, Integer>> scopes;
	private final Map<Integer, Reference> references;
	private final Map<Integer, String> map;
	
	public LinkerScope() {
		this.references = new HashMap<Integer, Reference>();
		this.importing = new ArrayList<>();
		this.exporting = new ArrayList<>();
		this.scopes = new LinkedList<>();
		this.map = new HashMap<>();
	}
	
	public void push() {
		scopes.add(new HashMap<>());
	}
	
	public void pop() {
		scopes.removeLast();
	}
	
	public Map<Integer, String> map() {
		return map;
	}
	
	public List<Reference> getImport() {
		return importing;
	}
	
	public List<Reference> getExport() {
		return exporting;
	}
	
	// IMPORTED
	public boolean hasImported(String name, Type type) { return getImported0(name, type) != -1; }
	public Reference getImported(String name, Type type) {
		int index = getImported0(name, type);
		if(index < 0) throw new NullPointerException("The imported " + (type == Type.VAR ? "variable":"function") + " '" + name + "' has not been defined");
		return importing.get(index);
	}
	protected int getImported0(String name, Type type) {
		for(int i = 0; i < importing.size(); i++) {
			Reference ref = importing.get(i);
			if(ref.name.equals(name) && ref.type == type) return i;
		}
		return -1;
	}
	public Reference addImported(String name, Type type) {
		if(hasImported(name, type)) throw new NullPointerException("The imported " + (type == Type.VAR ? "variable":"function") + " '" + name + "' has already been defined");
		Reference ref = Reference.unique(name, type, unique++);
		importing.add(ref);
		return ref;
	}
	
	
	// LOCAL
	public boolean hasLocal(String name) { return getLocal0(name) != -1; }
	public Reference getLocal(String name) {
		int index = getLocal0(name);
		if(index < 0) throw new NullPointerException("The local variable '" + name + "' does not exist");
		return references.get(index);
	}
	protected int getLocal0(String name) {
		for(int i = scopes.size() - 1; i >= 1; i--) {
			if(scopes.get(i).containsKey(name)) return scopes.get(i).get(name);
		}
		
		return -1;
	}
	public Reference addLocal(String name) {
		if(hasLocal(name)) throw new NullPointerException("The local variable '" + name + "' already exists");
		scopes.getLast().put(name, unique);
		map.put(unique, name);
		Reference ref = Reference.unique(name, unique);
		references.put(unique++, ref);
		return ref;
	}
	
	
	// GLOBAL
	public boolean hasGlobal(String name) { return getGlobal0(name) != -1; }
	protected int getGlobal0(String name) {
		if(scopes.isEmpty()) return -1;
		return scopes.get(0).getOrDefault(name, -1);
	}
	public Reference getGlobal(String name) {
		int index = getGlobal0(name);
		if(index < 0) throw new NullPointerException("The global variable '" + name + "' does not exist");
		return references.get(index);
	}
	public Reference addGlobal(String name) {
		if(hasGlobal(name)) throw new NullPointerException("The global variable '" + name + "' already exists");
		scopes.get(0).put(name, unique);
		map.put(unique, name);
		Reference ref = Reference.unique(name, unique);
		references.put(unique++, ref);
		return ref;
	}
}
