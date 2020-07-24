package hc.parser.syntax;

import static hc.parser.Identifier.*;
import static hc.parser.Modifier.*;
import static hc.parser.Primitive.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hc.parser.*;
import hc.token.Symbol;

// FIXME: What about nth-pointer primitive types 'int***'
public class MethodSyntax implements SyntaxReader {
	private static final Logger LOGGER = Logger.getLogger(MethodSyntax.class.getSimpleName());
	
	public Result compute(SyntaxTree tree, Symbol symbol) {
		List<Modifier> mods = new ArrayList<>();
		int count = 0;
		{
			Modifier modifier;
			while((modifier = getModifier(symbol.next(count))) != null) {
				mods.add(modifier);
				count++;
			}
		}
		
		boolean return_pointer = false;
		Symbol return_type = symbol.next(count);
		if(!isPrimitive(return_type)) return null; // FIXME: Implement non primitive return types
		
		if(symbol.next(count + 1).equals("*")) {
			return_pointer = true;
			count++;
		}
		
		Symbol name = symbol.next(count + 1);
		if(!isValidIdentifier(name)) return null;
		
		Symbol argsStart = name.next();
		if(!argsStart.toString().equals("(")) return null;
		count += 3;
		
		Symbol argsClose = null;
		List<Argument> args = new ArrayList<>();
		if(!argsStart.next().equals(")")) {
			int start = 1;
			
			Symbol arg;
			while(true) {
				arg = argsStart.next(start);
				start += 2;
				
				Primitive type = getPrimitive(arg);
				if(type == null) return null; // FIXME: Allow for non-primitive type arguments
				
				boolean type_pointer = false;
				if(arg.next().equals("*")) {
					type_pointer = true;
					start++;
					arg = arg.next();
				}
				
				Symbol iden = arg.next();
				if(!isValidIdentifier(iden)) return null;
				
				// System.out.println(type + " (pointer = " + type_pointer + "), " + iden);
				
				args.add(Argument.create(type, type_pointer, iden));
				Symbol next = arg.next(2);
				if(next.equals(",")) {
					start ++;
					continue;
				} else if(next.equals(")")) break;
				else return null; // Invalid character
			}
			
			// System.out.println("NEXT: " + argsStart.next(start));
			
			if(!argsStart.next(start).equals(")")) {
				return null; // Failed to close the bracket
			}
			
			argsClose = argsStart.next(start);
		} else {
			argsClose = argsStart.next();
		}
		
		count = symbol.indexOf(argsClose) + 1;
		
		LOGGER.log(Level.INFO, "Method Instruction:");
		LOGGER.log(Level.INFO, "      modifiers: {0}", mods);
		LOGGER.log(Level.INFO, "           type: {0} (pointer = {1})", new Object[] { return_type, return_pointer });
		LOGGER.log(Level.INFO, "           name: {0}", name);
		LOGGER.log(Level.INFO, "           args: {0}", args);
		LOGGER.log(Level.INFO, "               : {0}", symbol.toString(" ", count));
		LOGGER.log(Level.INFO, "           size: {0}", count);
		if(count < 1) return null;
		
		Symbol bodyStart = argsClose.next();
		Symbol bodyClose = bodyStart;
		if(bodyStart.equals("{")) {
			int level = 1;
			Symbol next = bodyStart;
			
			while(level > 0) {
				next = next.next();
				if(next == null) return null;
				
				if(next.equals("{")) level++;
				if(next.equals("}")) level--;
				// System.out.println(level + ", " + next);
			}
			
			bodyClose = next;
			if(level > 0 || !bodyClose.equals("}")) return null; // Failed to close method body
		} else if(bodyStart.equals(";")) {
			count++;
		}

		int bodySize = bodyStart.indexOf(bodyClose) + 1;
		LOGGER.log(Level.INFO, "           body: {0}", symbol.toString(" ", count));
		LOGGER.log(Level.INFO, "            str: {0}", bodyStart.toString(" ", bodySize));
		LOGGER.log(Level.INFO, "           size: {0}", bodySize);
		
		SyntaxNode method = tree.addNode(SyntaxType.METHOD);
		{
			SyntaxNode modifiers = method.addNode(SyntaxType.MODIFIERS);
			for(Modifier mod : mods) {
				modifiers.addNode(SyntaxType.LITERAL).setValue(mod);
			}
			
			SyntaxNode returnType = method.addNode(SyntaxType.PRIMITIVE);
			if(return_pointer) {
				returnType.addNode(SyntaxType.POINTER).setValue(return_type);
			} else {
				returnType.setValue(return_type);
			}
			
			method.addNode(SyntaxType.IDENTIFIER).setValue(name);
			
			SyntaxNode arguments = method.addNode(SyntaxType.ARGUMENTS);
			for(Argument arg : args) {
				SyntaxNode argument = arguments.addNode(SyntaxType.FIELD);
				
				if(arg.isPointer()) {
					argument.addNode(SyntaxType.PRIMITIVE).addNode(SyntaxType.POINTER).setValue(arg.getType());
				} else {
					argument.addNode(SyntaxType.PRIMITIVE).setValue(arg.getType());
				}
				
				argument.addNode(SyntaxType.IDENTIFIER).setValue(arg.getName());
			}
			
			method.addNode(SyntaxType.BODY).setValue(bodyStart.clone(bodySize));
		}
		
		return Result.create(count + bodySize);
	}
}
