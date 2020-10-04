package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This {@code Statement} class is used to create keywords containing
 * other objects.
 * 
 * @author HardCoded
 */
public class NestedStat implements Statement {
	public List<Statement> list = new ArrayList<>();
	
	public NestedStat() {
		
	}
	
	@Deprecated
	public NestedStat(Statement... fill) {
		list.addAll(Arrays.asList(fill));
	}
	
	public NestedStat(int length) {
		for(int i = 0; i < length; i++)
			list.add(Statement.EMPTY);
	}
	
	public boolean hasStatements() {
		return true;
	}
	
	public List<Statement> getStatements() {
		return list;
	}
	
	public int size() {
		return list.size();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Statement> T get(int index) {
		return (T)list.get(index);
	}
	
	public <T extends Statement> T getOrDefault(int index, T def) {
		T result = get(index);
		return result == null ? def:result;
	}
	
	public <T extends Statement> T add(T stat) {
		list.add(stat);
		return stat;
	}
	
	public <T extends Statement> T set(int index, T stat) {
		list.set(index, stat);
		return stat;
	}
	
	public void remove(int index) {
		list.remove(index);
	}
	
	public String asString() { return "BODY"; }
	public Object[] asList() { return list.toArray(); }
}