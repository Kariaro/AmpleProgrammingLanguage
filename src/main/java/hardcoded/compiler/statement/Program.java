package hardcoded.compiler.statement;

import java.io.File;
import java.util.*;

import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.constants.Identifier.IdType;
import hardcoded.compiler.errors.SyntaxMarker;
import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.impl.IBlock;
import hardcoded.compiler.impl.IFunction;
import hardcoded.compiler.impl.IProgram;
import hardcoded.compiler.types.HighType;
import hardcoded.visualization.Printable;

/**
 * A program class that contains blocks.
 */
public final class Program implements IProgram, Printable {
	private final Map<String, Expression> defined_globals;
	private final Map<String, HighType> defined_types;
	private final Set<String> importedFiles;
	private final List<SyntaxMarker> syntaxMarkers;
	private final List<Identifier> functionIdentifiers;
	private final List<Function> functions;
	private int function_index;
	private boolean hasErrors;
	
	public Program() {
		syntaxMarkers = new ArrayList<>();
		importedFiles = new HashSet<>();
		
		functionIdentifiers = new ArrayList<>();
		functions = new ArrayList<>();
		
		defined_globals = new LinkedHashMap<>();
		defined_types = new HashMap<>();
	}
	
	@Override
	public List<IFunction> getFunctions() {
		return List.copyOf(functions);
	}
	
	@Override
	public List<SyntaxMarker> getSyntaxMarkers() {
		return syntaxMarkers;
	}
	
	@Override
	public Set<String> getImportedFiles() {
		return importedFiles;
	}
	
	@Override
	public boolean hasErrors() {
		return hasErrors;
	}
	
	public boolean hasDefinedType(String typeName) {
		return defined_types.containsKey(typeName);
	}
	
	public boolean hasDefinedGlobal(String globalName) {
		return defined_globals.containsKey(globalName);
	}
	
	public void addDefinedType(String name, HighType type) {
		defined_types.put(name, type);
	}
	
	public HighType getDefinedType(String name) {
		return defined_types.get(name);
	}
	
	public void addDefinedGlobal(String name, Expression expr) {
		defined_globals.put(name, expr);
	}
	
	public Expression getDefinedGlobal(String name) {
		return defined_globals.get(name);
	}
	
	public boolean hasFunction(String name) {
		return getFunction(name) != null;
	}
	
	public Identifier getFunction(String name) {
		for(Identifier id : functionIdentifiers) {
			if(id.getIdType() == IdType.funct && id.getName().equals(name)) return id;
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
		functionIdentifiers.add(Identifier.createFuncIdent(func.getName(), function_index++, func));
		return func;
	}
	
	public boolean hasImportedFile(File file) {
		return importedFiles.contains(file.getAbsolutePath());
	}
	
	public void addImportedFile(File file) {
		importedFiles.add(file.getAbsolutePath());
	}
	
	public void addSyntaxMarker(SyntaxMarker syntaxMarker) {
		if(syntaxMarker.getSeverity() == SyntaxMarker.ERROR) {
			// Mark this program as having errors
			hasErrors = true;
		}
		
		syntaxMarkers.add(syntaxMarker);
	}
	
	public IBlock get(int index) {
		return functions.get(index);
	}
	
	public List<Function> list() {
		return functions;
	}
	
	@Override
	public String asString() {
		return "PROGRAM";
	}
	
	@Override
	public Object[] asList() {
		return functions.toArray();
	}
}
