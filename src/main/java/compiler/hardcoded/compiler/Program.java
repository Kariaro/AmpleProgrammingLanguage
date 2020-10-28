package hardcoded.compiler;

import java.util.*;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.errors.SyntaxMarker;
import hardcoded.visualization.Printable;

/**
 * A program class that contains blocks.
 */
public class Program implements Printable {
	// Context
	public List<SyntaxMarker> syntaxMarkers;
	public Set<String> importedFiles;
	public boolean hasErrors;
	
	
	private List<Identifier> idents;
	private List<Function> blocks; // Globals, Functions
	
	private int function_index;
	// private int globals_index;
	// private int class_index;
	
	public Program() {
		syntaxMarkers = new ArrayList<>();
		importedFiles = new HashSet<>();
		
		idents = new ArrayList<>();
		blocks = new ArrayList<>();
	}
	
	public boolean hasFunction(String name) {
		return getFunction(name) != null;
	}
	
	public Identifier getFunction(String name) {
		for(Identifier id : idents) {
			if(id.id_type() == IdType.funct && id.name().equals(name)) return id;
		}
		
		return null;
	}
	
	public Function getFunctionByName(String name) {
		if(name == null) return null;
		
		for(Block block : blocks) {
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			
			if(name.equals(func.getName())) {
				return func;
			}
		}
		
		return null;
	}
	
	public int size() {
		return blocks.size();
	}
	
	public boolean contains(Block block) {
		return blocks.contains(block);
	}
	
	// TODO: Use this function
	public Function addFunction(Function func) {
		// functions.add(func);
		blocks.add(func);
		idents.add(Identifier.createFuncIdent(func.name, function_index++, func));
		return func;
	}
	
	public Block.Function add(Block.Function block) {
		blocks.add(block);
		idents.add(Identifier.createFuncIdent(block.name, function_index++, block));
		return block;
	}
	
//	public <T extends Block> T add(T block) {
//		blocks.add(block);
//		return block;
//	}
	
	public Block get(int index) {
		return blocks.get(index);
	}
	
	public List<Function> list() {
		return blocks;
	}
	
	public boolean hasErrors() {
		return hasErrors;
	}
	
	public String asString() { return "PROGRAM"; }
	public Object[] asList() { return blocks.toArray(); };
}
