package hardcoded.compiler;

public class Type {
	public String typeName;
	public int pointerSize;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(typeName);
		for(int i = 0; i < pointerSize; i++) sb.append("*");
		return sb.toString();
	}
}
