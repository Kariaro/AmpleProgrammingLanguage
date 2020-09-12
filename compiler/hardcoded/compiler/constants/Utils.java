package hardcoded.compiler.constants;

import java.util.List;

import hardcoded.compiler.*;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Statement.*;

public final class Utils {
	private Utils() {}
	
	@FunctionalInterface
	public static interface Folding<T> {
		void constantFolding(List<T> parent, int index, Function func);
	}
	
	public static void execute_for_all_expressions(Function func, Folding<Expression> fc) {
		getAllExpressions(func, func.body, fc);
	}
	
	public static void execute_for_all_statements(Function func, Folding<Statement> fc) {
		getAllStatements(func, func.body, fc);
	}
	
	public static void getAllStatements(Function func, Statement stat, Folding<Statement> fc) {
		if(stat.hasStatements()) {
			List<Statement> list = stat.getStatements();
			for(int i = 0; i < list.size(); i++) {
				getAllStatements(func, list.get(i), fc);
				fc.constantFolding(list, i, func);
			}
		}
	}
	
	public static void getAllExpressions(Function func, Statement stat, Folding<Expression> fc) {
		if(stat.hasStatements()) {
			for(Statement s : stat.getStatements()) getAllExpressions(func, s, fc);
		}
		
		if(stat instanceof ExprStat) {
			ExprStat es = (ExprStat)stat;
			for(int i = 0; i < es.list.size(); i++) {
				Expression e = es.list.get(i);
				getAllExpressions(func, e, fc);
				fc.constantFolding(es.list, i, func);
				if(e != es.list.get(i)) i--;
			}
		}
		
//		if(stat instanceof Variable) {
//			Variable var = (Variable)stat;
//			for(int i = 0; i < var.list.size(); i++) {
//				getAllExpressions(func, var.list.get(i), fc);
//				fc.constantFolding(var.list, i, func);
//			}
//		}
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
	
	public static String printPretty(Function func) {
		String str = func.toString();
		StringBuilder sb = new StringBuilder().append(str.substring(0, str.length() - 1));
		if(func.isPlaceholder()) return sb.append(";").toString();
		return sb.append(" {").append(printPretty(func.body)).append("\n}").toString();
	}
	
	public static String printPretty(Statement stat) {
		StringBuilder sb = new StringBuilder();
		if(stat == null) return "?";
		
		if(stat instanceof IfStat) {
			IfStat is = (IfStat)stat;
			String str = is.toString();
			sb.append(str.substring(0, str.length() - 1)).append(" {").append(printPretty(is.body())).append("\n}");
			if(is.elseBody() != null) sb.append(" else {").append(printPretty(is.elseBody())).append("\n}");
			return sb.toString();
		}
		
		if(stat instanceof WhileStat || stat instanceof ForStat) {
			NestedStat ns = (NestedStat)stat;
			String str = ns.toString();
			return sb.append(str.substring(0, str.length() - 1)).append(" {").append(printPretty(ns.body())).append("\n}").toString();
		}
		
		if(stat.hasStatements()) {
			for(Statement s : stat.getStatements()) {
				String str = printPretty(s);
				if(str.startsWith("\n\t")) str = str.substring(2);
				sb.append("\n\t").append(str.replace("\n", "\n\t"));
			}
			
			return sb.toString();
		}
		
		return "\n\t" + stat.toString();
	}
}
