package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

public class Scope {
	public List<Identifier> list = new ArrayList<>();
	
	public boolean hasIdentifier(String name) {
		for(Identifier var : list) {
			if(var.name().equals(name)) return true;
		}
		
		return false;
	}
	
	public Identifier getIdentifier(String name) {
		for(Identifier var : list) {
			if(var.name().equals(name)) return var;
		}
		
		return null;
	}

	public void add(Identifier var) {
		list.add(var);
	}
}
