package hardcoded.compiler;

import java.io.File;
import java.util.*;

import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.errors.SyntaxMarker;
import hardcoded.compiler.impl.IBlock;
import hardcoded.compiler.impl.IFunction;
import hardcoded.compiler.impl.IProgram;
import hardcoded.visualization.Printable;

/**
 * A program class that contains blocks.
 */
public final class Program implements IProgram, Printable {
	private List<Identifier> idents;
	private int function_index;
	
	public List<SyntaxMarker> syntaxMarkers;
	public Set<String> importedFiles;
	public List<Function> functions;
	public boolean hasErrors;
	
	
	public Program() {
		syntaxMarkers = new ArrayList<>();
		importedFiles = new HashSet<>();
		
		idents = new ArrayList<>();
		functions = new ArrayList<>();
	}
	
	public List<IFunction> getFunctions() {
		return List.copyOf(functions);
	}
	
	public List<SyntaxMarker> getSyntaxMarkers() {
		return List.copyOf(syntaxMarkers);
	}
	
	public Set<String> getImportedFiles() {
		// Import order does not really matter when it's already compiled
		return Set.copyOf(importedFiles);
	}
	
	public boolean hasErrors() {
		return hasErrors;
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
		
		for(Function func : functions) {
			if(name.equals(func.getName())) {
				return func;
			}
		}
		
		return null;
	}
	
	public int size() {
		return functions.size();
	}
	
	public Function addFunction(Function func) {
		functions.add(func);
		idents.add(Identifier.createFuncIdent(func.name, function_index++, func));
		return func;
	}
	
	public boolean hasImportedFile(File file) {
		return importedFiles.contains(file.getAbsolutePath());
	}
	
	public IBlock get(int index) {
		return functions.get(index);
	}
	
	public List<Function> list() {
		return functions;
	}
	
	public String asString() { return "PROGRAM"; }
	public Object[] asList() { return functions.toArray(); };
}
