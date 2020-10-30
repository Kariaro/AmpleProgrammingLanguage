package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hardcoded.compiler.impl.IStatement;
import hardcoded.utils.UnmodifiableCastedSet;
import hardcoded.visualization.Printable;

public class Statement implements IStatement, Printable {
	protected final List<Statement> list;
	
	public Statement(boolean hasElements) {
		list = hasElements ? new ArrayList<>():null;
	}
	
	public final Set<IStatement> getStatements() {
		return new UnmodifiableCastedSet<IStatement>(list);
	}

	public final boolean hasStatements() {
		return list != null;
	}
	
	public final List<Statement> getElements() {
		return list;
	}
	
	public final boolean hasElements() {
		return list != null;
	}
	
	
	public boolean isEmptyStat() {
		return false;
	}
	
	/**
	 * Returns the size of this list.
	 * @return the size of this list
	 */
	public final int size() {
		return list.size();
	}
	
	/**
	 * Returns the element at the specified index.
	 * @param	index	the index of the element in this list
	 * @return	the element at the specified index
	 */
	public final Statement get(int index) {
		return list == null ? null:list.get(index);
	}
	
	/**
	 * Add a new element to this statement.
	 * @param	stat	the statement to add
	 */
	public final void add(Statement stat) {
		if(list == null) throw new UnsupportedOperationException();
		list.add(stat == null ? newEmpty():stat);
	}
	
	/**
	 * Replaces the element at the specified index with a new statement.
	 * @param	index	the index of the element that should be replaced
	 * @param	stat	the statement to replace with
	 */
	public final void set(int index, Statement stat) {
		if(list == null) throw new UnsupportedOperationException();
		list.set(index, stat == null ? newEmpty():stat);
	}
	
	/**
	 * Removes the element at the specified index.
	 * @param	index	the index of the element to remove
	 */
	public void remove(int index) {
		if(list == null) throw new UnsupportedOperationException();
		list.remove(index);
	}
	
	
	
	public String asString() { return "Undefined(" + this.getClass() + ")"; }
	public Object[] asList() { return list == null ? new Object[0]:list.toArray(); }
	
	public static Statement newEmpty() {
		return new Statement(false) {
			public boolean isEmptyStat() { return true; }
			public String asString() { return "{NOP}"; }
			public String toString() { return "{NOP}"; }
		};
	}
}
