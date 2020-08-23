package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import hardcoded.compiler.Identifier.VarIdent;

public interface Block extends Printable {
	public default boolean hasElements() {
		return false;
	}
	
	public default List<Statement> getElements() {
		return null;
	}
	
	public static class NestedBlock implements Block {
		public List<Statement> list = new ArrayList<>();
		
		@Override
		public boolean hasElements() {
			return true;
		}
		
		public List<Statement> getElements() {
			return list;
		}
		
		public String listnm() { return "null"; }
		public Object[] listme() { return list.toArray(); }
	}
	
	public static class ClassBlock extends NestedBlock {
		// TODO: Variables
		// TODO: Constructor
		// TODO: Methods
		// TODO: Operator overrides.
	}
	
	public static class Function implements Block {
		public Modifier modifier;
		public Type returnType;
		public String name;
		public List<Identifier> arguments;
		public Statement body;
		
		public int temp_counter;
		public int var_index;
		
		public Function() {
			arguments = new ArrayList<>();
			scopes = new Vector<>();
		}
		
		public Vector<Scope> scopes;
		public Scope getScope() {
			return scopes.lastElement();
		}
		
		public Identifier add(Variable var) {
			if(!hasIdentifier(var.name)) { // throw already defined?
				Identifier ident = new VarIdent(var.type, var.name, var_index++);
				getScope().add(ident);
				return ident;
			}
			
			return null; // TODO: Error?
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
		
		public VarIdent temp(Type type) {
			VarIdent result = new VarIdent(type, "$temp" + temp_counter, temp_counter);
			getScope().add(result);
			temp_counter++;
			
			return result;
		}
		
		public Function inc_scope() {
			scopes.add(new Scope());
			return this;
		}
		
		public Function dec_scope() {
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
			sb.append(returnType).append(" ").append(name).append("(");
			for(int i = 0; i < arguments.size(); i++) {
				String str = arguments.get(i).toString();
				sb.append(str.substring(0, str.length() - 1));
				if(i < arguments.size() - 1) sb.append(", ");
			}
			return sb.append(");").toString();
		}
		
		public String listnm() { return "function " + name + "[" + var_index + (var_index == 1 ? " variable":" variables") + "]"; }
		public Object[] listme() { return new Object[] { body }; }
	}

}
