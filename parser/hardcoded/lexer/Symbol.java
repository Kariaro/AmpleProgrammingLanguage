package hardcoded.lexer;

import java.util.Objects;

public class Symbol {
	private String value;
	private String group;
	private boolean discard;
	private int line;
	private int column;
	
	protected Symbol(String group, boolean discard, String value, int lineIndex, int columnIndex) {
		this.group = group;
		this.discard = discard;
		this.value = value;
		this.line = lineIndex;
		this.column = columnIndex;
	}
	
	/**
	 * Get the lineIndex that this symbol was read from.
	 */
	public int line() {
		return line;
	}
	
	/**
	 * Get the columnIndex that this symbol was read from.
	 */
	public int column() {
		return column;
	}
	
	public String group() {
		return group;
	}
	
	public String value() {
		return value;
	}
	
	public boolean shouldDiscard() {
		return discard;
	}
	
	public boolean groupEquals(String groupName) {
		return Objects.equals(groupName, group());
	}
	
	public boolean equals(String groupName, String value) {
		return Objects.equals(value, this.value) &&
			   groupEquals(groupName);
	}
	
	public String toString() {
		return value;
	}
}