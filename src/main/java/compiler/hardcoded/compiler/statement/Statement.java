package hardcoded.compiler.statement;

import java.util.List;

import hardcoded.visualization.Printable;

public interface Statement extends Printable {
	public static final Statement EMPTY = new Statement() {
		public boolean hasStatements() {
			return false;
		}
		
		public List<Statement> getStatements() {
			return null;
		}
		
		public String toString() { return ""; }
		public String asString() { return ""; }
	};
	
	/**
	 * Returns {@code true} if this statement has child nodes.
	 * @return {@code true} if this statement has child nodes
	 */
	public boolean hasStatements();
	
	/**
	 * Returns a list of child nodes.
	 * @return a list of child nodes
	 */
	public List<Statement> getStatements();
	
	public default String asString() {
		return "Undefined(" + this.getClass() + ")";
	}
	
	public default Object[] asList() {
		return new Object[0];
	};
}
