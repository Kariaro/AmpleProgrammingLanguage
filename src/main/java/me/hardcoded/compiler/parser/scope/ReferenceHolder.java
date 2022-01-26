package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.type.Reference;

public interface ReferenceHolder {
	Reference getReference();
	void setReference(Reference reference);
}
