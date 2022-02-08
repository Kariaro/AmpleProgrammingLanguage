package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.*;

public class FunctionScope {
	private final ProgramScope programScope;
	private final DataScope<Functions> functionScope;
	
	FunctionScope(ProgramScope programScope) {
		this.programScope = programScope;
		this.functionScope = new DataScope<>(Functions::new);
	}
	
	public void clear() {
		functionScope.clear();
	}
	
	public void pushBlock() {
		pushBlock(null);
	}
	
	public void pushBlock(String name) {
		String namespace = getNamespaceName();
		if (name != null) {
			if (namespace == null) {
				namespace = name + "::";
			} else {
				namespace += name + "::";
			}
		}
		
		functionScope.pushScope();
		functionScope.getScope().setPath(namespace);
	}
	
	public void popBlock() {
		functionScope.popScope();
	}
	
	public Reference addFunction(ValueType returnType, String name, List<Reference> parameters) {
		return functionScope.getScope().addFunction(returnType, name, parameters);
	}
	
	public Reference getGlobalFunction(String name) {
		return functionScope.getAllScopes().getFirst().getFunction(name);
	}
	
	public Reference getLocalFunction(String name) {
		return functionScope.getScope().getFunction(name);
	}
	
	public Reference getFunction(String name) {
		String namespace = getNamespaceName();
		
		Reference reference;
		if ((reference = getLocalFunction(name)) != null) {
			return reference;
		}
		
		if (namespace != null) {
			if ((reference = getLocalFunction(namespace + name)) != null) {
				return reference;
			}
			
			if ((reference = getGlobalFunction(namespace + name)) != null) {
				return reference;
			}
		}
		
		// We didn't find it so we check the global scope
		return getGlobalFunction(name);
	}
	
	public String getNamespaceName() {
		return functionScope.isEmpty() ? null : functionScope.getScope().namespace;
	}
	
	public class Functions {
		// TODO: Overloading
		private final Map<String, Reference> definedFunctions;
		private String namespace;
		
		private Functions() {
			this.definedFunctions = new HashMap<>();
		}
		
		public void setPath(String namespace) {
			this.namespace = namespace;
		}
		
		public Reference addFunction(ValueType returnType, String name, List<Reference> parameters) {
			if (definedFunctions.containsKey(name)) {
				return null;
			}
			
			String constructedName;
			if (namespace != null) {
				constructedName = namespace + name;
			} else {
				constructedName = name;
			}
			
			Reference reference = new Reference(constructedName, returnType, programScope.count++, Reference.FUNCTION);
			Functions global = functionScope.getAllScopes().getFirst();
			if (global != this) {
				if (global.definedFunctions.put(constructedName, reference) != null) {
					throw new ParseException("Function override");
				}
			}
			
			definedFunctions.put(constructedName, reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public Reference getFunction(String name) {
			return definedFunctions.get(name);
		}
	}
}
