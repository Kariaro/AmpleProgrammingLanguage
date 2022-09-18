package me.hardcoded.compiler.parser;

import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Associativity;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.utils.StringUtils;

import java.util.Iterator;

public class ParseUtil {
	public static String stat(Stat stat) {
		return switch (stat.getTreeType()) {
			case BREAK -> breakStat((BreakStat) stat);
			case CONTINUE -> continueStat((ContinueStat) stat);
			case EMPTY -> emptyStat((EmptyStat) stat);
			case FOR -> forStat((ForStat) stat);
			case FUNC -> funcStat((FuncStat) stat);
			case IF -> ifStat((IfStat) stat);
			//			case LABEL -> labelStat((LabelStat) stat);
			case PROGRAM -> programStat((ProgStat) stat);
			case COMPILER -> compilerStat((CompilerStat) stat);
			case RETURN -> returnStat((ReturnStat) stat);
			case SCOPE -> scopeStat((ScopeStat) stat);
			case VAR -> varStat((VarStat) stat);
			//			case WHILE -> whileStat((WhileStat) stat);
			case NAMESPACE -> namespaceStat((NamespaceStat) stat);
			
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
		return "for (" + stat(s.getInitializer()) +
			" " + expr(s.getCondition()) +
			"; " + expr(s.getAction()) + ") " + stat(s.getBody());
	}
	
	public static String funcStat(FuncStat stat) {
		StringBuilder sb = new StringBuilder();
		sb.append("func ");
		
		if (stat.getReference().isExported()) {
			sb.append("(export) ");
		}
		
		sb.append(getReferenceName(stat.getReference())).append(" (");
		
		Iterator<Reference> iter = stat.getParameters().iterator();
		while (iter.hasNext()) {
			Reference reference = iter.next();
			sb.append(reference.getValueType()).append(": ").append(reference.getName());
			
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		
		sb.append(") : ").append(stat.getReference().getValueType());
		
		if (stat.getBody().isEmpty()) {
			return sb.append(";").toString();
		}
		
		sb.append(" ").append(stat(stat.getBody()));
		return sb.toString();
	}
	
	public static String ifStat(IfStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("if (").append(expr(s.getValue())).append(") ").append(stat(s.getBody()));
		
		if (s.hasElseBody()) {
			sb.append(" else ").append(stat(s.getElseBody()));
		}
		
		return sb.toString().trim();
	}
	
	//	private static String labelStat(LabelStat s) {
	//		return s.getReference() + ":";
	//	}
	
	public static String programStat(ProgStat s) {
		StringBuilder sb = new StringBuilder();
		for (Stat stat : s.getElements()) {
			sb.append(stat(stat)).append('\n');
		}
		
		return sb.toString().trim();
	}
	
	public static String compilerStat(CompilerStat s) {
		StringBuilder sb = new StringBuilder();
		sb.append("compiler<").append(s.getTargetType()).append(">(\n");
		for (CompilerStat.Part part : s.getParts()) {
			sb.append("    \"").append(part.command()).append("\"");
			
			for (Reference ref : part.references()) {
				sb.append(" : ").append(getReferenceName(ref));
			}
			sb.append('\n');
		}
		sb.append(");");
		
		return sb.toString().trim();
	}
	
	public static String returnStat(ReturnStat s) {
		return "ret " + expr(s.getValue()) + ";";
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
		return s.getReference().getValueType() + ": " + getReferenceName(s.getReference()) + " = " + expr(s.getValue()) + ";";
	}
	
	//	public static String whileStat(WhileStat s) {
	//		return "while (" + expr(s.getCondition()) + ") " + stat(s.getBody());
	//	}
	
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
			case BINARY -> binaryExpr((BinaryExpr) expr);
			case CALL -> callExpr((CallExpr) expr);
			case CAST -> castExpr((CastExpr) expr);
			case STACK_ALLOC -> stackAllocExpr((StackAllocExpr) expr);
			case NAME -> nameExpr((NameExpr) expr);
			case NONE -> noneExpr((NoneExpr) expr);
			case NUM -> numExpr((NumExpr) expr);
			case STRING -> strExpr((StrExpr) expr);
			case UNARY -> unaryExpr((UnaryExpr) expr);
			default -> expr.toString();
		};
	}
	
	public static String binaryExpr(BinaryExpr e) {
		switch (e.getOperation()) {
			//			case MEMBER -> {
			//				return "(" + expr(e.getLeft()) + "." + expr(e.getRight()) + ")";
			//			}
			case ARRAY -> {
				return "(" + expr(e.getLeft()) + "[" + expr(e.getRight()) + "])";
			}
			default -> {
				return "(" + expr(e.getLeft()) + " " + e.getOperation().getName() + " " + expr(e.getRight()) + ")";
			}
		}
	}
	
	public static String callExpr(CallExpr e) {
		StringBuilder sb = new StringBuilder();
		sb.append(getReferenceName(e.getReference())).append("(");
		
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
		return "cast<" + e.getType() + ">(" + expr(e.getValue()) + ")";
	}
	
	public static String stackAllocExpr(StackAllocExpr e) {
		return "stack_alloc<" + e.getType() + ", " + e.getSize() + ">(" + expr(e.getValue()) + ")";
	}
	
	public static String nameExpr(NameExpr e) {
		return getReferenceName(e.getReference());
	}
	
	public static String noneExpr(NoneExpr e) {
		return "none";
	}
	
	public static String numExpr(NumExpr e) {
		return e.toString();
	}
	
	public static String strExpr(StrExpr e) {
		return "\"" + StringUtils.escapeString(e.getValue()) + "\"";
	}
	
	public static String unaryExpr(UnaryExpr e) {
		if (e.getOperation().getAssociativity() == Associativity.Left) {
			return "(" + e.getOperation().getName() + expr(e.getValue()) + ")";
		}
		
		return "(" + expr(e.getValue()) + e.getOperation().getName() + ")";
	}
	
	// References
	private static String getReferenceName(Reference reference) {
		return reference.getPath();
	}
}
