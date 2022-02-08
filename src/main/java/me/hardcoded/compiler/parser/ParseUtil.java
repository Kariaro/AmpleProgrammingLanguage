package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.utils.StringUtils;

import java.util.Iterator;

public class ParseUtil {
	public static String stat(Stat stat) {
		 return switch (stat.getTreeType()) {
			 case BREAK -> breakStat((BreakStat)stat);
			 case CONTINUE -> continueStat((ContinueStat)stat);
			 case EMPTY -> emptyStat((EmptyStat)stat);
			 case FOR -> forStat((ForStat)stat);
			 case FUNC -> funcStat((FuncStat)stat);
			 case GOTO -> gotoStat((GotoStat)stat);
			 case IF -> ifStat((IfStat)stat);
			 case LABEL -> labelStat((LabelStat)stat);
			 case PROG -> progStat((ProgStat)stat);
			 case RETURN -> returnStat((ReturnStat)stat);
			 case SCOPE -> scopeStat((ScopeStat)stat);
			 case VAR -> varStat((VarStat)stat);
			 case WHILE -> whileStat((WhileStat)stat);
			 case NAMESPACE -> namespaceStat((NamespaceStat)stat);
			 
			 default -> {
				 if (stat instanceof Expr e) {
					 yield expr(e) + ";";
				 }
				
				 yield stat.toString();
			 }
		 };
	}
	
	private static String breakStat(BreakStat s) {
		return "break;";
	}
	
	private static String continueStat(ContinueStat s) {
		return "continue;";
	}
	
	private static String emptyStat(EmptyStat s) {
		return ";";
	}
	
	public static String forStat(ForStat s) {
		return "for (" + stat(s.getStart()) +
			" " + expr(s.getCondition()) +
			"; " + expr(s.getAction()) + ") " + stat(s.getBody());
	}
	
	public static String funcStat(FuncStat stat) {
		StringBuilder sb = new StringBuilder();
		sb.append(stat.getReturnType()).append(" ").append(getReferenceName(stat.getReference())).append("(");
		
		Iterator<Reference> iter = stat.getParameters().iterator();
		while (iter.hasNext()) {
			Reference reference = iter.next();
			sb.append(reference.getValueType()).append(" ").append(getReferenceName(reference));
			
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
	
	private static String gotoStat(GotoStat s) {
		return "goto " + s.getReference() + ";";
	}
	
	public static String ifStat(IfStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("if (").append(expr(s.getCondition())).append(") ").append(stat(s.getBody()));
		
		if (s.hasElseBody()) {
			sb.append(" else ").append(stat(s.getElseBody()));
		}
		
		return sb.toString().trim();
	}
	
	private static String labelStat(LabelStat s) {
		return s.getReference() + ":";
	}
	
	public static String progStat(ProgStat s) {
		StringBuilder sb = new StringBuilder();
		for (Stat stat : s.getElements()) {
			sb.append(stat(stat)).append('\n');
		}
		
		return sb.toString().trim();
	}
	
	public static String returnStat(ReturnStat s) {
		return "return " + expr(s.getValue()) + ";";
	}
	
	public static String scopeStat(ScopeStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		
		for (Stat stat : s.getElements()) {
			sb.append(stat(stat).indent(4));
		}
		
		return sb.append("}").toString();
	}
	
	private static String varStat(VarStat s) {
		return s.getType() + " " + getReferenceName(s.getReference()) + " = " + expr(s.getValue()) + ";";
	}
	
	public static String whileStat(WhileStat s) {
		return "while (" + expr(s.getCondition()) + ") " + stat(s.getBody());
	}
	
	public static String namespaceStat(NamespaceStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("namespace ").append(s.getReference()).append(" {\n");
		
		for (Stat stat : s.getElements()) {
			sb.append(stat(stat).indent(4));
		}
		
		return sb.append("}").toString();
	}
	
	public static String expr(Expr expr) {
		return switch (expr.getTreeType()) {
			case BINARY -> binaryExpr((BinaryExpr)expr);
			case CALL -> callExpr((CallExpr)expr);
			case CAST -> castExpr((CastExpr)expr);
			case COMMA -> commaExpr((CommaExpr)expr);
			case NAME -> nameExpr((NameExpr)expr);
			case NULL -> nullExpr((NullExpr)expr);
			case NUM -> numExpr((NumExpr)expr);
			case STR -> strExpr((StrExpr)expr);
			case UNARY -> unaryExpr((UnaryExpr)expr);
			case CONDITIONAL -> conditionalExpr((ConditionalExpr)expr);
			default -> expr.toString();
		};
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
	
	public static String nameExpr(NameExpr e) {
		return getReferenceName(e.getReference());
	}
	
	public static String nullExpr(NullExpr e) {
		return "null";
	}
	
	public static String numExpr(NumExpr e) {
		return e.toString();
	}
	
	public static String strExpr(StrExpr e) {
		return "\"" + StringUtils.escapeString(e.getValue()) + "\"";
	}
	
	public static String unaryExpr(UnaryExpr e) {
		if (e.isPrefix()) {
			return "(" + e.getOperation() + expr(e.getValue()) + ")";
		}
		
		return "(" + expr(e.getValue()) + e.getOperation() + ")";
	}
	
	public static String conditionalExpr(ConditionalExpr e) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		
		Iterator<Expr> iter = e.getValues().iterator();
		while (iter.hasNext()) {
			sb.append(expr(iter.next()));
			
			if (iter.hasNext()) {
				sb.append(" ").append(e.getOperation()).append(" ");
			}
		}
		
		return sb.append(")").toString();
	}
	
	// References
	private static String getReferenceName(Reference reference) {
		if (DebugUtils.DEBUG_REFERENCE_INFORMATION) {
			return reference.getName() + ":" + reference.toSimpleString();
		}
		
		return reference.getName();
	}
}
