package hardcoded.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hardcoded.utils.StringUtils;

public interface Statement extends Stable {
	// public static class DeclareStatement implements Statement {}
	public static class IfStatement implements Statement {
		public Expression condition;
		public Statement body;
		public Statement elseBody;
		
		@Override
		public String toString() {
			if(elseBody == null) return "if (" + condition + ") " + body;
			return "if (" + condition + ") " + body + " else " + elseBody;
		}
		
		public String listnm() { return "IF"; }
		public Object[] listme() {
			if(elseBody == null) return new Object[] { condition, body };
			return new Object[] { condition, body, elseBody };
		}
	}
	
	// public static class ForStatement implements Statement {}
	
	public static class WhileStatement implements Statement {
		public Expression condition;
		public Statement body;
		
		@Override
		public String toString() {
			return "while (" + condition + ") " + body;
		}
		
		public String listnm() { return "WHILE"; }
		public Object[] listme() { return new Object[] { condition, body }; }
	}
	
	public static class ReturnStatement implements Statement {
		public Expression value;
		
		@Override
		public String toString() {
			if(value == null) return "return;";
			return "return " + value + ";";
		}
		
		public String listnm() { return "RETURN"; }
		public Object[] listme() {
			if(value == null) return new Object[] {};
			return new Object[] { value };
		}
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
		public Expression action;
		
		public ExprStatement(Expression action) {
			this.action = action;
		}
		
		public String toString() {
			return action.toString() + ";";
		}
		
		public String listnm() { return action.toString(); }
		public Object[] listme() { return new Object[] { action }; }
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
		
		public String listnm() { return "BODY"; }
		public Object[] listme() { return list.toArray(); }
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; };
}
