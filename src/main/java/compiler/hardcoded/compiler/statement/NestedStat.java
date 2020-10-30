package hardcoded.compiler.statement;

import java.util.Arrays;

/**
 * This {@code Statement} class is used to create keywords containing
 * other objects.
 * 
 * @author HardCoded
 */
public class NestedStat extends Statement {
	public NestedStat() {
		super(true);
	}
	
	@Deprecated
	public NestedStat(Statement... fill) {
		super(true);
		list.addAll(Arrays.asList(fill));
	}
	
	public NestedStat(int length) {
		super(true);
		for(int i = 0; i < length; i++)
			list.add(Statement.newEmpty());
	}
	
//	public int size() {
//		return list.size();
//	}
//	
//	@SuppressWarnings("unchecked")
//	public <T extends Statement> T get(int index) {
//		return (T)list.get(index);
//	}
//	
//	public <T extends Statement> T getOrDefault(int index, T def) {
//		T result = get(index);
//		return result == null ? def:result;
//	}
//	
//	public <T extends Statement> T add(T stat) {
//		list.add(stat);
//		return stat;
//	}
//	
//	public <T extends Statement> T set(int index, T stat) {
//		list.set(index, stat);
//		return stat;
//	}
//	
//	public void remove(int index) {
//		list.remove(index);
//	}
	
	public String asString() { return "BODY"; }
}