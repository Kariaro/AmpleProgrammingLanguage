package hardcoded.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HardCoded
 */
public class ParseTree {
	public List<Node> nodes;
	
	public ParseTree() {
		nodes = new ArrayList<>();
	}
	
	public ParseTree(ParseTree tree) {
		nodes = new ArrayList<>();
		for(Node node : tree.nodes) nodes.add(node.clone());
	}

	public void add(Node node) {
		nodes.add(node);
	}
	
	public void reduce(Node node, int count) {
		int index = nodes.size() - count;
		for(int i = 0; i < count; i++) {
			Node last = nodes.get(index);
			nodes.remove(index);
			node.nodes.add(last);
		}
		
		nodes.add(node);
	}
	
	public int size() {
		return nodes.size();
	}
	
	@Deprecated
	public void group() {
		for(int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			if(node.isControl()) {
				nodes.remove(i);
				nodes.addAll(i, node.nodes);
				i--;
			} else {
				node.group();
			}
		}
	}
	
	public static class Node {
		public List<Node> nodes;
		public String value;
		
		public Node() {
			nodes = new ArrayList<>();
		}
		
		public Node(String value) {
			this.nodes = new ArrayList<>();
			this.value = value;
		}
		
		public boolean isControl() {
			return value.indexOf('#') != -1;
		}
		
		@Deprecated
		public void group() {
			for(int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				if(node.isControl()) {
					nodes.remove(i);
					nodes.addAll(i, node.nodes);
					i--;
				} else {
					node.group();
				}
			}
		}
		
		public Node clone() {
			Node clone = new Node(value);
			for(Node node : nodes) clone.nodes.add(node.clone());
			return clone;
		}
		
		public String toString() {
			return value;
		}
	}
}
