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
	
//	public ValueType getType(String name) {
//		return getType(name, 0);
//	}
	
	public ValueType getType(String name, int depth) {
		Iterator<DataScope<Types>> iter = localScope.descendingIterator();
		
		while (iter.hasNext()) {
			Iterator<Types> iter2 = iter.next().getAllScopes().descendingIterator();
			while (iter2.hasNext()) {
				ValueType type = iter2.next().getType(name, depth);
				if (type != null) {
					return type;
				}
			}
		}
		
		return null;
	}
	
//	public ValueType getLocalType(String name) {
//		return getLocalType(name, 0);
//	}
	
	public ValueType getLocalType(String name, int depth) {
		Iterator<Types> iter = localScope.getLast().getAllScopes().descendingIterator();
		while (iter.hasNext()) {
			ValueType type = iter.next().getType(name, depth);
			if (type != null) {
				return type;
			}
		}
		
		return null;
	}
	
	public class Types {
		public final Map<String, ValueType> types;
		public final Map<ValueType, Map<Integer, ValueType>> arrayTypes;
		
		private Types() {
			this.types = new HashMap<>();
			this.arrayTypes = new HashMap<>();
		}
		
		// TODO: This should only add primitive non array types
		public ValueType addType(ValueType valueType) {
			if (types.containsKey(valueType.getName())) {
				// TODO: Throw exception
				return null;
			}
			
			types.put(valueType.getName(), valueType);
			return valueType;
		}
		
		public ValueType getType(String name, int depth) {
			ValueType type = types.get(name);
			
			if (type != null && depth > 0) {
				// Found a primitive type. Make that into a pointer type
				return arrayTypes
					.computeIfAbsent(type, v -> new HashMap<>())
					.computeIfAbsent(depth, v -> type.createArray(depth));
			}
			
			return type;
		}
		
		@Override
		public String toString() {
			return types.toString();
		}
	}
}
