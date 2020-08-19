package hardcoded.compiler;

import java.util.*;
import java.util.stream.Collectors;

import hardcoded.utils.StringUtils;

public interface Statement extends Stable {
	// TODO: Class statement and object statement.
	
	public default List<Statement> elements() {
		return null;
	}
	
	public default List<Expression> expressions() {
		List<Statement> list = elements();
		if(list == null) return exprs();
		return list.stream().filter(x -> x != null).flatMap(x -> x.expressions().stream()).collect(Collectors.toList());
	}
	
	public default List<Expression> exprs() {
		return Collections.emptyList();
	}
	
	public static class IfStatement implements Statement {
		public Expression condition;
		public Statement body;
		public Statement elseBody;
		
		@Override
		public String toString() {
			if(elseBody == null) return "if (" + condition + ") " + body;
			return "if (" + condition + ") " + body + " else " + elseBody;
		}
		
		@Override
		public List<Statement> elements() {
			if(elseBody == null) Arrays.asList(body);
			return Arrays.asList(body, elseBody);
		}
		
		@Override
		public List<Expression> exprs() {
			return Arrays.asList(condition);
		}
		
		public String listnm() { return "IF"; }
		public Object[] listme() {
			if(elseBody == null) return new Object[] { condition, body };
			return new Object[] { condition, body, elseBody };
		}
	}
	
	public static class ForStatement implements Statement {
		public MultiVariableStatement variables;
		public Expression condition;
		public Expression action;
		public Statement body;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("for(");
			if(variables != null) {
				String str = variables.toString();
				sb.append(str.substring(0, str.length() - 1));
			}
			sb.append("; ");
			if(condition != null) sb.append(condition);
			sb.append("; ");
			if(action != null) sb.append(action);
			sb.append(") ").append(body);
			return sb.toString();
		}
		
		@Override
		public List<Statement> elements() {
			return Arrays.asList(variables, body);
		}
		
		@Override
		public List<Expression> exprs() {
			return Arrays.asList(condition, action);
		}
		
		public String listnm() { return "FOR"; }
		public Object[] listme() { return new Object[] { variables, condition, action, body }; }
	}
	
	public static class WhileStatement implements Statement {
		public Expression condition;
		public Statement body;
		
		@Override
		public String toString() {
			return "while (" + condition + ") " + body;
		}
		
		@Override
		public List<Statement> elements() {
			return Arrays.asList(body);
		}
		
		@Override
		public List<Expression> exprs() {
			return Arrays.asList(condition);
		}
		
		public String listnm() { return "WHILE"; }
		public Object[] listme() { return new Object[] { condition, body }; }
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
		public Expression expr;
		
		public ExprStatement(Expression action) {
			this.expr = action;
		}
		
		@Override
		public List<Expression> exprs() {
			return Arrays.asList(expr);
		}
		
		public String toString() { return expr.toString() + ";"; }
		public String listnm() { return expr.toString(); }
		public Object[] listme() { return new Object[] { expr }; }
	}
	
	public static class Statements implements Statement {
		public List<Statement> list;
		
		public Statements() {
			this.list = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return "{" + StringUtils.join("", list) + "}";
		}
		
		@Override
		public List<Statement> elements() {
			return list;
		}
		
		public String listnm() { return "BODY"; }
		public Object[] listme() { return list.toArray(); }
	}
	
	public static class EmptyStatement implements Statement {
		public String toString() { return "{ }"; }
		public String listnm() { return toString(); }
		public Object[] listme() { return new Object[] {}; }
	}
	
	public static class StatementList implements Statement {
		public List<Statement> list;
		
		public StatementList() {
			this.list = new ArrayList<>();
		}
		
		@Override
		public List<Statement> elements() {
			return list;
		}
		
		public String toString() { return StringUtils.join("", list); }
		public String listnm() { return toString(); }
		public Object[] listme() { return list.toArray(); }
	}
	
	public static class MultiVariableStatement implements Statement {
		public List<Variable> define;
		public Type type;
		
		public MultiVariableStatement() {
			define = new ArrayList<>();
		}
		
		public Variable create() {
			Variable stat = new Variable();
			stat.type = type;
			define.add(stat);
			return stat;
		}
		
		@Override
		public List<Expression> exprs() {
			return define.stream().filter(x -> x.initialized).map(x -> x.value).collect(Collectors.toList());
		}
		
		@Override
		public String toString() {
			if(define.size() == 1) {
				return type + " " + define.get(0).shortString() + ";";
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(type).append(" ");
			for(Variable v : define) sb.append(v.shortString()).append(", ");
			
			if(!define.isEmpty()) {
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
			}
			
			return sb.append(";").toString();
		}
		

		public String listnm() { return "DEFINE"; }
		public Object[] listme() { return define.toArray(); }
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; };
}
