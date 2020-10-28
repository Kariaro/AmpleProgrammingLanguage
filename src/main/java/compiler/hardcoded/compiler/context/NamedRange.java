package hardcoded.compiler.context;

public class NamedRange {
	private final String name;
	private final int offset;
	private final int length;
	
	public NamedRange(String name, int offset, int length) {
		this.name = name;
		this.offset = offset;
		this.length = length;
	}
	
	public String getName() {
		return name;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public int hashCode() {
		return name.hashCode() ^ (offset << 16) ^ length;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof NamedRange)) return false;
		return hashCode() == obj.hashCode();
	}
	
	public String toString() {
		return "Range{name=\"" + name + "\", offset=" + offset + ", length=" + length + "}";
	}
}
