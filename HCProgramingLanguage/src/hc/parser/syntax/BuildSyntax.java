package hc.parser.syntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import hc.parser.Result;
import hc.parser.SyntaxType;
import hc.token.Symbol;

public class BuildSyntax implements SyntaxReader {
	private static final Logger LOGGER = Logger.getLogger(BuildSyntax.class.getSimpleName());
	
	public Result compute(SyntaxTree tree, Symbol symbol) {
		if(!symbol.equals("%")) return null;
		
		LOGGER.log(Level.INFO, "Build Instruction: {0}", symbol.toString(" ", 4));
		Symbol action = symbol.next();
		String command = action.toString();
		
		switch(command) {
			case "include": {
				SyntaxNode node = tree.addNode(SyntaxType.IMPORT);
				node.addNode(SyntaxType.LITERAL).setValue(symbol.next(2));
				Symbol end = symbol.next(3);
				if(!end.equals(";")) return null;
				
				return Result.create(4);
			}
//			case "macro": {
//				SyntaxNode node = tree.addNode(SyntaxType.DEFINE);
//				node.addNode(SyntaxType.IDENTIFIER).setValue(symbol.next(2));
//				node.addNode(SyntaxType.TYPE).setValue(symbol.next(3));
//				return Result.create(4);
//			}
		}
		
		return null;
	}
}
