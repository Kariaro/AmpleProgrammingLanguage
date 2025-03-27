package me.hardcoded.compiler.parser.type;

import me.hardcoded.compiler.impl.ISyntaxPos;

import java.util.Objects;

public class ReferenceSyntax {
	private final Reference reference;
	private final ISyntaxPos syntaxPosition;
	
	public ReferenceSyntax(Reference reference, ISyntaxPos syntaxPosition) {
		this.reference = Objects.requireNonNull(reference);
		this.syntaxPosition = Objects.requireNonNull(syntaxPosition);
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public ISyntaxPos getSyntaxPosition() {
		return syntaxPosition;
	}
	
	@Override
	public String toString() {
		return reference.toString();
	}
}
