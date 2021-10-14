package hardcoded.compiler.statement;

import java.util.List;

import hardcoded.utils.StringUtils;

public class StatementList extends Statement {
	public StatementList() {
		super(true);
	}
	
	public StatementList(List<? extends Statement> list) {
		super(true);
		this.list.addAll(list);
	}
	
	@Override
	public String asString() {
		return toString();
	}
	
	@Override
	public String toString() {
		return StringUtils.join("", list);
	}
}