package hardcoded.compiler;

import java.io.File;
import java.util.*;

import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.impl.IFunction;
import hardcoded.compiler.impl.IStatement;
import hardcoded.compiler.statement.Statement;
import hardcoded.compiler.statement.VariableStat;
import hardcoded.compiler.types.HighType;
import hardcoded.lexer.Token;
import hardcoded.utils.StringUtils;
import hardcoded.visualization.Printable;

public class Function implements IFunction, Printable {
	private int temp_counter;
	private int var_index;
	
	
	protected List<Modifier> modifiers;
	protected Vector<Scope> scopes;
	
	public String name;
	public HighType returnType;
	public File declaredFile;
	public int sourceLineIndex;
	public int sourceFileOffset;
	
	public List<Identifier> arguments;
	
	public Statement body = Statement.newEmpty();
	public Map<String, Token> requiredLabels;
	public List<String> labels;
	
	public Function() {
		modifiers = new ArrayList<>();
		arguments = new ArrayList<>();
		requiredLabels = new LinkedHashMap<>();
		labels = new ArrayList<>();
		scopes = new Vector<>();
	}
	
	public boolean hasLabel(String name) {
		return labels.contains(name);
	}
	
	public Scope getScope() {
		return scopes.lastElement();
	}
	
	public Identifier add(VariableStat var) {
		if(!hasIdentifier(var.name)) {
			Identifier ident = Identifier.createVarIdent(var.name, var_index++, var.type);
			getScope().add(ident);
			return ident;
		}
		
		// TODO: Throw error if we add a identifier that already exists?
		return null;
	}
	
	public boolean hasIdentifier(String name) {
		for(Identifier var : arguments) {
			if(var.name().equals(name)) return true;
		}
		
		for(Scope scope : scopes) {
			if(scope.hasIdentifier(name)) return true;
		}
		
		return false;
	}
	
	public Identifier getIdentifier(String name) {
		for(Identifier var : arguments) {
			if(var.name().equals(name)) return var;
		} 
		
		for(Scope scope : scopes) {
			Identifier ident = scope.getIdentifier(name);
			if(ident != null) return ident;
		}
		
		return null;
	}
	
	public Identifier temp(LowType type) {
		Identifier ident = Identifier.createVarIdent("$temp" + temp_counter, temp_counter, type, true);
		getScope().add(ident);
		temp_counter++;
		
		return ident;
	}
	
	// TODO: Rename
	public Function inc_scope() {
		scopes.add(new Scope());
		return this;
	}
	
	// TODO: Rename
	public Function dec_scope() {
		scopes.remove(scopes.size() - 1);
		return this;
	}
	
	public boolean isPlaceholder() {
		return body.isEmptyStat();
	}
	
	
	public void addModifier(Modifier modifier) {
		this.modifiers.add(modifier);
	}
	
	public void addArgument(VariableStat arg) {
		arguments.add(Identifier.createParamIdent(arg.name, arguments.size(), arg.type));
	}
	
	
	
	public String getName() {
		return name;
	}
	
	public List<Modifier> getModifiers() {
		return List.copyOf(modifiers);
	}
	
	public File getDeclaringFile() {
		return declaredFile;
	}
	
	public int getLineIndex() {
		return sourceLineIndex;
	}
	
	public int getFileOffset() {
		return sourceFileOffset;
	}

	public int getLength() {
		return name.length();
	}
	
	
	
	public HighType getReturnType() {
		return returnType;
	}
	
	public List<IStatement> getStatements() {
		return List.of(body);
	}
	
	
	
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(!modifiers.isEmpty()) sb.append(StringUtils.join(" ", modifiers)).append(" ");
		sb.append(returnType).append(" ").append(name).append("(");
		for(int i = 0; i < arguments.size(); i++) {
			Identifier ident = arguments.get(i);
			sb.append(ident.getHighType()).append(" ").append(ident);
			if(i < arguments.size() - 1) sb.append(", ");
		}
		
		return sb.append(");").toString();
	}
	
	public String asString() { return "function " + name + "[" + var_index + (var_index == 1 ? " variable":" variables") + "]"; }
	public Object[] asList() { return new Object[] { body }; }
}
