package hardcoded.tree;

import java.io.Serializable;
import java.util.*;

/**
 * https://en.wikipedia.org/wiki/Abstract_syntax_tree
 * 
 * <br>This class is serializable.
 * 
 * @author HardCoded
 */
public class AbstractSyntaxTree implements Serializable {
	private static final long serialVersionUID = 3307652007595711656L;
	private List<Node> nodes;
	
	public AbstractSyntaxTree() {
		this.nodes = new ArrayList<>();
	}
	
	public AbstractSyntaxTree(AbstractSyntaxTree ast) {
		this.nodes = new ArrayList<>();
		if(ast != null) {
			// TODO: Not safe if we change the nodes
			this.nodes.addAll(ast.nodes);
		}
	}
	
	public Node addNode() {
		Node node = new Node();
		nodes.add(node);
		return node;
	}
	
	public List<Node> nodes() {
		return nodes;
	}
	
	public static class Node {
		private List<Node> nodes;
		private Object value;
		
		public Node() {
			this.nodes = new ArrayList<>();
		}
		
		public Node(Object value) {
			this.nodes = new ArrayList<>();
			this.value = value;
		}
		
		public Node addNode() {
			Node node = new Node();
			nodes.add(node);
			return node;
		}
		
		public void setValue(Object obj) {
			this.value = obj;
		}
		
		public void addAll(Collection<Node> nodes) {
			this.nodes.addAll(nodes);
		}
		
		public List<Node> nodes() {
			return nodes;
		}
		
		public boolean isEmpty() {
			return nodes.isEmpty();
		}
		
		@Override
		public String toString() {
			return Objects.toString(value);
		}
	}
}
