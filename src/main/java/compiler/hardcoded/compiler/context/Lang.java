package hardcoded.compiler.context;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import hardcoded.lexer.Token;

public class Lang {
	private final List<Token> list;
	private int index;
	private LinkedList<Integer> marked;
	
	private Lang(List<Token> list) {
		this.marked = new LinkedList<>();
		this.list = list;
	}
	
	public int fileOffset() {
		return token().offset;
	}
	
	public int remaining() {
		return list.size() - index;
	}

	public Token token() {
		if(remaining() < 1) return null;
		return list.get(index);
	}
	
	public Lang mark() {
		marked.push(index);
		return this;
	}
	
	public Lang reset() {
		index = marked.poll();
		return this;
	}
	
	public Lang resetMarked() {
		marked.clear();
		return this;
	}
	
	public Lang next() {
		index++;
		return this;
	}
	
	public Lang nextClear() {
		index++;
		resetMarked();
		return this;
	}
	
	public Lang next(int count) {
		index += count;
		return this;
	}
	
	public Lang prev() {
		index--;
		return this;
	}
	
	public Lang prev(int count) {
		index -= count;
		return this;
	}
	
	/**
	 * Returns the current value and increments the index.
	 * @return the current value and increments the index
	 */
	public String valueAdvance() {
		if(remaining() < 1) return null;
		String value = token().value;
		index++;
		return value;
	}
	
	public String value() {
		if(remaining() < 1) return null;
		return token().value;
	}
	
	public String group() {
		if(remaining() < 1) return null;
		return token().group;
	}
	
	public int line() {
		if(remaining() < 1) return -1;
		return token().line;
	}
	
	public int column() {
		if(remaining() < 1) return -1;
		return token().column;
	}
	
	public String toString() {
		return value();
	}
	
	public boolean valueEquals(String value) {
		return Objects.equals(value(), value);
	}
	
	public boolean valueEqualsAdvance(String value) {
		boolean eq = valueEquals(value);
		if(eq) next();
		return eq;
	}
	
	public boolean groupEquals(String group) {
		return Objects.equals(group(), group);
	}
	
	public boolean equals(String group, String value) {
		return groupEquals(group)
			&& valueEquals(value);
	}
	
	public static Lang wrap(List<Token> list) {
		return new Lang(list);
	}
	
	
	
	// TODO: EXPERIMENTAL METHODS
	public LinkedList<Integer> ranges = new LinkedList<>();
	
	public void beginRange(String name) {
		beginRange(name, 0);
	}
	
	public void beginRange(String name, int offset) {
		int idx = index + offset;
		ranges.add(idx);
		// System.out.println("beginRange: [" + idx + "] (" + name + ") " + ranges);
	}
	
	public NamedRange closeRange(String name) {
		int idx = ranges.pollLast();
		
		Token first = list.get(idx);
		Token token = list.get(index - 1);
		
		NamedRange range = new NamedRange(name, first.offset, token.offset + token.value.length() - first.offset);
		
		// System.out.println("closeRange: [" + index + "] " + range);
		return range;
	}
}
