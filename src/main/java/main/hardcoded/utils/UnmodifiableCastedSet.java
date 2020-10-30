package hardcoded.utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnmodifiableCastedSet<E> implements Set<E> {
	private final Set<E> set;
	
	public UnmodifiableCastedSet(List<? extends E> list) {
		// Prevent NPE
		if(list == null) list = Collections.emptyList();
		
		set = Collections.unmodifiableSet(new LinkedHashSet<E>(
			list.stream().map(a -> (E)a).collect(Collectors.toList())
		));
	}
	
	public boolean add(E e) { throw new UnsupportedOperationException(); }
	public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }
	public boolean remove(Object o) { throw new UnsupportedOperationException(); }
	public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
	public boolean removeIf(Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
	
	public Iterator<E> iterator() {
		return set.iterator();
	}
	
	public boolean contains(Object o) {
		return set.contains(o);
	}
	
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}
	
	public boolean equals(Object obj) {
		return set.equals(obj);
	}
	
	public boolean isEmpty() {
		return set.isEmpty();
	}
	
	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}
	
	public int size() {
		return set.size();
	}
	
	public Object[] toArray() {
		return set.toArray();
	}
	
	public <U extends Object> U[] toArray(U[] a) {
		return set.toArray(a);
	}
	
	public String toString() {
		return set.toString();
	}
}
