package hardcoded.lexer;

import java.util.Objects;

public class TokenizerSymbol {
	private String value;
	private String group;
	private boolean discard;
	private int line;
	private int column;
	private int fileOffset;
	
	protected TokenizerSymbol(String group, boolean discard, String value, int lineIndex, int columnIndex, int fileOffset) {
		this.group = group;
		this.discard = discard;
		this.fileOffset = fileOffset;
		this.value = value;
		this.line = lineIndex;
		this.column = columnIndex;
	}
	
	public int line() {
		return line;
	}
	
	public int column() {
		return column;
	}
	
	public int fileOffset() {
		return fileOffset;
	}
	
	public String group() {
		return group;
	}
	
	public String value() {
		return value;
	}
	
	public boolean discard() {
		return discard;
	}
	
	public boolean groupEquals(String group) {
		return Objects.equals(group, group());
	}
	
	public boolean valueEquals(String value) {
		return Objects.equals(value, value());
	}
	
	public boolean equals(String group, String value) {
		return groupEquals(group) && valueEquals(value);
	}
	
	public String toString() {
		return value;
	}
}