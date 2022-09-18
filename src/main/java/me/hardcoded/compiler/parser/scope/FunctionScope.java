package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Namespace;
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
		functionScope.pushScope();
	}
	
	public void popBlock() {
		functionScope.popScope();
	}
	
	public Reference addFunction(ValueType type, Namespace namespace, String name, List<Reference> parameters) {
		return functionScope.getScope().addFunction(type, namespace, name, parameters);
	}
	
	public Reference getGlobalFunction(Namespace namespace, String name, List<Reference> parameters) {
		return functionScope.getAllScopes().getFirst().getFunction(namespace, name, parameters);
	}
	
	public Reference getLocalFunction(Namespace namespace, String name, List<Reference> parameters) {
		return functionScope.getScope().getFunction(namespace, name, parameters);
	}
	
	public Reference getFunction(Namespace namespace, String name, List<Reference> parameters) {
		Reference reference;
		if ((reference = getLocalFunction(namespace, name, parameters)) != null) {
			return reference;
		}
		
		Namespace relativeNamespace = programScope.getNamespaceScope().getRelativeNamespace(programScope.getNamespaceScope().getNamespace(), namespace);
		if (relativeNamespace != null) {
			if ((reference = getLocalFunction(relativeNamespace, name, parameters)) != null) {
				return reference;
			}
			
			if ((reference = getGlobalFunction(relativeNamespace, name, parameters)) != null) {
				return reference;
			}
		}
		
		//		if (namespace != null) {
		//			if ((reference = getLocalFunction(name, parameters)) != null) {
		//				return reference;
		//			}
		//
		//			if ((reference = getGlobalFunction(namespace + name, parameters)) != null) {
		//				return reference;
		//			}
		//		}
		
		// We didn't find it so we check the global scope
		return getGlobalFunction(namespace, name, parameters);
	}
	
	public class Functions {
		// TODO: Overloading
		private final Map<String, Reference> definedFunctions;
		
		private Functions() {
			this.definedFunctions = new HashMap<>();
		}
		
		public Reference addFunction(ValueType returnType, Namespace namespace, String name, List<Reference> parameters) {
			String mangledName = AmpleMangler.mangleFunction(namespace, name, parameters);
			if (definedFunctions.containsKey(mangledName)) {
				return null;
			}
			
			// TODO: Integrate the namespace in the map. Use a Reference to compare functions ???
			String constructedName = namespace.getPath() + name;
			String constructedEntryName = namespace.getPath() + mangledName;
			Reference reference = new Reference(name, namespace, returnType, programScope.count++, Reference.FUNCTION);
			Functions global = functionScope.getAllScopes().getFirst();
			if (global != this && global.definedFunctions.put(constructedEntryName, reference) != null) {
				throw new RuntimeException("Function override");
			}
			
			definedFunctions.put(mangledName, reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public Reference getFunction(Namespace namespace, String name, List<Reference> parameters) {
			String mangledName = AmpleMangler.mangleFunction(namespace, name, parameters);
			return definedFunctions.get(mangledName);
		}
	}
}
