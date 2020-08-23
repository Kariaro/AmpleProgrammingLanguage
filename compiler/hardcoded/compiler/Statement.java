package hardcoded.compiler;

import java.util.*;

import hardcoded.utils.StringUtils;

public interface Statement extends Printable {
	public static final Statement EMPTY = new Statement() {
		public String toString() { return ""; }
		public String listnm() { return ""; }
		public Object[] listme() { return new Object[] {}; }
	};
	
	public default boolean hasStatements() {
		return false;
	}
	
	public default List<Statement> getStatements() {
		return null;
	}
	
	public static class NestedStat implements Statement {
		public List<Statement> list = new ArrayList<>();
		
		public NestedStat() {
			
		}
		
		public NestedStat(Statement... fill) {
			list.addAll(Arrays.asList(fill));
		}
		
		public boolean hasStatements() {
			return true;
		}
		
		public List<Statement> getStatements() {
			return list;
		}
		
		public Statement body() {
			return null;
		}
		
		public int size() {
			return list.size();
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Statement> T get(int index) {
			return (T)list.get(index);
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
		
		public String listnm() { return "BODY"; }
		public Object[] listme() { return list.toArray(); }
	}
	
	public static class SwitchStat extends NestedStat {
		// TODO: Implement
	}
	
	public static class WhileStat extends NestedStat {
		public WhileStat() {
			super(null, null);
		}
		
		public Expression condition() {
			ExprStat stat = get(0);
			return stat == null ? null:(stat.expr());
		}
		
		public Statement body() {
			return get(1);
		}
		
		public void setCondition(Expression expr) {
			set(0, new ExprStat(expr));
		}
		
		public void setBody(Statement stat) {
			set(1, stat);
		}
		
		public String listnm() { return "WHILE"; }
		public String toString() { return "while(" + condition() + ");"; }
	}
	
	public static class ForStat extends NestedStat {
		public ForStat() {
			super(null, null, null, null);
		}
		
		public Statement variables() {
			return get(0);
		}
		
		public Expression condition() {
			ExprStat stat = get(1);
			return stat == null ? null:(stat.expr());
		}
		
		public Expression action() {
			ExprStat stat = get(2);
			return stat == null ? null:(stat.expr());
		}
		
		public Statement body() {
			return get(3);
		}
		
		// Statement or Expression ?
		public void setVariables(Statement stat) {
			set(1, stat);
		}
		
		public void setCondition(Expression expr) {
			set(1, new ExprStat(expr));
		}
		
		public void setAction(Expression expr) {
			set(2, new ExprStat(expr));
		}
		
		public void setBody(Statement stat) {
			set(3, stat);
		}
		
		public String listnm() { return "FOR"; }
		public String toString() {
			String vars = Objects.toString(variables(), null);
			String cond = Objects.toString(condition(), null);
			String acts = Objects.toString(action(), null);
			
			return "for(" + (vars == null ? "":vars) + ";" +
							(cond == null ? "":" " + cond) + ";" +
							(acts == null ? "":" " + acts) + ");";
		}
	}
	
	public static class IfStat extends NestedStat {
		public IfStat() {
			super(null, null);
		}
		
		public Expression condition() {
			ExprStat stat = get(0);
			return stat == null ? null:(stat.expr());
		}
		
		public Statement body() {
			return get(1);
		}
		
		public Statement elseBody() {
			if(size() < 3) return null;
			return get(2);
		}
		
		public void setCondition(Expression expr) {
			set(0, new ExprStat(expr));
		}
		
		public Statement setBody(Statement stat) {
			return set(1, stat);
		}
		
		public Statement setElseBody(Statement stat) {
			if(stat == null) {
				if(size() > 2) remove(2);
			} else {
				if(size() > 2) set(2, stat);
				else add(stat);
			}
			
			return stat;
		}
		
		public String listnm() { return "IF"; }
		public String toString() { return "if(" + condition() + ");"; } // TODO: Show if else?
	}
	
	public static class BreakStat implements Statement {
		public String toString() { return "break;"; }
		public String listnm() { return "BREAK"; }
		public Object[] listme() { return new Object[] {}; }
	}
	
	public static class ContinueStat implements Statement {
		public String toString() { return "continue;"; }
		public String listnm() { return "CONTINUE"; }
		public Object[] listme() { return new Object[] {}; }
	}
	
	public static class ReturnStat extends ExprStat {
		public ReturnStat() {
			super(null);
		}
		
		public void setValue(Expression expr) {
			list.set(0, expr);
		}
		
		public Expression value() {
			return list.get(0);
		}
		
		public String listnm() { return "RETURN"; }
		public Object[] listme() { return new Object[] { value() }; }
		public String toString() { return "return" + (value() == null ? "":(" " + value())) + ";"; }
	}
	
	// public static class GotoStatement implements Statement {}
	// public static class LabelStatement implements Statement {}
	
	public static class ExprStat implements Statement {
		public List<Expression> list = new ArrayList<>();
		
		public ExprStat(Expression expr) {
			list.add(expr);
		}
		
		public Expression expr() {
			if(list.isEmpty()) return null;
			return list.get(0);
		}
		
		public String listnm() { return toString(); }
		public Object[] listme() { return list.toArray(); }
		public String toString() { return Objects.toString(expr()); }
	}
	
	public static class StatementList implements Statement {
		public List<Statement> list;
		
		public StatementList() {
			this.list = new ArrayList<>();
		}
		
		public StatementList(List<? extends Statement> list) {
			this.list = new ArrayList<>(list);
		}
		
		@Override
		public boolean hasStatements() {
			return true;
		}
		
		@Override
		public List<Statement> getStatements() {
			return list;
		}
		
		public String toString() { return StringUtils.join("", list); }
		public String listnm() { return toString(); }
		public Object[] listme() { return list.toArray(); }
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; };
}
