package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

public class Scope {
	public List<Variable> list;
	public Scope() {
		list = new ArrayList<>();
	}
	
	public boolean hasIdentifier(String name) {
		for(Variable var : list) {
			if(var.name.equals(name)) return true; // TODO: Get the variable...
		}
		
		return false;
	}

	public void add(Variable var) {
		list.add(var);
	}
}
