package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

// TODO: Use this as an replacement for Reference inside IntermediateGenerator
public class InstRef extends Reference {
	public InstRef(String name, Namespace namespace, ValueType valueType, int id, int flags, int usages) {
		super(name, namespace, valueType, id, flags, usages);
	}
	
	public InstRef(String name, Namespace namespace, ValueType valueType, int id, int flags) {
		super(name, namespace, valueType, id, flags);
	}
}
