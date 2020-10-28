package hardcoded.compiler.statement;

import java.util.List;

import hardcoded.compiler.context.NamedRange;
import hardcoded.compiler.context.TokenContext;
import hardcoded.visualization.Printable;

public abstract class Statement implements Printable, TokenContext {
	@Deprecated
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
	
	private NamedRange range;
	public NamedRange getDefinedRange() {
		return range;
	}
	
	public void setDefinedRange(NamedRange range) {
		this.range = range;
	}
	
	public boolean isEMPTY() {
		return false;
	}
	
	public static Statement newEmpty() {
		return new Statement() {
			public boolean hasStatements() { return false; }
			public List<Statement> getStatements() { return null; }
			public Object[] asList() { return new Object[0]; }
			public String asString() { return ""; }
			public String toString() { return ""; }
			public boolean isEMPTY() { return true; }
		};
	}
}
