package hardcoded.compiler;

import java.util.*;
import java.util.stream.Collectors;

import hardcoded.utils.StringUtils;

public interface Statement extends Expression {
	// TODO: Make everything expressions maybe?
	
	@Deprecated
	public default List<Statement> elementsStatements() {
		return Collections.emptyList();
	}
	
	public default List<Expression> stat_expressions_NOP() {
		List<Statement> list = elementsStatements();
		if(list.size() < 1) return stat_expressions();
		return list.stream().filter(x -> x != null).flatMap(x -> x.stat_expressions_NOP().stream()).collect(Collectors.toList());
	}
	
	public default List<Expression> stat_expressions() {
		return Collections.emptyList();
	}
	
	public static class ForStatement implements Statement {
		public Statement variables;
		public List<Expression> list;
		public Expression body;
		
		//public Expression condition;
		//public Expression action;
		
		public ForStatement() {
			list = new ArrayList<>(Arrays.asList(null, null, null));
		}
		
		public void setAction(Expression expr) {
			list.set(2, expr);
		}
		
		public void setCondition(Expression expr) {
			list.set(1, expr);
		}
		
		public Expression action() {
			return list.get(2);
		}
		
		public Expression condition() {
			return list.get(1);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("for(");
			if(variables != null) {
				String str = variables.toString();
				sb.append(str.substring(0, str.length() - 1));
			}
			sb.append("; ");
			if(condition() != null) sb.append(condition());
			sb.append("; ");
			if(action() != null) sb.append(action());
			sb.append(") ").append(body);
			return sb.toString();
		}
		
		@Override
		public List<Statement> elementsStatements() {
			return null; //return Arrays.asList(variables, body);
		}
		
		@Override
		public List<Expression> stat_expressions() {
			return list;
		}
		
		public String listnm() { return "FOR"; }
		public Object[] listme() { return new Object[] { variables, condition(), action(), body }; }
	}
	
	public static class BreakStatement implements Statement {
		public String toString() { return "break;"; }
		public String listnm() { return "BREAK"; }
		public Object[] listme() { return new Object[] {}; }
	}
	
	public static class ContinueStatement implements Statement {
		public String toString() { return "continue;"; }
		public String listnm() { return "CONTINUE"; }
		public Object[] listme() { return new Object[] {}; }
	}
	
	// public static class GotoStatement implements Statement {}
	// public static class LabelStatement implements Statement {}
	
	public static class ExprStatement implements Statement {
		public List<Expression> list;
		
		public ExprStatement(Expression action) {
			list = new ArrayList<>();
			list.add(action);
		}
		
		public ExprStatement() {
			list = new ArrayList<>();
		}
		
		@Override
		public List<Expression> stat_expressions() {
			return list;
		}
		
		public String toString() {
			return StringUtils.join(";\n", list) + (list.isEmpty() ? "":";");
		}
		
		public String listnm() { return toString(); }
		public Object[] listme() { return list.toArray(); }
	}
	
	// TODO: Remove
	public static class Statements implements Statement {
		public List<Expression> list;
		
		public Statements() {
			this.list = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return "{" + StringUtils.join("", list) + "}";
		}
		
		@Override
		public List<Statement> elementsStatements() {
			return null; //return list;
		}
		
		public String listnm() { return "BODY"; }
		public Object[] listme() { return list.toArray(); }
	}
	
	public static class EmptyStatement implements Statement {
		public String toString() { return "{ }"; }
		public String listnm() { return toString(); }
		public Object[] listme() { return new Object[] {}; }
	}
	
	public static class ExpandStatement extends StatementList {
		public ExpandStatement(List<? extends Statement> list) {
			this.list = new ArrayList<>();
			this.list.addAll(list);
		}
	}
	
	public static class StatementList implements Statement {
		public List<Statement> list;
		
		public StatementList() {
			this.list = new ArrayList<>();
		}
		
		@Override
		public List<Statement> elementsStatements() {
			return list;
		}
		
		public String toString() { return StringUtils.join("", list); }
		public String listnm() { return toString(); }
		public Object[] listme() { return list.toArray(); }
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; };
}
