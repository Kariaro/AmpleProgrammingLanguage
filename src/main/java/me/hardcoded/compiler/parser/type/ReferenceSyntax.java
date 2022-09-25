package me.hardcoded.compiler.parser.type;

import me.hardcoded.compiler.impl.ISyntaxPosition;

public class ReferenceSyntax {
	private final Reference reference;
	private final ISyntaxPosition syntaxPosition;
	
	public ReferenceSyntax(Reference reference, ISyntaxPosition syntaxPosition) {
		this.reference = reference;
		this.syntaxPosition = syntaxPosition;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public ISyntaxPosition getSyntaxPosition() {
		return syntaxPosition;
	}
	
	@Override
	public String toString() {
		return reference.toString();
	}
}
