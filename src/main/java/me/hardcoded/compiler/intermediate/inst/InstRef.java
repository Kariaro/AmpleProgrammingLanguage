package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.type.Reference;

// TODO: Use this as an replacement for Reference inside InstGenerator
public class InstRef extends Reference {
	
	public InstRef(String name, int id, int flags, int usages) {
		super(name, id, flags, usages);
	}
	
	public InstRef(String name, int id, int flags) {
		super(name, id, flags);
	}
}
