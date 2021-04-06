package com.hardcoded.compiler.impl.context;

import java.util.*;
import java.util.stream.Collectors;

import com.hardcoded.compiler.impl.context.Reference.Type;

/**
 * A linker scope used to define imported and exported references
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class LinkerScope {
	// Have a scope for FUN/VAR references
	private final Map<Reference.Type, Map<String, Reference>> imported;
	private final Map<Reference.Type, Map<String, Reference>> exported;
	private final Map<Reference.Type, Map<String, Reference>> globals;
	private final Map<Reference.Type, Map<String, Reference>> locals;
	private int unique;
	
	private final LinkedList<Map<String, Integer>> scopes;
	private final Map<Integer, Reference> references;
	private final List<String> imported_files;
	
	public LinkerScope() {
		this.imported_files = new ArrayList<>();
		this.references = new HashMap<>();
		this.scopes = new LinkedList<>();
		this.imported = new HashMap<>();
		this.exported = new HashMap<>();
		this.globals = new HashMap<>();
		this.locals = new HashMap<>();
	}
	
	public void push() {
		scopes.add(new HashMap<>());
	}
	
	public void pop() {
		scopes.removeLast();
	}
	
	public List<String> getImportedFiles() {
		return imported_files;
	}
	
	public List<Reference> getImport() {
		List<Reference> list = imported.values().stream().flatMap(x -> x.values().stream()).collect(Collectors.toList());
		list.sort(Reference.COMPARATOR);
		return list;
	}
	
	public List<Reference> getExport() {
		List<Reference> list = exported.values().stream().flatMap(x -> x.values().stream()).collect(Collectors.toList());
		list.sort(Reference.COMPARATOR);
		return list;
	}
	
	public List<Reference> getGlobals() {
		List<Reference> list = globals.values().stream().flatMap(x -> x.values().stream()).collect(Collectors.toList());
		list.sort(Reference.COMPARATOR);
		return list;
	}
	
	// FILES
	public void addImportedFile(String path) {
		imported_files.add(path);
	}
	
	public int getUniqueIndex() {
		return unique;
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
		Reference ref = Reference.unique(name, unique);
		references.put(unique++, ref);
		return ref;
	}
	public Reference addDirectLocal(Reference ref) {
		if(hasLocal(ref.getName())) throw new NullPointerException("The local variable '" + ref.getName() + "' already exists");
		scopes.getLast().put(ref.getName(), unique);
		references.put(unique++, ref);
		return ref;
	}
	
	// LOCALS_NON_VARIABLE
	public boolean hasLocals(String name, Type type) { return _has(locals, name, type); }
	public Reference getLocals(String name, Type type) { return _get(locals, "local", name, type); }
	public Reference addLocals(String name, Type type) { return _add(locals, "local", name, type); }
	
	// EXPORTING
	public boolean hasExported(String name, Type type) { return _has(exported, name, type); }
	public Reference getExported(String name, Type type) { return _get(exported, "exported", name, type); }
	public Reference addExported(String name, Type type) { return _add(exported, "exported", name, type); }
	public Reference addDirectExported(Reference ref) { return _add_direct(exported, "exported", ref); }
	
	// IMPORTING
	public boolean hasImported(String name, Type type) { return _has(imported, name, type); }
	public Reference getImported(String name, Type type) { return _get(imported, "imported", name, type); }
	public Reference addImported(String name, Type type) { return _add(imported, "imported", name, type); }
	public Reference addDirectImported(Reference ref) { return _add_direct(imported, "imported", ref); }
	
	// GLOBALS
	public boolean hasGlobal(String name, Type type) { return _has(globals, name, type); }
	public Reference getGlobal(String name, Type type) { return _get(globals, "global", name, type); }
	public Reference addGlobal(String name, Type type) { return _add(globals, "global", name, type); }
	public Reference addDirectGlobal(Reference ref) { return _add_direct(globals, "global", ref); }
	
	// Generic
	private boolean _has(Map<Type, Map<String, Reference>> map, String name, Type type) { return _get0(map, name, type) != null; }
	private Reference _get(Map<Type, Map<String, Reference>> map, String called, String name, Type type) {
		Reference ref = _get0(map, name, type);
		if(ref == null) throw new NullPointerException("The " + called + " " + getTypeName(type) + " '" + name + "' has not been defined");
		return ref;
	}
	private Reference _get0(Map<Type, Map<String, Reference>> map, String name, Type type) {
		Map<String, Reference> m = map.get(type);
		if(m == null) return null;
		return m.get(name);
	}
	private Reference _add(Map<Type, Map<String, Reference>> map, String called, String name, Type type) {
		if(hasGlobal(name, type)) throw new NullPointerException("The " + called + " " + getTypeName(type) + " '" + name + "' has already been defined");
		Reference ref = Reference.unique(name, type, unique++);
		Map<String, Reference> m = map.get(type);
		if(m == null) map.put(type, m = new HashMap<>());
		m.put(name, ref);
		return ref;
	}
	private Reference _add_direct(Map<Type, Map<String, Reference>> map, String called, Reference ref) {
		if(hasGlobal(ref.getName(), ref.getType())) throw new NullPointerException("The " + called + " " + getTypeName(ref.getType()) + " '" + ref.getName() + "' has already been defined");
		Map<String, Reference> m = map.get(ref.getType());
		if(m == null) map.put(ref.getType(), m = new HashMap<>());
		m.put(ref.getName(), ref);
		return ref;
	}
	
	// Utility
	private String getTypeName(Reference.Type type) {
		switch(type) {
			case VAR: return "variable";
			case FUN: return "function";
			default: return type.name();
		}
	}
}
