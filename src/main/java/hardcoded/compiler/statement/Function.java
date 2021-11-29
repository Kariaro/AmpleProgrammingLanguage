package hardcoded.compiler.statement;

import java.io.File;
import java.util.*;

import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.impl.IFunction;
import hardcoded.compiler.impl.IStatement;
import hardcoded.compiler.impl.ISyntaxPosition;
import hardcoded.compiler.types.HighType;
import hardcoded.lexer.Token;
import hardcoded.utils.StringUtils;
import hardcoded.visualization.Printable;

public class Function implements IFunction, Printable {
	private final LinkedList<Map<String, Identifier>> scopes;
	private final List<Modifier> modifiers;
	private final List<Identifier> arguments;
	private final Set<String> labels;
	
	private final Map<String, Token> requiredLabels;
	private final HighType returnType;
	private final String name;
	private int temp_counter;
	private int var_index;
	
	// TODO: These variables should not be public.
	public ISyntaxPosition syntaxPosition;
	
	public Statement body = Statement.newEmpty();
	
	public Function(String functionName, HighType returnType, List<Modifier> modifiers) {
		this.modifiers = modifiers;
		this.returnType = returnType;
		this.name = functionName;
		
		this.arguments = new ArrayList<>();
		this.requiredLabels = new HashMap<>();
		this.labels = new HashSet<>();
		this.scopes = new LinkedList<>();
	}
	
	public boolean hasLabel(String name) {
		return labels.contains(name);
	}
	
	public void addLabel(String name) {
		labels.add(name);
	}
	
	public Identifier add(VariableStat var) {
		if(!hasIdentifier(var.getName())) {
			Identifier ident = Identifier.createLocalVariable(var.getName(), var_index++, var.getType());
			scopes.getLast().put(ident.getName(), ident);
			return ident;
		}
		
		return null;
	}
	
	public boolean hasIdentifier(String name) {
		for(Identifier var : arguments) {
			if(var.getName().equals(name)) return true;
		}
		
		for(Map<String, Identifier> scope : scopes) {
			if(scope.containsKey(name)) return true;
		}
		
		return false;
	}
	
	public Identifier getIdentifier(String name) {
		for(Identifier var : arguments) {
			if(var.getName().equals(name)) return var;
		} 
		
		for(Map<String, Identifier> scope : scopes) {
			Identifier ident = scope.get(name);
			if(ident != null) return ident;
		}
		
		return null;
	}
	
	public Identifier getTempLocal(LowType type) {
		Identifier ident = Identifier.createTempLocalVariable("$temp%s".formatted(temp_counter), temp_counter, type);
		scopes.getLast().put(ident.getName(), ident);
		temp_counter++;
		
		return ident;
	}
	
	public Function pushScope() {
		// We add a linked hash map to preserve the order of locals.
		scopes.addLast(new LinkedHashMap<>());
		return this;
	}
	
	public Function popScope() {
		scopes.removeLast();
		return this;
	}
	
	public Statement getBody() {
		return body;
	}
	
	public boolean isPlaceholder() {
		return body.isEmptyStat();
	}
	
	public void addModifier(Modifier modifier) {
		modifiers.add(modifier);
	}
	
	public void addArgument(VariableStat arg) {
		arguments.add(Identifier.createParamIdent(arg.getName(), arguments.size(), arg.getType()));
	}
	
	public List<Identifier> getArguments() {
		return arguments;
	}
	
	public void addRequiredLabel(String value, Token token) {
		requiredLabels.put(value, token);
	}
	
	public Map<String, Token> getRequiredLabels() {
		return requiredLabels;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public File getDeclaringFile() {
		return syntaxPosition.getStartPosition().file;
	}
	
	@Override
	public ISyntaxPosition getSyntaxPosition() {
		return syntaxPosition;
	}

	@Override
	public HighType getReturnType() {
		return returnType;
	}

	@Override
	public List<IStatement> getStatements() {
		return List.of(body);
	}
	
	@Override
	public Object[] asList() {
		return new Object[] { body };
	}
	
	@Override
	public String asString() {
		return "function %s[%s %s]".formatted(name, var_index, var_index == 1 ? "variable":"variables");
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(!modifiers.isEmpty()) {
			sb.append(StringUtils.join(" ", modifiers)).append(" ");
		}
		
		sb.append(returnType).append(" ").append(name).append("(");
		
		for(int i = 0; i < arguments.size(); i++) {
			Identifier ident = arguments.get(i);
			sb.append(ident.getHighType()).append(" ").append(ident);
			if(i < arguments.size() - 1) sb.append(", ");
		}
		
		return sb.append(")").toString();
	}
}
