package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

public class Program implements Stable {
	public List<Function> functions;
	
	public Program() {
		functions = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	public String listnm() { return "PROGRAM"; }
	public Object[] listme() { return functions.toArray(); };
}
