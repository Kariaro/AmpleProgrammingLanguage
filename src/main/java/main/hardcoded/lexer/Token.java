package hardcoded.lexer;

import java.util.Objects;

/**
 * A token class.
 * 
 * @author HardCoded
 */
public class Token {
	public final String value;
	public final String group;
	public final int offset;
	public final int line;
	public final int column;
	
	public Token(String value, String group, int offset, int line, int column) {
		this.value = value;
		this.group = group;
		this.offset = offset;
		this.line = line;
		this.column = column;
	}
	
	public boolean valueEquals(String string) {
		return Objects.equals(value, string);
	}
	
	public boolean groupEquals(String string) {
		return Objects.equals(group, string);
	}
	
	public boolean equals(String group, String value) {
		return valueEquals(value)
			&& groupEquals(group);
	}
	
	public String toString() {
		return value;
	}
}
