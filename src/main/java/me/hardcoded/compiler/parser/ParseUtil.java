package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.FuncParam;
import me.hardcoded.utils.StringUtils;

import java.util.Iterator;

public class ParseUtil {
	public static String stat(Stat stat) {
		if (stat instanceof ProgStat s) {
			return progStat(s);
		}
		if (stat instanceof FuncStat s) {
			return funcStat(s);
		}
		if (stat instanceof ScopeStat s) {
			return scopeStat(s);
		}
		if (stat instanceof IfStat s) {
			return ifStat(s);
		}
		if (stat instanceof ForStat s) {
			return forStat(s);
		}
		if (stat instanceof WhileStat s) {
			return whileStat(s);
		}
		if (stat instanceof VarStat s) {
			return varStat(s);
		}
		if (stat instanceof ReturnStat s) {
			return "return " + expr(s.getValue()) + ";";
		}
		if (stat instanceof GotoStat s) {
			return "goto " + s.getReference() + ";";
		}
		if (stat instanceof ContinueStat) {
			return "continue;";
		}
		if (stat instanceof BreakStat) {
			return "break;";
		}
		if (stat instanceof LabelStat s) {
			return s.getReference() + ":";
		}
		if (stat instanceof EmptyStat) {
			return ";";
		}
		if (stat instanceof Expr e) {
			return expr(e) + ";";
		}
		
		return stat.toString();
	}
	
	private static String varStat(VarStat s) {
		return s.getType() + " " + s.getReference() + " = " + expr(s.getValue()) + ";";
	}
	
	public static String progStat(ProgStat s) {
		StringBuilder sb = new StringBuilder();
		for (Stat stat : s.getElements()) {
			sb.append(stat(stat)).append('\n');
		}
		
		return sb.toString().trim();
	}
	
	public static String ifStat(IfStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("if (").append(expr(s.getCondition())).append(") ").append(stat(s.getBody()));
		
		if (s.hasElseBody()) {
			sb.append(" else ").append(stat(s.getElseBody()));
		}
		
		return sb.toString().trim();
	}
	
	public static String whileStat(WhileStat s) {
		return "while (" + expr(s.getCondition()) + ") " + stat(s.getBody());
	}
	
	public static String forStat(ForStat s) {
		return "for (" + stat(s.getStart()) +
			" " + expr(s.getCondition()) +
			"; " + expr(s.getAction()) + ") " + stat(s.getBody());
	}
	
	public static String scopeStat(ScopeStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		
		for (Stat stat : s.getElements()) {
			sb.append(stat(stat).indent(4));
		}
		
		return sb.append("}").toString();
	}
	
	public static String funcStat(FuncStat stat) {
		StringBuilder sb = new StringBuilder();
		sb.append(stat.getReturnType()).append(" ").append(stat.getReference()).append("(");
		
		Iterator<FuncParam> iter = stat.getParameters().iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		
		if (stat.getBody().isEmpty()) {
			return sb.append(");").toString();
		}
		
		sb.append(") ").append(stat(stat.getBody()));
		return sb.toString();
	}
	
	public static String expr(Expr expr) {
		if (expr instanceof UnaryExpr e) {
			return unaryExpr(e);
		}
		if (expr instanceof BinaryExpr e) {
			return binaryExpr(e);
		}
		if (expr instanceof CallExpr e) {
			return callExpr(e);
		}
		if (expr instanceof CommaExpr e) {
			return commaExpr(e);
		}
		if (expr instanceof CastExpr e) {
			return castExpr(e);
		}
		if (expr instanceof NullExpr) {
			return "null";
		}
		if (expr instanceof NameExpr e) {
			return e.getReference().toString();
		}
		if (expr instanceof NumExpr e) {
			return e.toString();
		}
		if (expr instanceof StrExpr e) {
			return "\"" + StringUtils.escapeString(e.getValue()) + "\"";
		}
		
		return expr.toString();
	}
	
	public static String castExpr(CastExpr e) {
		return "(:" + e.getCastType() + ")(" + expr(e.getValue()) + ")";
	}
	
	public static String commaExpr(CommaExpr e) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		
		Iterator<Expr> iter = e.getValues().iterator();
		while (iter.hasNext()) {
			sb.append(expr(iter.next()));
			
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		
		return sb.append(")").toString();
	}
	
	public static String callExpr(CallExpr e) {
		StringBuilder sb = new StringBuilder();
		sb.append(expr(e.getCaller())).append("(");
		
		Iterator<Expr> iter = e.getParameters().iterator();
		while (iter.hasNext()) {
			sb.append(expr(iter.next()));
			
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		
		return sb.append(")").toString();
	}
	
	public static String binaryExpr(BinaryExpr e) {
		switch(e.getOperation()) {
			case MEMBER -> {
				return "(" + expr(e.getLeft()) + "." + expr(e.getRight()) + ")";
			}
			case ARRAY -> {
				return "(" + expr(e.getLeft()) + "[" + expr(e.getRight()) + "])";
			}
			default -> {
				return "(" + expr(e.getLeft()) + " " + e.getOperation() + " " + expr(e.getRight()) + ")";
			}
		}
	}
	
	public static String unaryExpr(UnaryExpr e) {
		if (e.isPrefix()) {
			return "(" + e.getOperation() + expr(e.getValue()) + ")";
		}
		
		return "(" + expr(e.getValue()) + e.getOperation() + ")";
	}
}
