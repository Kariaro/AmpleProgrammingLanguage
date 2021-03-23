package com.hardcoded.compiler.impl.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 
 * @author HardCoded
 * @since 0.2.0
 */
@Deprecated
class Scope<T> {
	private Map<Integer, T> map = new HashMap<>();
	
	protected LinkedList<Map<T, Integer>> scopes = new LinkedList<>();
	protected int unique = 0;
	public Scope() {
		
	}
	
	public void inc() {
		scopes.add(new HashMap<>());
	}
	
	public void dec() {
		scopes.removeLast();
	}
	
	public boolean add(T value) {
		if(has(value)) {
			return false;
		}
		
		Map<T, Integer> map = scopes.getLast();
		this.map.put(unique, value);
		map.put(value, unique++);
		return true;
	}
	
	protected int add0(T value) {
		if(has(value)) return -1;
		
		scopes.getLast().put(value, unique);
		this.map.put(unique, value);
		return unique++;
	}
	
	public int get(T value) {
		// Most recent used first
		for(int i = scopes.size() - 1; i >= 0; i--) {
			Integer number = scopes.get(i).get(value);
			if(number != null) return number;
		}
		
		return -1;
	}
	
	public boolean has(T value) {
		return get(value) != -1;
	}
	
	public Map<Integer, T> map() {
		return map;
	}
}
