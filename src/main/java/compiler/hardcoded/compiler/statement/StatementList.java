package hardcoded.compiler.statement;

import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.StringUtils;

public class StatementList extends Statement {
	public List<Statement> list;
	
	public StatementList() {
		this.list = new ArrayList<>();
	}
	
	public StatementList(List<? extends Statement> list) {
		this.list = new ArrayList<>(list);
	}
	
	public boolean hasStatements() {
		return true;
	}
	
	public List<Statement> getStatements() {
		return list;
	}
	
	public String toString() { return StringUtils.join("", list); }
	public String asString() { return toString(); }
	public Object[] asList() { return list.toArray(); }
}