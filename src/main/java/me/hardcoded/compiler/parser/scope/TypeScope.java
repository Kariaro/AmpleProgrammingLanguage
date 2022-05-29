package me.hardcoded.compiler.parser.scope;

import me.hardcoded.compiler.parser.type.ValueType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class TypeScope {
	private final ProgramScope programScope;
	private final LinkedList<DataScope<Types>> localScope;
	
	TypeScope(ProgramScope programScope) {
		this.programScope = programScope;
		this.localScope = new LinkedList<>();
	}
	
	public void clear() {
		localScope.clear();
	}
	
	public void pushBlock() {
		localScope.addLast(new DataScope<>(Types::new));
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
	
	public ValueType addLocalType(ValueType valueType) {
		return localScope.getLast().getScope().addType(valueType);
	}
	
	public ValueType getType(String name) {
		Iterator<DataScope<Types>> iter = localScope.descendingIterator();
		
		while (iter.hasNext()) {
			Iterator<Types> iter2 = iter.next().getAllScopes().descendingIterator();
			while (iter2.hasNext()) {
				ValueType type = iter2.next().getType(name);
				if (type != null) {
					return type;
				}
			}
		}
		
		return null;
	}
	
	public ValueType getLocalType(String name) {
		Iterator<Types> iter = localScope.getLast().getAllScopes().descendingIterator();
		while (iter.hasNext()) {
			ValueType type = iter.next().getType(name);
			if (type != null) {
				return type;
			}
		}
		
		return null;
	}
	
	public class Types {
		public final Map<String, ValueType> types;
		
		private Types() {
			this.types = new HashMap<>();
		}
		
		public ValueType addType(ValueType valueType) {
			if (types.containsKey(valueType.getName())) {
				// TODO: Throw exception
				return null;
			}
			
			types.put(valueType.getName(), valueType);
			return valueType;
		}
		
		public ValueType getType(String name) {
			return types.get(name);
		}
		
		@Override
		public String toString() {
			return types.toString();
		}
	}
}
