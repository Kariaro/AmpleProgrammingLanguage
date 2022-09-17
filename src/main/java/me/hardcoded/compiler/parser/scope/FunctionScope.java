package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public Reference addFunction(ValueType type, String name, List<Reference> parameters) {
		return functionScope.getScope().addFunction(type, name, parameters);
	}
	
	public Reference getGlobalFunction(String name, List<Reference> parameters) {
		return functionScope.getAllScopes().getFirst().getFunction(name, parameters);
	}
	
	public Reference getLocalFunction(String name, List<Reference> parameters) {
		return functionScope.getScope().getFunction(name, parameters);
	}
	
	public Reference getFunction(String name, List<Reference> parameters) {
		String namespace = getNamespaceName();
		
		Reference reference;
		if ((reference = getLocalFunction(name, parameters)) != null) {
			return reference;
		}
		
		if (namespace != null) {
			if ((reference = getLocalFunction(namespace + name, parameters)) != null) {
				return reference;
			}
			
			if ((reference = getGlobalFunction(namespace + name, parameters)) != null) {
				return reference;
			}
		}
		
		// We didn't find it so we check the global scope
		return getGlobalFunction(name, parameters);
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
			String mangledName = AmpleMangler.mangleFunction(name, parameters);
			if (definedFunctions.containsKey(mangledName)) {
				return null;
			}
			
			String constructedName = (namespace == null ? name : (namespace + name));
			String constructedEntryName = (namespace == null ? mangledName : (namespace + mangledName));
			Reference reference = new Reference(constructedName, returnType, programScope.count++, Reference.FUNCTION);
			Functions global = functionScope.getAllScopes().getFirst();
			if (global != this && global.definedFunctions.put(constructedEntryName, reference) != null) {
				throw new RuntimeException("Function override");
			}
			
			definedFunctions.put(mangledName, reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public Reference getFunction(String name, List<Reference> parameters) {
			String mangledName = AmpleMangler.mangleFunction(name, parameters);
			return definedFunctions.get(mangledName);
		}
	}
}
