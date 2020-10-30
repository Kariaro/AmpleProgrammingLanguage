package hardcoded.compiler.statement;

import java.util.List;

import hardcoded.visualization.Printable;

public abstract class Statement implements Printable {
	/**
	 * Returns {@code true} if this statement has child nodes.
	 * @return {@code true} if this statement has child nodes
	 */
	public abstract boolean hasStatements();
	
	/**
	 * Returns a list of child nodes.
	 * @return a list of child nodes
	 */
	public abstract List<Statement> getStatements();
	
	public String asString() {
		return "Undefined(" + this.getClass() + ")";
	}
	
	public Object[] asList() {
		return new Object[0];
	}
	
	public boolean isEmptyStat() {
		return false;
	}
	
	public static Statement newEmpty() {
		return new Statement() {
			public boolean hasStatements() { return false; }
			public List<Statement> getStatements() { return null; }
			public Object[] asList() { return new Object[0]; }
			public String asString() { return "{NOP}"; }
			public String toString() { return "{NOP}"; }
			public boolean isEmptyStat() { return true; }
		};
	}
}
