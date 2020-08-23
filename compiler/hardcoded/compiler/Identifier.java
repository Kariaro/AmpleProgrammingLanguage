package hardcoded.compiler;

public interface Identifier {
	public enum IdType {
		clazz,
		funct,
		param,
		var
	}
	
	public String name();
	public IdType type();
	public int index();
	
	public static class FuncIdent implements Identifier {
		public String name;
		public int index;
		
		public FuncIdent(String name, int index) {
			this.name = name;
			this.index = index;
		}
		
		public String name() { return name; }
		public IdType type() { return IdType.funct; }
		public int index() { return index; }
	}
	
	public static class VarIdent implements Identifier {
		public String name;
		public int index;
		public Type type;
		
		public VarIdent(Type type, String name, int index) {
			this.type = type;
			this.name = name;
			this.index = index;
		}
		
		public String name() { return name; }
		public int index() { return index; }
		public IdType type() { return IdType.var; }
		public String toString() { return type + " " + name + ";"; }
	}
	
	public static class ParamIdent extends VarIdent {
		public ParamIdent(Type type, String name, int index) {
			super(type, name, index);
		}
		
		public String name() { return name; }
		public int index() { return index; }
		public IdType type() { return IdType.param; }
	}
}
