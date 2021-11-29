package hardcoded.compiler.statement;

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
	
	public NestedStat(int length) {
		super(true);
		for(int i = 0; i < length; i++) {
			list.add(Statement.newEmpty());
		}
	}
	
	@Override
	public String asString() {
		return "BODY";
	}
}