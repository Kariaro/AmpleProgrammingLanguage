package hardcoded.utils;

import java.util.List;

import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.statement.*;

public final class StatementUtils {
	@FunctionalInterface
	public static interface Folding<T> {
		void constantFolding(List<T> parent, int index, Function func);
	}
	
	public static void execute_for_all_expressions(Function func, Folding<Expression> fc) {
		getAllExpressions(func, func.body, fc);
	}
	
	public static void execute_for_all_statements(Function func, Folding<Statement> fc) {
		getAllStatements(func, func.body, fc);
		fc.constantFolding(List.of(func.body), 0, func);
	}
	
	public static void getAllStatements(Function func, Statement stat, Folding<Statement> fc) {
		if(stat.hasElements()) {
			List<Statement> list = stat.getElements();
			
			for(int i = 0; i < list.size(); i++) {
				getAllStatements(func, list.get(i), fc);
				fc.constantFolding(list, i, func);
			}
		}
	}
	
	public static void getAllExpressions(Function func, Statement stat, Folding<Expression> fc) {
		if(stat.hasElements()) {
			for(Statement s : stat.getElements()) {
				getAllExpressions(func, s, fc);
			}
		}
		
		if(stat instanceof ExprStat) {
			ExprStat es = (ExprStat)stat;
			for(int i = 0; i < es.list.size(); i++) {
				Expression e = es.list.get(i);
				getAllExpressions(func, e, fc);
				fc.constantFolding(es.list, i, func);
				
				// If this is the last element this could give a array index out of bounds !
				if(e != es.list.get(i)) i--;
			}
		}
		
		if(stat instanceof VariableStat) {
			VariableStat var = (VariableStat)stat;
			for(int i = 0; i < var.list.size(); i++) {
				getAllExpressions(func, var.list.get(i), fc);
				fc.constantFolding(var.list, i, func);
			}
		}
	}
	
	public static void getAllExpressions(Function func, Expression expr, Folding<Expression> fc) {
		if(expr.hasElements()) {
			List<Expression> list = expr.getElements();
			for(int i = 0; i < list.size(); i++) {
				getAllExpressions(func, list.get(i), fc);
				fc.constantFolding(list, i, func);
			}
		}
	}
	
	public static String printPretty(Program prog) {
		StringBuilder sb = new StringBuilder();
		for(Function func : prog.list()) {
			sb.append(printPretty(func)).append("\n");
		}
		return sb.toString().trim();
	}
	
	
	public static String printPretty(Function func) {
		StringBuilder sb = new StringBuilder().append(func.toString());
		if(func.isPlaceholder()) return sb.append(";").toString();

		String body = printPretty(func.getBody());
		if(!body.startsWith("{")) body = "{" + body + "\n}";
		
		return sb.append(" ").append(body).toString();
	}
	
	public static String printPretty(Statement stat) {
		StringBuilder sb = new StringBuilder();
		
		if(stat instanceof IfStat is) {
			String body = printPretty(is.getBody());
			if(!body.startsWith("{")) body = "{" + body + "\n}";
			
			sb.append(is.toString()).append(" ").append(body);
			if(is.hasElseBody()) {
				body = printPretty(is.getElseBody());
				if(!body.startsWith("{")) body = "{" + body + "\n}";
				sb.append(" else ").append(body);
			}
			
			return sb.toString();
		} else if(stat instanceof WhileStat ws) {
			String body = printPretty(ws.getBody());
			if(!body.startsWith("{")) body = "{" + body + "\n}";
			
			return sb.append(ws.toString()).append(" ").append(body).toString();
		} else if(stat instanceof ForStat fs) {
			String body = printPretty(fs.getBody());
			if(!body.startsWith("{")) body = "{" + body + "\n}";
			
			return sb.append(fs.toString()).append(" ").append(body).toString();
		}
		
		if(stat.hasElements()) {
			for(Statement s : stat.getElements()) {
				String str = printPretty(s);
				if(str.startsWith("\n\t")) str = str.substring(2);
				sb.append("\n\t").append(str.replace("\n", "\n\t"));
			}
			
			if(stat instanceof NestedStat) {
				sb.insert(0, "{").append("\n}");
			}
			
			return sb.toString();
		}
		
		return "\n\t" + stat.toString();
	}
}
