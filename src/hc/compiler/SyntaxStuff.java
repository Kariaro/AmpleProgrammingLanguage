package hc.compiler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.lexer.Token;
import hardcoded.tree.AbstractSyntaxTree;
import hardcoded.tree.AbstractSyntaxTree.Node;
import hardcoded.tree.ParseTree;
import hardcoded.tree.ParseTree.PNode;
import hardcoded.utils.StringUtils;

// TODO: Check for syntax errors and check for some build keywords
// TODO: Rename.
public class SyntaxStuff {
	public SyntaxStuff() {
		
	}
	
	public void analyse(Token token) {
		
	}
	
	public AbstractSyntaxTree createTree(ParseTree tree) {
		AbstractSyntaxTree ast = new AbstractSyntaxTree();
		
		PNode entry = tree.nodes.get(0);
		
		for(PNode function : entry.nodes) {
			Function f = new Function(function);
			Node node = ast.addNode();
			node.setValue(f);
			
			if(f.statements != null) {
				PNode stats = f.statements;
				
				for(PNode stat : stats.nodes) {
					Node next = node.addNode();
					next.setValue(Statement.create(next, stat));
				}
			}
		}
		
		// "x[y]" -> "*(x + y)"
		
		return ast;
	}
	
	private class Function {
		public int modifiers;
		public Type type;
		public String name;
		public Argument[] arguments;
		
		public transient PNode statements;
		
		public Function(PNode function) {
			List<PNode> nodes = function.nodes;
			
			int index = 0;
			if(nodes.get(0).equals("modifiers")) {
				index++;
			}
			
			this.type = new Type(nodes.get(index));
			this.name = nodes.get(index + 1).value;
			
			PNode args = nodes.get(index + 3);
			if(args.equals("arguments")) {
				this.arguments = new Argument[(args.nodes.size() + 1) / 2];
				
				for(int i = 0; i < this.arguments.length; i++) {
					this.arguments[i] = new Argument(args.nodes.get(i * 2));
				}
				
				index += 6;
			} else {
				index += 5;
				this.arguments = new Argument[0];
			}
			
			PNode stats = nodes.get(index);
			if(stats.equals("statements")) {
				statements = stats;
			}
		}
		
		@Override
		public String toString() {
			return type + " " + name + "(" + StringUtils.join(", ", arguments) + ")";
		}
	}
	
	private static class Statement {
		public PNode stat;
		public String value;
		
		public Statement(String value) {
			this.value = value;
		}
		
		public static Statement create(Node self, PNode stat) {
			// TODO: Check what type of statement this is...
			
			List<PNode> nodes = stat.nodes;
			
			PNode first = nodes.get(0);
			
			if(first.equals("{")) return new  Statement("while");
			if(first.equals("while")) return new  Statement("while");
			if(first.equals("if")) return new Statement("if");
			if(first.equals("for")) return new Statement("for");
			if(first.equals("type")) return new Statement("=");
			if(first.equals("return")) return new Statement("ret");
			if(first.equals("continue")) return new Statement("continue");
			if(first.equals("break")) return new Statement("break");
			
			return new Statement(StringUtils.join(", ", nodes));
		}
		
		@Override
		public String toString() {
			return stat == null ? value:StringUtils.join(", ", stat.nodes);
		}
	}
	
	private class Expression {
		
	}
	
	private class Argument {
		public Type type;
		public String name;
		
		public Argument(PNode node) {
			List<PNode> nodes = node.nodes;
			this.type = new Type(nodes.get(0));
			this.name = nodes.get(1).value;
		}
		
		@Override
		public String toString() {
			return type + " " + name;
		}
	}
	
	private class Type {
		public String typeName;
		public int pointers;
		
		public Type(PNode typeNode) {
			List<PNode> nodes = typeNode.nodes;
			
			PNode typeName = nodes.get(0);
			if(typeName.equals("rawtype")) {
				this.typeName = typeName.nodes.get(0).value;
			} else {
				this.typeName = typeName.value;
			}
			
			pointers = nodes.size() - 1;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(typeName);
			for(int i = 0; i < pointers; i++) sb.append('*');
			return sb.toString();
		}
	}
}
