package hc.parser.syntax;

import hc.parser.Result;
import hc.token.Symbol;

public interface SyntaxReader {
	public Result compute(SyntaxTree tree, Symbol symbol);
}
