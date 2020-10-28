package hardcoded.compiler.context;

public final class Category {
	public final String name;
	public final int index;
	
	private Category(String name, int index) {
		this.name = name;
		this.index = index;
	}
	
	public int hashCode() {
		return index;
	}
	
	public static Category get(String name, int index) {
		return new Category(name, index);
	}
}
