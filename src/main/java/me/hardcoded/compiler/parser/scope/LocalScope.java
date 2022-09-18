package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class LocalScope {
	private final ProgramScope programScope;
	private final LinkedList<DataScope<Locals>> localScope;
	
	LocalScope(ProgramScope programScope) {
		this.programScope = programScope;
		this.localScope = new LinkedList<>();
	}
	
	public void clear() {
		localScope.clear();
	}
	
	/**
	 * This will create a new block of variables. This represents different orders of visibility.
	 * <p>
	 * The first time this is called will be the block containing GLOBAL variables.
	 * The second time it will contain LOCAL variables.
	 * The third time it will contain LAMBDA variables.
	 */
	public void pushBlock() {
		localScope.addLast(new DataScope<>(Locals::new));
	}
	
	public void popBlock() {
		localScope.removeLast();
	}
	
	public void pushLocals() {
		System.out.println("push >>> " + localScope.getLast());
		localScope.getLast().pushScope();
	}
	
	public void popLocals() {
		System.out.println("pop >>> " + localScope.getLast());
		localScope.getLast().popScope();
	}
	
	public Reference addLocalVariable(Namespace namespace, ValueType valueType, String name) {
		return localScope.getLast().getScope().addLocal(valueType, namespace, name);
	}
	
	public Reference getVariable(Namespace namespace, String name) {
		Iterator<DataScope<Locals>> iter = localScope.descendingIterator();
		
		while (iter.hasNext()) {
			Iterator<Locals> iter2 = iter.next().getAllScopes().descendingIterator();
			while (iter2.hasNext()) {
				Reference reference = iter2.next().getLocal(namespace, name);
				if (reference != null) {
					return reference;
				}
			}
		}
		
		return null;
	}
	
	public Reference getLocal(Namespace namespace, String name) {
		Iterator<Locals> iter = localScope.getLast().getAllScopes().descendingIterator();
		while (iter.hasNext()) {
			Reference reference = iter.next().getLocal(namespace, name);
			if (reference != null) {
				return reference;
			}
		}
		
		return null;
	}
	
	public class Locals {
		public final Map<String, Reference> locals;
		
		private Locals() {
			this.locals = new HashMap<>();
		}
		
		public Reference addLocal(ValueType valueType, Namespace namespace, String name) {
			String mangledName = AmpleMangler.mangleVariable(namespace, name);
			if (locals.containsKey(mangledName)) {
				return null;
			}
			
			Reference reference = new Reference(name, namespace, valueType, programScope.count++, 0);
			locals.put(mangledName, reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public Reference getLocal(Namespace namespace, String name) {
			String mangledName = AmpleMangler.mangleVariable(namespace, name);
			Reference reference = locals.get(mangledName);
			if (reference != null && reference.getNamespace() != namespace) {
				throw new RuntimeException("Local did not have the correct namespace: (%s) (%s)".formatted(reference, namespace));
			}
			return reference;
		}
		
		@Override
		public String toString() {
			return locals.toString();
		}
	}
}
