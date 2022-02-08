package me.hardcoded.compiler.parser.scope;

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
	 *
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
		localScope.getLast().pushScope();
	}
	
	public void popLocals() {
		localScope.getLast().popScope();
	}
	
	public Reference addLocalVariable(ValueType valueType, String name) {
		return localScope.getLast().getScope().addLocal(valueType, name);
	}
	
	public Reference getVariable(String name) {
		Iterator<DataScope<Locals>> iter = localScope.descendingIterator();
		
		while (iter.hasNext()) {
			Iterator<Locals> iter2 = iter.next().getAllScopes().descendingIterator();
			while (iter2.hasNext()) {
				Reference reference = iter2.next().getLocal(name);
				if (reference != null) {
					return reference;
				}
			}
		}
		
		return null;
	}
	
	public Reference getLocal(String name) {
		Iterator<Locals> iter = localScope.getLast().getAllScopes().descendingIterator();
		while (iter.hasNext()) {
			Reference reference = iter.next().getLocal(name);
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
		
		public Reference addLocal(ValueType valueType, String name) {
			if (locals.containsKey(name)) {
				return null;
			}
			
			Reference reference = new Reference(name, valueType, programScope.count++, 0);
			locals.put(name, reference);
			programScope.allReferences.add(reference);
			return reference;
		}
		
		public Reference getLocal(String name) {
			return locals.get(name);
		}
		
		@Override
		public String toString() {
			return locals.toString();
		}
	}
}
