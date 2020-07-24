package hc.parser.syntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import hc.parser.*;
import hc.token.Symbol;

public class StructSyntax implements SyntaxReader {
	private static final Logger LOGGER = Logger.getLogger(StructSyntax.class.getSimpleName());
	
	public Result compute(SyntaxTree tree, Symbol symbol) {
		if(!symbol.equals("struct")) return null;
		
		
		Symbol name = symbol.next();
		if(!Identifier.isValidIdentifier(name)) return null;
		
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
			if(level > 0 || !bodyClose.equals("}")) return null; // Failed to close struct body
		}
		
		Symbol bodyEnd = bodyClose.next();
		if(!bodyEnd.equals(";")) return null;
		
		int size = symbol.indexOf(bodyEnd) + 1;
		LOGGER.log(Level.INFO, "Struct Instruction: {0}", symbol.toString(" ", size));
		
		SyntaxNode syntax = tree.addNode(SyntaxType.STRUCT);
		syntax.addNode(SyntaxType.IDENTIFIER).setValue(name);
		SyntaxNode bodyNode = syntax.addNode(SyntaxType.BODY);
		
		{
			Symbol body = bodyStart.clone(bodyStart.indexOf(bodyEnd)).next();
			
			while(body != null) {
				if(body.equals("}")) break;
				
				Symbol fieldType = body; // FIXME: Allow for more complex fields such as (object[] or object****)
				Symbol fieldName = fieldType.next();
				if(!Identifier.isValidIdentifier(fieldName)) return null;
				
				Symbol fieldEnd = fieldName.next();
				if(!fieldEnd.equals(";")) return null;
				
				SyntaxNode field = bodyNode.addNode(SyntaxType.FIELD);
				field.addNode(SyntaxType.TYPE).setValue(fieldType);
				field.addNode(SyntaxType.IDENTIFIER).setValue(fieldName);
				
				body = fieldEnd.next();
			}
		}
		
		return Result.create(size);
	}
}
