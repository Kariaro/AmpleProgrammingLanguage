package com.hardcoded.compiler.parsetree;

import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.expression.*;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.compiler.lexer.Token;

public class TreeUtils {
	private static String remove_last(String str) {
		if(str == null) return null;
		if(str.isEmpty()) return "";
		return str.substring(0, str.length() - 1);
	}
	
	public static String printTree(ProgramStat stat) {
		StringBuilder sb = new StringBuilder();
		
		for(Statement s : stat.getStatements()) {
			sb.append(printTree(s)).append("\n");
		}
		
		return sb.toString().trim();
	}
	
	public static String printTree(Statement stat) {
		StringBuilder sb = new StringBuilder();
		
		if(stat instanceof IfStat) {
			IfStat s = (IfStat)stat;
			List<Statement> list = stat.getStatements();
			sb.append("if(").append(list.get(0)).append(") ").append(printTree(list.get(1)));
			if(s.hasElse()) sb.append(" else ").append(printTree(list.get(2)));
			return sb.toString();
		}
		
		if(stat instanceof WhileStat) {
			List<Statement> list = stat.getStatements();
			sb.append("while(").append(list.get(0)).append(") ").append(printTree(list.get(1)));
			return sb.toString();
		}
		
		if(stat instanceof DoWhileStat) {
			List<Statement> list = stat.getStatements();
			sb.append("do ").append(printTree(list.get(0))).append(" while(").append(list.get(1)).append(");");
			return sb.toString();
		}
		
		if(stat instanceof ForStat) {
			List<Statement> list = stat.getStatements();
			String a = list.get(0).toString();
			String b = list.get(1).toString();
			String c = list.get(2).toString();
			sb.append("for(").append(a).append(b).append(c).append(") ").append(printTree(list.get(3)));
			return sb.toString();
		}
		
		if(stat instanceof FuncStat) {
			List<Statement> list = stat.getStatements();
			return remove_last(stat.toString()) + " " + printTree(list.get(0));
		}
		
		if(stat instanceof ClassStat) {
			List<Statement> list = stat.getStatements();
			if(list.isEmpty()) return stat.toString();
			return remove_last(stat.toString()) + " " + printTree(list.get(0));
		}
		
		if(stat instanceof ScopeStat) {
			List<Statement> list = stat.getStatements();
			if(!list.isEmpty()) {
				sb.append("{");
				for(Statement s : list) {
					String str = printTree(s);
					if(str.startsWith("\n\t")) str = str.substring(2);
					sb.append("\n\t").append(str.replace("\n", "\n\t"));
				}
				
				sb.append("\n}");
				return sb.toString();
			}
			
			return "{}";
		}
		
		if(stat instanceof ReturnStat
		|| stat instanceof BreakStat
		|| stat instanceof ContinueStat
		|| stat instanceof DefineStat
		|| stat instanceof GotoStat
		|| stat instanceof ImportStat
		|| stat instanceof EmptyStat) {
			return stat.toString();
		}
		
		{
			List<Statement> list = stat.getStatements();
			if(!list.isEmpty()) {
				for(Statement s : list) {
					String str = printTree(s);
					if(str.startsWith("\n\t")) str = str.substring(2);
					sb.append("\n\t").append(str.replace("\n", "\n\t"));
				}
				
				return sb.toString();
			}
		}
		
		
		return "\n\t" + stat.toString();
	}
	
	protected static Expression deepCopy(Expression elm) {
		if(EmptyExpr.isEmpty(elm)) return EmptyExpr.get();
		if(elm instanceof AtomExpr) return AtomExpr.get((AtomExpr)elm);
		if(elm instanceof UnaryExpr) {
			UnaryExpr ex = (UnaryExpr)elm;
			
			UnaryExpr expr = UnaryExpr.get(ex.getType(), Token.EMPTY);
			expr.setLocation(ex.getStartOffset(), ex.getEndOffset());
			for(Expression e : ex.getExpressions()) {
				expr.add(deepCopy(e));
			}
			return expr;
		}
		if(elm instanceof BinaryExpr) {
			BinaryExpr ex = (BinaryExpr)elm;
			
			BinaryExpr expr = BinaryExpr.get(ex.getType(), Token.EMPTY);
			expr.setLocation(ex.getStartOffset(), ex.getEndOffset());
			for(Expression e : ex.getExpressions()) {
				expr.add(deepCopy(e));
			}
			return expr;
		}
		
		throw new NullPointerException("Failed to clone expression: " + ((elm == null) ? "<null>":elm.getClass()));
	}
}
