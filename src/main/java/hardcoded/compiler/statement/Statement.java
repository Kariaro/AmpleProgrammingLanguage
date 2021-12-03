package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.impl.IStatement;
import hardcoded.compiler.impl.ISyntaxPosition;
import hardcoded.visualization.Printable;

public class Statement implements IStatement, Printable {
	private final List<Statement> list;
	private final boolean hasElements;
	
	// TODO: Make this private final.
	protected ISyntaxPosition syntaxPosition = ISyntaxPosition.empty();
	
	protected Statement(boolean hasElements) {
		this.list = hasElements ? new ArrayList<>():List.of();
		this.hasElements = hasElements;
	}
	
	@Override
	public List<IStatement> getStatements() {
		return List.copyOf(list);
	}
	
	public List<Statement> getElements() {
		return list;
	}
	
	public boolean hasElements() {
		return hasElements;
	}
	
	public boolean isEmptyStat() {
		return false;
	}
	
	/**
	 * Returns the size of this list.
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * Returns the element at the specified index.
	 * @param index the index of the element in this list
	 * @return the element at the specified index
	 */
	public Statement get(int index) {
		return list.get(index);
	}
	
	/**
	 * Add a new element to this statement.
	 * @param stat the statement to add
	 */
	public void add(Statement stat) {
		list.add(stat);
	}
	
	/**
	 * Replaces the element at the specified index with a new statement.
	 * @param index the index of the element that should be replaced
	 * @param stat the statement to replace with
	 */
	public void set(int index, Statement stat) {
		list.set(index, stat);
	}
	
	/**
	 * Removes the element at the specified index.
	 * @param index the index of the element to remove
	 */
	public void remove(int index) {
		list.remove(index);
	}
	
	@Override
	public ISyntaxPosition getSyntaxPosition() {
		return syntaxPosition;
	}
	
	@Override
	public String asString() {
		return "Undefined(%s)".formatted(this.getClass());
	}
	
	@Override
	public Object[] asList() {
		return list.toArray();
	}
	
	public static Statement newEmpty() {
		return new Statement(false) {
			@Override
			public boolean isEmptyStat() {
				return true;
			}
			
			@Override
			public String asString() {
				return "{NOP}";
			}
			
			@Override
			public String toString() {
				return "{NOP}";
			}
		};
	}
}
