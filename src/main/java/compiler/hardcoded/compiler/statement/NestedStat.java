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
	
	public String asString() { return "BODY"; }
}