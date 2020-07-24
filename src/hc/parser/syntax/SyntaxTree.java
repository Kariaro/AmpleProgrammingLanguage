package hc.parser.syntax;

import hc.parser.SyntaxType;

public class SyntaxTree extends SyntaxNode {
	public SyntaxTree() {
		super(null);
		setType(SyntaxType.ROOT);
	}
}
