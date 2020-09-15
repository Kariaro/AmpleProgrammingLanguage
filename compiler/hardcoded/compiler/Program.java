package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.constants.Printable;

/**
 * A program class that contains blocks.
 */
public class Program implements Printable {
	private List<Identifier> idents;
	private List<Block> blocks; // Globals, Functions
	
	private int function_index;
	private int globals_index;
	private int class_index;
	
	public Program() {
		idents = new ArrayList<>();
		blocks = new ArrayList<>();
	}
	
	public boolean hasFunction(String name) {
		return getFunction(name) != null;
	}
	
	public Identifier getFunction(String name) {
		for(Identifier id : idents)
			if(id.idtype() == IdType.funct && id.name().equals(name)) return id;
		return null;
	}
	
	public int size() {
		return blocks.size();
	}
	
	public boolean contains(Block block) {
		return blocks.contains(block);
	}
	
	public Block.Function add(Block.Function block) {
		blocks.add(block);
		idents.add(Identifier.createFuncIdent(block.name, function_index++, block));
		return block;
	}
	
	public <T extends Block> T add(T block) {
		blocks.add(block);
		return block;
	}
	
	public Block get(int index) {
		return blocks.get(index);
	}
	
	public List<Block> list() {
		return blocks;
	}
	
	public String asString() { return "PROGRAM"; }
	public Object[] asList() { return blocks.toArray(); };
}
