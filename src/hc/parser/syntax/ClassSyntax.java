package hc.parser.syntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import hc.parser.*;
import hc.token.Symbol;

public class ClassSyntax implements SyntaxReader {
	private static final Logger LOGGER = Logger.getLogger(ClassSyntax.class.getSimpleName());
	
	public Result compute(SyntaxTree tree, Symbol symbol) {
		if(!symbol.equals("class")) return null;
		
		Symbol name = symbol.next();
		if(!Identifier.isValidIdentifier(name)) return null;
		
		// TODO: EXTENDS or IMPLEMENTS
		
		Symbol bodyStart = name.next();
		Symbol bodyClose = null;
		
		if(!bodyStart.equals("{")) {
			return null;
		} else {
			int level = 1;
			Symbol next = bodyStart;
			
			while(level > 0) {
				next = next.next();
				if(next == null) return null;
				
				if(next.equals("{")) level++;
				if(next.equals("}")) level--;
			}
			
			bodyClose = next;
			if(level > 0 || !bodyClose.equals("}")) return null; // Failed to close class body
		}
		
		Symbol bodyEnd = bodyClose.next();
		if(!bodyEnd.equals(";")) return null;
		
		int count = symbol.indexOf(bodyEnd) + 1;
		Symbol body = bodyStart.clone(bodyStart.indexOf(bodyEnd));
		LOGGER.log(Level.INFO, "Class Instruction: line = {0}, column = {1}", new Object[] { symbol.getLineIndex(), symbol.getColumnIndex() });
		LOGGER.log(Level.INFO, "           name: {0}", name);
		LOGGER.log(Level.INFO, "           body: {0}", body.toString(" ", 1000000000));
		
		SyntaxNode syntax = tree.addNode(SyntaxType.CLASS);
		syntax.addNode(SyntaxType.IDENTIFIER).setValue(name);
		syntax.addNode(SyntaxType.BODY).setValue(body);
		
		return Result.create(count);
	}
}
