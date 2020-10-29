package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import hardcoded.compiler.constants.Modifiers.Modifier;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.statement.Statement;
import hardcoded.compiler.statement.Variable;
import hardcoded.compiler.types.HighType;
import hardcoded.lexer.Token;
import hardcoded.visualization.Printable;

public abstract class Block implements Printable {
	public static final Block EMPTY = new Block() {
		public String asString() { return ""; }
		public Object[] asList() { return new Object[] { }; }
		public boolean hasElements() { return false; }
		public List<Statement> getElements() { return null; }
		public String toString() { return "null"; }
	};
	
	public abstract boolean hasElements();
	public abstract List<Statement> getElements();
	
	public static class NestedBlock extends Block {
		public List<Statement> list = new ArrayList<>();
		
		@Override
		public boolean hasElements() {
			return true;
		}
		
		public List<Statement> getElements() {
			return list;
		}
		
		public String asString() { return "null"; }
		public Object[] asList() { return list.toArray(); }
	}
	
	public static class ClassBlock extends NestedBlock {
		// TODO: Variables, Constructor, Methods, Operator overrides
		
		public Token[] getTokens() {
			return null;
		}
	}
	
	public static class Function extends Block {
		public Modifier modifier;
		public HighType returnType;
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
			if(!hasIdentifier(var.name)) {
				Identifier ident = Identifier.createVarIdent(var.name, var_index++, var.type.type());
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
		
		public Function inc_scope() {
			scopes.add(new Scope());
			return this;
		}
		
		public Function dec_scope() {
			scopes.remove(scopes.size() - 1);
			return this;
		}
		
		public String getName() {
			return name;
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
				Identifier ident = arguments.get(i);
				sb.append(ident.getHighType()).append(" ").append(ident);
				if(i < arguments.size() - 1) sb.append(", ");
			}
			
			return sb.append(");").toString();
		}
		
		public boolean hasElements() { return false; }
		public List<Statement> getElements() { return null; }
		
		public String asString() { return "function " + name + "[" + var_index + (var_index == 1 ? " variable":" variables") + "]"; }
		public Object[] asList() { return new Object[] { body }; }
	}
}