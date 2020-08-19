package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

public class Program implements Stable {
	public List<Function> functions;
	
	public Program() {
		functions = new ArrayList<>();
	}
	
	public boolean hasFunction(String name) {
		throw new UnsupportedOperationException("Implement");
	}
	
	public Function current() {
		if(functions.isEmpty()) return null;
		return functions.get(functions.size() - 1);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	public String listnm() { return "PROGRAM"; }
	public Object[] listme() { return functions.toArray(); };
}
