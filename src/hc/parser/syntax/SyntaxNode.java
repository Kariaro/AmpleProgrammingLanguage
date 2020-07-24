package hc.parser.syntax;

import java.util.ArrayList;
import java.util.List;

import hc.parser.SyntaxType;

public class SyntaxNode {
	protected List<SyntaxNode> children;
	protected final SyntaxNode parent;
	protected SyntaxType type;
	protected Object value;
	
	public SyntaxNode(SyntaxNode parent) {
		this.parent = parent;
		children = new ArrayList<>();
	}
	
	protected SyntaxNode addNode() {
		return addNode(null);
	}
	
	protected SyntaxNode addNode(SyntaxType type) {
		SyntaxNode node = new SyntaxNode(this);
		node.setType(type);
		children.add(node);
		return node;
	}
	
	protected SyntaxNode setValue(Object value) {
		this.value = value;
		return this;
	}
	
	protected Object getValue() {
		return value;
	}
	
	protected void setType(SyntaxType type) {
		this.type = type;
	}
	
	protected SyntaxType getType() {
		return type;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		
		if(value != null)
			sb.append("(").append(value).append(")");
		
		if(!children.isEmpty()) {
			sb.append("\n");
			for(SyntaxNode node : children) {
				String string = node.toString();
				sb.append('\t').append(string.replace("\n", "\n\t")).append("\n");
			}
			
			sb.deleteCharAt(sb.length() - 1);
		}
		
		return sb.toString();
	}
}
