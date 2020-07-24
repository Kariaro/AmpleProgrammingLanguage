package hc.parser.syntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import hc.parser.*;
import hc.token.Symbol;

public class EnumSyntax implements SyntaxReader {
	private static final Logger LOGGER = Logger.getLogger(EnumSyntax.class.getSimpleName());
	
	public Result compute(SyntaxTree tree, Symbol symbol) {
		if(!symbol.equals("enum")) return null;
		
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
		LOGGER.log(Level.INFO, "Enum Instruction: {0}", symbol.toString(" ", size));
		
		SyntaxNode syntax = tree.addNode(SyntaxType.ENUM);
		syntax.addNode(SyntaxType.IDENTIFIER).setValue(name);
		SyntaxNode bodyNode = syntax.addNode(SyntaxType.BODY);
		
		{
			Symbol body = bodyStart.clone(bodyStart.indexOf(bodyEnd)).next();
			int index = 0;
			
			while(body != null) {
				if(body.equals("}")) break;
				
				Symbol fieldName = body;
				if(!Identifier.isValidIdentifier(fieldName)) return null;
				
				Symbol fieldBind = body.next();
				
				Symbol fieldEnd = fieldBind;
				if(fieldBind.equals("=")) {
					Symbol fieldValue = fieldBind.next();
					index = Integer.valueOf(fieldValue.toString());
					fieldEnd = fieldValue.next();
				} else {
					index ++;
				}
				
				
				SyntaxNode field = bodyNode.addNode(SyntaxType.FIELD);
				field.addNode(SyntaxType.IDENTIFIER).setValue(fieldName);
				field.addNode(SyntaxType.INTEGERLITERAL).setValue(index);
				
				if(fieldEnd.equals(",")) {
					body = fieldEnd.next();
					continue;
				} else if(fieldEnd.equals("}")) {
					break;
				}
			}
		}
		
		return Result.create(size);
	}
}
