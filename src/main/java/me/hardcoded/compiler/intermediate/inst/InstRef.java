package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

// TODO: Use this as an replacement for Reference inside InstGenerator
public class InstRef extends Reference {
	private static final Namespace NONE = new Namespace();
	
	public InstRef(String name, ValueType valueType, int id, int flags, int usages) {
		super(name, NONE, valueType, id, flags, usages);
	}
	
	public InstRef(String name, ValueType valueType, int id, int flags) {
		super(name, NONE, valueType, id, flags);
	}
}
