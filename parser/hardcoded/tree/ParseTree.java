package hardcoded.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HardCoded
 */
public class ParseTree {
	public List<PNode> nodes;
	
	// TODO: Keep last action and make it possible to back track....
	
	public ParseTree() {
		nodes = new ArrayList<>();
	}
	
	// 63487
	// 63487
	public ParseTree(ParseTree tree) {
		nodes = new ArrayList<>(tree.nodes);
		// for(Node node : tree.nodes) nodes.add(node.clone());
	}

	public void add(PNode node) {
		nodes.add(node);
	}
	
	public void reduce(PNode node, int count) {
		int index = nodes.size() - count;
		for(int i = 0; i < count; i++) {
			PNode last = nodes.get(index);
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
			PNode node = nodes.get(i);
			
			boolean test = node.value.startsWith("_") && node.nodes.size() == 1;
			if(node.isControl() || test) {
				nodes.remove(i);
				nodes.addAll(i, node.nodes);
				i--;
			} else {
				node.group();
			}
		}
	}
	
	public static class PNode {
		public List<PNode> nodes;
		public String value;
		
		public PNode() {
			nodes = new ArrayList<>();
		}
		
		public PNode(String value) {
			this.nodes = new ArrayList<>();
			this.value = value;
		}
		
		public boolean isControl() {
			return value.indexOf('#') != -1;
		}
		
		@Deprecated
		public void group() {
			for(int i = 0; i < nodes.size(); i++) {
				PNode node = nodes.get(i);
				
				boolean test = node.value.startsWith("_") && node.nodes.size() == 1;
				if(node.isControl() || test) {
					nodes.remove(i);
					nodes.addAll(i, node.nodes);
					i--;
				} else {
					node.group();
				}
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof String) {
				return ((String)obj).equals(value);
			}
			
			return obj == value;
		}
		public PNode clone() {
			PNode clone = new PNode(value);
			for(PNode node : nodes) clone.nodes.add(node.clone());
			return clone;
		}
		
		public String toString() {
			return value;
		}
	}
}
