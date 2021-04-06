package com.hardcoded.compiler.impl.context;

import java.util.*;

public class NonNullList<T> implements List<T> {
	private final List<T> list = new ArrayList<>();
	private final T def;
	
	public NonNullList(T def) {
		this.def = Objects.requireNonNull(def, "The default value must not be null");
	}
	
	public NonNullList(T def, int size) {
		this.def = Objects.requireNonNull(def, "The default value must not be null");
		for(int i = 0; i < size; i++) {
			this.list.add(def);
		}
	}
	
	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return list.add(e == null ? def:e);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for(T e : c) add(e);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		for(T e : c) add(index++, e);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public T set(int index, T element) {
		T result = list.set(index, element == null ? def:element);
		return result == null ? def:result;
	}

	@Override
	public void add(int index, T element) {
		list.add(index, element == null ? def:element);
	}

	@Override
	public T remove(int index) {
		T result = list.remove(index);
		return result == null ? def:result;
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
	
	@Override
	public String toString() {
		return list.toString();
	}
}
