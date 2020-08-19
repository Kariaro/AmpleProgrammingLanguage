package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import hardcoded.utils.StringUtils;


// TODO: Function declaration or definition.
public class Function implements Stable {
	public Modifier modifier;
	public Type returnType;
	public String name;
	public List<Variable> arguments;
	public Statement body;
	public int stackLength;
	
	public Function() {
		arguments = new ArrayList<>();
		scopes = new Vector<>();
	}
	
	public Vector<Scope> scopes;
	public Scope getScope() { return scopes.lastElement(); }
	
	public boolean hasIdentifier(String name) {
		for(Variable var : arguments) {
			if(var.name.equals(name)) return true;
		}
		
		for(Scope scope : scopes) {
			if(scope.hasIdentifier(name)) return true;
		}
		
		return false;
	}
	
	public Variable getVariableFromName(String value) {
		for(Variable var : arguments) {
			if(var.name.equals(value)) return var;
		}
		
		for(Scope scope : scopes) {
			for(Variable var : scope.list) {
				if(var.name.equals(value)) return var;
			}
		}
		
		throw new RuntimeException("Could not find the variable '" + value + "' was not found in the current scope.");
	}
	
	public int getVariableStackIndex(Variable v) {
		int index = 0;
		for(Variable var : arguments) {
			if(var == v) return index;
			if(var.isArray) {
				index += var.arraySize * var.type.size();
			} else {
				index += var.type.size();
			}
		}
		
		for(Scope scope : scopes) {
			for(Variable var : scope.list) {
				if(var == v) return index;
				if(var.isArray) {
					index += var.arraySize * var.type.size();
				} else {
					index += var.type.size();
				}
			}
		}
		
		// Invalid
		throw new RuntimeException("The variable '" + v + "' was not found in the current scope.");
	}
	
	public int getStackIndex() {
		int index = 0;
		for(Variable var : arguments) {
			if(var.isArray) {
				index += var.arraySize * var.type.size();
			} else {
				index += var.type.size();
			}
		}
		
		for(Scope scope : scopes) {
			for(Variable var : scope.list) {
				if(var.isArray) {
					index += var.arraySize * var.type.size();
				} else {
					index += var.type.size();
				}
			}
		}
		
		return index;
	}
	
	public Function inc_scope() {
		scopes.add(new Scope());
		return this;
	}
	
	public Function dec_scope() {
		int size = getStackIndex();
		if(size > stackLength) stackLength = size;
		
		scopes.remove(scopes.size() - 1);
		return this;
	}
	
	public boolean isPlaceholder() {
		return body == null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(modifier != null) sb.append(modifier).append(" ");
		sb.append(returnType).append(" ").append(name).append("(").append(StringUtils.join(", ", arguments)).append(") ").append(body);
		return sb.toString();
	}
	
	public String listnm() { return "function " + name + " [" + stackLength + " byte" + (stackLength == 1 ? "":"s")+  " stack]"; }
	public Object[] listme() { return new Object[] { body }; }
}
