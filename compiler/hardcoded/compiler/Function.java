package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.StringUtils;


// TODO: Function declaration or definition.
public class Function implements Stable {
	public Modifier modifier;
	public Type returnType;
	public String name;
	public List<Argument> arguments;
	public Statement body;
	
	public Function() {
		arguments = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(modifier != null) sb.append(modifier).append(" ");
		sb.append(returnType).append(" ").append(name).append("(").append(StringUtils.join(", ", arguments)).append(") ").append(body);
		return sb.toString();
	}
	
	public String listnm() { return "function " + name; }
	public Object[] listme() { return new Object[] { body }; };
}
