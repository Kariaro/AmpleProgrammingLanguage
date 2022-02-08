package me.hardcoded.compiler.parser.scope;

import java.util.LinkedList;
import java.util.function.Supplier;

public class DataScope<T> {
	private final LinkedList<T> scopes;
	private final Supplier<T> supplier;
	
	DataScope(Supplier<T> supplier) {
		this.scopes = new LinkedList<>();
		this.supplier = supplier;
		this.scopes.add(supplier.get());
	}
	
	public void clear() {
		scopes.clear();
	}
	
	public boolean isEmpty() {
		return scopes.isEmpty();
	}
	
	public void pushScope() {
		scopes.addLast(supplier.get());
	}
	
	public void popScope() {
		scopes.removeLast();
	}
	
	public T getScope() {
		return scopes.getLast();
	}
	
	public LinkedList<T> getAllScopes() {
		return scopes;
	}
	
	@Override
	public String toString() {
		return scopes.toString();
	}
}
