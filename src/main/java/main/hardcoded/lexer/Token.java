package hardcoded.lexer;

import java.util.Objects;

/**
 * A token class.
 * 
 * @author HardCoded
 */
public class Token {
	/** This value will never be {@code null}. */
	public final String value;
	/** This value will never be {@code null}. */
	public final String group;
	public final int offset;
	public final int line;
	public final int column;
	
	public Token(String value, String group, int offset, int line, int column) {
		this.value = Objects.requireNonNull(value, "Token value must not be null");
		this.group = Objects.requireNonNull(group, "Token group must not be null");
		this.offset = offset;
		this.line = line;
		this.column = column;
	}
	
	public boolean valueEquals(String string) {
		return value.equals(string);
	}
	
	public boolean groupEquals(String string) {
		return group.equals(string);
	}
	
	public boolean equals(String group, String value) {
		return valueEquals(value)
			&& groupEquals(group);
	}
	
	public boolean isInvalid() {
		return column < 0 || offset < 0 || line < 0;
	}
	
	public String toString() {
		return value;
	}
}
