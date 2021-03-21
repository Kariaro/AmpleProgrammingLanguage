package com.hardcoded.compiler.parsetree;

import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.statement.*;

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
			List<Statement> list = stat.getStatements();
			String str = remove_last(list.get(0).toString());
			sb.append("if(").append(str).append(") {").append(printTree(list.get(1))).append("\n}");
			return sb.toString();
		}
		
		if(stat instanceof IfElseStat) {
			List<Statement> list = stat.getStatements();
			String str = remove_last(list.get(0).toString());
			sb.append("if(").append(str).append(") {").append(printTree(list.get(1)))
								 .append("\n} else {").append(printTree(list.get(2))).append("\n}");
			return sb.toString();
		}
		
		if(stat instanceof WhileStat) {
			List<Statement> list = stat.getStatements();
			String str = remove_last(list.get(0).toString());
			sb.append("while(").append(str).append(") {").append(printTree(list.get(1))).append("\n}");
			return sb.toString();
		}
		
		if(stat instanceof DoWhileStat) {
			List<Statement> list = stat.getStatements();
			String str = remove_last(list.get(1).toString());
			sb.append("do {").append(printTree(list.get(0))).append("\n} while(").append(str).append(");");
			return sb.toString();
		}
		
		if(stat instanceof ForStat) {
			List<Statement> list = stat.getStatements();
			String a = list.get(0).toString();
			String b = list.get(1).toString();
			String c = list.get(2).toString();
			sb.append("for(").append(a).append(b).append(c).append(") {").append(printTree(list.get(3))).append("\n}");
			return sb.toString();
		}
		
		if(stat instanceof DefineStat) {
			List<Statement> list = stat.getStatements();
			String a = remove_last(stat.toString());
			if(list.size() == 0) return a + ";";
			return a + " = " + list.get(0).toString();
		}
		
		if(stat instanceof FuncStat) {
			List<Statement> list = stat.getStatements();
			return remove_last(stat.toString()) + " {" + printTree(list.get(0)) + "\n}";
		}
		
		if(stat instanceof ReturnStat
		|| stat instanceof BreakStat
		|| stat instanceof ContinueStat
		|| stat instanceof GotoStat) {
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
}
