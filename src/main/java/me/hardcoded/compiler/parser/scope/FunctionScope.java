package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.types.MangledFunctionMap;

import java.util.List;

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
	
	public Reference getFunctionBlocking(Namespace namespace, String name, List<Reference> parameters) {
		return functionScope.getScope().getFunctionBlocking(namespace, name, parameters);
	}
	
	public Reference importFunction(Namespace namespace, String name, List<Reference> parameters) {
		Reference reference = addFunction(Primitives.LINKED, namespace, name, parameters);
		reference.setImported(true);
		reference.setMangledName(AmpleMangler.mangleFunction(Primitives.LINKED, namespace, name, parameters));
		return reference;
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
		
		// We didn't find it so we check the global scope
		return getGlobalFunction(namespace, name, parameters);
	}
	
	public class Functions {
		private final MangledFunctionMap definedFunctions;
		
		private Functions() {
			this.definedFunctions = new MangledFunctionMap();
		}
		
		public Reference addFunction(ValueType returnType, Namespace namespace, String name, List<Reference> parameters) {
			String mangledName = AmpleMangler.mangleFunction(returnType, namespace, name, parameters);
			if (definedFunctions.contains(mangledName)) {
				return null;
			}
			
			Reference reference = new Reference(name, namespace, returnType, programScope.count++, Reference.FUNCTION);
			reference.setMangledName(mangledName);
			Functions global = functionScope.getAllScopes().getFirst();
			if (global != this && global.definedFunctions.getBlocker(reference) != null) {
				throw new RuntimeException("Function override");
			}
			
			definedFunctions.put(reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public Reference getFunctionBlocking(Namespace namespace, String name, List<Reference> parameters) {
			String mangledName = AmpleMangler.mangleFunction(Primitives.LINKED, namespace, name, parameters);
			return definedFunctions.getBlocker(mangledName);
		}
		
		public Reference getFunction(Namespace namespace, String name, List<Reference> parameters) {
			String mangledName = AmpleMangler.mangleFunction(Primitives.LINKED, namespace, name, parameters);
			return definedFunctions.get(mangledName);
		}
	}
}
